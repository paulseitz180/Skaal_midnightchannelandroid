package com.skaalsolutions.midnightchannel.ui.shell

import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.skaalsolutions.midnightchannel.connectivity.ConnectivityStatus
import com.skaalsolutions.midnightchannel.connectivity.LocalConnectivityMonitor
import com.skaalsolutions.midnightchannel.connectivity.isRetryAvailable
import com.skaalsolutions.midnightchannel.navigation.ShellController
import com.skaalsolutions.midnightchannel.navigation.ShellEvent
import com.skaalsolutions.midnightchannel.navigation.ShellState
import com.skaalsolutions.midnightchannel.navigation.acceptsRetry
import com.skaalsolutions.midnightchannel.navigation.isReconnecting
import com.skaalsolutions.midnightchannel.navigation.revealsChannelContent
import com.skaalsolutions.midnightchannel.navigation.showsOfflineSurface
import com.skaalsolutions.midnightchannel.navigation.showsSplashSurface
import com.skaalsolutions.midnightchannel.ui.channel.ChannelWebViewHost
import com.skaalsolutions.midnightchannel.ui.offline.OfflineErrorScreen
import com.skaalsolutions.midnightchannel.ui.splash.SplashScreen
import com.skaalsolutions.midnightchannel.ui.theme.MidnightTheme
import com.skaalsolutions.midnightchannel.util.WebViewHistoryBackHandler
import com.skaalsolutions.midnightchannel.webview.ChannelDestination
import com.skaalsolutions.midnightchannel.webview.shellRoutingWebViewCallbacks

/**
 * Production shell composition driven exclusively by [ShellController].
 *
 * Performance notes (TASK 20):
 * - Connectivity Flow collected only while Offline surface is visible
 * - WebView uses View.INVISIBLE under overlays (not Compose alpha)
 * - Stable WebView create / load callbacks via rememberUpdatedState
 */
@Composable
fun MidnightShell(
    shell: ShellController,
    modifier: Modifier = Modifier,
    onSplashFloorElapsed: () -> Unit = {},
) {
    val colors = MidnightTheme.colors
    val motion = MidnightTheme.motion
    val connectivityMonitor = LocalConnectivityMonitor.current
    val recovery = shell.recovery

    val shellState by shell.state.collectAsState()
    val showOffline = shellState.showsOfflineSurface
    val revealChannel = shellState.revealsChannelContent

    // Collect network status only while Offline/Retrying — skip churn during Ready.
    var connectivity by remember {
        mutableStateOf(
            connectivityMonitor?.currentStatus() ?: ConnectivityStatus.Available,
        )
    }
    LaunchedEffect(showOffline, connectivityMonitor) {
        if (!showOffline || connectivityMonitor == null) return@LaunchedEffect
        connectivity = connectivityMonitor.currentStatus()
        connectivityMonitor.status.collect { connectivity = it }
    }

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var initialLoadStarted by remember { mutableStateOf(false) }
    var recoveryTick by remember { mutableIntStateOf(0) }

    val onSplashFloor = rememberUpdatedState(onSplashFloorElapsed)

    DisposableEffect(recovery) {
        onDispose { recovery?.cancel() }
    }

    val clientCallbacks = remember(shell) {
        shellRoutingWebViewCallbacks(emit = shell::dispatch)
    }

    val fadeInSpec = remember(motion.screenCrossfadeMs) {
        fadeIn(
            animationSpec = tween(
                durationMillis = motion.screenCrossfadeMs,
                easing = FastOutSlowInEasing,
            ),
        )
    }
    val fadeOutSpec = remember(motion.screenCrossfadeMs) {
        fadeOut(
            animationSpec = tween(
                durationMillis = motion.screenCrossfadeMs,
                easing = FastOutSlowInEasing,
            ),
        )
    }

    val isRetrying = shellState is ShellState.Retrying
    LaunchedEffect(isRetrying, recoveryTick) {
        if (!isRetrying) return@LaunchedEffect
        recovery?.awaitAndStartReload(
            webView = webViewRef,
            labelSwapMs = motion.retryLabelSwapMs,
        )
    }

    LaunchedEffect(shellState, recoveryTick) {
        if (shellState !is ShellState.Loading) return@LaunchedEffect
        if (recovery?.isInFlight != true) return@LaunchedEffect
        recovery.awaitLoadOutcomeOrTimeout(webView = webViewRef)
    }

    ShellBackHandler(
        enabled = true,
        confirmationWindowMs = motion.exitConfirmationWindowMs,
        historyBackHandler = WebViewHistoryBackHandler.Disabled,
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        ChannelWebViewHost(
            modifier = Modifier.fillMaxSize(),
            contentVisible = revealChannel,
            clientCallbacks = clientCallbacks,
            onWebViewCreated = { webView ->
                webViewRef = webView
                if (!initialLoadStarted) {
                    initialLoadStarted = true
                    val restoredUrl = webView.url
                    val needsLoad = restoredUrl.isNullOrBlank() ||
                        restoredUrl == "about:blank" ||
                        restoredUrl.startsWith("data:")
                    if (needsLoad) {
                        runCatching {
                            webView.loadUrl(ChannelDestination.HOME_URL)
                        }.onFailure {
                            shell.dispatch(ShellEvent.WebViewInitFailed)
                        }
                    } else {
                        shell.dispatch(ShellEvent.MainFrameLoadFinished)
                    }
                }
            },
        )

        AnimatedVisibility(
            visible = shellState.showsSplashSurface,
            enter = fadeInSpec,
            exit = fadeOutSpec,
        ) {
            SplashScreen(
                onMinimumDurationElapsed = {
                    onSplashFloor.value()
                    shell.dispatch(ShellEvent.SplashFloorElapsed)
                },
            )
        }

        AnimatedVisibility(
            visible = showOffline,
            enter = fadeInSpec,
            exit = fadeOutSpec,
        ) {
            val retryEnabled =
                connectivity.isRetryAvailable &&
                    shellState.acceptsRetry &&
                    recovery?.isInFlight != true

            OfflineErrorScreen(
                isReconnecting = shellState.isReconnecting || recovery?.isInFlight == true,
                retryEnabled = retryEnabled,
                onRetryClick = {
                    if (recovery?.requestRetry() == true) {
                        recoveryTick += 1
                    }
                },
            )
        }
    }
}

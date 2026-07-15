package com.skaalsolutions.midnightchannel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.skaalsolutions.midnightchannel.connectivity.ConnectivityMonitor
import com.skaalsolutions.midnightchannel.connectivity.ConnectivityShellBridge
import com.skaalsolutions.midnightchannel.connectivity.LocalConnectivityMonitor
import com.skaalsolutions.midnightchannel.navigation.ShellController
import com.skaalsolutions.midnightchannel.recovery.ChannelRecoveryController
import com.skaalsolutions.midnightchannel.ui.shell.MidnightShell
import com.skaalsolutions.midnightchannel.ui.theme.MidnightChannelTheme
import com.skaalsolutions.midnightchannel.webview.LocalWebViewLifecycleCoordinator
import com.skaalsolutions.midnightchannel.webview.WEBVIEW_STATE_BUNDLE_KEY
import com.skaalsolutions.midnightchannel.webview.WebViewLifecycleCoordinator
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch

/**
 * Single-Activity shell host.
 *
 * Startup: system splash → Compose content with WebView loading under native splash
 * (parallel with floor timer). Connectivity monitoring starts only while STARTED.
 *
 * Orientation / keyboard / uiMode are absorbed via Manifest `configChanges`
 * (no custom onConfigurationChanged handling required).
 */
class MainActivity : ComponentActivity() {

    private val webViewLifecycle = WebViewLifecycleCoordinator()
    private val shellController = ShellController()
    private val connectivityMonitor by lazy(LazyThreadSafetyMode.NONE) {
        ConnectivityMonitor(this)
    }
    private val recovery by lazy(LazyThreadSafetyMode.NONE) {
        ChannelRecoveryController(
            dispatch = shellController::dispatch,
            connectivitySnapshot = connectivityMonitor::currentStatus,
            currentState = shellController::current,
        ).also(shellController::bindRecovery)
    }
    private val connectivityBridge by lazy(LazyThreadSafetyMode.NONE) {
        ConnectivityShellBridge(
            monitor = connectivityMonitor,
            shell = shellController,
            scope = lifecycleScope,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        var keepSystemSplash = true
        installSplashScreen().setKeepOnScreenCondition { keepSystemSplash }
        super.onCreate(savedInstanceState)

        recovery // bind retry before first offline frame

        webViewLifecycle.prepareRestore(savedInstanceState?.getBundle(WEBVIEW_STATE_BUNDLE_KEY))

        enableEdgeToEdge()

        // Chromium remote debugging — debug builds only (never in release / AAB).
        android.webkit.WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                connectivityBridge.start()
                try {
                    awaitCancellation()
                } finally {
                    connectivityBridge.stop()
                }
            }
        }

        setContent {
            CompositionLocalProvider(
                LocalWebViewLifecycleCoordinator provides webViewLifecycle,
                LocalConnectivityMonitor provides connectivityMonitor,
            ) {
                MidnightChannelTheme {
                    MidnightShell(
                        shell = shellController,
                        onSplashFloorElapsed = { keepSystemSplash = false },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        webViewLifecycle.onHostResume()
    }

    override fun onPause() {
        webViewLifecycle.onHostPause()
        super.onPause()
    }

    override fun onDestroy() {
        webViewLifecycle.onHostDestroy()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webViewLifecycle.saveState()?.let { state ->
            outState.putBundle(WEBVIEW_STATE_BUNDLE_KEY, state)
        }
    }
}

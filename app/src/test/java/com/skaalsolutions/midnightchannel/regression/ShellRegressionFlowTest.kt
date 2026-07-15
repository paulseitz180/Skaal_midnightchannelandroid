package com.skaalsolutions.midnightchannel.regression

import com.google.common.truth.Truth.assertThat
import com.skaalsolutions.midnightchannel.connectivity.ConnectivityStatus
import com.skaalsolutions.midnightchannel.navigation.ShellController
import com.skaalsolutions.midnightchannel.navigation.ShellEvent
import com.skaalsolutions.midnightchannel.navigation.ShellState
import com.skaalsolutions.midnightchannel.navigation.isReconnecting
import com.skaalsolutions.midnightchannel.navigation.revealsChannelContent
import com.skaalsolutions.midnightchannel.navigation.showsOfflineSurface
import com.skaalsolutions.midnightchannel.navigation.showsSplashSurface
import com.skaalsolutions.midnightchannel.recovery.ChannelRecoveryController
import com.skaalsolutions.midnightchannel.testing.ControllableClock
import com.skaalsolutions.midnightchannel.testing.ShellReducerFixtures
import com.skaalsolutions.midnightchannel.util.BackNavigationResult
import com.skaalsolutions.midnightchannel.util.ShellBackNavigator
import com.skaalsolutions.midnightchannel.util.WebViewHistoryBackHandler
import com.skaalsolutions.midnightchannel.webview.ChannelDestination
import com.skaalsolutions.midnightchannel.webview.MainFrameFailure
import com.skaalsolutions.midnightchannel.webview.shellRoutingWebViewCallbacks
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * TASK 35 — end-to-end shell regression scenarios across prior TASK boundaries.
 *
 * Stitches reducer + controller + recovery + WebView routing + back-exit without
 * a live Chromium WebView / Activity (those remain Orchestrator device QA).
 */
class ShellRegressionFlowTest {

    @Test
    fun `cold start splash then channel ready reveals content and clears splash`() {
        val shell = ShellController()
        assertThat(shell.current().showsSplashSurface).isTrue()
        assertThat(shell.current().revealsChannelContent).isFalse()

        ShellReducerFixtures.eventsToReady().forEach(shell::dispatch)

        assertThat(shell.current()).isEqualTo(ShellState.Ready)
        assertThat(shell.current().showsSplashSurface).isFalse()
        assertThat(shell.current().showsOfflineSurface).isFalse()
        assertThat(shell.current().revealsChannelContent).isTrue()
    }

    @Test
    fun `cold start offline webview init failure routes to offline surface`() {
        val shell = ShellController()
        shell.dispatch(ShellEvent.WebViewInitFailed)

        assertThat(shell.current()).isEqualTo(ShellState.Offline)
        assertThat(shell.current().showsSplashSurface).isFalse()
        assertThat(shell.current().showsOfflineSurface).isTrue()
        assertThat(shell.current().revealsChannelContent).isFalse()
    }

    @Test
    fun `webview main-frame failure routes through shell callbacks to offline`() {
        val shell = ShellController()
        ShellReducerFixtures.eventsToReady().forEach(shell::dispatch)
        val callbacks = shellRoutingWebViewCallbacks(emit = shell::dispatch)

        callbacks.onMainFrameFailed(
            MainFrameFailure.Network(
                errorCode = -2,
                description = "net::ERR_INTERNET_DISCONNECTED",
                failingUrl = ChannelDestination.HOME_URL,
            ),
        )

        assertThat(shell.current()).isEqualTo(ShellState.Offline)
        assertThat(shell.current().showsOfflineSurface).isTrue()
    }

    @Test
    fun `about blank page finished does not clear splash or offline`() {
        val shell = ShellController()
        val callbacks = shellRoutingWebViewCallbacks(emit = shell::dispatch)

        callbacks.onPageFinished("about:blank")
        assertThat(shell.current()).isInstanceOf(ShellState.Splash::class.java)

        shell.dispatch(ShellEvent.NetworkLost)
        callbacks.onPageFinished("about:blank")
        assertThat(shell.current()).isEqualTo(ShellState.Offline)
    }

    @Test
    fun `offline retry success path reaches ready and clears recovery flight`() = runTest {
        val shell = ShellController()
        val recovery = ChannelRecoveryController(
            dispatch = shell::dispatch,
            connectivitySnapshot = { ConnectivityStatus.Available },
            currentState = shell::current,
        )
        shell.bindRecovery(recovery)

        shell.dispatch(ShellEvent.NetworkLost)
        assertThat(recovery.requestRetry()).isTrue()
        assertThat(shell.current()).isEqualTo(ShellState.Retrying)
        assertThat(shell.current().isReconnecting).isTrue()

        shell.dispatch(ShellEvent.RetryLoadStarted)
        assertThat(shell.current()).isEqualTo(ShellState.Loading)

        shell.dispatch(ShellEvent.MainFrameLoadFinished)
        assertThat(shell.current()).isEqualTo(ShellState.Ready)
        assertThat(recovery.isInFlight).isFalse()
        assertThat(shell.current().revealsChannelContent).isTrue()
    }

    @Test
    fun `offline retry failure remains offline and clears recovery flight`() = runTest {
        val shell = ShellController()
        val recovery = ChannelRecoveryController(
            dispatch = shell::dispatch,
            connectivitySnapshot = { ConnectivityStatus.Available },
            currentState = shell::current,
        )
        shell.bindRecovery(recovery)

        shell.dispatch(ShellEvent.NetworkLost)
        assertThat(recovery.requestRetry()).isTrue()
        recovery.awaitAndStartReload(webView = null, labelSwapMs = 0)

        assertThat(shell.current()).isEqualTo(ShellState.Offline)
        assertThat(recovery.isInFlight).isFalse()
        assertThat(shell.current().showsOfflineSurface).isTrue()
    }

    @Test
    fun `ready then mid session network lost returns to offline without splash`() {
        val shell = ShellController()
        ShellReducerFixtures.eventsToReady().forEach(shell::dispatch)
        shell.dispatch(ShellEvent.NetworkLost)

        assertThat(shell.current()).isEqualTo(ShellState.Offline)
        assertThat(shell.current().showsSplashSurface).isFalse()
        assertThat(shell.current().showsOfflineSurface).isTrue()
    }

    @Test
    fun `mvp back navigation never consumes history and requires double press to exit`() {
        val clock = ControllableClock(1_000L)
        val navigator = ShellBackNavigator(
            confirmationWindowMs = 2_000L,
            historyBackHandler = WebViewHistoryBackHandler.Disabled,
            nowMs = clock.asProvider,
        )

        assertThat(navigator.onBackPressed()).isEqualTo(BackNavigationResult.ShowExitToast)
        clock.advanceBy(500L)
        assertThat(navigator.onBackPressed()).isEqualTo(BackNavigationResult.ExitApp)
    }

    @Test
    fun `splash paint before floor still waits then advances when floor elapses`() {
        val shell = ShellController()
        shell.dispatch(ShellEvent.MainFrameLoadFinished)
        assertThat(shell.current().showsSplashSurface).isTrue()
        assertThat(shell.current().revealsChannelContent).isFalse()

        shell.dispatch(ShellEvent.SplashFloorElapsed)
        assertThat(shell.current()).isEqualTo(ShellState.Ready)
    }
}

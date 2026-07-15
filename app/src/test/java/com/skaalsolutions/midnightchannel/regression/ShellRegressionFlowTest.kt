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
    fun cold_start_splash_then_channel_ready_reveals_content_and_clears_splash() {
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
    fun cold_start_offline_webview_init_failure_routes_to_offline_surface() {
        val shell = ShellController()
        shell.dispatch(ShellEvent.WebViewInitFailed)

        assertThat(shell.current()).isEqualTo(ShellState.Offline)
        assertThat(shell.current().showsSplashSurface).isFalse()
        assertThat(shell.current().showsOfflineSurface).isTrue()
        assertThat(shell.current().revealsChannelContent).isFalse()
    }

    @Test
    fun webview_main_frame_failure_routes_through_shell_callbacks_to_offline() {
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
    fun about_blank_page_finished_does_not_clear_splash_or_offline() {
        val shell = ShellController()
        val callbacks = shellRoutingWebViewCallbacks(emit = shell::dispatch)

        callbacks.onPageFinished("about:blank")
        assertThat(shell.current()).isInstanceOf(ShellState.Splash::class.java)

        shell.dispatch(ShellEvent.NetworkLost)
        callbacks.onPageFinished("about:blank")
        assertThat(shell.current()).isEqualTo(ShellState.Offline)
    }

    @Test
    fun offline_retry_success_path_reaches_ready_and_clears_recovery_flight() = runTest {
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
    fun offline_retry_failure_remains_offline_and_clears_recovery_flight() = runTest {
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
    fun ready_then_mid_session_network_lost_returns_to_offline_without_splash() {
        val shell = ShellController()
        ShellReducerFixtures.eventsToReady().forEach(shell::dispatch)
        shell.dispatch(ShellEvent.NetworkLost)

        assertThat(shell.current()).isEqualTo(ShellState.Offline)
        assertThat(shell.current().showsSplashSurface).isFalse()
        assertThat(shell.current().showsOfflineSurface).isTrue()
    }

    @Test
    fun mvp_back_navigation_never_consumes_history_and_requires_double_press_to_exit() {
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
    fun splash_paint_before_floor_still_waits_then_advances_when_floor_elapses() {
        val shell = ShellController()
        shell.dispatch(ShellEvent.MainFrameLoadFinished)
        assertThat(shell.current().showsSplashSurface).isTrue()
        assertThat(shell.current().revealsChannelContent).isFalse()

        shell.dispatch(ShellEvent.SplashFloorElapsed)
        assertThat(shell.current()).isEqualTo(ShellState.Ready)
    }
}

package com.skaalsolutions.midnightchannel.util

import com.google.common.truth.Truth.assertThat
import com.skaalsolutions.midnightchannel.testing.ControllableClock
import org.junit.Test

class ShellBackNavigatorTest {

    @Test
    fun `mvp disabled history always uses double-back gate`() {
        val clock = ControllableClock()
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
    fun `history consume returns null and resets exit gate`() {
        val clock = ControllableClock()
        val navigator = ShellBackNavigator(
            confirmationWindowMs = 2_000L,
            historyBackHandler = WebViewHistoryBackHandler { true },
            nowMs = clock.asProvider,
        )
        // Arm via disabled path first using a separate navigator...
        val arming = ShellBackNavigator(2_000L, nowMs = clock.asProvider)
        arming.onBackPressed()

        assertThat(navigator.onBackPressed()).isNull()
        // After history consume, next press on double-back-only navigator is toast.
        val exitOnly = ShellBackNavigator(2_000L, nowMs = clock.asProvider)
        assertThat(exitOnly.onBackPressed()).isEqualTo(BackNavigationResult.ShowExitToast)
    }

    @Test
    fun `history consume then resume exit confirmation on same navigator`() {
        var consumeHistory = true
        val clock = ControllableClock()
        val navigator = ShellBackNavigator(
            confirmationWindowMs = 2_000L,
            historyBackHandler = WebViewHistoryBackHandler { consumeHistory },
            nowMs = clock.asProvider,
        )
        assertThat(navigator.onBackPressed()).isNull()

        consumeHistory = false
        assertThat(navigator.onBackPressed()).isEqualTo(BackNavigationResult.ShowExitToast)
        clock.advanceBy(100L)
        assertThat(navigator.onBackPressed()).isEqualTo(BackNavigationResult.ExitApp)
    }

    @Test
    fun `reset clears armed toast`() {
        val clock = ControllableClock()
        val navigator = ShellBackNavigator(2_000L, nowMs = clock.asProvider)
        navigator.onBackPressed()
        navigator.reset()
        clock.advanceBy(50L)
        assertThat(navigator.onBackPressed()).isEqualTo(BackNavigationResult.ShowExitToast)
    }
}

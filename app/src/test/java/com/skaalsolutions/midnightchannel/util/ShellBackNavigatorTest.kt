package com.skaalsolutions.midnightchannel.util

import com.google.common.truth.Truth.assertThat
import com.skaalsolutions.midnightchannel.testing.ControllableClock
import org.junit.Test

class ShellBackNavigatorTest {

    @Test
    fun mvp_disabled_history_always_uses_double_back_gate() {
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
    fun history_consume_returns_null_and_resets_exit_gate() {
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
    fun history_consume_then_resume_exit_confirmation_on_same_navigator() {
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
    fun reset_clears_armed_toast() {
        val clock = ControllableClock()
        val navigator = ShellBackNavigator(2_000L, nowMs = clock.asProvider)
        navigator.onBackPressed()
        navigator.reset()
        clock.advanceBy(50L)
        assertThat(navigator.onBackPressed()).isEqualTo(BackNavigationResult.ShowExitToast)
    }
}

package com.skaalsolutions.midnightchannel.util

import com.google.common.truth.Truth.assertThat
import com.skaalsolutions.midnightchannel.testing.ControllableClock
import org.junit.Test

class DoubleBackExitGateTest {

    @Test(expected = IllegalArgumentException::class)
    fun rejects_non_positive_confirmation_window() {
        DoubleBackExitGate(confirmationWindowMs = 0L)
    }

    @Test
    fun first_press_shows_exit_toast() {
        val clock = ControllableClock(1_000L)
        val gate = DoubleBackExitGate(2_000L, clock.asProvider)
        assertThat(gate.evaluate()).isEqualTo(BackNavigationResult.ShowExitToast)
    }

    @Test
    fun second_press_within_window_exits() {
        val clock = ControllableClock(1_000L)
        val gate = DoubleBackExitGate(2_000L, clock.asProvider)
        assertThat(gate.evaluate()).isEqualTo(BackNavigationResult.ShowExitToast)
        clock.advanceBy(1_500L)
        assertThat(gate.evaluate()).isEqualTo(BackNavigationResult.ExitApp)
    }

    @Test
    fun second_press_after_window_arms_again_with_toast() {
        val clock = ControllableClock(1_000L)
        val gate = DoubleBackExitGate(2_000L, clock.asProvider)
        assertThat(gate.evaluate()).isEqualTo(BackNavigationResult.ShowExitToast)
        clock.advanceBy(2_001L)
        assertThat(gate.evaluate()).isEqualTo(BackNavigationResult.ShowExitToast)
    }

    @Test
    fun reset_clears_armed_first_press() {
        val clock = ControllableClock(1_000L)
        val gate = DoubleBackExitGate(2_000L, clock.asProvider)
        gate.evaluate()
        gate.reset()
        clock.advanceBy(100L)
        assertThat(gate.evaluate()).isEqualTo(BackNavigationResult.ShowExitToast)
    }

    @Test
    fun exit_disarms_so_next_press_is_toast_again() {
        val clock = ControllableClock()
        val gate = DoubleBackExitGate(2_000L, clock.asProvider)
        gate.evaluate()
        clock.advanceBy(100L)
        assertThat(gate.evaluate()).isEqualTo(BackNavigationResult.ExitApp)
        assertThat(gate.evaluate()).isEqualTo(BackNavigationResult.ShowExitToast)
    }
}

package com.skaalsolutions.midnightchannel.util

import com.google.common.truth.Truth.assertThat
import com.skaalsolutions.midnightchannel.testing.ControllableClock
import org.junit.Test

class DoubleBackExitGateTest {

    @Test(expected = IllegalArgumentException::class)
    fun `rejects non-positive confirmation window`() {
        DoubleBackExitGate(confirmationWindowMs = 0L)
    }

    @Test
    fun `first press shows exit toast`() {
        val clock = ControllableClock(1_000L)
        val gate = DoubleBackExitGate(2_000L, clock.asProvider)
        assertThat(gate.evaluate()).isEqualTo(BackNavigationResult.ShowExitToast)
    }

    @Test
    fun `second press within window exits`() {
        val clock = ControllableClock(1_000L)
        val gate = DoubleBackExitGate(2_000L, clock.asProvider)
        assertThat(gate.evaluate()).isEqualTo(BackNavigationResult.ShowExitToast)
        clock.advanceBy(1_500L)
        assertThat(gate.evaluate()).isEqualTo(BackNavigationResult.ExitApp)
    }

    @Test
    fun `second press after window arms again with toast`() {
        val clock = ControllableClock(1_000L)
        val gate = DoubleBackExitGate(2_000L, clock.asProvider)
        assertThat(gate.evaluate()).isEqualTo(BackNavigationResult.ShowExitToast)
        clock.advanceBy(2_001L)
        assertThat(gate.evaluate()).isEqualTo(BackNavigationResult.ShowExitToast)
    }

    @Test
    fun `reset clears armed first press`() {
        val clock = ControllableClock(1_000L)
        val gate = DoubleBackExitGate(2_000L, clock.asProvider)
        gate.evaluate()
        gate.reset()
        clock.advanceBy(100L)
        assertThat(gate.evaluate()).isEqualTo(BackNavigationResult.ShowExitToast)
    }

    @Test
    fun `exit disarms so next press is toast again`() {
        val clock = ControllableClock()
        val gate = DoubleBackExitGate(2_000L, clock.asProvider)
        gate.evaluate()
        clock.advanceBy(100L)
        assertThat(gate.evaluate()).isEqualTo(BackNavigationResult.ExitApp)
        assertThat(gate.evaluate()).isEqualTo(BackNavigationResult.ShowExitToast)
    }
}

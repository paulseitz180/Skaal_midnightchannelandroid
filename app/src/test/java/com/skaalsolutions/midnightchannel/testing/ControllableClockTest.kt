package com.skaalsolutions.midnightchannel.testing

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ControllableClockTest {

    @Test
    fun advance_and_set_update_provider() {
        val clock = ControllableClock(10L)
        assertThat(clock.asProvider()).isEqualTo(10L)
        clock.advanceBy(5L)
        assertThat(clock.nowMs).isEqualTo(15L)
        clock.set(100L)
        assertThat(clock.asProvider()).isEqualTo(100L)
    }

    @Test(expected = IllegalArgumentException::class)
    fun advance_rejects_negative_delta() {
        ControllableClock().advanceBy(-1L)
    }
}

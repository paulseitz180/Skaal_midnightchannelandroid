package com.skaalsolutions.midnightchannel.connectivity

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ConnectivityStatusTest {

    @Test
    fun `retry available only when validated network present`() {
        assertThat(ConnectivityStatus.Available.isRetryAvailable).isTrue()
        assertThat(ConnectivityStatus.Unavailable.isRetryAvailable).isFalse()
    }
}

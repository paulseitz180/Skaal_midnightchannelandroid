package com.skaalsolutions.midnightchannel.connectivity

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ConnectivityStatusTest {

    @Test
    fun retry_available_only_when_validated_network_present() {
        assertThat(ConnectivityStatus.Available.isRetryAvailable).isTrue()
        assertThat(ConnectivityStatus.Unavailable.isRetryAvailable).isFalse()
    }
}

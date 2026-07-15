package com.skaalsolutions.midnightchannel.webview

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ChannelDestinationTest {

    @Test
    fun `home url is https product host with trailing slash`() {
        assertThat(ChannelDestination.HOST).isEqualTo("midnightchannel.live")
        assertThat(ChannelDestination.HOME_URL).isEqualTo("https://midnightchannel.live/")
        assertThat(ChannelDestination.HOME_URL).startsWith("https://")
        assertThat(ChannelDestination.HOME_URL).contains(ChannelDestination.HOST)
    }
}

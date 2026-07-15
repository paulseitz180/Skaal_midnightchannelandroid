package com.skaalsolutions.midnightchannel.webview

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ChannelDestinationTest {

    @Test
    fun home_url_is_https_product_host_with_trailing_slash() {
        assertThat(ChannelDestination.HOST).isEqualTo("midnightchannel.live")
        assertThat(ChannelDestination.HOME_URL).isEqualTo("https://midnightchannel.live/")
        assertThat(ChannelDestination.HOME_URL).startsWith("https://")
        assertThat(ChannelDestination.HOME_URL).contains(ChannelDestination.HOST)
    }
}

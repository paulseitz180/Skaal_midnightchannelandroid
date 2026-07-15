package com.skaalsolutions.midnightchannel.webview

/**
 * Single remote destination for the Midnight Channel shell.
 * Native never invents alternate product URLs.
 */
object ChannelDestination {
    const val HOST: String = "midnightchannel.live"
    const val HOME_URL: String = "https://$HOST/"
}

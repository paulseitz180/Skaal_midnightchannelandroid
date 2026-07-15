package com.skaalsolutions.midnightchannel.testing

import android.net.Uri
import androidx.core.net.toUri
import com.skaalsolutions.midnightchannel.webview.ChannelDestination

/**
 * URI fixtures for [com.skaalsolutions.midnightchannel.webview.MidnightOriginPolicy] tests.
 *
 * Prefer these builders over raw strings in unit tests for consistency.
 */
object OriginTestFixtures {

    val homeHttps: Uri = ChannelDestination.HOME_URL.toUri()

    fun httpsOnHost(host: String = ChannelDestination.HOST, path: String = "/"): Uri =
        "https://$host$path".toUri()

    fun httpOnHost(host: String = ChannelDestination.HOST, path: String = "/"): Uri =
        "http://$host$path".toUri()

    fun externalHttps(host: String = "example.com", path: String = "/"): Uri =
        "https://$host$path".toUri()

    fun mailto(address: String = "info@example.com"): Uri =
        "mailto:$address".toUri()

    fun tel(number: String = "+15551212"): Uri =
        "tel:$number".toUri()

    val aboutBlank: Uri = "about:blank".toUri()

    val dataHtml: Uri = "data:text/html,<html></html>".toUri()
}

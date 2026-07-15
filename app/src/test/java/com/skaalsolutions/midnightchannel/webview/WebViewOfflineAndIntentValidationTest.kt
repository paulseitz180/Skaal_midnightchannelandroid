package com.skaalsolutions.midnightchannel.webview

import com.google.common.truth.Truth.assertThat
import com.skaalsolutions.midnightchannel.navigation.ShellEvent
import com.skaalsolutions.midnightchannel.navigation.ShellReducer
import com.skaalsolutions.midnightchannel.navigation.ShellState
import com.skaalsolutions.midnightchannel.navigation.showsOfflineSurface
import com.skaalsolutions.midnightchannel.testing.MidnightRobolectricTest
import com.skaalsolutions.midnightchannel.testing.OriginTestFixtures
import com.skaalsolutions.midnightchannel.util.ExternalLinkNavigator
import org.junit.Test

/**
 * End-to-end shell routing validation: WebView callbacks → events → Offline,
 * plus external Intent readiness for leave-WebView URIs.
 */
class WebViewOfflineAndIntentValidationTest : MidnightRobolectricTest() {

    @Test
    fun `main-frame failure callback reduces splash to offline`() {
        val events = mutableListOf<ShellEvent>()
        val callbacks = shellRoutingWebViewCallbacks(emit = { events += it })
        callbacks.onMainFrameFailed(
            MainFrameFailure.Network(
                errorCode = -2,
                description = "offline",
                failingUrl = ChannelDestination.HOME_URL,
            ),
        )
        assertThat(events).containsExactly(ShellEvent.MainFrameLoadFailed)
        val next = ShellReducer.reduce(ShellState.Splash(), events.single())
        assertThat(next).isEqualTo(ShellState.Offline)
    }

    @Test
    fun `ready state goes offline on routed main-frame failure`() {
        val events = mutableListOf<ShellEvent>()
        val callbacks = shellRoutingWebViewCallbacks(emit = { events += it })
        callbacks.onMainFrameFailed(
            MainFrameFailure.Http(statusCode = 503, failingUrl = ChannelDestination.HOME_URL),
        )
        val next = ShellReducer.reduce(ShellState.Ready, events.single())
        assertThat(next).isEqualTo(ShellState.Offline)
        assertThat(next.showsOfflineSurface).isTrue()
    }

    @Test
    fun `external leave-webview uris are externally handled for intents`() {
        val external = OriginTestFixtures.externalHttps()
        assertThat(MidnightOriginPolicy.shouldLeaveWebView(external)).isTrue()
        assertThat(ExternalLinkNavigator.isExternallyHandledUri(external)).isTrue()
        assertThat(ExternalLinkNavigator.buildValidatedIntent(external)).isNotNull()

        val mailto = OriginTestFixtures.mailto()
        assertThat(ExternalLinkNavigator.buildValidatedIntent(mailto)?.action)
            .isEqualTo(android.content.Intent.ACTION_SENDTO)
    }

    @Test
    fun `same-origin destination never leaves webview for intents`() {
        assertThat(MidnightOriginPolicy.shouldLeaveWebView(OriginTestFixtures.homeHttps))
            .isFalse()
        assertThat(ExternalLinkNavigator.isExternallyHandledUri(OriginTestFixtures.homeHttps))
            .isFalse()
        // open() also rejects same-origin; scheme validation alone is not sufficient.
        assertThat(MidnightOriginPolicy.shouldLoadInsideWebView(OriginTestFixtures.homeHttps))
            .isTrue()
    }

    @Test
    fun `channel destination is https product home`() {
        assertThat(ChannelDestination.HOME_URL).isEqualTo("https://midnightchannel.live/")
        assertThat(MidnightOriginPolicy.isSameOrigin(ChannelDestination.HOME_URL)).isTrue()
    }

    @Test
    fun `crt blank document uses shared chrome background token`() {
        assertThat(CrtShellChrome.BACKGROUND_HEX).isEqualTo("#0A0A0A")
    }
}

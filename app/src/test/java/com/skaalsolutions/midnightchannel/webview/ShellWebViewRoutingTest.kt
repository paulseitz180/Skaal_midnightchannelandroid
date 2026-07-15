package com.skaalsolutions.midnightchannel.webview

import com.google.common.truth.Truth.assertThat
import com.skaalsolutions.midnightchannel.navigation.ShellEvent
import com.skaalsolutions.midnightchannel.testing.MidnightRobolectricTest
import com.skaalsolutions.midnightchannel.testing.OriginTestFixtures
import org.junit.Test

class ShellWebViewRoutingTest : MidnightRobolectricTest() {

    @Test
    fun `page started emits load started for origin url`() {
        val events = mutableListOf<ShellEvent>()
        val callbacks = shellRoutingWebViewCallbacks(emit = { events += it })
        callbacks.onPageStarted(ChannelDestination.HOME_URL)
        assertThat(events).containsExactly(ShellEvent.MainFrameLoadStarted)
    }

    @Test
    fun `page started ignores about blank`() {
        val events = mutableListOf<ShellEvent>()
        val callbacks = shellRoutingWebViewCallbacks(emit = { events += it })
        callbacks.onPageStarted("about:blank")
        assertThat(events).isEmpty()
    }

    @Test
    fun `page finished emits only for same origin`() {
        val events = mutableListOf<ShellEvent>()
        val callbacks = shellRoutingWebViewCallbacks(emit = { events += it })
        callbacks.onPageFinished(OriginTestFixtures.externalHttps().toString())
        assertThat(events).isEmpty()
        callbacks.onPageFinished(ChannelDestination.HOME_URL)
        assertThat(events).containsExactly(ShellEvent.MainFrameLoadFinished)
    }

    @Test
    fun `main frame failure emits load failed`() {
        val events = mutableListOf<ShellEvent>()
        val callbacks = shellRoutingWebViewCallbacks(emit = { events += it })
        callbacks.onMainFrameFailed(
            MainFrameFailure.Network(
                errorCode = -2,
                description = "net",
                failingUrl = ChannelDestination.HOME_URL,
            ),
        )
        assertThat(events).containsExactly(ShellEvent.MainFrameLoadFailed)
    }

    @Test
    fun `main frame failure on data document ignored`() {
        val events = mutableListOf<ShellEvent>()
        val callbacks = shellRoutingWebViewCallbacks(emit = { events += it })
        callbacks.onMainFrameFailed(
            MainFrameFailure.Http(statusCode = 404, failingUrl = "data:text/html,x"),
        )
        assertThat(events).isEmpty()
    }

    @Test
    fun `renderer recovery default emits load failed`() {
        val events = mutableListOf<ShellEvent>()
        val callbacks = shellRoutingWebViewCallbacks(emit = { events += it })
        callbacks.onRendererRecoveryRequired(MainFrameFailure.RendererProcessGone(didCrash = true))
        assertThat(events).containsExactly(ShellEvent.MainFrameLoadFailed)
    }
}

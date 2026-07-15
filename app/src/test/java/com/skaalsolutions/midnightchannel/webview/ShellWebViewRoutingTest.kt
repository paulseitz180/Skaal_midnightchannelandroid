package com.skaalsolutions.midnightchannel.webview

import com.google.common.truth.Truth.assertThat
import com.skaalsolutions.midnightchannel.navigation.ShellEvent
import com.skaalsolutions.midnightchannel.testing.MidnightRobolectricTest
import com.skaalsolutions.midnightchannel.testing.OriginTestFixtures
import org.junit.Test

class ShellWebViewRoutingTest : MidnightRobolectricTest() {

    @Test
    fun page_started_emits_load_started_for_origin_url() {
        val events = mutableListOf<ShellEvent>()
        val callbacks = shellRoutingWebViewCallbacks(emit = { events += it })
        callbacks.onPageStarted(ChannelDestination.HOME_URL)
        assertThat(events).containsExactly(ShellEvent.MainFrameLoadStarted)
    }

    @Test
    fun page_started_ignores_about_blank() {
        val events = mutableListOf<ShellEvent>()
        val callbacks = shellRoutingWebViewCallbacks(emit = { events += it })
        callbacks.onPageStarted("about:blank")
        assertThat(events).isEmpty()
    }

    @Test
    fun page_finished_emits_only_for_same_origin() {
        val events = mutableListOf<ShellEvent>()
        val callbacks = shellRoutingWebViewCallbacks(emit = { events += it })
        callbacks.onPageFinished(OriginTestFixtures.externalHttps().toString())
        assertThat(events).isEmpty()
        callbacks.onPageFinished(ChannelDestination.HOME_URL)
        assertThat(events).containsExactly(ShellEvent.MainFrameLoadFinished)
    }

    @Test
    fun main_frame_failure_emits_load_failed() {
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
    fun main_frame_failure_on_data_document_ignored() {
        val events = mutableListOf<ShellEvent>()
        val callbacks = shellRoutingWebViewCallbacks(emit = { events += it })
        callbacks.onMainFrameFailed(
            MainFrameFailure.Http(statusCode = 404, failingUrl = "data:text/html,x"),
        )
        assertThat(events).isEmpty()
    }

    @Test
    fun renderer_recovery_default_emits_load_failed() {
        val events = mutableListOf<ShellEvent>()
        val callbacks = shellRoutingWebViewCallbacks(emit = { events += it })
        callbacks.onRendererRecoveryRequired(MainFrameFailure.RendererProcessGone(didCrash = true))
        assertThat(events).containsExactly(ShellEvent.MainFrameLoadFailed)
    }
}

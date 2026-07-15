package com.skaalsolutions.midnightchannel.navigation

import com.google.common.truth.Truth.assertThat
import com.skaalsolutions.midnightchannel.testing.ShellReducerFixtures
import org.junit.Test

class ShellStateSurfaceTest {

    @Test
    fun `splash surface flags`() {
        val splash = ShellState.Splash()
        assertThat(splash.showsSplashSurface).isTrue()
        assertThat(splash.showsOfflineSurface).isFalse()
        assertThat(splash.revealsChannelContent).isFalse()
        assertThat(splash.acceptsRetry).isFalse()
        assertThat(splash.isReconnecting).isFalse()
    }

    @Test
    fun `ready reveals channel and hides overlays`() {
        assertThat(ShellState.Ready.revealsChannelContent).isTrue()
        assertThat(ShellState.Ready.showsSplashSurface).isFalse()
        assertThat(ShellState.Ready.showsOfflineSurface).isFalse()
        assertThat(ShellState.Ready.acceptsRetry).isFalse()
    }

    @Test
    fun `loading reveals channel content`() {
        assertThat(ShellState.Loading.revealsChannelContent).isTrue()
    }

    @Test
    fun `offline accepts retry`() {
        assertThat(ShellState.Offline.showsOfflineSurface).isTrue()
        assertThat(ShellState.Offline.acceptsRetry).isTrue()
        assertThat(ShellState.Offline.revealsChannelContent).isFalse()
        assertThat(ShellState.Offline.isReconnecting).isFalse()
    }

    @Test
    fun `retrying shows offline surface as reconnecting`() {
        assertThat(ShellState.Retrying.showsOfflineSurface).isTrue()
        assertThat(ShellState.Retrying.isReconnecting).isTrue()
        assertThat(ShellState.Retrying.acceptsRetry).isFalse()
    }

    @Test
    fun `happy path surfaces after reducer fixtures`() {
        val ready = ShellReducerFixtures.reduceAll(
            ShellReducer.initial(),
            *ShellReducerFixtures.eventsToReady().toTypedArray(),
        )
        assertThat(ready.revealsChannelContent).isTrue()
    }
}

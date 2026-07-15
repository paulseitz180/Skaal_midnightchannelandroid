package com.skaalsolutions.midnightchannel.navigation

import com.google.common.truth.Truth.assertThat
import com.skaalsolutions.midnightchannel.testing.ShellReducerFixtures
import org.junit.Test

class ShellStateSurfaceTest {

    @Test
    fun splash_surface_flags() {
        val splash = ShellState.Splash()
        assertThat(splash.showsSplashSurface).isTrue()
        assertThat(splash.showsTitleSurface).isFalse()
        assertThat(splash.showsOfflineSurface).isFalse()
        assertThat(splash.revealsChannelContent).isFalse()
        assertThat(splash.acceptsRetry).isFalse()
        assertThat(splash.isReconnecting).isFalse()
    }

    @Test
    fun title_page_surface_flags() {
        val title = ShellState.TitlePage()
        assertThat(title.showsTitleSurface).isTrue()
        assertThat(title.showsSplashSurface).isFalse()
        assertThat(title.showsLaunchOverlay).isTrue()
        assertThat(title.revealsChannelContent).isFalse()
        assertThat(title.canAdvance).isFalse()
    }

    @Test
    fun ready_reveals_channel_and_hides_overlays() {
        assertThat(ShellState.Ready.revealsChannelContent).isTrue()
        assertThat(ShellState.Ready.showsSplashSurface).isFalse()
        assertThat(ShellState.Ready.showsTitleSurface).isFalse()
        assertThat(ShellState.Ready.showsOfflineSurface).isFalse()
        assertThat(ShellState.Ready.acceptsRetry).isFalse()
    }

    @Test
    fun loading_reveals_channel_content() {
        assertThat(ShellState.Loading.revealsChannelContent).isTrue()
    }

    @Test
    fun offline_accepts_retry() {
        assertThat(ShellState.Offline.showsOfflineSurface).isTrue()
        assertThat(ShellState.Offline.acceptsRetry).isTrue()
        assertThat(ShellState.Offline.revealsChannelContent).isFalse()
        assertThat(ShellState.Offline.isReconnecting).isFalse()
    }

    @Test
    fun retrying_shows_offline_surface_as_reconnecting() {
        assertThat(ShellState.Retrying.showsOfflineSurface).isTrue()
        assertThat(ShellState.Retrying.isReconnecting).isTrue()
        assertThat(ShellState.Retrying.acceptsRetry).isFalse()
    }

    @Test
    fun happy_path_surfaces_after_reducer_fixtures() {
        val ready = ShellReducerFixtures.reduceAll(
            ShellReducer.initial(),
            *ShellReducerFixtures.eventsToReady().toTypedArray(),
        )
        assertThat(ready.revealsChannelContent).isTrue()
    }
}

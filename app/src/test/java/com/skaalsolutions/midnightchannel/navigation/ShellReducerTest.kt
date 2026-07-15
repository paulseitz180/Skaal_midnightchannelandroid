package com.skaalsolutions.midnightchannel.navigation

import com.google.common.truth.Truth.assertThat
import com.skaalsolutions.midnightchannel.testing.ShellReducerFixtures
import org.junit.Test

/**
 * Pure [ShellReducer] unit tests — shell presentation state machine.
 */
class ShellReducerTest {

    @Test
    fun initial_state_is_splash_with_paint_unset() {
        val state = ShellReducer.initial() as ShellState.Splash
        assertThat(state.firstPaintReady).isFalse()
    }

    @Test
    fun logo_expand_floor_advances_to_title_page_without_waiting_for_webview() {
        val title = ShellReducer.reduce(
            ShellState.Splash(),
            ShellEvent.SplashFloorElapsed,
        ) as ShellState.TitlePage
        assertThat(title.floorElapsed).isFalse()
        assertThat(title.firstPaintReady).isFalse()
        assertThat(title.canAdvance).isFalse()
    }

    @Test
    fun title_page_advances_only_when_floor_and_main_frame_finish() {
        val afterFloor = ShellReducer.reduce(
            ShellState.TitlePage(),
            ShellEvent.TitleFloorElapsed,
        ) as ShellState.TitlePage
        assertThat(afterFloor.floorElapsed).isTrue()
        assertThat(afterFloor).isInstanceOf(ShellState.TitlePage::class.java)

        val afterPaintOnly = ShellReducer.reduce(
            ShellState.TitlePage(),
            ShellEvent.MainFrameLoadFinished,
        ) as ShellState.TitlePage
        assertThat(afterPaintOnly.firstPaintReady).isTrue()
        assertThat(afterPaintOnly).isInstanceOf(ShellState.TitlePage::class.java)

        val ready = ShellReducerFixtures.reduceAll(
            ShellReducer.initial(),
            *ShellReducerFixtures.eventsToReady().toTypedArray(),
        )
        assertThat(ready).isEqualTo(ShellState.Ready)
    }

    @Test
    fun title_page_order_independent_for_floor_and_paint() {
        val paintThenFloor = ShellReducerFixtures.reduceAll(
            ShellState.TitlePage(),
            ShellEvent.MainFrameLoadFinished,
            ShellEvent.TitleFloorElapsed,
        )
        assertThat(paintThenFloor).isEqualTo(ShellState.Ready)
    }

    @Test
    fun paint_during_logo_expand_carries_into_title_page() {
        val afterPaint = ShellReducer.reduce(
            ShellState.Splash(),
            ShellEvent.MainFrameLoadFinished,
        ) as ShellState.Splash
        assertThat(afterPaint.firstPaintReady).isTrue()

        val title = ShellReducer.reduce(
            afterPaint,
            ShellEvent.SplashFloorElapsed,
        ) as ShellState.TitlePage
        assertThat(title.firstPaintReady).isTrue()
        assertThat(title.floorElapsed).isFalse()

        val ready = ShellReducer.reduce(title, ShellEvent.TitleFloorElapsed)
        assertThat(ready).isEqualTo(ShellState.Ready)
    }

    @Test
    fun splash_network_lost_goes_offline() {
        assertThat(
            ShellReducer.reduce(ShellState.Splash(), ShellEvent.NetworkLost),
        ).isEqualTo(ShellState.Offline)
        assertThat(
            ShellReducer.reduce(ShellState.Splash(), ShellEvent.MainFrameLoadFailed),
        ).isEqualTo(ShellState.Offline)
        assertThat(
            ShellReducer.reduce(ShellState.Splash(), ShellEvent.WebViewInitFailed),
        ).isEqualTo(ShellState.Offline)
    }

    @Test
    fun title_page_network_lost_goes_offline() {
        assertThat(
            ShellReducer.reduce(ShellState.TitlePage(), ShellEvent.NetworkLost),
        ).isEqualTo(ShellState.Offline)
    }

    @Test
    fun ready_reload_start_enters_loading_then_ready_on_finish() {
        val loading = ShellReducer.reduce(ShellState.Ready, ShellEvent.MainFrameLoadStarted)
        assertThat(loading).isEqualTo(ShellState.Loading)

        val ready = ShellReducer.reduce(ShellState.Loading, ShellEvent.MainFrameLoadFinished)
        assertThat(ready).isEqualTo(ShellState.Ready)
    }

    @Test
    fun ready_failure_enters_offline() {
        assertThat(
            ShellReducer.reduce(ShellState.Ready, ShellEvent.NetworkLost),
        ).isEqualTo(ShellState.Offline)
    }

    @Test
    fun offline_retry_enters_retrying_then_loading() {
        val retrying = ShellReducer.reduce(ShellState.Offline, ShellEvent.RetryRequested)
        assertThat(retrying).isEqualTo(ShellState.Retrying)

        val loading = ShellReducer.reduce(ShellState.Retrying, ShellEvent.RetryLoadStarted)
        assertThat(loading).isEqualTo(ShellState.Loading)
    }

    @Test
    fun retrying_may_finish_early_to_ready_on_main_frame_finished() {
        assertThat(
            ShellReducer.reduce(ShellState.Retrying, ShellEvent.MainFrameLoadFinished),
        ).isEqualTo(ShellState.Ready)
    }

    @Test
    fun retrying_failure_returns_offline() {
        assertThat(
            ShellReducer.reduce(ShellState.Retrying, ShellEvent.RetryFailed),
        ).isEqualTo(ShellState.Offline)
    }

    @Test
    fun offline_ignores_non_retry_events() {
        assertThat(
            ShellReducer.reduce(ShellState.Offline, ShellEvent.MainFrameLoadFinished),
        ).isEqualTo(ShellState.Offline)
        assertThat(
            ShellReducer.reduce(ShellState.Offline, ShellEvent.SplashFloorElapsed),
        ).isEqualTo(ShellState.Offline)
        assertThat(
            ShellReducer.reduce(ShellState.Offline, ShellEvent.TitleFloorElapsed),
        ).isEqualTo(ShellState.Offline)
    }

    @Test
    fun retrying_ignores_nested_retry_until_load_started() {
        assertThat(
            ShellReducer.reduce(ShellState.Retrying, ShellEvent.RetryRequested),
        ).isEqualTo(ShellState.Retrying)
        assertThat(
            ShellReducer.reduce(ShellState.Retrying, ShellEvent.MainFrameLoadStarted),
        ).isEqualTo(ShellState.Retrying)
    }

    @Test
    fun loading_ignores_redundant_start_events() {
        assertThat(
            ShellReducer.reduce(ShellState.Loading, ShellEvent.MainFrameLoadStarted),
        ).isEqualTo(ShellState.Loading)
    }
}

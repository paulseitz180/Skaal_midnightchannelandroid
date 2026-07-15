package com.skaalsolutions.midnightchannel.navigation

import com.google.common.truth.Truth.assertThat
import com.skaalsolutions.midnightchannel.testing.ShellReducerFixtures
import org.junit.Test

/**
 * Pure [ShellReducer] unit tests — shell presentation state machine.
 */
class ShellReducerTest {

    @Test
    fun `initial state is splash with gates unset`() {
        val state = ShellReducer.initial() as ShellState.Splash
        assertThat(state.floorElapsed).isFalse()
        assertThat(state.firstPaintReady).isFalse()
        assertThat(state.canAdvance).isFalse()
    }

    @Test
    fun `splash advances only when floor and main-frame finish`() {
        val afterFloor = ShellReducer.reduce(
            ShellState.Splash(),
            ShellEvent.SplashFloorElapsed,
        ) as ShellState.Splash
        assertThat(afterFloor.floorElapsed).isTrue()
        assertThat(afterFloor).isInstanceOf(ShellState.Splash::class.java)

        val afterPaintOnly = ShellReducer.reduce(
            ShellState.Splash(),
            ShellEvent.MainFrameLoadFinished,
        ) as ShellState.Splash
        assertThat(afterPaintOnly.firstPaintReady).isTrue()
        assertThat(afterPaintOnly).isInstanceOf(ShellState.Splash::class.java)

        val ready = ShellReducerFixtures.reduceAll(
            ShellReducer.initial(),
            *ShellReducerFixtures.eventsToReady().toTypedArray(),
        )
        assertThat(ready).isEqualTo(ShellState.Ready)
    }

    @Test
    fun `splash order independent for floor and paint`() {
        val paintThenFloor = ShellReducerFixtures.reduceAll(
            ShellState.Splash(),
            ShellEvent.MainFrameLoadFinished,
            ShellEvent.SplashFloorElapsed,
        )
        assertThat(paintThenFloor).isEqualTo(ShellState.Ready)
    }

    @Test
    fun `splash network lost goes offline`() {
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
    fun `ready reload start enters loading then ready on finish`() {
        val loading = ShellReducer.reduce(ShellState.Ready, ShellEvent.MainFrameLoadStarted)
        assertThat(loading).isEqualTo(ShellState.Loading)

        val ready = ShellReducer.reduce(ShellState.Loading, ShellEvent.MainFrameLoadFinished)
        assertThat(ready).isEqualTo(ShellState.Ready)
    }

    @Test
    fun `ready failure enters offline`() {
        assertThat(
            ShellReducer.reduce(ShellState.Ready, ShellEvent.NetworkLost),
        ).isEqualTo(ShellState.Offline)
    }

    @Test
    fun `offline retry enters retrying then loading`() {
        val retrying = ShellReducer.reduce(ShellState.Offline, ShellEvent.RetryRequested)
        assertThat(retrying).isEqualTo(ShellState.Retrying)

        val loading = ShellReducer.reduce(ShellState.Retrying, ShellEvent.RetryLoadStarted)
        assertThat(loading).isEqualTo(ShellState.Loading)
    }

    @Test
    fun `retrying may finish early to ready on main-frame finished`() {
        assertThat(
            ShellReducer.reduce(ShellState.Retrying, ShellEvent.MainFrameLoadFinished),
        ).isEqualTo(ShellState.Ready)
    }

    @Test
    fun `retrying failure returns offline`() {
        assertThat(
            ShellReducer.reduce(ShellState.Retrying, ShellEvent.RetryFailed),
        ).isEqualTo(ShellState.Offline)
    }

    @Test
    fun `offline ignores non-retry events`() {
        assertThat(
            ShellReducer.reduce(ShellState.Offline, ShellEvent.MainFrameLoadFinished),
        ).isEqualTo(ShellState.Offline)
        assertThat(
            ShellReducer.reduce(ShellState.Offline, ShellEvent.SplashFloorElapsed),
        ).isEqualTo(ShellState.Offline)
    }

    @Test
    fun `retrying ignores nested retry until load started`() {
        assertThat(
            ShellReducer.reduce(ShellState.Retrying, ShellEvent.RetryRequested),
        ).isEqualTo(ShellState.Retrying)
        assertThat(
            ShellReducer.reduce(ShellState.Retrying, ShellEvent.MainFrameLoadStarted),
        ).isEqualTo(ShellState.Retrying)
    }

    @Test
    fun `loading ignores redundant start events`() {
        assertThat(
            ShellReducer.reduce(ShellState.Loading, ShellEvent.MainFrameLoadStarted),
        ).isEqualTo(ShellState.Loading)
    }
}

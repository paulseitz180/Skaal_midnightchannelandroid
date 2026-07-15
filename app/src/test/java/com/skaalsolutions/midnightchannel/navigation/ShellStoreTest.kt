package com.skaalsolutions.midnightchannel.navigation

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ShellStoreTest {

    @Test
    fun `dispatch updates state for meaningful transitions`() {
        val store = ShellStore()
        assertThat(store.current()).isInstanceOf(ShellState.Splash::class.java)

        store.dispatch(ShellEvent.SplashFloorElapsed)
        store.dispatch(ShellEvent.MainFrameLoadFinished)
        assertThat(store.current()).isEqualTo(ShellState.Ready)
    }

    @Test
    fun `dispatch skips no-op transitions`() {
        val store = ShellStore()
        store.dispatch(ShellEvent.SplashFloorElapsed)
        store.dispatch(ShellEvent.MainFrameLoadFinished)
        assertThat(store.current()).isEqualTo(ShellState.Ready)

        // Ready + RetryRequested is a no-op — same instance equality for data objects.
        store.dispatch(ShellEvent.RetryRequested)
        assertThat(store.current()).isEqualTo(ShellState.Ready)
    }

    @Test
    fun `state flow mirrors current`() {
        val store = ShellStore()
        store.dispatch(ShellEvent.NetworkLost)
        assertThat(store.state.value).isEqualTo(ShellState.Offline)
        assertThat(store.current()).isEqualTo(ShellState.Offline)
    }
}

package com.skaalsolutions.midnightchannel.connectivity

import com.google.common.truth.Truth.assertThat
import com.skaalsolutions.midnightchannel.navigation.ShellController
import com.skaalsolutions.midnightchannel.navigation.ShellEvent
import com.skaalsolutions.midnightchannel.navigation.ShellState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

/**
 * TASK 35 — [ConnectivityShellBridge] regression without device NetworkCallback.
 * Uses a mocked [ConnectivityMonitor] publishing a controllable [StateFlow].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ConnectivityShellBridgeTest {

    @Test
    fun cold_start_unavailable_dispatches_NetworkLost_once() = runTest(UnconfinedTestDispatcher()) {
        val status = MutableStateFlow<ConnectivityStatus>(ConnectivityStatus.Unavailable)
        val monitor = fakeMonitor(status)
        val shell = ShellController()
        val bridge = ConnectivityShellBridge(monitor, shell, backgroundScope)

        bridge.start()

        assertThat(shell.current()).isEqualTo(ShellState.Offline)
        verify(monitor).start()
    }

    @Test
    fun cold_start_available_does_not_force_offline() = runTest(UnconfinedTestDispatcher()) {
        val status = MutableStateFlow<ConnectivityStatus>(ConnectivityStatus.Available)
        val monitor = fakeMonitor(status)
        val shell = ShellController()
        val bridge = ConnectivityShellBridge(monitor, shell, backgroundScope)

        bridge.start()

        assertThat(shell.current()).isInstanceOf(ShellState.Splash::class.java)
    }

    @Test
    fun available_to_unavailable_after_start_dispatches_NetworkLost() = runTest(UnconfinedTestDispatcher()) {
        val status = MutableStateFlow<ConnectivityStatus>(ConnectivityStatus.Available)
        val monitor = fakeMonitor(status)
        val shell = ShellController()
        shell.dispatch(ShellEvent.SplashFloorElapsed)
        shell.dispatch(ShellEvent.TitleFloorElapsed)
        shell.dispatch(ShellEvent.MainFrameLoadFinished)
        assertThat(shell.current()).isEqualTo(ShellState.Ready)

        val bridge = ConnectivityShellBridge(monitor, shell, backgroundScope)
        bridge.start()

        status.value = ConnectivityStatus.Unavailable

        assertThat(shell.current()).isEqualTo(ShellState.Offline)
    }

    @Test
    fun unavailable_to_available_does_not_auto_retry_into_shell() = runTest(UnconfinedTestDispatcher()) {
        val status = MutableStateFlow<ConnectivityStatus>(ConnectivityStatus.Unavailable)
        val monitor = fakeMonitor(status)
        val shell = ShellController()
        val bridge = ConnectivityShellBridge(monitor, shell, backgroundScope)
        bridge.start()
        assertThat(shell.current()).isEqualTo(ShellState.Offline)

        status.value = ConnectivityStatus.Available

        // Restoration enables Retry UI only — reducer stays Offline until RetryRequested.
        assertThat(shell.current()).isEqualTo(ShellState.Offline)
    }

    @Test
    fun stop_cancels_collection_and_stops_monitor() = runTest(UnconfinedTestDispatcher()) {
        val status = MutableStateFlow<ConnectivityStatus>(ConnectivityStatus.Available)
        val monitor = fakeMonitor(status)
        val shell = ShellController()
        val bridge = ConnectivityShellBridge(monitor, shell, backgroundScope)
        bridge.start()
        bridge.stop()
        verify(monitor).stop()

        status.value = ConnectivityStatus.Unavailable
        assertThat(shell.current()).isInstanceOf(ShellState.Splash::class.java)
    }

    private fun fakeMonitor(
        status: MutableStateFlow<ConnectivityStatus>,
    ): ConnectivityMonitor =
        mock {
            on { start() } doAnswer { }
            on { stop() } doAnswer { }
            on { this.status } doReturn status
            on { currentStatus() } doAnswer { status.value }
        }
}

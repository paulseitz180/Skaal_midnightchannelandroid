package com.skaalsolutions.midnightchannel.recovery

import com.google.common.truth.Truth.assertThat
import com.skaalsolutions.midnightchannel.connectivity.ConnectivityStatus
import com.skaalsolutions.midnightchannel.navigation.ShellEvent
import com.skaalsolutions.midnightchannel.navigation.ShellState
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ChannelRecoveryControllerTest {

    @Test
    fun `requestRetry rejected when not offline`() {
        val events = mutableListOf<ShellEvent>()
        val recovery = ChannelRecoveryController(
            dispatch = { events += it },
            connectivitySnapshot = { ConnectivityStatus.Available },
            currentState = { ShellState.Ready },
        )
        assertThat(recovery.requestRetry()).isFalse()
        assertThat(events).isEmpty()
        assertThat(recovery.isInFlight).isFalse()
    }

    @Test
    fun `requestRetry rejected when network unavailable`() {
        val events = mutableListOf<ShellEvent>()
        val recovery = ChannelRecoveryController(
            dispatch = { events += it },
            connectivitySnapshot = { ConnectivityStatus.Unavailable },
            currentState = { ShellState.Offline },
        )
        assertThat(recovery.requestRetry()).isFalse()
        assertThat(events).isEmpty()
    }

    @Test
    fun `requestRetry dispatches when offline and available`() {
        val events = mutableListOf<ShellEvent>()
        val recovery = ChannelRecoveryController(
            dispatch = { events += it },
            connectivitySnapshot = { ConnectivityStatus.Available },
            currentState = { ShellState.Offline },
        )
        assertThat(recovery.requestRetry()).isTrue()
        assertThat(events).containsExactly(ShellEvent.RetryRequested)
        assertThat(recovery.isInFlight).isTrue()
    }

    @Test
    fun `overlapping requestRetry ignored while in flight`() {
        val events = mutableListOf<ShellEvent>()
        val recovery = ChannelRecoveryController(
            dispatch = { events += it },
            connectivitySnapshot = { ConnectivityStatus.Available },
            currentState = { ShellState.Offline },
        )
        assertThat(recovery.requestRetry()).isTrue()
        assertThat(recovery.requestRetry()).isFalse()
        assertThat(events).hasSize(1)
    }

    @Test
    fun `awaitAndStartReload fails when webView null`() = runTest {
        val events = mutableListOf<ShellEvent>()
        var state: ShellState = ShellState.Offline
        val recovery = ChannelRecoveryController(
            dispatch = {
                events += it
                if (it == ShellEvent.RetryRequested) state = ShellState.Retrying
                if (it == ShellEvent.RetryFailed) state = ShellState.Offline
            },
            connectivitySnapshot = { ConnectivityStatus.Available },
            currentState = { state },
        )
        assertThat(recovery.requestRetry()).isTrue()
        recovery.awaitAndStartReload(webView = null, labelSwapMs = 0)
        assertThat(events).contains(ShellEvent.RetryFailed)
        assertThat(recovery.isInFlight).isFalse()
    }

    @Test
    fun `awaitAndStartReload fails when connectivity drops during label swap`() = runTest {
        val events = mutableListOf<ShellEvent>()
        var state: ShellState = ShellState.Offline
        var connectivity: ConnectivityStatus = ConnectivityStatus.Available
        val recovery = ChannelRecoveryController(
            dispatch = {
                events += it
                when (it) {
                    ShellEvent.RetryRequested -> state = ShellState.Retrying
                    ShellEvent.RetryFailed -> state = ShellState.Offline
                    else -> Unit
                }
            },
            connectivitySnapshot = { connectivity },
            currentState = { state },
        )
        assertThat(recovery.requestRetry()).isTrue()
        connectivity = ConnectivityStatus.Unavailable
        recovery.awaitAndStartReload(webView = null, labelSwapMs = 0)
        assertThat(events).contains(ShellEvent.RetryFailed)
        assertThat(recovery.isInFlight).isFalse()
    }

    @Test
    fun `onChannelReady and onChannelFailed are no-ops when idle`() {
        val recovery = ChannelRecoveryController(
            dispatch = {},
            connectivitySnapshot = { ConnectivityStatus.Available },
            currentState = { ShellState.Ready },
        )
        recovery.onChannelReady()
        recovery.onChannelFailed()
        assertThat(recovery.isInFlight).isFalse()
    }

    @Test
    fun `cancel clears in-flight session`() {
        val recovery = ChannelRecoveryController(
            dispatch = {},
            connectivitySnapshot = { ConnectivityStatus.Available },
            currentState = { ShellState.Offline },
        )
        assertThat(recovery.requestRetry()).isTrue()
        recovery.cancel()
        assertThat(recovery.isInFlight).isFalse()
        // After cancel, a new retry can start.
        assertThat(recovery.requestRetry()).isTrue()
    }

    @Test
    fun `awaitLoadOutcomeOrTimeout fails hung loading`() = runTest {
        val events = mutableListOf<ShellEvent>()
        var state: ShellState = ShellState.Loading
        val recovery = ChannelRecoveryController(
            dispatch = {
                events += it
                if (it == ShellEvent.RetryFailed) state = ShellState.Offline
            },
            connectivitySnapshot = { ConnectivityStatus.Available },
            currentState = { state },
        )
        // Enter flight without going through Offline (simulate in-flight Loading).
        assertThat(recovery.requestRetry()).isFalse() // not Offline
        // Force in-flight via cancel/request from Offline path:
        state = ShellState.Offline
        assertThat(recovery.requestRetry()).isTrue()
        state = ShellState.Loading
        recovery.awaitLoadOutcomeOrTimeout(webView = null, timeoutMs = 0L)
        assertThat(events).contains(ShellEvent.RetryFailed)
        assertThat(recovery.isInFlight).isFalse()
    }
}

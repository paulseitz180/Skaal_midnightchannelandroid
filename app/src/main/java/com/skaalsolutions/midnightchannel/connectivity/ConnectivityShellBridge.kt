package com.skaalsolutions.midnightchannel.connectivity

import androidx.compose.runtime.staticCompositionLocalOf
import com.skaalsolutions.midnightchannel.navigation.ShellController
import com.skaalsolutions.midnightchannel.navigation.ShellEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

/**
 * Bridges [ConnectivityMonitor] → [ShellController] without polling.
 *
 * Emits [ShellEvent.NetworkLost] on cold-start offline and Available→Unavailable.
 * Does **not** emit restoration into the shell reducer — Retry enablement reads
 * [ConnectivityMonitor.status] directly (single source of truth).
 */
class ConnectivityShellBridge(
    private val monitor: ConnectivityMonitor,
    private val shell: ShellController,
    private val scope: CoroutineScope,
) {
    private var collectJob: Job? = null
    private var lastStatus: ConnectivityStatus? = null
    private var coldStartChecked: Boolean = false

    fun start() {
        monitor.start()
        val initial = monitor.currentStatus()
        lastStatus = initial
        if (!coldStartChecked) {
            coldStartChecked = true
            if (initial is ConnectivityStatus.Unavailable) {
                shell.dispatch(ShellEvent.NetworkLost)
            }
        }

        collectJob?.cancel()
        collectJob = scope.launch {
            monitor.status
                .drop(1)
                .distinctUntilChanged()
                .collect { status ->
                    val previous = lastStatus
                    lastStatus = status
                    if (previous is ConnectivityStatus.Available &&
                        status is ConnectivityStatus.Unavailable
                    ) {
                        shell.dispatch(ShellEvent.NetworkLost)
                    }
                }
        }
    }

    fun stop() {
        collectJob?.cancel()
        collectJob = null
        monitor.stop()
    }
}

val LocalConnectivityMonitor =
    staticCompositionLocalOf<ConnectivityMonitor?> { null }

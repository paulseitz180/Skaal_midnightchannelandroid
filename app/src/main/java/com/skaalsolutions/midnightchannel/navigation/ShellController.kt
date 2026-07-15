package com.skaalsolutions.midnightchannel.navigation

import com.skaalsolutions.midnightchannel.recovery.ChannelRecoveryController
import kotlinx.coroutines.flow.StateFlow

/**
 * Single entry point for shell state changes.
 *
 * All UI / WebView / connectivity / recovery code must [dispatch] here —
 * never mutate presentation outside [ShellReducer].
 *
 * Bind [ChannelRecoveryController] via [bindRecovery] after construction
 * (recovery also dispatches through this controller — no dual emit paths).
 */
class ShellController(
    private val store: ShellStore = ShellStore(),
) {
    var recovery: ChannelRecoveryController? = null
        private set

    val state: StateFlow<ShellState> = store.state

    fun current(): ShellState = store.current()

    fun bindRecovery(controller: ChannelRecoveryController) {
        recovery = controller
    }

    fun dispatch(event: ShellEvent) {
        store.dispatch(event)
        notifyRecovery(event)
    }

    private fun notifyRecovery(event: ShellEvent) {
        val hooks = recovery ?: return
        when (event) {
            ShellEvent.MainFrameLoadFinished -> {
                if (store.current() is ShellState.Ready) {
                    hooks.onChannelReady()
                }
            }
            ShellEvent.MainFrameLoadFailed,
            ShellEvent.RetryFailed,
            ShellEvent.NetworkLost,
            ShellEvent.WebViewInitFailed,
            -> hooks.onChannelFailed()
            else -> Unit
        }
    }
}

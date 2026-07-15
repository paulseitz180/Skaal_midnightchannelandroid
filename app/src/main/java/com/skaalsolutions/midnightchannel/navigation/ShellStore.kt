package com.skaalsolutions.midnightchannel.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory shell state holder. Single source for Splash / Channel / Offline.
 * Not persisted — matches Grande Document transient native state rules.
 */
class ShellStore {
    private val _state = MutableStateFlow(ShellReducer.initial())
    val state: StateFlow<ShellState> = _state.asStateFlow()

    fun dispatch(event: ShellEvent) {
        val current = _state.value
        val next = ShellReducer.reduce(current, event)
        // Skip no-op transitions — avoids Compose recomposition for redundant events
        // (e.g. MainFrameLoadStarted while already Loading).
        if (next != current) {
            _state.value = next
        }
    }

    fun current(): ShellState = _state.value
}

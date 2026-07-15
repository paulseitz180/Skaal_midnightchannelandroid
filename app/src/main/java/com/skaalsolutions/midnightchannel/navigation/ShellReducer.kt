package com.skaalsolutions.midnightchannel.navigation

/**
 * Pure reducer for [ShellState] × [ShellEvent] → [ShellState].
 *
 * Rules:
 * - Every handled event has an explicit destination (or stable no-op).
 * - Failure paths share [toOffline] — no duplicated Offline branches.
 * - Illegal / irrelevant events leave state unchanged.
 *
 * ```
 * Splash ──(floor ∧ paint)──► Ready
 *    │                           │
 *    │ NetworkLost / fail        │ reload start
 *    ▼                           ▼
 * Offline ◄── fail ────────── Loading ──finish──► Ready
 *    │                              ▲
 *    │ RetryRequested               │
 *    ▼                              │
 * Retrying ──RetryLoadStarted───────┘
 *    │
 *    └── fail ──► Offline
 * ```
 */
object ShellReducer {

    fun initial(): ShellState = ShellState.Splash()

    fun reduce(state: ShellState, event: ShellEvent): ShellState =
        when (state) {
            is ShellState.Splash -> reduceSplash(state, event)
            ShellState.Loading -> reduceLoading(event)
            ShellState.Ready -> reduceReady(event)
            ShellState.Offline -> reduceOffline(event)
            ShellState.Retrying -> reduceRetrying(event)
        }

    private fun reduceSplash(state: ShellState.Splash, event: ShellEvent): ShellState =
        when (event) {
            ShellEvent.SplashFloorElapsed ->
                advanceSplashIfReady(state.copy(floorElapsed = true))

            ShellEvent.MainFrameLoadFinished ->
                advanceSplashIfReady(state.copy(firstPaintReady = true))

            ShellEvent.WebViewInitFailed,
            ShellEvent.NetworkLost,
            ShellEvent.MainFrameLoadFailed,
            -> toOffline()

            ShellEvent.MainFrameLoadStarted,
            ShellEvent.RetryRequested,
            ShellEvent.RetryLoadStarted,
            ShellEvent.RetryFailed,
            -> state
        }

    private fun advanceSplashIfReady(state: ShellState.Splash): ShellState =
        if (state.canAdvance) ShellState.Ready else state

    private fun reduceLoading(event: ShellEvent): ShellState =
        when (event) {
            ShellEvent.MainFrameLoadFinished -> ShellState.Ready

            ShellEvent.MainFrameLoadFailed,
            ShellEvent.NetworkLost,
            ShellEvent.RetryFailed,
            ShellEvent.WebViewInitFailed,
            -> toOffline()

            ShellEvent.MainFrameLoadStarted,
            ShellEvent.RetryLoadStarted,
            ShellEvent.RetryRequested,
            ShellEvent.SplashFloorElapsed,
            -> ShellState.Loading
        }

    private fun reduceReady(event: ShellEvent): ShellState =
        when (event) {
            ShellEvent.MainFrameLoadStarted,
            ShellEvent.RetryLoadStarted,
            -> ShellState.Loading

            ShellEvent.MainFrameLoadFinished -> ShellState.Ready

            ShellEvent.MainFrameLoadFailed,
            ShellEvent.NetworkLost,
            ShellEvent.RetryFailed,
            ShellEvent.WebViewInitFailed,
            -> toOffline()

            ShellEvent.SplashFloorElapsed,
            ShellEvent.RetryRequested,
            -> ShellState.Ready
        }

    private fun reduceOffline(event: ShellEvent): ShellState =
        when (event) {
            ShellEvent.RetryRequested -> ShellState.Retrying
            else -> ShellState.Offline
        }

    private fun reduceRetrying(event: ShellEvent): ShellState =
        when (event) {
            ShellEvent.RetryLoadStarted -> ShellState.Loading

            ShellEvent.MainFrameLoadFinished -> ShellState.Ready

            ShellEvent.RetryFailed,
            ShellEvent.MainFrameLoadFailed,
            ShellEvent.NetworkLost,
            ShellEvent.WebViewInitFailed,
            -> toOffline()

            // Stay on RECONNECTING… until RetryLoadStarted (ignore nested Retry / early page-start).
            ShellEvent.RetryRequested,
            ShellEvent.MainFrameLoadStarted,
            ShellEvent.SplashFloorElapsed,
            -> ShellState.Retrying
        }

    private fun toOffline(): ShellState = ShellState.Offline
}

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
 * Splash ──SplashFloorElapsed──► TitlePage ──(floor ∧ paint)──► Ready
 *    │                              │                              │
 *    │ NetworkLost / fail           │ NetworkLost / fail           │ reload start
 *    ▼                              ▼                              ▼
 * Offline ◄── fail ──────────…────────────────────────────── Loading ──finish──► Ready
 *    │                                                              ▲
 *    │ RetryRequested                                               │
 *    ▼                                                              │
 * Retrying ──RetryLoadStarted───────────────────────────────────────┘
 * ```
 *
 * WebView load starts during Logo Expand ([Splash]); Title Page never falls
 * through to an unready player — [TitlePage.canAdvance] requires both gates.
 */
object ShellReducer {

    fun initial(): ShellState = ShellState.Splash()

    fun reduce(state: ShellState, event: ShellEvent): ShellState =
        when (state) {
            is ShellState.Splash -> reduceSplash(state, event)
            is ShellState.TitlePage -> reduceTitlePage(state, event)
            ShellState.Loading -> reduceLoading(event)
            ShellState.Ready -> reduceReady(event)
            ShellState.Offline -> reduceOffline(event)
            ShellState.Retrying -> reduceRetrying(event)
        }

    private fun reduceSplash(state: ShellState.Splash, event: ShellEvent): ShellState =
        when (event) {
            ShellEvent.SplashFloorElapsed ->
                ShellState.TitlePage(firstPaintReady = state.firstPaintReady)

            ShellEvent.MainFrameLoadFinished ->
                state.copy(firstPaintReady = true)

            ShellEvent.WebViewInitFailed,
            ShellEvent.NetworkLost,
            ShellEvent.MainFrameLoadFailed,
            -> toOffline()

            ShellEvent.TitleFloorElapsed,
            ShellEvent.MainFrameLoadStarted,
            ShellEvent.RetryRequested,
            ShellEvent.RetryLoadStarted,
            ShellEvent.RetryFailed,
            -> state
        }

    private fun reduceTitlePage(state: ShellState.TitlePage, event: ShellEvent): ShellState =
        when (event) {
            ShellEvent.TitleFloorElapsed ->
                advanceTitleIfReady(state.copy(floorElapsed = true))

            ShellEvent.MainFrameLoadFinished ->
                advanceTitleIfReady(state.copy(firstPaintReady = true))

            ShellEvent.WebViewInitFailed,
            ShellEvent.NetworkLost,
            ShellEvent.MainFrameLoadFailed,
            -> toOffline()

            ShellEvent.SplashFloorElapsed,
            ShellEvent.MainFrameLoadStarted,
            ShellEvent.RetryRequested,
            ShellEvent.RetryLoadStarted,
            ShellEvent.RetryFailed,
            -> state
        }

    private fun advanceTitleIfReady(state: ShellState.TitlePage): ShellState =
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
            ShellEvent.TitleFloorElapsed,
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
            ShellEvent.TitleFloorElapsed,
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

            ShellEvent.RetryRequested,
            ShellEvent.MainFrameLoadStarted,
            ShellEvent.SplashFloorElapsed,
            ShellEvent.TitleFloorElapsed,
            -> ShellState.Retrying
        }

    private fun toOffline(): ShellState = ShellState.Offline
}

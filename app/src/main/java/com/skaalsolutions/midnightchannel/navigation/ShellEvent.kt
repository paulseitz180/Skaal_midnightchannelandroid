package com.skaalsolutions.midnightchannel.navigation

/**
 * Inputs to the shell state machine.
 *
 * One event vocabulary for splash timing, WebView load, connectivity loss, and Retry.
 * Connectivity *restoration* is **not** a shell event — [ConnectivityMonitor.status]
 * alone drives Retry enablement (avoids no-op / duplicate transitions).
 *
 * Listening / unmute is site-owned inside the WebView — no native PlaybackStarted event.
 */
sealed interface ShellEvent {

    // —— Splash ——

    /** Minimum CRT boot floor time has elapsed. */
    data object SplashFloorElapsed : ShellEvent

    /** WebView failed to initialize at the OS level → Offline. */
    data object WebViewInitFailed : ShellEvent

    // —— Channel load ——

    /** Main-frame navigation / reload started → Loading (when on channel). */
    data object MainFrameLoadStarted : ShellEvent

    /** Same-origin main frame finished → Ready (or unlocks Splash paint gate). */
    data object MainFrameLoadFinished : ShellEvent

    /** Main-frame failure → Offline. */
    data object MainFrameLoadFailed : ShellEvent

    // —— Connectivity ——

    /**
     * Validated network unavailable (cold start or runtime).
     * Replaces separate OfflineAtLaunch / RuntimeConnectivityLost events.
     */
    data object NetworkLost : ShellEvent

    // —— Offline / Retry ——

    /** User activated RETRY (accepted only from Offline). */
    data object RetryRequested : ShellEvent

    /** Recovery reload underway → Loading. */
    data object RetryLoadStarted : ShellEvent

    /** Retry failed / timed out → Offline. */
    data object RetryFailed : ShellEvent
}

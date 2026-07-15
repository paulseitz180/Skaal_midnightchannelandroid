package com.skaalsolutions.midnightchannel.navigation

/**
 * Inputs to the shell state machine.
 *
 * One event vocabulary for launch timing, WebView load, connectivity loss, and Retry.
 * Connectivity *restoration* is **not** a shell event — [ConnectivityMonitor.status]
 * alone drives Retry enablement (avoids no-op / duplicate transitions).
 *
 * Listening / unmute is site-owned inside the WebView — no native PlaybackStarted event.
 */
sealed interface ShellEvent {

    // —— Launch ——

    /** Logo Expand floor elapsed → advance to Title Page. */
    data object SplashFloorElapsed : ShellEvent

    /** Title Page 1500ms floor elapsed (may still wait for WebView). */
    data object TitleFloorElapsed : ShellEvent

    /** WebView failed to initialize at the OS level → Offline. */
    data object WebViewInitFailed : ShellEvent

    // —— Channel load ——

    /** Main-frame navigation / reload started → Loading (when on channel). */
    data object MainFrameLoadStarted : ShellEvent

    /** Same-origin main frame finished → Ready gate (or Title Page paint unlock). */
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

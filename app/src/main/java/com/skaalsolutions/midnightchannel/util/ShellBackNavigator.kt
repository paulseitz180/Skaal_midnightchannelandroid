package com.skaalsolutions.midnightchannel.util

/**
 * Resolves system back for the Midnight Channel shell.
 *
 * Resolution order (future-proof):
 * 1. Optional [WebViewHistoryBackHandler] — **disabled in MVP**
 * 2. [DoubleBackExitGate] — toast then exit within [confirmationWindowMs]
 *
 * Enabling history later is a one-line handler swap; toast / exit logic stays put.
 */
class ShellBackNavigator(
    confirmationWindowMs: Long,
    private val historyBackHandler: WebViewHistoryBackHandler = WebViewHistoryBackHandler.Disabled,
    nowMs: () -> Long = System::currentTimeMillis,
) {
    private val exitGate = DoubleBackExitGate(
        confirmationWindowMs = confirmationWindowMs,
        nowMs = nowMs,
    )

    /**
     * @return [BackNavigationResult] for toast / finish, or `null` when history
     * consumed the press (future path only while [WebViewHistoryBackHandler] is enabled).
     */
    fun onBackPressed(): BackNavigationResult? {
        if (historyBackHandler.tryNavigateBack()) {
            // Don't leave exit toast armed across in-page history navigation.
            exitGate.reset()
            return null
        }
        return exitGate.evaluate()
    }

    fun reset() {
        exitGate.reset()
    }
}

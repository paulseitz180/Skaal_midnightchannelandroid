package com.skaalsolutions.midnightchannel.util

/**
 * Pure double-back-to-exit timing gate (Grande Document §5.4 Exit Confirmation).
 *
 * Unit-testable: no Android UI / Toast. Callers map [BackNavigationResult] to toast
 * or [android.app.Activity.finish].
 *
 * @param confirmationWindowMs Configurable arm window (Production Bible default ~2000ms).
 * @param nowMs Clock for tests.
 */
class DoubleBackExitGate(
    private val confirmationWindowMs: Long,
    private val nowMs: () -> Long = System::currentTimeMillis,
) {
    init {
        require(confirmationWindowMs > 0L) { "confirmationWindowMs must be > 0" }
    }

    private var firstPressAtMs: Long = UNARMED

    fun evaluate(): BackNavigationResult {
        val now = nowMs()
        return if (firstPressAtMs != UNARMED && now - firstPressAtMs <= confirmationWindowMs) {
            firstPressAtMs = UNARMED
            BackNavigationResult.ExitApp
        } else {
            firstPressAtMs = now
            BackNavigationResult.ShowExitToast
        }
    }

    /** Clears a pending first press (e.g. after leaving Main Channel or history consume). */
    fun reset() {
        firstPressAtMs = UNARMED
    }

    private companion object {
        const val UNARMED: Long = -1L
    }
}

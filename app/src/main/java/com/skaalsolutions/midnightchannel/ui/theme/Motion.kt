package com.skaalsolutions.midnightchannel.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Motion grammar from the Production Bible (integer ms).
 * Compose is the live source of truth — XML `integers.xml` was removed as a duplicate.
 */
@Immutable
data class CrtMotion(
    /** Splash floor lower bound (spec ~400–600ms). */
    val splashFlickerMinMs: Int = 400,
    /** Default splash floor / flicker period. */
    val splashFlickerMs: Int = 500,
    val screenCrossfadeMs: Int = 200,
    val retryLabelSwapMs: Int = 150,
    /** Double-back exit confirmation window (Grande Document §5.4). */
    val exitConfirmationWindowMs: Int = 2_000,
)

val LocalCrtMotion = staticCompositionLocalOf { CrtMotion() }

package com.skaalsolutions.midnightchannel.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

/** Default splash floor — matches Grande Document ~400–600ms default (500ms). */
const val DEFAULT_SPLASH_FLICKER_MS: Int = 500

/**
 * Motion grammar — Production Bible timings + live-site CRT blink language.
 * Compose is the live source of truth — XML `integers.xml` was removed as a duplicate.
 */
@Immutable
data class CrtMotion(
    /** Splash floor lower bound (spec ~400–600ms). */
    val splashFlickerMinMs: Int = 400,
    /** Default splash floor before cross-fade may proceed. */
    val splashFlickerMs: Int = DEFAULT_SPLASH_FLICKER_MS,
    /**
     * Splash phosphor blink period — matches site `#tap-prompt` `tap-prompt-blink` (2s).
     * Independent of [splashFlickerMs] floor so fidelity does not stretch cold-start gate.
     */
    val splashPhosphorBlinkMs: Int = 2_000,
    /** Site tap-prompt low plate (`50%, 100% { opacity: 0.15 }`). */
    val splashPhosphorMinAlpha: Float = 0.15f,
    val screenCrossfadeMs: Int = 200,
    val retryLabelSwapMs: Int = 150,
    /** Double-back exit confirmation window (Grande Document §5.4). */
    val exitConfirmationWindowMs: Int = 2_000,
)

val LocalCrtMotion = staticCompositionLocalOf { CrtMotion() }

package com.skaalsolutions.midnightchannel.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

/** Logo Expand floor — Grande Document ~400–600ms default (500ms). */
const val DEFAULT_SPLASH_FLICKER_MS: Int = 500

/** Title Page floor — 1500ms (3 beats @ 120 BPM). */
const val DEFAULT_TITLE_PAGE_FLOOR_MS: Int = 1_500

/**
 * Motion grammar — Production Bible timings + live-site CRT blink language.
 * Compose is the live source of truth — XML `integers.xml` was removed as a duplicate.
 */
@Immutable
data class CrtMotion(
    /** Logo Expand floor lower bound (spec ~400–600ms). */
    val splashFlickerMinMs: Int = 400,
    /** Logo Expand floor before cross-fade to Title Page. */
    val splashFlickerMs: Int = DEFAULT_SPLASH_FLICKER_MS,
    /**
     * Title Page phosphor blink — matches site `#tap-prompt` `tap-prompt-blink` (2s).
     * Independent of launch floors so fidelity does not stretch cold-start gates.
     */
    val splashPhosphorBlinkMs: Int = 2_000,
    /** Site tap-prompt low plate (`50%, 100% { opacity: 0.15 }`). */
    val splashPhosphorMinAlpha: Float = 0.15f,
    /** Title Page fixed floor before Player may appear (extends if WebView still loading). */
    val titlePageFloorMs: Int = DEFAULT_TITLE_PAGE_FLOOR_MS,
    val screenCrossfadeMs: Int = 200,
    val retryLabelSwapMs: Int = 150,
    /** Double-back exit confirmation window (Grande Document §5.4). */
    val exitConfirmationWindowMs: Int = 2_000,
)

val LocalCrtMotion = staticCompositionLocalOf { CrtMotion() }

package com.skaalsolutions.midnightchannel.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * CRT colour tokens aligned to live midnightchannel.live + Production Bible.
 *
 * - Field / theme-color: `#0A0A0A` (meta theme-color; WebView blank / window)
 * - Body phosphor: `#00FF00` (site `color: #0f0`, channel chrome, OG wordmark)
 * - Interactive accent: `#00FF41` (site `#tap-prompt` / v1.2 green)
 * Error states stay in-world (no separate red palette).
 */
object CrtColors {
    val Background = Color(0xFF0A0A0A)
    val Black = Color(0xFF000000)
    val Accent = Color(0xFF00FF41)
    val Phosphor = Color(0xFF00FF00)
    val AccentMuted = Color(0x9900FF41)
    val AccentDim = Color(0x4D00FF41)
    /** Phosphor at ~60% — secondary body copy matching site opacity, not Material grey. */
    val PhosphorMuted = Color(0x9900FF00)
    /** Phosphor at ~30% — disabled CRT chrome. */
    val PhosphorDim = Color(0x4D00FF00)
    /** Default on-field text follows site body `#0f0`. */
    val OnBackground = Phosphor
    val Scrim = Color(0xCC0A0A0A)
    val Transparent = Color(0x00000000)

    /** Soft glow approximating site `text-shadow: 0 0 6px #0f0`. */
    val PhosphorGlow = Color(0x9900FF00)

    /** Status / navigation bar chrome — matched to theme-color field. */
    val SystemBar = Background
}

@Immutable
data class CrtColorPalette(
    val background: Color = CrtColors.Background,
    val black: Color = CrtColors.Black,
    val accent: Color = CrtColors.Accent,
    val phosphor: Color = CrtColors.Phosphor,
    val accentMuted: Color = CrtColors.AccentMuted,
    val accentDim: Color = CrtColors.AccentDim,
    val phosphorMuted: Color = CrtColors.PhosphorMuted,
    val phosphorDim: Color = CrtColors.PhosphorDim,
    val onBackground: Color = CrtColors.OnBackground,
    val phosphorGlow: Color = CrtColors.PhosphorGlow,
    val scrim: Color = CrtColors.Scrim,
    val systemBar: Color = CrtColors.SystemBar,
)

val LocalCrtColors = staticCompositionLocalOf { CrtColorPalette() }

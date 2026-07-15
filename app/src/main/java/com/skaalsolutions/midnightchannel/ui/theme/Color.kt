package com.skaalsolutions.midnightchannel.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * CRT colour tokens aligned to XML `colors.xml` and the Production Bible.
 *
 * Background `#0A0A0A` and accent `#00FF41` match the live midnightchannel.live
 * theme-color / v1.2 green. Error states stay in-world (no separate red palette).
 */
object CrtColors {
    val Background = Color(0xFF0A0A0A)
    val Black = Color(0xFF000000)
    val Accent = Color(0xFF00FF41)
    val Phosphor = Color(0xFF00FF00)
    val AccentMuted = Color(0x9900FF41)
    val AccentDim = Color(0x4D00FF41)
    val OnBackground = Accent
    val Scrim = Color(0xCC0A0A0A)
    val Transparent = Color(0x00000000)

    /** Status / navigation bar chrome — matched to the CRT field. */
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
    val onBackground: Color = CrtColors.OnBackground,
    val scrim: Color = CrtColors.Scrim,
    val systemBar: Color = CrtColors.SystemBar,
)

val LocalCrtColors = staticCompositionLocalOf { CrtColorPalette() }

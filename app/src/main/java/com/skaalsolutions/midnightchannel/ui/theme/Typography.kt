package com.skaalsolutions.midnightchannel.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

/**
 * Site type stack is `'Courier New', Courier, monospace`.
 * Compose uses [FontFamily.Monospace] until a licensed Courier New asset is bundled.
 *
 * Weights stay [FontWeight.Normal] — the site does not use Medium / bold chrome.
 * Tracking mirrors site `#tap-prompt` (`letter-spacing: 0.25em`) and OG wordmark spacing.
 */
private val CrtFontFamily = FontFamily.Monospace

/** Soft phosphor halo matching site `text-shadow: 0 0 6px #0f0`. */
private val PhosphorHalo = Shadow(
    color = CrtColors.PhosphorGlow,
    offset = Offset.Zero,
    blurRadius = 12f,
)

private val AccentHalo = Shadow(
    color = CrtColors.AccentMuted,
    offset = Offset.Zero,
    blurRadius = 12f,
)

private val BaseMono = TextStyle(
    fontFamily = CrtFontFamily,
    fontWeight = FontWeight.Normal,
    color = CrtColors.OnBackground,
)

/**
 * Named styles for every native surface in the Grande Document:
 * Splash, Offline/Error, and Channel host chrome (copy only — no Material buttons).
 */
@Immutable
data class CrtTypographyTokens(
    val caption: TextStyle = BaseMono.copy(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        // ~0.25em tracking at 14sp — site tap-prompt / channel chrome language
        letterSpacing = 3.5.sp,
        color = CrtColors.PhosphorMuted,
        shadow = PhosphorHalo,
    ),
    val body: TextStyle = BaseMono.copy(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        color = CrtColors.OnBackground,
    ),
    val bodyEmphasis: TextStyle = BaseMono.copy(
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.5.sp,
        color = CrtColors.OnBackground,
    ),
    val title: TextStyle = BaseMono.copy(
        fontSize = 22.sp,
        lineHeight = 30.sp,
        letterSpacing = 4.sp,
        fontWeight = FontWeight.Normal,
        color = CrtColors.Phosphor,
        shadow = PhosphorHalo,
    ),
    val display: TextStyle = BaseMono.copy(
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 6.sp,
        fontWeight = FontWeight.Normal,
        color = CrtColors.Phosphor,
        shadow = PhosphorHalo,
    ),
    /** Splash centred wordmark (`MIDNIGHT CHANNEL`) — OG / site phosphor + wide tracking. */
    val wordmark: TextStyle = BaseMono.copy(
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 8.sp,
        fontWeight = FontWeight.Normal,
        color = CrtColors.Phosphor,
        textAlign = TextAlign.Center,
        shadow = PhosphorHalo,
    ),
    /**
     * Offline/Error CTA — plain monospace text link.
     * Colour follows site interactive green `#00FF41` (tap-prompt), not phosphor body.
     */
    val link: TextStyle = BaseMono.copy(
        fontSize = 18.sp,
        lineHeight = 28.sp,
        letterSpacing = 4.5.sp,
        fontWeight = FontWeight.Normal,
        color = CrtColors.Accent,
        shadow = AccentHalo,
    ),
)

val CrtTextStyles = CrtTypographyTokens()

val LocalCrtTypography = staticCompositionLocalOf { CrtTextStyles }

/**
 * Material 3 typography bridge so MaterialTheme consumers inherit monospace CRT type.
 */
val MidnightTypography = Typography(
    displayLarge = CrtTextStyles.display,
    displayMedium = CrtTextStyles.display,
    displaySmall = CrtTextStyles.title,
    headlineLarge = CrtTextStyles.title,
    headlineMedium = CrtTextStyles.title,
    headlineSmall = CrtTextStyles.bodyEmphasis,
    titleLarge = CrtTextStyles.title,
    titleMedium = CrtTextStyles.bodyEmphasis,
    titleSmall = CrtTextStyles.body,
    bodyLarge = CrtTextStyles.body,
    bodyMedium = CrtTextStyles.body,
    bodySmall = CrtTextStyles.caption,
    labelLarge = CrtTextStyles.link,
    labelMedium = CrtTextStyles.body,
    labelSmall = CrtTextStyles.caption,
)

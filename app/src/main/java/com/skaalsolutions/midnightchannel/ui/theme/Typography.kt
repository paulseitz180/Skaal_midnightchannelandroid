package com.skaalsolutions.midnightchannel.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

/**
 * Site type stack is `'Courier New', Courier, monospace`.
 * Compose uses [FontFamily.Monospace] until a licensed Courier New asset is bundled.
 */
private val CrtFontFamily = FontFamily.Monospace

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
        letterSpacing = 0.06.sp,
        color = CrtColors.AccentMuted,
    ),
    val body: TextStyle = BaseMono.copy(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.04.sp,
        color = CrtColors.OnBackground,
    ),
    val bodyEmphasis: TextStyle = BaseMono.copy(
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.04.sp,
        color = CrtColors.OnBackground,
    ),
    val title: TextStyle = BaseMono.copy(
        fontSize = 22.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.08.sp,
        fontWeight = FontWeight.Medium,
        color = CrtColors.Accent,
    ),
    val display: TextStyle = BaseMono.copy(
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.1.sp,
        fontWeight = FontWeight.Medium,
        color = CrtColors.Accent,
    ),
    /** Splash centred wordmark (`MIDNIGHT CHANNEL`). */
    val wordmark: TextStyle = BaseMono.copy(
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.12.sp,
        fontWeight = FontWeight.Medium,
        color = CrtColors.Accent,
        textAlign = TextAlign.Center,
    ),
    /** Offline/Error primary CTA — plain monospace text link, not a filled button. */
    val link: TextStyle = BaseMono.copy(
        fontSize = 18.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.1.sp,
        fontWeight = FontWeight.Medium,
        color = CrtColors.Accent,
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

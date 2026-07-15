package com.skaalsolutions.midnightchannel.ui.theme

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * CRT dark colour scheme — always-on; Midnight Channel has no light theme.
 * Phosphor / accent are drawn as text, not filled Material primary surfaces.
 */
private val MidnightColorScheme = darkColorScheme(
    primary = CrtColors.Accent,
    onPrimary = CrtColors.Background,
    primaryContainer = CrtColors.Background,
    onPrimaryContainer = CrtColors.Accent,
    secondary = CrtColors.Phosphor,
    onSecondary = CrtColors.Background,
    secondaryContainer = CrtColors.Background,
    onSecondaryContainer = CrtColors.PhosphorMuted,
    tertiary = CrtColors.AccentMuted,
    onTertiary = CrtColors.Background,
    background = CrtColors.Background,
    onBackground = CrtColors.OnBackground,
    surface = CrtColors.Background,
    onSurface = CrtColors.OnBackground,
    surfaceVariant = CrtColors.Black,
    onSurfaceVariant = CrtColors.PhosphorMuted,
    error = CrtColors.Accent,
    onError = CrtColors.Background,
    outline = CrtColors.PhosphorDim,
    outlineVariant = CrtColors.AccentDim,
    scrim = CrtColors.Scrim,
)

/**
 * Root Compose theme for every native screen (Splash, Channel host, Offline/Error).
 *
 * Applies CRT palette / monospace type / hard shapes, paints a **single** full-bleed
 * CRT field (children must not re-paint the same radial — avoids stacked drawWithCache
 * cost under Splash flicker), and configures immersive system bars.
 */
@Composable
fun MidnightChannelTheme(
    content: @Composable () -> Unit,
) {
    ConfigureCrtSystemBars()

    CompositionLocalProvider(
        LocalCrtColors provides DefaultCrtColors,
        LocalCrtTypography provides CrtTextStyles,
        LocalCrtSpacing provides DefaultCrtSpacing,
        LocalCrtShapes provides DefaultCrtShapes,
        LocalCrtMotion provides DefaultCrtMotion,
    ) {
        MaterialTheme(
            colorScheme = MidnightColorScheme,
            typography = MidnightTypography,
            shapes = MidnightShapes,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .crtFieldBackground(),
            ) {
                content()
            }
        }
    }
}

/** Theme-scoped token singletons — avoid per-composition `remember { }` allocations. */
private val DefaultCrtColors = CrtColorPalette()
private val DefaultCrtSpacing = CrtSpacing()
private val DefaultCrtShapes = CrtShapes()
private val DefaultCrtMotion = CrtMotion()

/**
 * Edge-to-edge immersive chrome for Splash, Channel host, and Offline/Error.
 *
 * Relies on Activity [androidx.activity.enableEdgeToEdge] for transparent system bars
 * colour-matched via the CRT window theme (`#0A0A0A`). Forces dark-scrim icon
 * appearance so status/navigation icons remain light on the phosphor field.
 */
@Composable
private fun ConfigureCrtSystemBars() {
    val view = LocalView.current
    if (view.isInEditMode) return

    // Once per composition entry — SideEffect would re-run after every recomposition.
    DisposableEffect(view) {
        val activity = view.context as? Activity
        if (activity != null) {
            val window = activity.window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
        onDispose { }
    }
}

/** Accessor for CRT tokens from any composable under [MidnightChannelTheme]. */
object MidnightTheme {
    val colors: CrtColorPalette
        @Composable
        get() = LocalCrtColors.current

    val typography: CrtTypographyTokens
        @Composable
        get() = LocalCrtTypography.current

    val spacing: CrtSpacing
        @Composable
        get() = LocalCrtSpacing.current

    val shapes: CrtShapes
        @Composable
        get() = LocalCrtShapes.current

    val motion: CrtMotion
        @Composable
        get() = LocalCrtMotion.current
}

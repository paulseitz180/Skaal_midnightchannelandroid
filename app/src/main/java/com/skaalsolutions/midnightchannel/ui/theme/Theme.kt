package com.skaalsolutions.midnightchannel.ui.theme

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * CRT dark colour scheme — always-on; Midnight Channel has no light theme.
 * Accent is drawn as text/phosphor, not as filled Material primary surfaces.
 */
private val MidnightColorScheme = darkColorScheme(
    primary = CrtColors.Accent,
    onPrimary = CrtColors.Background,
    primaryContainer = CrtColors.Background,
    onPrimaryContainer = CrtColors.Accent,
    secondary = CrtColors.Phosphor,
    onSecondary = CrtColors.Background,
    secondaryContainer = CrtColors.Background,
    onSecondaryContainer = CrtColors.AccentMuted,
    tertiary = CrtColors.AccentMuted,
    onTertiary = CrtColors.Background,
    background = CrtColors.Background,
    onBackground = CrtColors.OnBackground,
    surface = CrtColors.Background,
    onSurface = CrtColors.OnBackground,
    surfaceVariant = CrtColors.Black,
    onSurfaceVariant = CrtColors.AccentMuted,
    error = CrtColors.Accent,
    onError = CrtColors.Background,
    outline = CrtColors.AccentDim,
    outlineVariant = CrtColors.AccentDim,
    scrim = CrtColors.Scrim,
)

/**
 * Root Compose theme for every native screen (Splash, Channel host, Offline/Error).
 *
 * Applies CRT palette / monospace type / hard shapes, paints a full-bleed CRT
 * background to prevent white flash, and configures immersive system bars
 * (dark icons off — phosphor-on-black field).
 */
@Composable
fun MidnightChannelTheme(
    content: @Composable () -> Unit,
) {
    val colors = remember { CrtColorPalette() }
    val typography = remember { CrtTextStyles }
    val spacing = remember { CrtSpacing() }
    val shapes = remember { CrtShapes() }
    val motion = remember { CrtMotion() }

    ConfigureCrtSystemBars()

    CompositionLocalProvider(
        LocalCrtColors provides colors,
        LocalCrtTypography provides typography,
        LocalCrtSpacing provides spacing,
        LocalCrtShapes provides shapes,
        LocalCrtMotion provides motion,
    ) {
        MaterialTheme(
            colorScheme = MidnightColorScheme,
            typography = MidnightTypography,
            shapes = MidnightShapes,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background),
            ) {
                content()
            }
        }
    }
}

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

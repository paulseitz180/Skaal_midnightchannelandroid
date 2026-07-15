package com.skaalsolutions.midnightchannel.ui.a11y

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.getSystemService
import com.skaalsolutions.midnightchannel.ui.theme.CrtColorPalette

/**
 * Native-shell accessibility helpers (TalkBack, contrast, motion).
 * Does not touch the WebView site tree.
 */
data class ShellAccessibilityState(
    val highTextContrastEnabled: Boolean,
    val reduceMotionPreferred: Boolean,
)

@Composable
fun rememberShellAccessibilityState(): ShellAccessibilityState {
    val context = LocalContext.current
    return remember(context) {
        val am = context.getSystemService<AccessibilityManager>()
        ShellAccessibilityState(
            highTextContrastEnabled = isHighTextContrastEnabled(context),
            reduceMotionPreferred = isReduceMotionPreferred(context, am),
        )
    }
}

/** Secondary body copy — full phosphor under high-text-contrast mode. */
fun CrtColorPalette.accessibleSecondary(a11y: ShellAccessibilityState): Color =
    if (a11y.highTextContrastEnabled) phosphor else phosphorMuted

/**
 * Disabled / dim body copy in the phosphor family (not interactive `#00FF41` CTAs).
 * Offline RETRY disabled/reconnecting states stay on the accent family intentionally.
 */
fun CrtColorPalette.accessibleDisabled(a11y: ShellAccessibilityState): Color =
    if (a11y.highTextContrastEnabled) phosphorMuted else phosphorDim

/**
 * Announce [message] for TalkBack. Prefer this alongside Toast for exit confirmation
 * (toasts are not reliably spoken).
 */
@Suppress("DEPRECATION")
fun Context.announceForAccessibility(message: CharSequence) {
    val am = getSystemService<AccessibilityManager>() ?: return
    if (!am.isEnabled) return
    runCatching {
        (this as? android.app.Activity)?.window?.decorView
            ?.announceForAccessibility(message)
    }
}

private fun isHighTextContrastEnabled(context: Context): Boolean =
    runCatching {
        Settings.Secure.getInt(
            context.contentResolver,
            HIGH_TEXT_CONTRAST_SECURE_SETTING,
            0,
        ) == 1
    }.getOrDefault(false)

private fun isReduceMotionPreferred(
    context: Context,
    am: AccessibilityManager?,
): Boolean {
    val animatorDuration = runCatching {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        )
    }.getOrDefault(1f)
    if (animatorDuration == 0f) return true
    return am?.isTouchExplorationEnabled == true &&
        !am.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK,
        ).isNullOrEmpty()
}

private const val HIGH_TEXT_CONTRAST_SECURE_SETTING = "high_text_contrast_enabled"

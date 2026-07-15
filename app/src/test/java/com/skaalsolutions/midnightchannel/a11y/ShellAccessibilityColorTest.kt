package com.skaalsolutions.midnightchannel.a11y

import com.google.common.truth.Truth.assertThat
import com.skaalsolutions.midnightchannel.ui.a11y.ShellAccessibilityState
import com.skaalsolutions.midnightchannel.ui.a11y.accessibleDisabled
import com.skaalsolutions.midnightchannel.ui.a11y.accessibleSecondary
import com.skaalsolutions.midnightchannel.ui.theme.CrtColorPalette
import org.junit.Test

/**
 * TASK 35 — accessibility colour helper regression (high-contrast / secondary copy).
 * System Settings / TalkBack service wiring remains Orchestrator device QA.
 */
class ShellAccessibilityColorTest {

    private val colors = CrtColorPalette()

    @Test
    fun secondary_copy_uses_phosphorMuted_unless_high_contrast() {
        val normal = ShellAccessibilityState(
            highTextContrastEnabled = false,
            reduceMotionPreferred = false,
        )
        val highContrast = ShellAccessibilityState(
            highTextContrastEnabled = true,
            reduceMotionPreferred = false,
        )

        assertThat(colors.accessibleSecondary(normal)).isEqualTo(colors.phosphorMuted)
        assertThat(colors.accessibleSecondary(highContrast)).isEqualTo(colors.phosphor)
    }

    @Test
    fun disabled_copy_uses_phosphorDim_unless_high_contrast() {
        val normal = ShellAccessibilityState(
            highTextContrastEnabled = false,
            reduceMotionPreferred = true,
        )
        val highContrast = ShellAccessibilityState(
            highTextContrastEnabled = true,
            reduceMotionPreferred = true,
        )

        assertThat(colors.accessibleDisabled(normal)).isEqualTo(colors.phosphorDim)
        assertThat(colors.accessibleDisabled(highContrast)).isEqualTo(colors.phosphorMuted)
    }
}

package com.skaalsolutions.midnightchannel.theme

import androidx.compose.ui.graphics.toArgb
import com.google.common.truth.Truth.assertThat
import com.skaalsolutions.midnightchannel.ui.theme.CrtColors
import com.skaalsolutions.midnightchannel.ui.theme.CrtField
import com.skaalsolutions.midnightchannel.ui.theme.CrtMotion
import com.skaalsolutions.midnightchannel.ui.theme.CrtSpacing
import com.skaalsolutions.midnightchannel.ui.theme.CrtTextStyles
import com.skaalsolutions.midnightchannel.webview.CrtShellChrome
import org.junit.Test

/**
 * TASK 35 — regression locks on Grande Document / live-site visual & motion tokens
 * so later TASKS cannot silently drift splash timing or CRT palette.
 */
class CrtTokenRegressionTest {

    @Test
    fun `splash floor and motion grammar match production bible defaults`() {
        val motion = CrtMotion()
        assertThat(motion.splashFlickerMinMs).isEqualTo(400)
        assertThat(motion.splashFlickerMs).isEqualTo(500)
        assertThat(motion.splashPhosphorBlinkMs).isEqualTo(2_000)
        assertThat(motion.splashPhosphorMinAlpha).isWithin(0.001f).of(0.15f)
        assertThat(motion.screenCrossfadeMs).isEqualTo(200)
        assertThat(motion.retryLabelSwapMs).isEqualTo(150)
        assertThat(motion.exitConfirmationWindowMs).isEqualTo(2_000)
    }

    @Test
    fun `crt colour tokens match live site theme-color phosphor and accent`() {
        assertThat(CrtColors.Background.toArgb()).isEqualTo(0xFF0A0A0A.toInt())
        assertThat(CrtColors.Accent.toArgb()).isEqualTo(0xFF00FF41.toInt())
        assertThat(CrtColors.Phosphor.toArgb()).isEqualTo(0xFF00FF00.toInt())
        assertThat(CrtColors.OnBackground).isEqualTo(CrtColors.Phosphor)
        assertThat(CrtShellChrome.BACKGROUND_HEX).isEqualTo("#0A0A0A")
    }

    @Test
    fun `crt field stops match live site body radial gradient`() {
        assertThat(CrtField.Center.toArgb()).isEqualTo(0xFF0A0804.toInt())
        assertThat(CrtField.Mid.toArgb()).isEqualTo(0xFF020202.toInt())
        assertThat(CrtField.Edge.toArgb()).isEqualTo(0xFF000000.toInt())
    }

    @Test
    fun `native type scale stays readable and monospace CRT`() {
        assertThat(CrtTextStyles.body.fontSize.value).isAtLeast(16f)
        assertThat(CrtTextStyles.wordmark.fontFamily)
            .isEqualTo(CrtTextStyles.body.fontFamily)
        assertThat(CrtTextStyles.link.color).isEqualTo(CrtColors.Accent)
        assertThat(CrtTextStyles.wordmark.color).isEqualTo(CrtColors.Phosphor)
        assertThat(CrtTextStyles.display.color).isEqualTo(CrtColors.Phosphor)
    }

    @Test
    fun `spacing unit and hard edges remain CRT constraints`() {
        val spacing = CrtSpacing()
        assertThat(spacing.unit.value).isEqualTo(8f)
        assertThat(spacing.touchTargetMin.value).isEqualTo(48f)
        assertThat(spacing.screenEdge.value).isEqualTo(16f)
    }
}

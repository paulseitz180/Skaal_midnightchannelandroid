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
    fun splash_floor_and_motion_grammar_match_production_bible_defaults() {
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
    fun crt_colour_tokens_match_live_site_theme_color_phosphor_and_accent() {
        assertThat(CrtColors.Background.toArgb()).isEqualTo(0xFF0A0A0A.toInt())
        assertThat(CrtColors.Accent.toArgb()).isEqualTo(0xFF00FF41.toInt())
        assertThat(CrtColors.Phosphor.toArgb()).isEqualTo(0xFF00FF00.toInt())
        assertThat(CrtColors.OnBackground).isEqualTo(CrtColors.Phosphor)
        assertThat(CrtShellChrome.BACKGROUND_HEX).isEqualTo("#0A0A0A")
    }

    @Test
    fun crt_field_stops_match_live_site_body_radial_gradient() {
        assertThat(CrtField.Center.toArgb()).isEqualTo(0xFF0A0804.toInt())
        assertThat(CrtField.Mid.toArgb()).isEqualTo(0xFF020202.toInt())
        assertThat(CrtField.Edge.toArgb()).isEqualTo(0xFF000000.toInt())
    }

    @Test
    fun native_type_scale_stays_readable_and_monospace_CRT() {
        assertThat(CrtTextStyles.body.fontSize.value).isAtLeast(16f)
        assertThat(CrtTextStyles.wordmark.fontFamily)
            .isEqualTo(CrtTextStyles.body.fontFamily)
        assertThat(CrtTextStyles.link.color).isEqualTo(CrtColors.Accent)
        assertThat(CrtTextStyles.wordmark.color).isEqualTo(CrtColors.Phosphor)
        assertThat(CrtTextStyles.display.color).isEqualTo(CrtColors.Phosphor)
    }

    @Test
    fun spacing_unit_and_hard_edges_remain_CRT_constraints() {
        val spacing = CrtSpacing()
        assertThat(spacing.unit.value).isEqualTo(8f)
        assertThat(spacing.touchTargetMin.value).isEqualTo(48f)
        assertThat(spacing.screenEdge.value).isEqualTo(16f)
    }
}

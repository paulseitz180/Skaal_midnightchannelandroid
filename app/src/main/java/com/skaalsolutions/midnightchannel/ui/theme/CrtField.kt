package com.skaalsolutions.midnightchannel.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.max

/**
 * Live site body field (midnightchannel.live, ENABLE_V12_VISUAL off):
 * `radial-gradient(ellipse at 50% 60%, rgba(10,8,4,1) 0%, rgba(2,2,2,1) 60%, #000 100%)`.
 *
 * Compose owns these stops (not duplicated in `colors.xml`) so the CRT field brush
 * stays the single source for Splash / Offline / shell chrome.
 */
object CrtField {
    val Center = Color(0xFF0A0804)
    val Mid = Color(0xFF020202)
    val Edge = Color(0xFF000000)

    /** Shared stops — avoid allocating a new array on every size change. */
    private val ColorStops: Array<Pair<Float, Color>> = arrayOf(
        0.0f to Center,
        0.6f to Mid,
        1.0f to Edge,
    )

    fun brush(sizeWidth: Float, sizeHeight: Float): Brush {
        val center = Offset(sizeWidth * 0.5f, sizeHeight * 0.6f)
        val radius = max(sizeWidth, sizeHeight) * 0.95f
        return Brush.radialGradient(
            colorStops = ColorStops,
            center = center,
            radius = radius.coerceAtLeast(1f),
        )
    }
}

/**
 * Full-bleed CRT field. Uses [drawWithCache] so the radial [Brush] is rebuilt only
 * when size changes — not on every flicker/recomposition frame.
 */
fun Modifier.crtFieldBackground(): Modifier =
    drawWithCache {
        val fieldBrush = CrtField.brush(size.width, size.height)
        onDrawBehind {
            drawRect(brush = fieldBrush)
        }
    }

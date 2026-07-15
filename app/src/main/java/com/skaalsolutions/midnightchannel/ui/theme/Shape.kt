package com.skaalsolutions.midnightchannel.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Production Bible: corner radius 0 — hard edges throughout.
 * Native chrome must not look like rounded Material components.
 */
@Immutable
data class CrtShapes(
    val none: Shape = RectangleShape,
    val surface: Shape = RoundedCornerShape(0.dp),
)

val LocalCrtShapes = staticCompositionLocalOf { CrtShapes() }

val MidnightShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp),
)

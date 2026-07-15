package com.skaalsolutions.midnightchannel.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing tokens — Production Bible base unit = 8dp.
 */
@Immutable
data class CrtSpacing(
    val unit: Dp = 8.dp,
    val none: Dp = 0.dp,
    val half: Dp = 4.dp,
    val one: Dp = 8.dp,
    val two: Dp = 16.dp,
    val three: Dp = 24.dp,
    val four: Dp = 32.dp,
    val five: Dp = 40.dp,
    val six: Dp = 48.dp,
    val eight: Dp = 64.dp,
    val screenEdge: Dp = 16.dp,
    val contentMaxWidth: Dp = 480.dp,
    val touchTargetMin: Dp = 48.dp,
    val splashMarkSize: Dp = 96.dp,
)

val LocalCrtSpacing = staticCompositionLocalOf { CrtSpacing() }

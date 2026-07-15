package com.skaalsolutions.midnightchannel.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import com.skaalsolutions.midnightchannel.R
import com.skaalsolutions.midnightchannel.ui.testing.ShellUiTestTags
import com.skaalsolutions.midnightchannel.ui.theme.DEFAULT_SPLASH_FLICKER_MS
import com.skaalsolutions.midnightchannel.ui.theme.MidnightTheme
import kotlinx.coroutines.delay

/**
 * Phase 1 — Logo Expand.
 *
 * Brief CRT dial scale/reveal after icon tap. WebView load runs in parallel
 * under this overlay; advancing to Title Page does **not** wait for the channel.
 */
@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    minDisplayDurationMs: Int = DEFAULT_SPLASH_FLICKER_MS,
    contentAlpha: Float = 1f,
    onMinimumDurationElapsed: () -> Unit = {},
) {
    val spacing = MidnightTheme.spacing
    val motion = MidnightTheme.motion
    val onElapsed = rememberUpdatedState(onMinimumDurationElapsed)
    val scale = remember { Animatable(0.82f) }

    LaunchedEffect(minDisplayDurationMs) {
        val floorMs = minDisplayDurationMs.coerceAtLeast(motion.splashFlickerMinMs).toLong()
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = floorMs.toInt().coerceAtLeast(1),
                easing = FastOutSlowInEasing,
            ),
        )
    }

    LaunchedEffect(minDisplayDurationMs) {
        delay(minDisplayDurationMs.coerceAtLeast(motion.splashFlickerMinMs).toLong())
        onElapsed.value()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(contentAlpha.coerceIn(0f, 1f))
            .testTag(ShellUiTestTags.SPLASH_ROOT)
            .semantics {
                contentDescription = "Midnight Channel"
            },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.splash_mark),
            contentDescription = null,
            modifier = Modifier
                .systemBarsPadding()
                .size(spacing.splashMarkSize)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                }
                .semantics { hideFromAccessibility() },
        )
    }
}

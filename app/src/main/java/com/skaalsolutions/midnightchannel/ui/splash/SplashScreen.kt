package com.skaalsolutions.midnightchannel.ui.splash

import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.skaalsolutions.midnightchannel.R
import com.skaalsolutions.midnightchannel.ui.a11y.accessibleSecondary
import com.skaalsolutions.midnightchannel.ui.a11y.announceForAccessibility
import com.skaalsolutions.midnightchannel.ui.a11y.rememberShellAccessibilityState
import com.skaalsolutions.midnightchannel.ui.testing.ShellUiTestTags
import com.skaalsolutions.midnightchannel.ui.theme.DEFAULT_SPLASH_FLICKER_MS
import com.skaalsolutions.midnightchannel.ui.theme.MidnightTheme
import kotlinx.coroutines.delay

/**
 * Native CRT Splash Screen (Grande Document Screen Blueprint 1).
 *
 * Visual language mirrors live midnightchannel.live: phosphor wordmark + glow,
 * tap-prompt-style step blink (2s / 0.15 floor). CRT field is painted once by
 * [com.skaalsolutions.midnightchannel.ui.theme.MidnightChannelTheme] (no stacked gradients).
 * Accessibility: merged pane description, polite live region, TalkBack announce,
 * scalable type with scroll, reduce-motion disables phosphor flicker.
 */
@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    minDisplayDurationMs: Int = DEFAULT_SPLASH_FLICKER_MS,
    showCaption: Boolean = true,
    contentAlpha: Float = 1f,
    onMinimumDurationElapsed: () -> Unit = {},
) {
    val colors = MidnightTheme.colors
    val typography = MidnightTheme.typography
    val spacing = MidnightTheme.spacing
    val motion = MidnightTheme.motion
    val context = LocalContext.current
    val a11y = rememberShellAccessibilityState()
    val accessibilityDescription = stringResource(R.string.splash_content_description)
    val statusAnnouncement = stringResource(R.string.splash_status_announcement)
    val onElapsed = rememberUpdatedState(onMinimumDurationElapsed)
    val captionColor = colors.accessibleSecondary(a11y)

    LaunchedEffect(Unit) {
        context.announceForAccessibility(statusAnnouncement)
    }

    LaunchedEffect(minDisplayDurationMs) {
        delay(minDisplayDurationMs.coerceAtLeast(motion.splashFlickerMinMs).toLong())
        onElapsed.value()
    }

    val blinkMs = motion.splashPhosphorBlinkMs.coerceAtLeast(2)
    val blinkFloor = motion.splashPhosphorMinAlpha
    val flicker = rememberInfiniteTransition(label = "crtSplashFlicker")
    val animatedFlicker by flicker.animateFloat(
        initialValue = 1f,
        targetValue = blinkFloor,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = blinkMs
                // Match site `tap-prompt-blink`: hard hold, then step to low plate.
                1f at 0
                1f at blinkMs / 2 - 1
                blinkFloor at blinkMs / 2
                blinkFloor at blinkMs
            },
        ),
        label = "crtSplashFlickerAlpha",
    )
    val flickerAlpha = if (a11y.reduceMotionPreferred) 1f else animatedFlicker

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(contentAlpha.coerceIn(0f, 1f))
            .testTag(ShellUiTestTags.SPLASH_ROOT)
            .semantics(mergeDescendants = true) {
                contentDescription = accessibilityDescription
                liveRegion = LiveRegionMode.Polite
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.screenEdge)
                .graphicsLayer { alpha = flickerAlpha },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.splash_mark),
                contentDescription = null,
                modifier = Modifier
                    .size(spacing.splashMarkSize)
                    .semantics { hideFromAccessibility() },
            )

            Spacer(modifier = Modifier.height(spacing.two))

            Text(
                text = stringResource(R.string.splash_wordmark),
                style = typography.wordmark,
                color = colors.phosphor,
                textAlign = TextAlign.Center,
                softWrap = true,
                modifier = Modifier.semantics { heading() },
            )

            if (showCaption) {
                Spacer(modifier = Modifier.height(spacing.one))
                Text(
                    text = stringResource(R.string.splash_caption),
                    style = typography.caption,
                    color = captionColor,
                    textAlign = TextAlign.Center,
                    softWrap = true,
                )
            }
        }
    }
}

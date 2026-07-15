package com.skaalsolutions.midnightchannel.ui.offline

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.skaalsolutions.midnightchannel.R
import com.skaalsolutions.midnightchannel.ui.a11y.accessibleDisabled
import com.skaalsolutions.midnightchannel.ui.a11y.accessibleSecondary
import com.skaalsolutions.midnightchannel.ui.a11y.announceForAccessibility
import com.skaalsolutions.midnightchannel.ui.a11y.rememberShellAccessibilityState
import com.skaalsolutions.midnightchannel.ui.theme.MidnightTheme

/**
 * Native Offline / Error Screen (Grande Document Screen Blueprint 3).
 *
 * Accessibility: heading → helper → 48dp Retry (keyboard-focusable), live region,
 * state descriptions for reconnecting / no-network, high-contrast secondary copy.
 */
@Composable
fun OfflineErrorScreen(
    modifier: Modifier = Modifier,
    isReconnecting: Boolean = false,
    retryEnabled: Boolean = true,
    contentAlpha: Float = 1f,
    onRetryClick: () -> Unit = {},
) {
    val colors = MidnightTheme.colors
    val typography = MidnightTheme.typography
    val spacing = MidnightTheme.spacing
    val motion = MidnightTheme.motion
    val context = LocalContext.current
    val a11y = rememberShellAccessibilityState()
    val screenDescription = stringResource(R.string.offline_screen_content_description)
    val helperColor = colors.accessibleSecondary(a11y)
    val retryFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        context.announceForAccessibility(screenDescription)
        // Move keyboard / D-pad focus to Retry when the pane appears.
        runCatching { retryFocus.requestFocus() }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(contentAlpha.coerceIn(0f, 1f))
            .background(colors.background)
            .semantics {
                contentDescription = screenDescription
                liveRegion = LiveRegionMode.Assertive
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.screenEdge)
                .focusGroup(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = spacing.contentMaxWidth)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.signal_lost_heading),
                    style = typography.display,
                    color = colors.accent,
                    textAlign = TextAlign.Center,
                    softWrap = true,
                    modifier = Modifier
                        .semantics { heading() }
                        .focusProperties { canFocus = false },
                )

                Spacer(modifier = Modifier.height(spacing.two))

                Text(
                    text = stringResource(R.string.signal_lost_helper),
                    style = typography.body,
                    color = helperColor,
                    textAlign = TextAlign.Center,
                    softWrap = true,
                    modifier = Modifier.focusProperties { canFocus = false },
                )

                Spacer(modifier = Modifier.height(spacing.four))

                RetryControl(
                    isReconnecting = isReconnecting,
                    retryEnabled = retryEnabled,
                    labelSwapDurationMs = motion.retryLabelSwapMs,
                    focusRequester = retryFocus,
                    onRetryClick = onRetryClick,
                )
            }
        }
    }
}

/**
 * Plain monospace text-link CTA — min 48dp target, focus ring, TalkBack states.
 */
@Composable
private fun RetryControl(
    isReconnecting: Boolean,
    retryEnabled: Boolean,
    labelSwapDurationMs: Int,
    focusRequester: FocusRequester,
    onRetryClick: () -> Unit,
) {
    val colors = MidnightTheme.colors
    val typography = MidnightTheme.typography
    val spacing = MidnightTheme.spacing
    val a11y = rememberShellAccessibilityState()
    val retryDescription = stringResource(R.string.retry_content_description)
    val reconnectingDescription = stringResource(R.string.retry_state_reconnecting)
    val disabledDescription = stringResource(R.string.retry_disabled_no_network)
    val interactionSource = remember { MutableInteractionSource() }
    var focused by remember { mutableStateOf(false) }
    val strokeWidthPx = with(LocalDensity.current) { FOCUS_RING_WIDTH.toPx() }
    val interactive = retryEnabled && !isReconnecting

    val semanticsDescription = when {
        isReconnecting -> reconnectingDescription
        !retryEnabled -> disabledDescription
        else -> retryDescription
    }
    val stateDesc = when {
        isReconnecting -> reconnectingDescription
        !retryEnabled -> disabledDescription
        else -> null
    }
    val labelColor = when {
        isReconnecting -> colors.accessibleSecondary(a11y)
        !retryEnabled -> colors.accessibleDisabled(a11y)
        else -> colors.accent
    }

    Box(
        modifier = Modifier
            .focusRequester(focusRequester)
            .defaultMinSize(
                minWidth = spacing.touchTargetMin,
                minHeight = spacing.touchTargetMin,
            )
            .onFocusChanged { focused = it.isFocused }
            .drawBehind {
                if (focused && interactive) {
                    drawRoundRect(
                        color = colors.accent,
                        cornerRadius = CornerRadius(0f, 0f),
                        style = Stroke(width = strokeWidthPx),
                    )
                }
            }
            .clickable(
                enabled = interactive,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onRetryClick,
            )
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = semanticsDescription
                if (stateDesc != null) {
                    stateDescription = stateDesc
                }
                if (!interactive) {
                    disabled()
                }
            }
            .padding(horizontal = spacing.two, vertical = spacing.one),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = isReconnecting,
            transitionSpec = {
                fadeIn(animationSpec = tween(labelSwapDurationMs)) togetherWith
                    fadeOut(animationSpec = tween(labelSwapDurationMs))
            },
            label = "offlineRetryLabel",
        ) { reconnecting ->
            Text(
                text = stringResource(
                    if (reconnecting) R.string.reconnecting else R.string.retry,
                ),
                style = typography.link,
                color = labelColor,
                textAlign = TextAlign.Center,
                softWrap = true,
            )
        }
    }
}

private val FOCUS_RING_WIDTH = 2.dp

package com.skaalsolutions.midnightchannel.ui.offline

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.skaalsolutions.midnightchannel.testing.MidnightComposeTest
import com.skaalsolutions.midnightchannel.ui.testing.ShellUiTestTags
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OfflineErrorScreenComposeTest : MidnightComposeTest() {

    @Test
    fun offline_shows_signal_lost_copy_and_retry() {
        composeRule.setMidnightContent {
            OfflineErrorScreen(retryEnabled = true)
        }

        composeRule.onNodeWithTag(ShellUiTestTags.OFFLINE_ROOT).assertIsDisplayed()
        composeRule.onNodeWithText("SIGNAL LOST").assertIsDisplayed()
        composeRule.onNodeWithText("Check your connection and try again.").assertIsDisplayed()
        composeRule.onNodeWithText("RETRY").assertIsDisplayed()
        composeRule
            .onNode(
                hasContentDescription(
                    "Signal lost. Check your connection and try again.",
                ),
            )
            .assertExists()
    }

    @Test
    fun retry_action_invokes_callback_when_enabled() {
        var clicks = 0
        composeRule.setMidnightContent {
            OfflineErrorScreen(
                retryEnabled = true,
                onRetryClick = { clicks++ },
            )
        }

        composeRule.onNodeWithTag(ShellUiTestTags.OFFLINE_RETRY)
            .assertHasClickAction()
            .assertIsEnabled()
            .performClick()
        assertThat(clicks).isEqualTo(1)
    }

    @Test
    fun retry_disabled_when_no_network() {
        var clicks = 0
        composeRule.setMidnightContent {
            OfflineErrorScreen(
                retryEnabled = false,
                onRetryClick = { clicks++ },
            )
        }

        composeRule.onNodeWithTag(ShellUiTestTags.OFFLINE_RETRY)
            .assertIsNotEnabled()
            .assertExists()
        composeRule
            .onNode(hasContentDescription("Retry unavailable. No network connection."))
            .assertExists()
        composeRule.onNodeWithTag(ShellUiTestTags.OFFLINE_RETRY).performClick()
        assertThat(clicks).isEqualTo(0)
    }

    @Test
    fun reconnecting_state_shows_reconnecting_label_and_blocks_retry() {
        var clicks = 0
        composeRule.setMidnightContent {
            OfflineErrorScreen(
                isReconnecting = true,
                retryEnabled = true,
                onRetryClick = { clicks++ },
            )
        }

        composeRule.onNodeWithText("RECONNECTING…").assertIsDisplayed()
        composeRule.onNodeWithText("RETRY").assertDoesNotExist()
        composeRule
            .onNode(hasContentDescription("Reconnecting to Midnight Channel"))
            .assertExists()
        composeRule.onNodeWithTag(ShellUiTestTags.OFFLINE_RETRY).performClick()
        assertThat(clicks).isEqualTo(0)
    }
}

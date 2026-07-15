package com.skaalsolutions.midnightchannel.ui.title

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.skaalsolutions.midnightchannel.testing.MidnightComposeTest
import com.skaalsolutions.midnightchannel.ui.testing.ShellUiTestTags
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TitlePageComposeTest : MidnightComposeTest() {

    @Test
    fun title_shows_wordmark_caption_and_accessibility_description() {
        composeRule.setMidnightContent {
            TitlePageScreen(minDisplayDurationMs = 60_000)
        }

        composeRule.onNodeWithTag(ShellUiTestTags.TITLE_ROOT).assertIsDisplayed()
        composeRule.onNodeWithText("MIDNIGHT CHANNEL").assertIsDisplayed()
        composeRule.onNodeWithText("tuning in…").assertIsDisplayed()
        composeRule
            .onNode(hasContentDescription("Midnight Channel. Tuning in."))
            .assertExists()
    }

    @Test
    fun title_floor_elapsed_callback_fires_after_minimum_duration() {
        var elapsed = false
        composeRule.mainClock.autoAdvance = false
        composeRule.setMidnightContent {
            TitlePageScreen(
                minDisplayDurationMs = 1_500,
                onMinimumDurationElapsed = { elapsed = true },
            )
        }

        assertThat(elapsed).isFalse()
        composeRule.mainClock.advanceTimeBy(1_499)
        composeRule.waitForIdle()
        assertThat(elapsed).isFalse()

        composeRule.mainClock.advanceTimeBy(50)
        composeRule.waitForIdle()
        assertThat(elapsed).isTrue()
    }

    @Test
    fun title_can_hide_caption() {
        composeRule.setMidnightContent {
            TitlePageScreen(showCaption = false, minDisplayDurationMs = 60_000)
        }
        composeRule.onNodeWithText("MIDNIGHT CHANNEL").assertIsDisplayed()
        composeRule.onNodeWithText("tuning in…").assertDoesNotExist()
    }
}

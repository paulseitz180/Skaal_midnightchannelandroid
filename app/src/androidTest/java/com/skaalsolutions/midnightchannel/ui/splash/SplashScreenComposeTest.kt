package com.skaalsolutions.midnightchannel.ui.splash

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.skaalsolutions.midnightchannel.testing.MidnightComposeTest
import com.skaalsolutions.midnightchannel.ui.testing.ShellUiTestTags
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SplashScreenComposeTest : MidnightComposeTest() {

    @Test
    fun splash_shows_logo_expand_root_and_accessibility_description() {
        composeRule.setMidnightContent {
            SplashScreen(minDisplayDurationMs = 60_000)
        }

        composeRule.onNodeWithTag(ShellUiTestTags.SPLASH_ROOT).assertIsDisplayed()
        composeRule
            .onNode(hasContentDescription("Midnight Channel"))
            .assertExists()
    }

    @Test
    fun splash_floor_elapsed_callback_fires_after_minimum_duration() {
        var elapsed = false
        composeRule.mainClock.autoAdvance = false
        composeRule.setMidnightContent {
            SplashScreen(
                minDisplayDurationMs = 500,
                onMinimumDurationElapsed = { elapsed = true },
            )
        }

        assertThat(elapsed).isFalse()
        composeRule.mainClock.advanceTimeBy(499)
        composeRule.waitForIdle()
        assertThat(elapsed).isFalse()

        composeRule.mainClock.advanceTimeBy(50)
        composeRule.waitForIdle()
        assertThat(elapsed).isTrue()
    }

    @Test
    fun splash_floor_coerces_below_four_hundred_milliseconds_up_to_floor_minimum() {
        // Production bible: floor lower bound 400ms even if a caller passes a shorter value.
        var elapsed = false
        composeRule.mainClock.autoAdvance = false
        composeRule.setMidnightContent {
            SplashScreen(
                minDisplayDurationMs = 100,
                onMinimumDurationElapsed = { elapsed = true },
            )
        }

        composeRule.mainClock.advanceTimeBy(399)
        composeRule.waitForIdle()
        assertThat(elapsed).isFalse()

        composeRule.mainClock.advanceTimeBy(50)
        composeRule.waitForIdle()
        assertThat(elapsed).isTrue()
    }
}

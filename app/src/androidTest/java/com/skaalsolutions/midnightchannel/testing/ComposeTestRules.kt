package com.skaalsolutions.midnightchannel.testing

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.skaalsolutions.midnightchannel.MainActivity
import com.skaalsolutions.midnightchannel.ui.theme.MidnightChannelTheme
import org.junit.Rule

/**
 * Factory helpers for Compose UI tests.
 *
 * Prefer [createMidnightComposeRule] for isolated screen tests with
 * [MidnightChannelTheme]. Prefer [createMidnightActivityRule] for full
 * Activity / WebView host smoke tests (orchestrated on device/emulator).
 *
 * Uses Compose UI Test junit4 **v2** factories (`StandardTestDispatcher`).
 */
object ComposeTestRules {

    fun createMidnightComposeRule(): ComposeContentTestRule =
        createComposeRule()

    fun createMidnightActivityRule() =
        createAndroidComposeRule<MainActivity>()
}

/**
 * Base for isolated Compose UI tests. Subclasses declare additional rules as needed.
 * Set content inside `@Test` methods via [setMidnightContent].
 */
abstract class MidnightComposeTest {

    @get:Rule
    val composeRule: ComposeContentTestRule = ComposeTestRules.createMidnightComposeRule()

    /** Wrap [content] in the production CRT theme so typography / colours match. */
    fun ComposeContentTestRule.setMidnightContent(content: @Composable () -> Unit) {
        setContent {
            MidnightChannelTheme {
                content()
            }
        }
    }
}

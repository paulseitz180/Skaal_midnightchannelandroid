package com.skaalsolutions.midnightchannel.ui.shell

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.skaalsolutions.midnightchannel.connectivity.ConnectivityStatus
import com.skaalsolutions.midnightchannel.connectivity.LocalConnectivityMonitor
import com.skaalsolutions.midnightchannel.navigation.ShellController
import com.skaalsolutions.midnightchannel.navigation.ShellEvent
import com.skaalsolutions.midnightchannel.navigation.ShellState
import com.skaalsolutions.midnightchannel.recovery.ChannelRecoveryController
import com.skaalsolutions.midnightchannel.testing.MidnightComposeTest
import com.skaalsolutions.midnightchannel.ui.testing.ShellUiTestTags
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Shell-level Compose tests for native overlay navigation / state transitions.
 *
 * [LocalConnectivityMonitor] is left unset so Offline Retry defaults to Available
 * (see [MidnightShell] fallback) without a device NetworkCallback.
 */
@RunWith(AndroidJUnit4::class)
class MidnightShellComposeTest : MidnightComposeTest() {

    @Test
    fun cold_start_shows_splash_overlay() {
        val shell = ShellController()
        composeRule.setMidnightContent {
            CompositionLocalProvider(LocalConnectivityMonitor provides null) {
                MidnightShell(
                    shell = shell,
                    onSplashFloorElapsed = {},
                )
            }
        }

        composeRule.onNodeWithTag(ShellUiTestTags.SPLASH_ROOT).assertIsDisplayed()
        composeRule.onNodeWithTag(ShellUiTestTags.CHANNEL_HOST).assertExists()
        assertThat(shell.current()).isInstanceOf(ShellState.Splash::class.java)
    }

    @Test
    fun network_lost_transitions_splash_to_offline_surface() {
        val shell = ShellController()
        composeRule.setMidnightContent {
            CompositionLocalProvider(LocalConnectivityMonitor provides null) {
                MidnightShell(shell = shell)
            }
        }

        composeRule.onNodeWithTag(ShellUiTestTags.SPLASH_ROOT).assertIsDisplayed()
        composeRule.runOnIdle { shell.dispatch(ShellEvent.NetworkLost) }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(ShellUiTestTags.OFFLINE_ROOT).assertIsDisplayed()
        composeRule.onNodeWithText("SIGNAL LOST").assertIsDisplayed()
        assertThat(shell.current()).isEqualTo(ShellState.Offline)
    }

    @Test
    fun ready_state_hides_splash_and_reveals_channel_host() {
        val shell = ShellController()
        composeRule.mainClock.autoAdvance = true
        composeRule.setMidnightContent {
            CompositionLocalProvider(LocalConnectivityMonitor provides null) {
                MidnightShell(
                    shell = shell,
                    onSplashFloorElapsed = {},
                )
            }
        }

        composeRule.runOnIdle {
            shell.dispatch(ShellEvent.SplashFloorElapsed)
            shell.dispatch(ShellEvent.TitleFloorElapsed)
            shell.dispatch(ShellEvent.MainFrameLoadFinished)
        }
        composeRule.waitForIdle()
        // Allow AnimatedVisibility exit (200ms cross-fade).
        composeRule.mainClock.advanceTimeBy(250)
        composeRule.waitForIdle()

        assertThat(shell.current()).isEqualTo(ShellState.Ready)
        composeRule.onNodeWithTag(ShellUiTestTags.SPLASH_ROOT).assertDoesNotExist()
        composeRule.onNodeWithTag(ShellUiTestTags.TITLE_ROOT).assertDoesNotExist()
        composeRule.onNodeWithTag(ShellUiTestTags.CHANNEL_HOST).assertExists()
        composeRule.onNodeWithTag(ShellUiTestTags.OFFLINE_ROOT).assertDoesNotExist()
    }

    @Test
    fun retry_action_from_offline_enters_reconnecting_via_recovery() {
        val shell = ShellController()
        val recovery = ChannelRecoveryController(
            dispatch = shell::dispatch,
            connectivitySnapshot = { ConnectivityStatus.Available },
            currentState = shell::current,
        )
        shell.bindRecovery(recovery)

        composeRule.setMidnightContent {
            CompositionLocalProvider(LocalConnectivityMonitor provides null) {
                MidnightShell(shell = shell)
            }
        }

        composeRule.runOnIdle { shell.dispatch(ShellEvent.NetworkLost) }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(ShellUiTestTags.OFFLINE_RETRY).assertIsDisplayed()
        composeRule.onNodeWithTag(ShellUiTestTags.OFFLINE_RETRY).performClick()
        composeRule.waitForIdle()

        assertThat(shell.current()).isEqualTo(ShellState.Retrying)
        composeRule.onNodeWithText("RECONNECTING…").assertIsDisplayed()
        assertThat(recovery.isInFlight).isTrue()
    }
}

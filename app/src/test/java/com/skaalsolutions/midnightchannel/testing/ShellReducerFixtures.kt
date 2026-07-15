package com.skaalsolutions.midnightchannel.testing

import com.skaalsolutions.midnightchannel.navigation.ShellEvent
import com.skaalsolutions.midnightchannel.navigation.ShellReducer
import com.skaalsolutions.midnightchannel.navigation.ShellState

/**
 * Shared helpers for [ShellReducer] JVM tests — no Activity / WebView.
 *
 * Fold events onto state; use when writing future `ShellReducerTest` cases.
 */
object ShellReducerFixtures {

    fun reduceAll(
        initial: ShellState = ShellReducer.initial(),
        vararg events: ShellEvent,
    ): ShellState =
        events.fold(initial) { state, event -> ShellReducer.reduce(state, event) }

    /**
     * Happy-path launch: Logo Expand floor → Title Page floor → main-frame finish.
     * Order of Title floor vs paint may vary; SplashFloor must precede TitlePage.
     */
    fun eventsToReady(): List<ShellEvent> = listOf(
        ShellEvent.SplashFloorElapsed,
        ShellEvent.TitleFloorElapsed,
        ShellEvent.MainFrameLoadFinished,
    )

    fun eventsToOfflineFromSplash(): List<ShellEvent> = listOf(
        ShellEvent.NetworkLost,
    )
}

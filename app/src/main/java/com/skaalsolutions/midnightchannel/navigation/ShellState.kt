package com.skaalsolutions.midnightchannel.navigation

/**
 * In-memory presentation state for the Midnight Channel shell.
 *
 * Launch: Logo Expand ([Splash]) → Title Page ([TitlePage]) → Player ([Loading]/[Ready]).
 * Runtime: Ready ↔ Loading ↔ Offline ↔ Retrying.
 * **Not persisted** — site state (including unmuted / listening) stays in the WebView.
 *
 * Surfaces:
 * - [Splash] → Logo Expand (icon scale/reveal)
 * - [TitlePage] → Title Page (wordmark; 1500ms floor ∧ WebView ready)
 * - [Loading] / [Ready] → Main Channel (WebView visible)
 * - [Offline] / [Retrying] → Offline / Error Screen
 */
sealed interface ShellState {

    /**
     * Phase 1 — Logo Expand (icon-tap entry).
     * Advances to [TitlePage] when the logo floor elapses.
     * [firstPaintReady] is remembered if the WebView finishes during this phase
     * so Title Page does not re-wait for a paint that already happened.
     */
    data class Splash(
        val firstPaintReady: Boolean = false,
    ) : ShellState

    /**
     * Phase 2 — Title Page.
     * Cross-fades in after Logo Expand. Holds a fixed 1500ms floor; if the
     * WebView is not ready when the floor elapses, stays here until ready.
     * Advances to Player only when **both** floor and first paint are true.
     */
    data class TitlePage(
        val floorElapsed: Boolean = false,
        val firstPaintReady: Boolean = false,
    ) : ShellState {
        val canAdvance: Boolean get() = floorElapsed && firstPaintReady
    }

    /** Main Channel — main-frame load / reload in progress. */
    data object Loading : ShellState

    /** Main Channel — document ready (site may show tap-to-unmute). */
    data object Ready : ShellState

    /** Native SIGNAL LOST — connectivity or main-frame failure. */
    data object Offline : ShellState

    /** User Retry — RECONNECTING… before [Loading] or back to [Offline]. */
    data object Retrying : ShellState
}

/** Offline / Error screen (includes reconnecting). */
val ShellState.showsOfflineSurface: Boolean
    get() = this is ShellState.Offline || this is ShellState.Retrying

/** Logo Expand overlay. */
val ShellState.showsSplashSurface: Boolean
    get() = this is ShellState.Splash

/** Title Page overlay. */
val ShellState.showsTitleSurface: Boolean
    get() = this is ShellState.TitlePage

/** Any launch overlay (Logo Expand or Title Page). */
val ShellState.showsLaunchOverlay: Boolean
    get() = showsSplashSurface || showsTitleSurface

/**
 * WebView content should be visible (not covered by launch / Offline overlays).
 * Distinct from "host composed" — host stays mounted under overlays for load/retry.
 */
val ShellState.revealsChannelContent: Boolean
    get() = this is ShellState.Loading || this is ShellState.Ready

/** Retry CTA may be offered. */
val ShellState.acceptsRetry: Boolean
    get() = this is ShellState.Offline

/** Offline surface shows RECONNECTING…. */
val ShellState.isReconnecting: Boolean
    get() = this is ShellState.Retrying

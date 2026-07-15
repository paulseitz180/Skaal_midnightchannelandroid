package com.skaalsolutions.midnightchannel.util

/**
 * Result of resolving a system back press for the Midnight Channel shell.
 *
 * MVP produces only toast confirmation / exit. A future WebView-history step can
 * return `null` from [ShellBackNavigator.onBackPressed] (consumed) without changing
 * toast / exit handling.
 */
sealed interface BackNavigationResult {

    /** First back within the confirmation window — show the exit toast (no dialog). */
    data object ShowExitToast : BackNavigationResult

    /** Second back within the confirmation window — finish the Activity. */
    data object ExitApp : BackNavigationResult
}

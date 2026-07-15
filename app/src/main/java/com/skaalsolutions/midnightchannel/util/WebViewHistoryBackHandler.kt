package com.skaalsolutions.midnightchannel.util

/**
 * Optional in-WebView history step for system back.
 *
 * MVP: use [Disabled] — Grande Document requires double-back exit on Main Channel,
 * not browser `WebView.goBack()`.
 *
 * Later: provide `{ webView.canGoBack().also { if (it) webView.goBack() } }` (or
 * equivalent) to [ShellBackNavigator] without rewriting Compose call sites.
 */
fun interface WebViewHistoryBackHandler {

    /**
     * @return `true` when back was consumed by WebView history (caller must not
     * arm / evaluate the double-back exit gate).
     */
    fun tryNavigateBack(): Boolean

    companion object {
        /** MVP default — never consumes; exit confirmation always runs. */
        val Disabled: WebViewHistoryBackHandler = WebViewHistoryBackHandler { false }
    }
}

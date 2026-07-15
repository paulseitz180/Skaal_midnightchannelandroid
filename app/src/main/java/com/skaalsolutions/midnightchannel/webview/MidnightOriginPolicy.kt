package com.skaalsolutions.midnightchannel.webview

import android.net.Uri
import androidx.core.net.toUri

/**
 * Navigation scope for the Midnight Channel WebView shell.
 *
 * - Same-origin `midnightchannel.live` → load inside WebView (site parity)
 * - `about:` / `data:` → allowed (CRT blank / error suppression documents)
 * - Everything else → must not load in-WebView; hand off externally when launchable
 */
object MidnightOriginPolicy {

    /** Canonical product host — single source shared with [ChannelDestination]. */
    const val ALLOWED_HOST: String = ChannelDestination.HOST

    /**
     * Schemes the shell may leave the WebView for (external Intent hand-off).
     * Shared with [com.skaalsolutions.midnightchannel.util.ExternalLinkNavigator].
     */
    val EXTERNAL_HANDOFF_SCHEMES: Set<String> = setOf(
        "https",
        "http",
        "mailto",
        "tel",
        "sms",
        "smsto",
        "market",
        "intent",
    )

    fun isSameOrigin(uri: Uri?): Boolean {
        if (uri == null) return false
        val host = uri.host?.lowercase() ?: return false
        // Grande Document / ENGINEERING_SPECIFICATION: primary content HTTPS-only.
        if (uri.scheme?.lowercase() != "https") return false
        return host == ALLOWED_HOST || host.endsWith(".$ALLOWED_HOST")
    }

    fun isSameOrigin(url: String?): Boolean =
        url?.let { isSameOrigin(it.toUri()) } == true

    /** Documents the WebView may load that are not midnightchannel.live pages. */
    fun isShellInternalDocument(uri: Uri?): Boolean {
        if (uri == null) return false
        val scheme = uri.scheme?.lowercase() ?: return false
        return scheme == "about" || scheme == "data"
    }

    /**
     * True when [shouldOverrideUrlLoading] must return false and let WebView navigate.
     */
    fun shouldLoadInsideWebView(uri: Uri?): Boolean =
        isSameOrigin(uri) || isShellInternalDocument(uri)

    /**
     * True when the WebView must **not** navigate and the shell should attempt
     * an external Intent (via ExternalLinkNavigator). Complements
     * [shouldLoadInsideWebView] without depending on the util layer.
     */
    fun shouldLeaveWebView(uri: Uri?): Boolean {
        if (uri == null) return false
        if (shouldLoadInsideWebView(uri)) return false
        val scheme = uri.scheme?.lowercase() ?: return false
        return scheme in EXTERNAL_HANDOFF_SCHEMES
    }
}

package com.skaalsolutions.midnightchannel.webview

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView

/**
 * Applies Grande Document Section 08 WebSettings **exactly**.
 *
 * Getting any of these wrong changes product behaviour (autoplay gate, site state,
 * HTTPS hygiene), not merely implementation detail. Do not add settings beyond
 * this normative table.
 *
 * Does not attach clients, load URLs, or handle errors — those are separate TASKs.
 */
object MidnightWebSettings {

    /**
     * Configure [webView] for production Midnight Channel shell playback.
     *
     * User-Agent is intentionally **not** overridden — the default WebView UA is
     * required so midnightchannel.live treats the app as a standard mobile browser.
     */
    @SuppressLint("SetJavaScriptEnabled")
    fun apply(webView: WebView) {
        val settings = webView.settings

        // JavaScript ENABLED — the environmental DJ engine and tap-to-unmute gate
        // are both implemented in the site's JavaScript; without JS the product
        // cannot run.
        settings.javaScriptEnabled = true

        // DOM storage ENABLED — required for the site's existing localStorage-based
        // shuffle / state memory (KAIZEN-13 history). Native code must not duplicate
        // this state; WebView storage is the sole owner.
        settings.domStorageEnabled = true

        // Web database / IndexedDB support ENABLED — accompanies DOM storage for the
        // same site-side state layer. Grande Document lists databaseEnabled=true even
        // though the platform API is deprecated; suppress and keep the normative value.
        @Suppress("DEPRECATION")
        settings.databaseEnabled = true

        // Media playback must NOT require an additional WebView-level user gesture.
        // The site's own tap-to-unmute JavaScript remains the single source of truth,
        // matching mobile-browser behaviour exactly. Setting this true would regress
        // parity with midnightchannel.live in Chrome.
        settings.mediaPlaybackRequiresUserGesture = false

        // Mixed content NEVER allowed — the destination is HTTPS-only. Relaxing this
        // would violate the shell security model and cleartext network policy.
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW

        // Cache mode DEFAULT — browser-equivalent HTTP caching for the live site;
        // no custom offline cache of audio/playlists (explicitly out of MVP scope).
        settings.cacheMode = WebSettings.LOAD_DEFAULT

        // Multiple windows DISABLED — this is a single-destination shell; there is
        // no product reason to spawn additional WebView windows or popups.
        settings.setSupportMultipleWindows(false)

        // File access DISABLED — midnightchannel.live does not need local file://
        // access; keeping this false reduces the attack surface of the WebView.
        settings.allowFileAccess = false

        // Safe Browsing ENABLED — standard hygiene for WebView navigations. The
        // destination is trusted, but leaving Safe Browsing on costs nothing and
        // matches Grande Document Section 08 / application meta-data opt-in.
        settings.safeBrowsingEnabled = true

        // Default User-Agent — deliberately untouched. Do not call setUserAgentString.
        // Overriding UA risks breaking IP geo / weather / playlist selection parity
        // with mobile Chrome (open engineering question relies on default UA).
    }
}

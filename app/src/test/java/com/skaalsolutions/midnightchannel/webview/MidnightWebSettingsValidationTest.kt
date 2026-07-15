package com.skaalsolutions.midnightchannel.webview

import android.webkit.WebSettings
import android.webkit.WebView
import com.skaalsolutions.midnightchannel.testing.MidnightRobolectricTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.robolectric.RuntimeEnvironment

/**
 * Grande Document Section 08 — normative [MidnightWebSettings] validation.
 * Verifies the apply contract (setters / never override UA) so results do not
 * depend on incomplete Robolectric WebSettings shadows.
 */
class MidnightWebSettingsValidationTest : MidnightRobolectricTest() {

    @Test
    fun `apply writes normative grande document settings`() {
        val settings = mock<WebSettings>()
        val webView = mock<WebView> {
            on { getSettings() } doReturn settings
        }

        MidnightWebSettings.apply(webView)

        verify(settings).javaScriptEnabled = true
        verify(settings).domStorageEnabled = true
        @Suppress("DEPRECATION")
        verify(settings).databaseEnabled = true
        verify(settings).mediaPlaybackRequiresUserGesture = false
        verify(settings).mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        verify(settings).cacheMode = WebSettings.LOAD_DEFAULT
        verify(settings).setSupportMultipleWindows(false)
        verify(settings).allowFileAccess = false
        verify(settings).allowContentAccess = false
        @Suppress("DEPRECATION")
        verify(settings).allowFileAccessFromFileURLs = false
        @Suppress("DEPRECATION")
        verify(settings).allowUniversalAccessFromFileURLs = false
        verify(settings).safeBrowsingEnabled = true
        verify(settings, never()).userAgentString = any()
        verify(settings, never()).setUserAgentString(any())
    }

    @Test
    fun `apply on real webview leaves user agent unchanged`() {
        val webView = WebView(RuntimeEnvironment.getApplication())
        val before = webView.settings.userAgentString
        MidnightWebSettings.apply(webView)
        com.google.common.truth.Truth.assertThat(webView.settings.userAgentString).isEqualTo(before)
        com.google.common.truth.Truth.assertThat(webView.settings.userAgentString.lowercase())
            .doesNotContain("midnightchannel")
        disposeWebView(webView)
    }

    @Test
    fun `apply on real webview enables javascript and disables file access`() {
        // Spot-check getters that Robolectric shadows reliably persist.
        val webView = WebView(RuntimeEnvironment.getApplication())
        MidnightWebSettings.apply(webView)
        com.google.common.truth.Truth.assertThat(webView.settings.javaScriptEnabled).isTrue()
        com.google.common.truth.Truth.assertThat(webView.settings.domStorageEnabled).isTrue()
        com.google.common.truth.Truth.assertThat(webView.settings.mediaPlaybackRequiresUserGesture)
            .isFalse()
        com.google.common.truth.Truth.assertThat(webView.settings.mixedContentMode)
            .isEqualTo(WebSettings.MIXED_CONTENT_NEVER_ALLOW)
        com.google.common.truth.Truth.assertThat(webView.settings.allowFileAccess).isFalse()
        com.google.common.truth.Truth.assertThat(webView.settings.allowContentAccess).isFalse()
        disposeWebView(webView)
    }
}

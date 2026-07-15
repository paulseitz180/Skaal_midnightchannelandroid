package com.skaalsolutions.midnightchannel.webview

import android.net.Uri
import android.webkit.WebView
import com.google.common.truth.Truth.assertThat
import com.skaalsolutions.midnightchannel.testing.MidnightRobolectricTest
import com.skaalsolutions.midnightchannel.testing.OriginTestFixtures
import com.skaalsolutions.midnightchannel.testing.fakeWebResourceRequest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.robolectric.RuntimeEnvironment

/**
 * WebViewClient navigation rules — Grande Document / ENGINEERING_SPECIFICATION §4.1.
 */
class MidnightWebViewClientNavigationTest : MidnightRobolectricTest() {

    private lateinit var webView: WebView
    private val external = mutableListOf<Uri>()
    private lateinit var client: MidnightWebViewClient

    @Before
    fun setUp() {
        webView = WebView(RuntimeEnvironment.getApplication())
        client = MidnightWebViewClient(
            MidnightWebViewClientCallbacks(
                onExternalNavigation = { external += it },
            ),
        )
    }

    @After
    fun tearDown() {
        disposeWebView(webView)
    }

    @Test
    fun `same-origin main frame stays inside webview`() {
        val request = fakeWebResourceRequest(ChannelDestination.HOME_URL, isForMainFrame = true)
        assertThat(client.shouldOverrideUrlLoading(webView, request)).isFalse()
        assertThat(external).isEmpty()
    }

    @Test
    fun `about blank stays inside webview`() {
        val request = fakeWebResourceRequest("about:blank", isForMainFrame = true)
        assertThat(client.shouldOverrideUrlLoading(webView, request)).isFalse()
        assertThat(external).isEmpty()
    }

    @Test
    fun `external https main frame blocks load and emits external navigation`() {
        val uri = OriginTestFixtures.externalHttps()
        val request = fakeWebResourceRequest(uri.toString(), isForMainFrame = true)
        assertThat(client.shouldOverrideUrlLoading(webView, request)).isTrue()
        assertThat(external).containsExactly(uri)
    }

    @Test
    fun `mailto main frame blocks load and emits external navigation`() {
        val uri = OriginTestFixtures.mailto()
        val request = fakeWebResourceRequest(uri.toString(), isForMainFrame = true)
        assertThat(client.shouldOverrideUrlLoading(webView, request)).isTrue()
        assertThat(external).containsExactly(uri)
    }

    @Test
    fun `subframe external url is blocked without intent callback`() {
        val uri = OriginTestFixtures.externalHttps()
        val request = fakeWebResourceRequest(uri.toString(), isForMainFrame = false)
        assertThat(client.shouldOverrideUrlLoading(webView, request)).isTrue()
        assertThat(external).isEmpty()
    }

    @Test
    fun `subframe same-origin stays inside webview`() {
        val request = fakeWebResourceRequest(
            ChannelDestination.HOME_URL + "embed",
            isForMainFrame = false,
        )
        assertThat(client.shouldOverrideUrlLoading(webView, request)).isFalse()
        assertThat(external).isEmpty()
    }

    @Test
    fun `http product host leaves webview as external`() {
        val uri = OriginTestFixtures.httpOnHost()
        val request = fakeWebResourceRequest(uri.toString(), isForMainFrame = true)
        assertThat(client.shouldOverrideUrlLoading(webView, request)).isTrue()
        assertThat(external).containsExactly(uri)
    }

    @Deprecated("Exercise string overload path")
    @Test
    @Suppress("DEPRECATION")
    fun `string override delegates same as request path for external`() {
        val uri = OriginTestFixtures.externalHttps()
        assertThat(client.shouldOverrideUrlLoading(webView, uri.toString())).isTrue()
        assertThat(external).containsExactly(uri)
    }
}

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
    fun same_origin_main_frame_stays_inside_webview() {
        val request = fakeWebResourceRequest(ChannelDestination.HOME_URL, isForMainFrame = true)
        assertThat(client.shouldOverrideUrlLoading(webView, request)).isFalse()
        assertThat(external).isEmpty()
    }

    @Test
    fun about_blank_stays_inside_webview() {
        val request = fakeWebResourceRequest("about:blank", isForMainFrame = true)
        assertThat(client.shouldOverrideUrlLoading(webView, request)).isFalse()
        assertThat(external).isEmpty()
    }

    @Test
    fun external_https_main_frame_blocks_load_and_emits_external_navigation() {
        val uri = OriginTestFixtures.externalHttps()
        val request = fakeWebResourceRequest(uri.toString(), isForMainFrame = true)
        assertThat(client.shouldOverrideUrlLoading(webView, request)).isTrue()
        assertThat(external).containsExactly(uri)
    }

    @Test
    fun mailto_main_frame_blocks_load_and_emits_external_navigation() {
        val uri = OriginTestFixtures.mailto()
        val request = fakeWebResourceRequest(uri.toString(), isForMainFrame = true)
        assertThat(client.shouldOverrideUrlLoading(webView, request)).isTrue()
        assertThat(external).containsExactly(uri)
    }

    @Test
    fun subframe_external_url_is_blocked_without_intent_callback() {
        val uri = OriginTestFixtures.externalHttps()
        val request = fakeWebResourceRequest(uri.toString(), isForMainFrame = false)
        assertThat(client.shouldOverrideUrlLoading(webView, request)).isTrue()
        assertThat(external).isEmpty()
    }

    @Test
    fun subframe_same_origin_stays_inside_webview() {
        val request = fakeWebResourceRequest(
            ChannelDestination.HOME_URL + "embed",
            isForMainFrame = false,
        )
        assertThat(client.shouldOverrideUrlLoading(webView, request)).isFalse()
        assertThat(external).isEmpty()
    }

    @Test
    fun http_product_host_leaves_webview_as_external() {
        val uri = OriginTestFixtures.httpOnHost()
        val request = fakeWebResourceRequest(uri.toString(), isForMainFrame = true)
        assertThat(client.shouldOverrideUrlLoading(webView, request)).isTrue()
        assertThat(external).containsExactly(uri)
    }

    @Deprecated("Exercise string overload path")
    @Test
    @Suppress("DEPRECATION")
    fun string_override_delegates_same_as_request_path_for_external() {
        val uri = OriginTestFixtures.externalHttps()
        assertThat(client.shouldOverrideUrlLoading(webView, uri.toString())).isTrue()
        assertThat(external).containsExactly(uri)
    }
}

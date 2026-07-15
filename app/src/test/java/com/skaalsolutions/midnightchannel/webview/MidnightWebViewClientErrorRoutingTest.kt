package com.skaalsolutions.midnightchannel.webview

import android.net.http.SslError
import android.webkit.RenderProcessGoneDetail
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.common.truth.Truth.assertThat
import com.skaalsolutions.midnightchannel.testing.MidnightRobolectricTest
import com.skaalsolutions.midnightchannel.testing.fakeWebResourceRequest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.RuntimeEnvironment

/**
 * Main-frame error routing — Grande Document: never show browser error page;
 * subframe failures must not force Offline.
 */
class MidnightWebViewClientErrorRoutingTest : MidnightRobolectricTest() {

    private lateinit var webView: WebView
    private val failures = mutableListOf<MainFrameFailure>()
    private lateinit var client: MidnightWebViewClient

    @Before
    fun setUp() {
        webView = WebView(RuntimeEnvironment.getApplication())
        client = MidnightWebViewClient(
            MidnightWebViewClientCallbacks(
                onMainFrameFailed = { failures += it },
            ),
        )
    }

    @After
    fun tearDown() {
        disposeWebView(webView)
    }

    @Test
    fun main_frame_network_error_routes_to_failure_callback() {
        val request = fakeWebResourceRequest(ChannelDestination.HOME_URL, isForMainFrame = true)
        val error = mock<WebResourceError> {
            on { errorCode } doReturn WebViewClient.ERROR_HOST_LOOKUP
            on { description } doReturn "host lookup failed"
        }
        client.onReceivedError(webView, request, error)
        assertThat(failures).hasSize(1)
        val failure = failures.single() as MainFrameFailure.Network
        assertThat(failure.errorCode).isEqualTo(WebViewClient.ERROR_HOST_LOOKUP)
        assertThat(failure.failingUrl).isEqualTo(ChannelDestination.HOME_URL)
    }

    @Test
    fun subframe_network_error_does_not_route_offline() {
        val request = fakeWebResourceRequest(
            "https://cdn.example.com/asset.js",
            isForMainFrame = false,
        )
        val error = mock<WebResourceError> {
            on { errorCode } doReturn WebViewClient.ERROR_FAILED_SSL_HANDSHAKE
            on { description } doReturn "ssl"
        }
        client.onReceivedError(webView, request, error)
        assertThat(failures).isEmpty()
    }

    @Test
    fun main_frame_http_500_routes_offline() {
        val request = fakeWebResourceRequest(ChannelDestination.HOME_URL, isForMainFrame = true)
        val response = WebResourceResponse(
            "text/html",
            "utf-8",
            500,
            "Server Error",
            emptyMap(),
            null,
        )
        client.onReceivedHttpError(webView, request, response)
        assertThat(failures).hasSize(1)
        val failure = failures.single() as MainFrameFailure.Http
        assertThat(failure.statusCode).isEqualTo(500)
    }

    @Test
    fun main_frame_http_204_is_ignored() {
        val request = fakeWebResourceRequest(ChannelDestination.HOME_URL, isForMainFrame = true)
        val response = WebResourceResponse(
            "text/plain",
            "utf-8",
            204,
            "No Content",
            emptyMap(),
            null,
        )
        client.onReceivedHttpError(webView, request, response)
        assertThat(failures).isEmpty()
    }

    @Test
    fun ssl_error_always_cancels_and_routes_when_main_frame_url_matches() {
        webView.loadUrl(ChannelDestination.HOME_URL)
        val handler = mock<SslErrorHandler>()
        val error = mock<SslError> {
            on { url } doReturn ChannelDestination.HOME_URL
            on { primaryError } doReturn SslError.SSL_UNTRUSTED
        }
        client.onReceivedSslError(webView, handler, error)
        verify(handler).cancel()
        assertThat(failures).hasSize(1)
        assertThat(failures.single()).isInstanceOf(MainFrameFailure.Ssl::class.java)
    }

    @Test
    fun ssl_error_on_different_url_cancels_but_does_not_force_offline() {
        webView.loadUrl(ChannelDestination.HOME_URL)
        val handler = mock<SslErrorHandler>()
        val error = mock<SslError> {
            on { url } doReturn "https://cdn.example.com/frame.html"
            on { primaryError } doReturn SslError.SSL_UNTRUSTED
        }
        client.onReceivedSslError(webView, handler, error)
        verify(handler).cancel()
        assertThat(failures).isEmpty()
    }

    @Test
    fun renderer_process_gone_invokes_recovery_callback_and_claims_ownership() {
        var recoveryCalled = false
        val recoveryClient = MidnightWebViewClient(
            MidnightWebViewClientCallbacks(
                onRendererRecoveryRequired = { recoveryCalled = true },
            ),
        )
        val detail = mock<RenderProcessGoneDetail> {
            on { didCrash() } doReturn true
        }
        assertThat(recoveryClient.onRenderProcessGone(webView, detail)).isTrue()
        assertThat(recoveryCalled).isTrue()
    }
}

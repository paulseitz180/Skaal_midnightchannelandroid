package com.skaalsolutions.midnightchannel.testing

import android.net.Uri
import android.webkit.WebResourceRequest
import androidx.core.net.toUri

/**
 * Lightweight [WebResourceRequest] for WebViewClient validation tests.
 */
fun fakeWebResourceRequest(
    url: String,
    isForMainFrame: Boolean,
    isRedirect: Boolean = false,
    hasGesture: Boolean = true,
    method: String = "GET",
): WebResourceRequest {
    val uri = url.toUri()
    return object : WebResourceRequest {
        override fun getUrl(): Uri = uri
        override fun isForMainFrame(): Boolean = isForMainFrame
        override fun isRedirect(): Boolean = isRedirect
        override fun hasGesture(): Boolean = hasGesture
        override fun getMethod(): String = method
        override fun getRequestHeaders(): MutableMap<String, String> = mutableMapOf()
    }
}

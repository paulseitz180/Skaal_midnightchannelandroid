package com.skaalsolutions.midnightchannel.webview

import android.net.http.SslError

/**
 * Main-frame failure kinds routed to the native Offline / Error flow.
 * Subframe / resource errors are intentionally excluded.
 */
sealed interface MainFrameFailure {

    /** Network / DNS / connectivity style WebViewClient error on the main frame. */
    data class Network(
        val errorCode: Int,
        val description: String?,
        val failingUrl: String?,
    ) : MainFrameFailure

    /** HTTP error status on the main document (e.g. 4xx / 5xx). */
    data class Http(
        val statusCode: Int,
        val failingUrl: String?,
    ) : MainFrameFailure

    /** TLS / certificate failure on the main frame. Proceed-past is never allowed. */
    data class Ssl(
        val primaryError: Int,
        val url: String?,
    ) : MainFrameFailure {
        companion object {
            fun from(error: SslError): Ssl =
                Ssl(primaryError = error.primaryError, url = error.url)
        }
    }

    /** Chromium renderer process crashed or was killed — host must recover. */
    data class RendererProcessGone(
        val didCrash: Boolean,
    ) : MainFrameFailure
}

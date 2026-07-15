package com.skaalsolutions.midnightchannel.webview

import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.webkit.RenderProcessGoneDetail
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.net.toUri

/**
 * Production [WebViewClient] for the Midnight Channel shell (Grande Document §08).
 *
 * Navigation rules (TASK 13):
 * - Same-origin `midnightchannel.live` → stay inside the WebView
 * - External / special schemes → block in-WebView load and delegate to
 *   [MidnightWebViewClientCallbacks.onExternalNavigation] (Intents)
 * - Never leave Android's default browser error page visible
 * - Main-frame failures route to native offline / Error via callbacks
 */
class MidnightWebViewClient(
    callbacks: MidnightWebViewClientCallbacks = MidnightWebViewClientCallbacks(),
) : WebViewClient() {

    @Volatile
    var callbacks: MidnightWebViewClientCallbacks = callbacks

    // —— Page lifecycle ——

    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        callbacks.onPageStarted(url)
    }

    override fun onPageFinished(view: WebView, url: String?) {
        callbacks.onPageFinished(url)
    }

    // —— Main-frame navigation ——

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        // Subframe / iframe navigations must not spawn external Intents (immersion).
        if (!request.isForMainFrame) {
            return !MidnightOriginPolicy.shouldLoadInsideWebView(request.url)
        }
        return handleNavigation(request.url)
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean =
        // String overload is invoked for top-level navigations on pre-N paths.
        handleNavigation(url?.toUri())

    /**
     * @return true when WebView must **not** navigate (external / blocked);
     * false when WebView should load the URL itself.
     */
    private fun handleNavigation(uri: Uri?): Boolean {
        if (uri == null) return true
        if (MidnightOriginPolicy.shouldLoadInsideWebView(uri)) {
            return false
        }
        // Block in-WebView load for any non-origin URL; launch externally when possible.
        if (MidnightOriginPolicy.shouldLeaveWebView(uri)) {
            callbacks.onExternalNavigation(uri)
        }
        return true
    }

    // —— Network / resource errors (main frame only) ——

    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceError,
    ) {
        if (!request.isForMainFrame) return
        suppressDefaultErrorPage(view)
        callbacks.onMainFrameFailed(
            MainFrameFailure.Network(
                errorCode = error.errorCode,
                description = error.description?.toString(),
                failingUrl = request.url?.toString(),
            ),
        )
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onReceivedError(
        view: WebView,
        errorCode: Int,
        description: String?,
        failingUrl: String?,
    ) {
        // Unused on minSdk 26+: the WebResourceRequest overload is always delivered.
        // Keep the override empty so the platform does not fall through to a default
        // that could surface the browser error page or mis-attribute subframe failures.
    }

    // —— HTTP errors (main frame only) ——

    override fun onReceivedHttpError(
        view: WebView,
        request: WebResourceRequest,
        errorResponse: WebResourceResponse,
    ) {
        if (!request.isForMainFrame) return
        val status = errorResponse.statusCode
        if (status < HTTP_CLIENT_ERROR_MIN) return
        suppressDefaultErrorPage(view)
        callbacks.onMainFrameFailed(
            MainFrameFailure.Http(
                statusCode = status,
                failingUrl = request.url?.toString(),
            ),
        )
    }

    // —— SSL errors (never proceed; Offline only for main-frame) ——

    override fun onReceivedSslError(
        view: WebView,
        handler: SslErrorHandler,
        error: SslError,
    ) {
        handler.cancel()
        // SslError has no isForMainFrame; treat as main-frame when the failing URL
        // matches the WebView's top-level URL (or top-level is still unset / blank).
        if (!isLikelyMainFrameSslFailure(view, error)) return
        suppressDefaultErrorPage(view)
        callbacks.onMainFrameFailed(MainFrameFailure.Ssl.from(error))
    }

    private fun isLikelyMainFrameSslFailure(view: WebView, error: SslError): Boolean {
        val failing = error.url?.takeIf { it.isNotBlank() } ?: return true
        val top = view.url?.takeIf { it.isNotBlank() } ?: return true
        if (top == "about:blank" || top.startsWith("data:")) return true
        // Exact URL match only — same-host subframe SSL must not force Offline.
        return failing == top
    }

    // —— Renderer process failures + recovery routing ——

    override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
        suppressDefaultErrorPage(view)
        val failure = MainFrameFailure.RendererProcessGone(didCrash = detail.didCrash())
        callbacks.onMainFrameFailed(failure)
        callbacks.onRendererRecoveryRequired(failure)
        return true
    }

    private fun suppressDefaultErrorPage(view: WebView) {
        runCatching {
            view.stopLoading()
            view.loadDataWithBaseURL(
                /* baseUrl = */ null,
                /* data = */ CRT_BLANK_DOCUMENT,
                /* mimeType = */ "text/html",
                /* encoding = */ "UTF-8",
                /* historyUrl = */ null,
            )
        }
    }

    private companion object {
        const val HTTP_CLIENT_ERROR_MIN: Int = 400

        val CRT_BLANK_DOCUMENT: String =
            "<!DOCTYPE html><html><head>" +
                "<meta name=\"color-scheme\" content=\"dark\">" +
                "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">" +
                "<style>html,body{margin:0;background:${CrtShellChrome.BACKGROUND_HEX};height:100%;}</style>" +
                "</head><body></body></html>"
    }
}

/**
 * Callbacks from [MidnightWebViewClient] into the Compose / shell layer.
 *
 * [onExternalNavigation] should open Intents via [ExternalLinkNavigator]
 * (wired by default in [MidnightWebView]).
 */
data class MidnightWebViewClientCallbacks(
    val onPageStarted: (url: String?) -> Unit = {},
    val onPageFinished: (url: String?) -> Unit = {},
    val onMainFrameFailed: (MainFrameFailure) -> Unit = {},
    val onExternalNavigation: (Uri) -> Unit = {},
    val onRendererRecoveryRequired: (MainFrameFailure.RendererProcessGone) -> Unit = {},
)

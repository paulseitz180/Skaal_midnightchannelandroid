package com.skaalsolutions.midnightchannel.webview

import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.toColorInt
import com.skaalsolutions.midnightchannel.util.ExternalLinkNavigator

/** Cached CRT backdrop — avoid re-parsing hex on every WebView create. */
private val CrtWebViewBackgroundColor: Int = CrtShellChrome.BACKGROUND_HEX.toColorInt()

/**
 * Production Compose host for a **single** [WebView] instance.
 *
 * Lifecycle pause/resume is owned by [WebViewLifecycleCoordinator] via the host
 * Activity (not a second LifecycleObserver here — avoids duplicate onPause/onResume).
 */
@Composable
fun MidnightWebView(
    modifier: Modifier = Modifier,
    contentVisible: Boolean = true,
    clientCallbacks: MidnightWebViewClientCallbacks = MidnightWebViewClientCallbacks(),
    chromeCallbacks: MidnightWebChromeClientCallbacks = MidnightWebChromeClientCallbacks(),
    onCreated: (WebView) -> Unit = {},
) {
    val context = LocalContext.current
    val coordinator = LocalWebViewLifecycleCoordinator.current
    val onCreatedState = rememberUpdatedState(onCreated)
    val visibleState = rememberUpdatedState(contentVisible)

    // Stable holders — no per-recomposition callback data-class copies.
    val clientHolder = remember { CallbackHolder(clientCallbacks) }
    clientHolder.current = clientCallbacks
    val chromeHolder = remember { ChromeCallbackHolder(chromeCallbacks) }
    chromeHolder.current = chromeCallbacks

    val webViewClient = remember {
        MidnightWebViewClient(
            MidnightWebViewClientCallbacks(
                onPageStarted = { clientHolder.current.onPageStarted(it) },
                onPageFinished = { clientHolder.current.onPageFinished(it) },
                onMainFrameFailed = { clientHolder.current.onMainFrameFailed(it) },
                onExternalNavigation = { uri ->
                    clientHolder.current.onExternalNavigation(uri)
                    ExternalLinkNavigator.open(context, uri)
                },
                onRendererRecoveryRequired = {
                    clientHolder.current.onRendererRecoveryRequired(it)
                },
            ),
        )
    }
    val webChromeClient = remember {
        MidnightWebChromeClient(
            MidnightWebChromeClientCallbacks(
                onProgressChanged = { p -> chromeHolder.current.onProgressChanged(p) },
                onReceivedTitle = { t -> chromeHolder.current.onReceivedTitle(t) },
            ),
        )
    }

    val webView = remember(context) {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            setBackgroundColor(CrtWebViewBackgroundColor)
            MidnightWebSettings.apply(this)
            this.webViewClient = webViewClient
            this.webChromeClient = webChromeClient
        }
    }

    DisposableEffect(webView, coordinator) {
        coordinator?.attach(webView)
        onCreatedState.value(webView)
        onDispose {
            if (coordinator != null) {
                coordinator.detach(webView, destroy = true)
            } else {
                disposeWebView(webView)
            }
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier.fillMaxSize(),
        update = { view ->
            // INVISIBLE (not Compose alpha) — skips compositing while Splash/Offline cover.
            view.visibility =
                if (visibleState.value) View.VISIBLE else View.INVISIBLE
        },
    )
}

private class CallbackHolder(
    @JvmField var current: MidnightWebViewClientCallbacks,
)

private class ChromeCallbackHolder(
    @JvmField var current: MidnightWebChromeClientCallbacks,
)

/**
 * Memory-safe teardown for a shell-owned WebView.
 * Stops work, detaches from the parent, then [WebView.destroy].
 *
 * Idempotent: Activity [WebViewLifecycleCoordinator.onHostDestroy] and Compose
 * [DisposableEffect] may both attempt dispose on finish — calling [WebView.destroy]
 * twice crashes the process (Grande Document lifecycle / cold-exit stability).
 */
internal fun disposeWebView(webView: WebView) {
    val alreadyDisposed = runCatching {
        synchronized(webView) {
            if (webView.getTag(WEBVIEW_DISPOSED_TAG_KEY) == true) {
                true
            } else {
                webView.setTag(WEBVIEW_DISPOSED_TAG_KEY, true)
                false
            }
        }
    }.getOrDefault(true)
    if (alreadyDisposed) return
    runCatching {
        webView.onPause()
        webView.stopLoading()
        webView.loadUrl("about:blank")
        (webView.parent as? ViewGroup)?.removeView(webView)
        webView.removeAllViews()
        webView.destroy()
    }
}

/** Tag key marking a WebView that has already been [WebView.destroy]ed. */
private const val WEBVIEW_DISPOSED_TAG_KEY: Int = 0x4D435F44 // "MC_D"

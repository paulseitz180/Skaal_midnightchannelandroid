package com.skaalsolutions.midnightchannel.webview

import android.os.Bundle
import android.webkit.WebView
import androidx.compose.runtime.staticCompositionLocalOf
import java.lang.ref.WeakReference

/**
 * Activity-scoped WebView lifecycle coordinator.
 *
 * Responsibilities:
 * - Pause / resume the live WebView with the Activity (browser-parity media freeze)
 * - Save / restore navigation state across process death
 * - Avoid strong retention of [WebView] outside Compose (WeakReference only)
 * - Centralize destroy / resource cleanup
 *
 * Configuration changes are primarily absorbed by Manifest `android:configChanges`
 * so the Activity (and [remember]ed WebView) are **not** recreated on rotation.
 * Process death still uses [saveState] / [prepareRestore].
 */
class WebViewLifecycleCoordinator {

    @Volatile
    private var webViewRef: WeakReference<WebView>? = null

    @Volatile
    private var pendingRestoreState: Bundle? = null

    val webViewOrNull: WebView?
        get() = webViewRef?.get()

    /**
     * Stash state from [android.app.Activity.onCreate] / process recreation so the
     * next [attach] can [WebView.restoreState] without racing Compose.
     */
    fun prepareRestore(state: Bundle?) {
        pendingRestoreState = state?.let { Bundle(it) }
    }

    /**
     * Register the Compose-owned WebView. Restores pending process-death state once.
     */
    fun attach(webView: WebView) {
        webViewRef = WeakReference(webView)
        pendingRestoreState?.let { restored ->
            runCatching { webView.restoreState(restored) }
            pendingRestoreState = null
        }
    }

    /**
     * Drop the reference. When [destroy] is true, perform full memory cleanup.
     */
    fun detach(webView: WebView, destroy: Boolean) {
        if (webViewRef?.get() === webView) {
            webViewRef = null
        }
        if (destroy) {
            disposeWebView(webView)
        }
    }

    /** Activity / fragment [onResume] — resume DOM timers & media for this WebView. */
    fun onHostResume() {
        webViewRef?.get()?.let { view ->
            runCatching { view.onResume() }
        }
    }

    /** Activity / fragment [onPause] — pause this WebView (MVP: no background audio). */
    fun onHostPause() {
        webViewRef?.get()?.let { view ->
            runCatching { view.onPause() }
        }
    }

    /**
     * Snapshot for [android.app.Activity.onSaveInstanceState].
     * Returns null when no WebView is attached (e.g. still on Splash).
     */
    fun saveState(): Bundle? {
        val view = webViewRef?.get() ?: return null
        return Bundle().also { bundle ->
            runCatching { view.saveState(bundle) }
        }
    }

    /**
     * Final Activity teardown. Clears coordinator refs only — Compose
     * [DisposableEffect] in [MidnightWebView] owns [disposeWebView] so finish
     * does not double-[WebView.destroy] (Activity.onDestroy + composition dispose).
     */
    fun onHostDestroy() {
        webViewRef = null
        pendingRestoreState = null
    }
}

/** Provided from [com.skaalsolutions.midnightchannel.MainActivity] for Compose hosts. */
val LocalWebViewLifecycleCoordinator =
    staticCompositionLocalOf<WebViewLifecycleCoordinator?> { null }

internal const val WEBVIEW_STATE_BUNDLE_KEY: String = "midnight.webview.state"

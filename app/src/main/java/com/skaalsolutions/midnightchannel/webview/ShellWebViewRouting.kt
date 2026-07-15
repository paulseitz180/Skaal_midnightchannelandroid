package com.skaalsolutions.midnightchannel.webview

import android.net.Uri
import androidx.core.net.toUri
import com.skaalsolutions.midnightchannel.navigation.ShellEvent

/**
 * Maps WebViewClient lifecycle / failure callbacks onto [ShellEvent].
 *
 * Success emits a **single** [ShellEvent.MainFrameLoadFinished] (reducer also
 * treats it as Splash first-paint). CRT blank / non-origin docs are ignored.
 */
fun shellRoutingWebViewCallbacks(
    emit: (ShellEvent) -> Unit,
    onExternalNavigation: (Uri) -> Unit = {},
    onRendererRecoveryRequired: (MainFrameFailure.RendererProcessGone) -> Unit = {
        emit(ShellEvent.MainFrameLoadFailed)
    },
): MidnightWebViewClientCallbacks = MidnightWebViewClientCallbacks(
    onPageStarted = { url ->
        if (isIgnorableShellDocument(url)) return@MidnightWebViewClientCallbacks
        emit(ShellEvent.MainFrameLoadStarted)
    },
    onPageFinished = { url ->
        if (isIgnorableShellDocument(url)) return@MidnightWebViewClientCallbacks
        if (!MidnightOriginPolicy.isSameOrigin(url)) return@MidnightWebViewClientCallbacks
        emit(ShellEvent.MainFrameLoadFinished)
    },
    onMainFrameFailed = { failure ->
        if (isIgnorableShellDocument(failure.failingUrlOrNull())) return@MidnightWebViewClientCallbacks
        emit(ShellEvent.MainFrameLoadFailed)
    },
    onExternalNavigation = onExternalNavigation,
    onRendererRecoveryRequired = onRendererRecoveryRequired,
)

private fun isIgnorableShellDocument(url: String?): Boolean {
    if (url.isNullOrBlank()) return false
    return MidnightOriginPolicy.isShellInternalDocument(url.toUri())
}

private fun MainFrameFailure.failingUrlOrNull(): String? =
    when (this) {
        is MainFrameFailure.Network -> failingUrl
        is MainFrameFailure.Http -> failingUrl
        is MainFrameFailure.Ssl -> null
        is MainFrameFailure.RendererProcessGone -> null
    }

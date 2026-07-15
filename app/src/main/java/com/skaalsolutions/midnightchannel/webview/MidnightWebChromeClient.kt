package com.skaalsolutions.midnightchannel.webview

import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.skaalsolutions.midnightchannel.BuildConfig

/**
 * Production [WebChromeClient] for the Midnight Channel shell (Grande Document §08).
 *
 * Supported today:
 * - Page load progress (`onProgressChanged`)
 * - Document title (`onReceivedTitle`)
 * - Console logging in **debug builds only** (`onConsoleMessage`)
 *
 * Explicitly denied (no permission UX; Manifest declares none):
 * - Geolocation prompts
 * - Generic WebRTC / media permission requests
 * - File chooser is not overridden (defaults cannot grant storage without Manifest permission)
 *
 * JavaScript alert / confirm / prompt remain platform defaults (site does not rely on them).
 */
class MidnightWebChromeClient(
    callbacks: MidnightWebChromeClientCallbacks = MidnightWebChromeClientCallbacks(),
) : WebChromeClient() {

    private val callbacks: MidnightWebChromeClientCallbacks = callbacks

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        callbacks.onProgressChanged(newProgress.coerceIn(0, 100))
    }

    override fun onReceivedTitle(view: WebView?, title: String?) {
        callbacks.onReceivedTitle(title)
    }

    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        if (!BuildConfig.DEBUG || consoleMessage == null) {
            // Release: swallow quietly. Debug: forward + log.
            return true
        }
        callbacks.onConsoleMessage(consoleMessage)
        Log.println(
            consoleMessage.messageLevel().toAndroidPriority(),
            DEBUG_CONSOLE_TAG,
            "${consoleMessage.sourceId()}:${consoleMessage.lineNumber()} ${consoleMessage.message()}",
        )
        return true
    }

    override fun onGeolocationPermissionsShowPrompt(
        origin: String?,
        callback: GeolocationPermissions.Callback?,
    ) {
        // Product geo is IP/timezone-based in site JS — never grant device geolocation.
        callback?.invoke(origin, /* allow = */ false, /* retain = */ false)
    }

    override fun onPermissionRequest(request: PermissionRequest?) {
        // Camera / microphone / MIDI etc. — deny; Manifest has no matching permissions.
        request?.deny()
    }

    private companion object {
        const val DEBUG_CONSOLE_TAG = "MCWebChrome"
    }
}

/**
 * Narrow callback surface for chrome events used by the shell.
 * Extend this data class when a future TASK needs an additional chrome hook.
 */
data class MidnightWebChromeClientCallbacks(
    val onProgressChanged: (progress: Int) -> Unit = {},
    val onReceivedTitle: (title: String?) -> Unit = {},
    val onConsoleMessage: (ConsoleMessage) -> Unit = {},
)

private fun ConsoleMessage.MessageLevel.toAndroidPriority(): Int = when (this) {
    ConsoleMessage.MessageLevel.TIP,
    ConsoleMessage.MessageLevel.LOG,
    -> Log.INFO
    ConsoleMessage.MessageLevel.WARNING -> Log.WARN
    ConsoleMessage.MessageLevel.ERROR -> Log.ERROR
    ConsoleMessage.MessageLevel.DEBUG -> Log.DEBUG
}

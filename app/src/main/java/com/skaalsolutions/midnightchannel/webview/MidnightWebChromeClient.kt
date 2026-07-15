package com.skaalsolutions.midnightchannel.webview

import android.util.Log
import android.webkit.ConsoleMessage
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
 * Explicitly **not** overridden (defaults stand; unsupported in MVP):
 * - File chooser / file picker
 * - Camera / microphone permission prompts
 * - Geolocation prompts
 * - Generic permission requests
 * - JavaScript alert / confirm / prompt dialogs
 *
 * Future extensibility: add optional hooks to [MidnightWebChromeClientCallbacks]
 * and override the matching method only when a later TASK requires it — do not
 * pre-enable unsupported surfaces here.
 */
class MidnightWebChromeClient(
    callbacks: MidnightWebChromeClientCallbacks = MidnightWebChromeClientCallbacks(),
) : WebChromeClient() {

    @Volatile
    var callbacks: MidnightWebChromeClientCallbacks = callbacks

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

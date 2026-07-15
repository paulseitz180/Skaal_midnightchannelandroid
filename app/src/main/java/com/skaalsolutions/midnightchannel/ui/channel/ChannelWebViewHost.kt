package com.skaalsolutions.midnightchannel.ui.channel

import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.skaalsolutions.midnightchannel.webview.MidnightWebChromeClientCallbacks
import com.skaalsolutions.midnightchannel.webview.MidnightWebView
import com.skaalsolutions.midnightchannel.webview.MidnightWebViewClientCallbacks

/**
 * Main Channel View WebView container.
 *
 * No extra CRT Box/background layer — the shell host already paints `#0A0A0A`
 * (saves one Compose draw pass under Splash/Offline).
 */
@Composable
fun ChannelWebViewHost(
    modifier: Modifier = Modifier,
    contentVisible: Boolean = true,
    clientCallbacks: MidnightWebViewClientCallbacks = MidnightWebViewClientCallbacks(),
    chromeCallbacks: MidnightWebChromeClientCallbacks = MidnightWebChromeClientCallbacks(),
    onWebViewCreated: (WebView) -> Unit = {},
) {
    MidnightWebView(
        modifier = modifier.fillMaxSize(),
        contentVisible = contentVisible,
        clientCallbacks = clientCallbacks,
        chromeCallbacks = chromeCallbacks,
        onCreated = onWebViewCreated,
    )
}

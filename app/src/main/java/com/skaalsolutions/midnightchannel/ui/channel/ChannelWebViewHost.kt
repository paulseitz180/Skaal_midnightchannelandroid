package com.skaalsolutions.midnightchannel.ui.channel

import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.skaalsolutions.midnightchannel.ui.testing.ShellUiTestTags
import com.skaalsolutions.midnightchannel.webview.MidnightWebChromeClientCallbacks
import com.skaalsolutions.midnightchannel.webview.MidnightWebView
import com.skaalsolutions.midnightchannel.webview.MidnightWebViewClientCallbacks

/**
 * Main Channel View WebView container.
 *
 * No extra CRT Box/background layer — [MidnightChannelTheme] paints the field once.
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
        modifier = modifier
            .fillMaxSize()
            .testTag(ShellUiTestTags.CHANNEL_HOST),
        contentVisible = contentVisible,
        clientCallbacks = clientCallbacks,
        chromeCallbacks = chromeCallbacks,
        onCreated = onWebViewCreated,
    )
}

package com.skaalsolutions.midnightchannel.webview

import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

/**
 * [MidnightWebChromeClient] progress / title + permission denial hygiene (TASK 37).
 */
class MidnightWebChromeClientTest {

    @Test
    fun `progress changed clamps to zero through one hundred`() {
        val seen = mutableListOf<Int>()
        val client = MidnightWebChromeClient(
            MidnightWebChromeClientCallbacks(onProgressChanged = { seen += it }),
        )

        client.onProgressChanged(view = null, newProgress = -5)
        client.onProgressChanged(view = null, newProgress = 42)
        client.onProgressChanged(view = null, newProgress = 150)

        assertThat(seen).containsExactly(0, 42, 100).inOrder()
    }

    @Test
    fun `received title forwards nullable title to callbacks`() {
        var last: String? = "unset"
        val client = MidnightWebChromeClient(
            MidnightWebChromeClientCallbacks(onReceivedTitle = { last = it }),
        )

        client.onReceivedTitle(view = null, title = "Midnight Channel")
        assertThat(last).isEqualTo("Midnight Channel")

        client.onReceivedTitle(view = null, title = null)
        assertThat(last).isNull()
    }

    @Test
    fun `console message is always consumed so default chrome UI is suppressed`() {
        val client = MidnightWebChromeClient()
        assertThat(client.onConsoleMessage(consoleMessage = null)).isTrue()
    }

    @Test
    fun `geolocation permission prompts are denied`() {
        val callback = mock<GeolocationPermissions.Callback>()
        MidnightWebChromeClient().onGeolocationPermissionsShowPrompt("https://evil.example", callback)
        verify(callback).invoke("https://evil.example", false, false)
    }

    @Test
    fun `webrtc permission requests are denied`() {
        val request = mock<PermissionRequest>()
        MidnightWebChromeClient().onPermissionRequest(request)
        verify(request).deny()
    }
}

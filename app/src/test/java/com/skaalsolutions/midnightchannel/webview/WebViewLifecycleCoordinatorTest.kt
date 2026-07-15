package com.skaalsolutions.midnightchannel.webview

import android.os.Bundle
import android.webkit.WebView
import com.google.common.truth.Truth.assertThat
import com.skaalsolutions.midnightchannel.testing.MidnightRobolectricTest
import org.junit.Test
import org.robolectric.RuntimeEnvironment

/**
 * Activity ↔ WebView lifecycle coordinator — pause/resume, weak attach, destroy ownership.
 */
class WebViewLifecycleCoordinatorTest : MidnightRobolectricTest() {

    @Test
    fun `attach exposes webview and detach destroy clears it`() {
        val coordinator = WebViewLifecycleCoordinator()
        val webView = WebView(RuntimeEnvironment.getApplication())
        coordinator.attach(webView)
        assertThat(coordinator.webViewOrNull).isSameInstanceAs(webView)

        coordinator.detach(webView, destroy = true)
        assertThat(coordinator.webViewOrNull).isNull()
    }

    @Test
    fun `onHostDestroy clears refs without requiring second destroy`() {
        val coordinator = WebViewLifecycleCoordinator()
        val webView = WebView(RuntimeEnvironment.getApplication())
        coordinator.attach(webView)
        coordinator.onHostDestroy()
        assertThat(coordinator.webViewOrNull).isNull()
        // Compose still owns destroy — idempotent dispose must be safe afterward.
        disposeWebView(webView)
        disposeWebView(webView)
    }

    @Test
    fun `saveState returns null when nothing attached`() {
        val coordinator = WebViewLifecycleCoordinator()
        assertThat(coordinator.saveState()).isNull()
    }

    @Test
    fun `saveState returns bundle when webview attached`() {
        val coordinator = WebViewLifecycleCoordinator()
        val webView = WebView(RuntimeEnvironment.getApplication())
        coordinator.attach(webView)
        val state = coordinator.saveState()
        assertThat(state).isNotNull()
        coordinator.detach(webView, destroy = true)
    }

    @Test
    fun `prepareRestore then attach consumes pending state once`() {
        val coordinator = WebViewLifecycleCoordinator()
        val pending = Bundle().apply { putString("probe", "1") }
        coordinator.prepareRestore(pending)
        val webView = WebView(RuntimeEnvironment.getApplication())
        coordinator.attach(webView)
        // Second attach must not keep a stale pending copy.
        val other = WebView(RuntimeEnvironment.getApplication())
        coordinator.attach(other)
        coordinator.detach(other, destroy = true)
        disposeWebView(webView)
    }

    @Test
    fun `pause and resume are safe with and without attachment`() {
        val coordinator = WebViewLifecycleCoordinator()
        coordinator.onHostPause()
        coordinator.onHostResume()
        val webView = WebView(RuntimeEnvironment.getApplication())
        coordinator.attach(webView)
        coordinator.onHostPause()
        coordinator.onHostResume()
        coordinator.detach(webView, destroy = true)
    }
}

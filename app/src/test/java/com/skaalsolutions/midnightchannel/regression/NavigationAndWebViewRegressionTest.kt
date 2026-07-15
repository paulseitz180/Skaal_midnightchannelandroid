package com.skaalsolutions.midnightchannel.regression

import android.content.Intent
import android.webkit.WebView
import com.google.common.truth.Truth.assertThat
import com.skaalsolutions.midnightchannel.testing.MidnightRobolectricTest
import com.skaalsolutions.midnightchannel.testing.OriginTestFixtures
import com.skaalsolutions.midnightchannel.util.ExternalLinkNavigator
import com.skaalsolutions.midnightchannel.webview.ChannelDestination
import com.skaalsolutions.midnightchannel.webview.MidnightOriginPolicy
import com.skaalsolutions.midnightchannel.webview.MidnightWebSettings
import com.skaalsolutions.midnightchannel.webview.disposeWebView
import org.junit.Test
import org.robolectric.RuntimeEnvironment

/**
 * TASK 35 — origin / Intent / WebSettings regression smoke across earlier TASKS.
 */
class NavigationAndWebViewRegressionTest : MidnightRobolectricTest() {

    @Test
    fun `home destination remains https product host and same origin`() {
        assertThat(ChannelDestination.HOME_URL).startsWith("https://")
        assertThat(MidnightOriginPolicy.isSameOrigin(OriginTestFixtures.homeHttps)).isTrue()
        assertThat(MidnightOriginPolicy.isSameOrigin(OriginTestFixtures.httpOnHost())).isFalse()
    }

    @Test
    fun `external https builds view intent while same origin is not externally handled`() {
        val external = OriginTestFixtures.externalHttps()
        assertThat(ExternalLinkNavigator.isExternallyHandledUri(external)).isTrue()
        val intent = ExternalLinkNavigator.buildValidatedIntent(external)
        assertThat(intent).isNotNull()
        assertThat(intent!!.action).isEqualTo(Intent.ACTION_VIEW)

        assertThat(ExternalLinkNavigator.isExternallyHandledUri(OriginTestFixtures.homeHttps))
            .isFalse()
    }

    @Test
    fun `web settings apply keeps javascript on and custom ua off`() {
        val webView = WebView(RuntimeEnvironment.getApplication())
        val beforeUa = webView.settings.userAgentString
        MidnightWebSettings.apply(webView)
        assertThat(webView.settings.javaScriptEnabled).isTrue()
        assertThat(webView.settings.userAgentString).isEqualTo(beforeUa)
        disposeWebView(webView)
    }
}

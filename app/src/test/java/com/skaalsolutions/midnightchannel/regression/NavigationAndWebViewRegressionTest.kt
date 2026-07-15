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
    fun home_destination_remains_https_product_host_and_same_origin() {
        assertThat(ChannelDestination.HOME_URL).startsWith("https://")
        assertThat(MidnightOriginPolicy.isSameOrigin(OriginTestFixtures.homeHttps)).isTrue()
        assertThat(MidnightOriginPolicy.isSameOrigin(OriginTestFixtures.httpOnHost())).isFalse()
    }

    @Test
    fun external_https_builds_view_intent_while_same_origin_is_not_externally_handled() {
        val external = OriginTestFixtures.externalHttps()
        assertThat(ExternalLinkNavigator.isExternallyHandledUri(external)).isTrue()
        val intent = ExternalLinkNavigator.buildValidatedIntent(external)
        assertThat(intent).isNotNull()
        assertThat(intent!!.action).isEqualTo(Intent.ACTION_VIEW)

        assertThat(ExternalLinkNavigator.isExternallyHandledUri(OriginTestFixtures.homeHttps))
            .isFalse()
    }

    @Test
    fun web_settings_apply_keeps_javascript_on_and_custom_ua_off() {
        val webView = WebView(RuntimeEnvironment.getApplication())
        val beforeUa = webView.settings.userAgentString
        MidnightWebSettings.apply(webView)
        assertThat(webView.settings.javaScriptEnabled).isTrue()
        assertThat(webView.settings.userAgentString).isEqualTo(beforeUa)
        disposeWebView(webView)
    }
}

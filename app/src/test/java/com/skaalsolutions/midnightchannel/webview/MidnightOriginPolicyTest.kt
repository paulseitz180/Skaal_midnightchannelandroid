package com.skaalsolutions.midnightchannel.webview

import com.google.common.truth.Truth.assertThat
import com.skaalsolutions.midnightchannel.testing.MidnightRobolectricTest
import com.skaalsolutions.midnightchannel.testing.OriginTestFixtures
import org.junit.Test
import androidx.core.net.toUri

class MidnightOriginPolicyTest : MidnightRobolectricTest() {

    @Test
    fun `https product host is same origin`() {
        assertThat(MidnightOriginPolicy.isSameOrigin(OriginTestFixtures.homeHttps)).isTrue()
        assertThat(MidnightOriginPolicy.isSameOrigin(ChannelDestination.HOME_URL)).isTrue()
        assertThat(
            MidnightOriginPolicy.isSameOrigin(OriginTestFixtures.httpsOnHost(path = "/path")),
        ).isTrue()
    }

    @Test
    fun `subdomain of product host is same origin`() {
        assertThat(
            MidnightOriginPolicy.isSameOrigin(
                OriginTestFixtures.httpsOnHost(host = "www.midnightchannel.live"),
            ),
        ).isTrue()
    }

    @Test
    fun `http product host is not same origin`() {
        assertThat(MidnightOriginPolicy.isSameOrigin(OriginTestFixtures.httpOnHost())).isFalse()
        assertThat(MidnightOriginPolicy.shouldLoadInsideWebView(OriginTestFixtures.httpOnHost()))
            .isFalse()
        assertThat(MidnightOriginPolicy.shouldLeaveWebView(OriginTestFixtures.httpOnHost()))
            .isTrue()
    }

    @Test
    fun `external https leaves webview`() {
        val external = OriginTestFixtures.externalHttps()
        assertThat(MidnightOriginPolicy.isSameOrigin(external)).isFalse()
        assertThat(MidnightOriginPolicy.shouldLoadInsideWebView(external)).isFalse()
        assertThat(MidnightOriginPolicy.shouldLeaveWebView(external)).isTrue()
    }

    @Test
    fun `about and data are shell internal`() {
        assertThat(MidnightOriginPolicy.isShellInternalDocument(OriginTestFixtures.aboutBlank))
            .isTrue()
        assertThat(MidnightOriginPolicy.isShellInternalDocument(OriginTestFixtures.dataHtml))
            .isTrue()
        assertThat(MidnightOriginPolicy.shouldLoadInsideWebView(OriginTestFixtures.aboutBlank))
            .isTrue()
        assertThat(MidnightOriginPolicy.shouldLeaveWebView(OriginTestFixtures.aboutBlank))
            .isFalse()
    }

    @Test
    fun `unsafe about and data schemes are not shell internal`() {
        assertThat(
            MidnightOriginPolicy.isShellInternalDocument("about:config".toUri()),
        ).isFalse()
        assertThat(
            MidnightOriginPolicy.isShellInternalDocument("data:text/javascript,alert(1)".toUri()),
        ).isFalse()
        assertThat(
            MidnightOriginPolicy.isShellInternalDocument("data:image/svg+xml,<svg></svg>".toUri()),
        ).isFalse()
        assertThat(MidnightOriginPolicy.shouldLoadInsideWebView("javascript:void(0)".toUri()))
            .isFalse()
        assertThat(MidnightOriginPolicy.shouldLeaveWebView("javascript:void(0)".toUri()))
            .isFalse()
    }

    @Test
    fun `mailto and tel leave webview`() {
        assertThat(MidnightOriginPolicy.shouldLeaveWebView(OriginTestFixtures.mailto())).isTrue()
        assertThat(MidnightOriginPolicy.shouldLeaveWebView(OriginTestFixtures.tel())).isTrue()
        assertThat(MidnightOriginPolicy.shouldLoadInsideWebView(OriginTestFixtures.mailto()))
            .isFalse()
    }

    @Test
    fun `null and blank urls are rejected`() {
        assertThat(MidnightOriginPolicy.isSameOrigin(null as String?)).isFalse()
        assertThat(MidnightOriginPolicy.isSameOrigin(null as android.net.Uri?)).isFalse()
        assertThat(MidnightOriginPolicy.shouldLoadInsideWebView(null)).isFalse()
        assertThat(MidnightOriginPolicy.shouldLeaveWebView(null)).isFalse()
    }

    @Test
    fun `allowed host constant matches channel destination`() {
        assertThat(MidnightOriginPolicy.ALLOWED_HOST).isEqualTo(ChannelDestination.HOST)
    }
}

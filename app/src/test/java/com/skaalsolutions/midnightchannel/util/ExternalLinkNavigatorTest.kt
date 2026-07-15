package com.skaalsolutions.midnightchannel.util

import android.content.Intent
import androidx.core.net.toUri
import com.google.common.truth.Truth.assertThat
import com.skaalsolutions.midnightchannel.testing.MidnightRobolectricTest
import com.skaalsolutions.midnightchannel.testing.OriginTestFixtures
import org.junit.Test

class ExternalLinkNavigatorTest : MidnightRobolectricTest() {

    @Test
    fun `same origin is not externally handled`() {
        assertThat(ExternalLinkNavigator.isExternallyHandledUri(OriginTestFixtures.homeHttps))
            .isFalse()
        assertThat(ExternalLinkNavigator.isExternallyHandledUri(OriginTestFixtures.aboutBlank))
            .isFalse()
    }

    @Test
    fun `external https builds view intent`() {
        val uri = OriginTestFixtures.externalHttps()
        assertThat(ExternalLinkNavigator.isExternallyHandledUri(uri)).isTrue()
        val intent = ExternalLinkNavigator.buildValidatedIntent(uri)
        assertThat(intent).isNotNull()
        assertThat(intent!!.action).isEqualTo(Intent.ACTION_VIEW)
        assertThat(intent.data).isEqualTo(uri)
    }

    @Test
    fun `mailto builds sendto intent`() {
        val intent = ExternalLinkNavigator.buildValidatedIntent(OriginTestFixtures.mailto())
        assertThat(intent).isNotNull()
        assertThat(intent!!.action).isEqualTo(Intent.ACTION_SENDTO)
    }

    @Test
    fun `tel builds dial intent not call`() {
        val intent = ExternalLinkNavigator.buildValidatedIntent(OriginTestFixtures.tel())
        assertThat(intent).isNotNull()
        assertThat(intent!!.action).isEqualTo(Intent.ACTION_DIAL)
        assertThat(intent.action).isNotEqualTo(Intent.ACTION_CALL)
    }

    @Test
    fun `invalid http without host rejected`() {
        assertThat(ExternalLinkNavigator.buildValidatedIntent("https://".toUri())).isNull()
    }

    @Test
    fun `tel without digits rejected`() {
        assertThat(ExternalLinkNavigator.buildValidatedIntent("tel:not-a-number".toUri())).isNull()
    }

    @Test
    fun `sanitized intents clear package component and grant flags`() {
        val intent = ExternalLinkNavigator.buildValidatedIntent(OriginTestFixtures.externalHttps())
        assertThat(intent).isNotNull()
        assertThat(intent!!.`package`).isNull()
        assertThat(intent.component).isNull()
        assertThat(intent.clipData).isNull()
        assertThat(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION).isEqualTo(0)
    }

    @Test
    fun `intent uri with install action rejected`() {
        val uri =
            "intent://example.com#Intent;scheme=https;action=android.intent.action.INSTALL_PACKAGE;end"
                .toUri()
        assertThat(ExternalLinkNavigator.buildValidatedIntent(uri)).isNull()
    }
}

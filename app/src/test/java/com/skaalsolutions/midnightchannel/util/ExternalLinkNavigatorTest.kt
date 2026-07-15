package com.skaalsolutions.midnightchannel.util

import android.content.Intent
import androidx.core.net.toUri
import com.google.common.truth.Truth.assertThat
import com.skaalsolutions.midnightchannel.testing.MidnightRobolectricTest
import com.skaalsolutions.midnightchannel.testing.OriginTestFixtures
import org.junit.Test

class ExternalLinkNavigatorTest : MidnightRobolectricTest() {

    @Test
    fun same_origin_is_not_externally_handled() {
        assertThat(ExternalLinkNavigator.isExternallyHandledUri(OriginTestFixtures.homeHttps))
            .isFalse()
        assertThat(ExternalLinkNavigator.isExternallyHandledUri(OriginTestFixtures.aboutBlank))
            .isFalse()
    }

    @Test
    fun external_https_builds_view_intent() {
        val uri = OriginTestFixtures.externalHttps()
        assertThat(ExternalLinkNavigator.isExternallyHandledUri(uri)).isTrue()
        val intent = ExternalLinkNavigator.buildValidatedIntent(uri)
        assertThat(intent).isNotNull()
        assertThat(intent!!.action).isEqualTo(Intent.ACTION_VIEW)
        assertThat(intent.data).isEqualTo(uri)
    }

    @Test
    fun mailto_builds_sendto_intent() {
        val intent = ExternalLinkNavigator.buildValidatedIntent(OriginTestFixtures.mailto())
        assertThat(intent).isNotNull()
        assertThat(intent!!.action).isEqualTo(Intent.ACTION_SENDTO)
    }

    @Test
    fun tel_builds_dial_intent_not_call() {
        val intent = ExternalLinkNavigator.buildValidatedIntent(OriginTestFixtures.tel())
        assertThat(intent).isNotNull()
        assertThat(intent!!.action).isEqualTo(Intent.ACTION_DIAL)
        assertThat(intent.action).isNotEqualTo(Intent.ACTION_CALL)
    }

    @Test
    fun invalid_http_without_host_rejected() {
        assertThat(ExternalLinkNavigator.buildValidatedIntent("https://".toUri())).isNull()
    }

    @Test
    fun tel_without_digits_rejected() {
        assertThat(ExternalLinkNavigator.buildValidatedIntent("tel:not-a-number".toUri())).isNull()
    }

    @Test
    fun sanitized_intents_clear_package_component_and_grant_flags() {
        val intent = ExternalLinkNavigator.buildValidatedIntent(OriginTestFixtures.externalHttps())
        assertThat(intent).isNotNull()
        assertThat(intent!!.`package`).isNull()
        assertThat(intent.component).isNull()
        assertThat(intent.clipData).isNull()
        assertThat(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION).isEqualTo(0)
    }

    @Test
    fun intent_uri_with_install_action_rejected() {
        val uri =
            "intent://example.com#Intent;scheme=https;action=android.intent.action.INSTALL_PACKAGE;end"
                .toUri()
        assertThat(ExternalLinkNavigator.buildValidatedIntent(uri)).isNull()
    }
}

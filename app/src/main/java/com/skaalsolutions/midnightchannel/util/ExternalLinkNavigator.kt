package com.skaalsolutions.midnightchannel.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.skaalsolutions.midnightchannel.BuildConfig
import com.skaalsolutions.midnightchannel.webview.MidnightOriginPolicy

/**
 * External application launching for destinations that must leave the WebView
 * (Grande Document — External Link rule / browser parity).
 *
 * Supported:
 * - Browser — `https` / `http` (non–same-origin only)
 * - Email — `mailto`
 * - Telephone — `tel` (dialer; never `CALL` without permission)
 * - SMS — `sms` / `smsto`
 * - Google Play — `market` and Play Store https hosts
 * - Android Intent URLs — `intent:` with `browser_fallback_url`
 *
 * All Intents are validated and resolved before launch. Failures never crash the shell.
 */
object ExternalLinkNavigator {

    private const val TAG = "MCExternalNav"

    /**
     * Opens [uri] in an external app when safe and resolvable.
     *
     * @return true if an Activity was started; false if rejected, unsupported, or no handler.
     */
    fun open(context: Context, uri: Uri): Boolean =
        runCatching { openInternal(context, uri) }
            .onFailure { t -> logDebug("Unhandled open failure for $uri: ${t.message}") }
            .getOrDefault(false)

    /**
     * True when the URI scheme is one the shell may hand off externally.
     * Same-origin midnightchannel.live is never "external" even on https.
     */
    fun isExternallyHandledUri(uri: Uri): Boolean =
        MidnightOriginPolicy.shouldLeaveWebView(uri)

    private fun openInternal(context: Context, uri: Uri): Boolean {
        // Defense in depth: never Intent-out same-origin / shell-internal documents.
        if (MidnightOriginPolicy.shouldLoadInsideWebView(uri)) {
            logDebug("Rejected internal/same-origin URI for external launch: $uri")
            return false
        }

        if (!isExternallyHandledUri(uri)) {
            logDebug("Unsupported scheme for external launch: $uri")
            return false
        }

        val intent = buildValidatedIntent(uri) ?: run {
            logDebug("Failed validation / build for: $uri")
            return false
        }

        prepareForLaunch(context, intent)

        if (!canResolve(context, intent)) {
            logDebug("No resolver for: $uri")
            return tryFallback(context, uri, intent)
        }

        return startActivitySafely(context, intent) || tryFallback(context, uri, intent)
    }

    /**
     * Build a schema-appropriate Intent and strip unsafe fields.
     * Returns null when the URI is malformed or unsafe.
     */
    fun buildValidatedIntent(uri: Uri): Intent? {
        val scheme = uri.scheme?.lowercase() ?: return null
        if (scheme !in MidnightOriginPolicy.EXTERNAL_HANDOFF_SCHEMES) return null
        if (!isStructurallyValid(uri, scheme)) return null

        val intent = when (scheme) {
            "https", "http" -> Intent(Intent.ACTION_VIEW, uri)

            "mailto" -> Intent(Intent.ACTION_SENDTO, uri)

            // ACTION_DIAL never places a call — no CALL_PHONE permission; browser parity.
            "tel" -> Intent(Intent.ACTION_DIAL, uri)

            "sms", "smsto" -> Intent(Intent.ACTION_SENDTO, uri)

            "market" -> Intent(Intent.ACTION_VIEW, uri)

            "intent" -> parseIntentUri(uri)

            else -> null
        } ?: return null

        return sanitizeIntent(intent)
    }

    private fun isStructurallyValid(uri: Uri, scheme: String): Boolean =
        when (scheme) {
            "https", "http" -> !uri.host.isNullOrBlank()
            "mailto" -> {
                // mailto:user@host or mailto:?subject=… are valid; empty scheme-only is not.
                uri.schemeSpecificPart?.isNotBlank() == true
            }
            "tel" -> {
                val ssp = uri.schemeSpecificPart?.trim().orEmpty()
                ssp.isNotEmpty() && ssp.any { it.isDigit() }
            }
            "sms", "smsto" -> uri.schemeSpecificPart?.isNotBlank() == true
            "market" -> uri.schemeSpecificPart?.isNotBlank() == true
            "intent" -> uri.toString().length in 1..MAX_INTENT_URI_LENGTH
            else -> false
        }

    /**
     * Chrome-like Intent URL parsing: allow VIEW/SENDTO/DIAL; never CALL;
     * always clear explicit component / selector so the platform resolves safely.
     */
    private fun parseIntentUri(uri: Uri): Intent? =
        runCatching {
            val parsed = Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME)
            val action = parsed.action
            when {
                action.isNullOrBlank() -> parsed.action = Intent.ACTION_VIEW
                action == Intent.ACTION_CALL ||
                    action == Intent.ACTION_CALL_BUTTON ||
                    action == "android.intent.action.CALL_PRIVILEGED" -> {
                    logDebug("Rejected privileged call action in intent: URI")
                    return null
                }
            }
            parsed
        }.getOrNull()

    private fun sanitizeIntent(intent: Intent): Intent =
        intent.apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            // Never honor a forced component from a web-sourced Intent URL.
            component = null
            selector = null
            // Drop URI permission grants from untrusted sources.
            flags = flags and (
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
                ).inv()
        }

    private fun canResolve(context: Context, intent: Intent): Boolean =
        runCatching {
            @Suppress("DEPRECATION")
            intent.resolveActivity(context.packageManager) != null
        }.getOrDefault(false)

    private fun prepareForLaunch(context: Context, intent: Intent) {
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    private fun startActivitySafely(context: Context, intent: Intent): Boolean =
        try {
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: SecurityException) {
            logDebug("SecurityException launching ${intent.data}")
            false
        } catch (t: Throwable) {
            logDebug("startActivity failed: ${t.message}")
            false
        }

    /**
     * `intent:` URI fallback: `S.browser_fallback_url` / common extras → plain https VIEW.
     */
    private fun tryFallback(context: Context, original: Uri, failed: Intent): Boolean {
        if (original.scheme?.equals("intent", ignoreCase = true) != true) {
            // Non-intent schemes: last chance Play Store → https://play.google.com
            if (original.scheme.equals("market", ignoreCase = true)) {
                return openPlayStoreHttpsFallback(context, original)
            }
            return false
        }

        val fallback = failed.getStringExtra(EXTRA_BROWSER_FALLBACK_URL)
            ?: failed.getStringExtra(EXTRA_FALLBACK_URL)
            ?: return false

        val fallbackUri = runCatching { fallback.toUri() }.getOrNull() ?: return false
        if (!fallbackUri.scheme.equals("https", ignoreCase = true) &&
            !fallbackUri.scheme.equals("http", ignoreCase = true)
        ) {
            logDebug("Rejected non-http(s) intent fallback: $fallbackUri")
            return false
        }
        if (MidnightOriginPolicy.shouldLoadInsideWebView(fallbackUri)) {
            return false
        }

        val view = sanitizeIntent(Intent(Intent.ACTION_VIEW, fallbackUri))
        prepareForLaunch(context, view)
        if (!canResolve(context, view)) return false
        return startActivitySafely(context, view)
    }

    private fun openPlayStoreHttpsFallback(context: Context, marketUri: Uri): Boolean {
        // market://details?id=com.example → https://play.google.com/store/apps/details?id=…
        val id = marketUri.getQueryParameter("id") ?: return false
        val https = "https://play.google.com/store/apps/details?id=$id".toUri()
        val view = sanitizeIntent(Intent(Intent.ACTION_VIEW, https))
        prepareForLaunch(context, view)
        if (!canResolve(context, view)) return false
        return startActivitySafely(context, view)
    }

    private fun logDebug(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message)
        }
    }

    private const val EXTRA_BROWSER_FALLBACK_URL = "browser_fallback_url"
    private const val EXTRA_FALLBACK_URL = "fallback_url"
    private const val MAX_INTENT_URI_LENGTH = 8_192
}

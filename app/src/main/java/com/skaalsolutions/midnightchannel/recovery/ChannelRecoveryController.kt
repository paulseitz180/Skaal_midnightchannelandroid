package com.skaalsolutions.midnightchannel.recovery

import android.webkit.WebView
import com.skaalsolutions.midnightchannel.connectivity.ConnectivityStatus
import com.skaalsolutions.midnightchannel.connectivity.isRetryAvailable
import com.skaalsolutions.midnightchannel.navigation.ShellEvent
import com.skaalsolutions.midnightchannel.navigation.ShellState
import com.skaalsolutions.midnightchannel.webview.ChannelDestination
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.delay

/**
 * Single-flight Retry / recovery workflow (Grande Document Offline blueprint).
 *
 * Guarantees:
 * - Connectivity is verified before entering Retrying and again before reload
 * - At most one reload per user Retry press
 * - Overlapping Retry presses are ignored while recovery is in flight
 * - No auto-retry loops — only explicit user Retry starts recovery
 * - Hung loads time out back to Offline ([RETRY_LOAD_TIMEOUT_MS])
 */
class ChannelRecoveryController(
    private val dispatch: (ShellEvent) -> Unit,
    private val connectivitySnapshot: () -> ConnectivityStatus,
    private val currentState: () -> ShellState,
) {
    private val inFlight = AtomicBoolean(false)
    private val session = AtomicInteger(0)
    private val reloadIssued = AtomicBoolean(false)

    val isInFlight: Boolean
        get() = inFlight.get()

    /**
     * Accepts a user Retry only from Offline + validated network + not in flight.
     * @return true when [ShellEvent.RetryRequested] was dispatched.
     */
    fun requestRetry(): Boolean {
        if (!inFlight.compareAndSet(false, true)) return false
        if (currentState() !is ShellState.Offline) {
            inFlight.set(false)
            return false
        }
        if (!connectivitySnapshot().isRetryAvailable) {
            inFlight.set(false)
            return false
        }
        reloadIssued.set(false)
        session.incrementAndGet()
        dispatch(ShellEvent.RetryRequested)
        return true
    }

    /**
     * Runs after shell enters [ShellState.Retrying]:
     * label swap → connectivity re-check → one reload → Loading.
     */
    suspend fun awaitAndStartReload(webView: WebView?, labelSwapMs: Int) {
        val activeSession = session.get()
        delay(labelSwapMs.coerceAtLeast(0).toLong())

        if (session.get() != activeSession) return
        if (currentState() !is ShellState.Retrying) {
            finishFlight()
            return
        }

        // Re-verify connectivity after the RECONNECTING… label is shown.
        if (!connectivitySnapshot().isRetryAvailable) {
            dispatch(ShellEvent.RetryFailed)
            finishFlight()
            return
        }

        if (webView == null) {
            dispatch(ShellEvent.RetryFailed)
            finishFlight()
            return
        }

        if (!reloadIssued.compareAndSet(false, true)) {
            // Duplicate reload blocked for this session.
            return
        }

        dispatch(ShellEvent.RetryLoadStarted)
        val started = runCatching {
            webView.stopLoading()
            webView.loadUrl(ChannelDestination.HOME_URL)
        }.isSuccess

        if (!started) {
            dispatch(ShellEvent.RetryFailed)
            finishFlight()
        }
    }

    /**
     * Watching [ShellState.Loading] after a retry reload — abort hung attempts.
     */
    suspend fun awaitLoadOutcomeOrTimeout(
        webView: WebView?,
        timeoutMs: Long = RETRY_LOAD_TIMEOUT_MS,
    ) {
        if (!inFlight.get()) return
        val activeSession = session.get()
        delay(timeoutMs)
        if (session.get() != activeSession) return
        if (!inFlight.get()) return

        when (currentState()) {
            ShellState.Loading,
            ShellState.Retrying,
            -> {
                webView?.let { view -> runCatching { view.stopLoading() } }
                dispatch(ShellEvent.RetryFailed)
                finishFlight()
            }
            else -> finishFlight()
        }
    }

    /** Successful same-origin main-frame finish ends the flight. */
    fun onChannelReady() {
        if (!inFlight.get()) return
        finishFlight()
    }

    /** Failure ends the flight; reducer owns Offline surface. */
    fun onChannelFailed() {
        if (!inFlight.get()) return
        finishFlight()
    }

    fun cancel() {
        session.incrementAndGet()
        reloadIssued.set(false)
        finishFlight()
    }

    private fun finishFlight() {
        inFlight.set(false)
    }

    companion object {
        /** Soft ceiling so Retry cannot leave the user on a stuck Loading surface. */
        const val RETRY_LOAD_TIMEOUT_MS: Long = 30_000L
    }
}

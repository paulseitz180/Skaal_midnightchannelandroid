package com.skaalsolutions.midnightchannel.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Handler
import android.os.Looper
import androidx.core.content.getSystemService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Production connectivity monitor using [ConnectivityManager.NetworkCallback].
 *
 * - No polling
 * - Callback-driven only while [start]ed (bind to Activity STARTED)
 * - Publishes [ConnectivityStatus] for shell integration / Retry availability
 */
class ConnectivityMonitor(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val connectivityManager =
        requireNotNull(appContext.getSystemService<ConnectivityManager>()) {
            "ConnectivityManager required"
        }

    private val mainHandler = Handler(Looper.getMainLooper())

    private val _status = MutableStateFlow(probeActiveNetwork())
    val status: StateFlow<ConnectivityStatus> = _status.asStateFlow()

    val isRetryAvailable: Boolean
        get() = _status.value.isRetryAvailable

    @Volatile
    private var started: Boolean = false

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            publish(probeActiveNetwork())
        }

        override fun onLost(network: Network) {
            publish(probeActiveNetwork())
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities,
        ) {
            publish(statusFrom(networkCapabilities))
        }

        override fun onUnavailable() {
            publish(ConnectivityStatus.Unavailable)
        }
    }

    /**
     * Begin receiving system connectivity callbacks. Idempotent.
     * Call from a foreground lifecycle scope (e.g. STARTED); pair with [stop].
     */
    fun start() {
        if (started) return
        started = true
        publish(probeActiveNetwork())
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        runCatching {
            connectivityManager.registerNetworkCallback(request, networkCallback, mainHandler)
        }.onFailure {
            // Older / restricted devices — fall back to default-network callback.
            runCatching {
                connectivityManager.registerDefaultNetworkCallback(networkCallback, mainHandler)
            }
        }
    }

    /**
     * Unregister callbacks — stops all connectivity work while the UI is stopped.
     */
    fun stop() {
        if (!started) return
        started = false
        runCatching { connectivityManager.unregisterNetworkCallback(networkCallback) }
    }

    /** Synchronous snapshot (launch-time offline detection). */
    fun currentStatus(): ConnectivityStatus = probeActiveNetwork()

    private fun publish(status: ConnectivityStatus) {
        // Skip identical emissions — onCapabilitiesChanged can fire frequently.
        if (_status.value == status) return
        _status.value = status
    }

    private fun probeActiveNetwork(): ConnectivityStatus {
        val active = connectivityManager.activeNetwork ?: return ConnectivityStatus.Unavailable
        val caps = connectivityManager.getNetworkCapabilities(active)
            ?: return ConnectivityStatus.Unavailable
        return statusFrom(caps)
    }

    private fun statusFrom(capabilities: NetworkCapabilities): ConnectivityStatus {
        val hasInternet =
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val validated =
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        return if (hasInternet && validated) {
            ConnectivityStatus.Available
        } else {
            ConnectivityStatus.Unavailable
        }
    }
}

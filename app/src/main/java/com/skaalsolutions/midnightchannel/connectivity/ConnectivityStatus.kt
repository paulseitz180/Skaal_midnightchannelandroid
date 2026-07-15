package com.skaalsolutions.midnightchannel.connectivity

/**
 * Validated network reachability for the Midnight Channel shell.
 *
 * [Available] means the default network reports INTERNET + VALIDATED
 * (usable for loading midnightchannel.live). [Unavailable] covers airplane
 * mode, captive portals without validation, and total loss.
 */
sealed interface ConnectivityStatus {
    data object Available : ConnectivityStatus
    data object Unavailable : ConnectivityStatus
}

/** Retry CTA may proceed only when a validated network is present. */
val ConnectivityStatus.isRetryAvailable: Boolean
    get() = this is ConnectivityStatus.Available

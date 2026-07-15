package com.skaalsolutions.midnightchannel.testing

/**
 * Controllable clock for pure timing helpers (e.g. [com.skaalsolutions.midnightchannel.util.DoubleBackExitGate]).
 *
 * JVM unit tests advance time without sleeping or using wall-clock.
 */
class ControllableClock(
    initialMs: Long = 0L,
) {
    @Volatile
    var nowMs: Long = initialMs
        private set

    fun advanceBy(deltaMs: Long) {
        require(deltaMs >= 0L) { "deltaMs must be >= 0" }
        nowMs += deltaMs
    }

    fun set(nowMs: Long) {
        require(nowMs >= 0L) { "nowMs must be >= 0" }
        this.nowMs = nowMs
    }

    val asProvider: () -> Long = { nowMs }
}

package com.skaalsolutions.midnightchannel.testing

/**
 * Naming conventions for instrumentation / Compose suites — mirrors
 * [com.skaalsolutions.midnightchannel.testing.TestConventions] on the JVM side.
 *
 * - Class: `{Screen}ComposeTest` or `{Type}InstrumentedTest`
 * - Method: backtick requirement sentences
 * - Runner: AndroidJUnit4 (set on each suite when cases are added)
 */
object AndroidTestConventions {
    const val INSTRUMENTATION_RUNNER: String =
        "androidx.test.runner.AndroidJUnitRunner"
}

package com.skaalsolutions.midnightchannel.testing

/**
 * Naming conventions for instrumentation / Compose suites — mirrors
 * [com.skaalsolutions.midnightchannel.testing.TestConventions] on the JVM side.
 *
 * - Class: `{Screen}ComposeTest` or `{Surface}InstrumentedTest`
 * - Method: backtick requirement sentences
 * - Test tags: import [com.skaalsolutions.midnightchannel.ui.testing.ShellUiTestTags]
 * - Runner: AndroidJUnit4 (set on each suite)
 */
object AndroidTestConventions {
    const val INSTRUMENTATION_RUNNER: String =
        "androidx.test.runner.AndroidJUnitRunner"
}

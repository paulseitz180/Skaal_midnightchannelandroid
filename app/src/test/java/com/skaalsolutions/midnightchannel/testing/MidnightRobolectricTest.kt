package com.skaalsolutions.midnightchannel.testing

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Base for JVM unit tests that touch Android types (`Uri`, Resources, etc.).
 *
 * Pure Kotlin tests (e.g. [ShellReducer] with no Android APIs) may omit this
 * and use plain JUnit 4. Tests using [OriginTestFixtures] should extend this
 * or apply the same annotations.
 *
 * sdk = 26 matches app minSdk.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26], manifest = Config.NONE)
abstract class MidnightRobolectricTest

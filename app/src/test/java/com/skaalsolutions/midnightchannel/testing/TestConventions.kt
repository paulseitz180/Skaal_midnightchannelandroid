package com.skaalsolutions.midnightchannel.testing

/**
 * Naming and packaging conventions for Midnight Channel tests (TASK 30).
 *
 * ## Source sets
 *
 * | Source set | Runner | Purpose |
 * |------------|--------|---------|
 * | `src/test` | JVM (JUnit 4) | Pure logic — no Android framework / WebView |
 * | `src/androidTest` | Instrumentation | Device/emulator, Compose UI, Activity |
 *
 * ## Class names
 *
 * - Unit: `{TypeUnderTest}Test` — e.g. `ShellReducerTest`, `MidnightOriginPolicyTest`
 * - Instrumentation: `{Surface}InstrumentedTest` — e.g. `MainActivityInstrumentedTest`
 * - Compose UI: `{Screen}ComposeTest` — e.g. `OfflineErrorScreenComposeTest`
 *
 * ## Method names
 *
 * Use backtick descriptive names that read as requirements:
 * ```
 * fun `splash advances only when floor and main-frame finish`()
 * fun `same origin rejects http scheme`()
 * ```
 *
 * Pattern: `{condition}_{expectedOutcome}` is also acceptable for shorter cases.
 *
 * ## Package layout (mirrors production)
 *
 * ```
 * test/.../midnightchannel/
 *   testing/          shared JVM utilities
 *   navigation/       ShellReducer, ShellStore
 *   util/             DoubleBackExitGate, ExternalLinkNavigator (pure)
 *   webview/          MidnightOriginPolicy, ChannelDestination
 *   connectivity/     ConnectivityStatus + ConnectivityShellBridge
 *   a11y/             ShellAccessibility colour helpers
 *   theme/            CrtMotion / palette regression locks
 *   regression/       Cross-TASK integration scenarios (TASK 35)
 *
 * androidTest/.../midnightchannel/
 *   testing/          Compose rules + UI fixtures
 *   ui/               Splash / Offline / Shell Compose tests
 * ```
 *
 * ## What not to test here
 *
 * - Site DJ / tap-to-unmute / CRT inside WebView (Orchestrator + web assignment)
 * - Play Store / Codemagic signing
 * - Live NetworkCallback airplane mode / TalkBack service / Activity.finish (device QA)
 */
object TestConventions {
    const val UNIT_SOURCE_SET: String = "test"
    const val ANDROID_SOURCE_SET: String = "androidTest"
}

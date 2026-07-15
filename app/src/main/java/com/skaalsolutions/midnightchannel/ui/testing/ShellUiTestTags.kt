package com.skaalsolutions.midnightchannel.ui.testing

/**
 * Compose test tags for native shell surfaces.
 * Prefixed `mc_` to avoid collisions with site WebView DOM ids.
 */
object ShellUiTestTags {
    const val SPLASH_ROOT: String = "mc_splash_root"
    const val TITLE_ROOT: String = "mc_title_root"
    const val OFFLINE_ROOT: String = "mc_offline_root"
    const val OFFLINE_RETRY: String = "mc_offline_retry"
    const val CHANNEL_HOST: String = "mc_channel_host"
}

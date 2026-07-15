package com.skaalsolutions.midnightchannel.ui.shell

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.skaalsolutions.midnightchannel.R
import com.skaalsolutions.midnightchannel.ui.a11y.announceForAccessibility
import com.skaalsolutions.midnightchannel.ui.theme.MidnightTheme
import com.skaalsolutions.midnightchannel.util.BackNavigationResult
import com.skaalsolutions.midnightchannel.util.ShellBackNavigator
import com.skaalsolutions.midnightchannel.util.WebViewHistoryBackHandler

/**
 * System back for Midnight Channel — toast confirmation, never a modal dialog.
 *
 * TalkBack: first press announces the confirmation string (Toast alone is unreliable).
 */
@Composable
fun ShellBackHandler(
    enabled: Boolean,
    confirmationWindowMs: Int = MidnightTheme.motion.exitConfirmationWindowMs,
    historyBackHandler: WebViewHistoryBackHandler = WebViewHistoryBackHandler.Disabled,
    confirmationMessage: String = stringResource(R.string.press_back_again_to_exit),
    onExit: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val navigator = remember(confirmationWindowMs, historyBackHandler) {
        ShellBackNavigator(
            confirmationWindowMs = confirmationWindowMs.toLong(),
            historyBackHandler = historyBackHandler,
        )
    }

    BackHandler(enabled = enabled) {
        when (navigator.onBackPressed()) {
            null -> Unit
            BackNavigationResult.ShowExitToast -> {
                context.announceForAccessibility(confirmationMessage)
                Toast.makeText(context, confirmationMessage, Toast.LENGTH_SHORT).show()
            }
            BackNavigationResult.ExitApp -> {
                onExit?.invoke()
                    ?: (context as? Activity)?.finish()
            }
        }
    }
}

package com.apuntia.app.ui.vault

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.apuntia.app.R
import com.apuntia.app.security.PinRules

private const val SECONDS_PER_MINUTE = 60
private const val MILLIS_PER_SECOND = 1_000L

/** Texto del aviso [message]; [hasTarget] indica si se intenta abrir una nota concreta. */
@Composable
internal fun messageText(message: VaultMessage, hasTarget: Boolean): String = when (message) {
    VaultMessage.InvalidPin ->
        stringResource(R.string.vault_error_invalid, PinRules.MIN_LENGTH, PinRules.MAX_LENGTH)
    VaultMessage.PinsDontMatch -> stringResource(R.string.vault_error_mismatch)
    is VaultMessage.WrongPin -> stringResource(wrongPinRes(message.willWipe, hasTarget), message.attemptsLeft)
    is VaultMessage.LockedOut ->
        stringResource(R.string.vault_locked_out, formatWait(message.remainingMillis / MILLIS_PER_SECOND))
    VaultMessage.Failed -> stringResource(R.string.vault_error_failed)
}

private fun wrongPinRes(willWipe: Boolean, hasTarget: Boolean): Int = when {
    !willWipe -> R.string.vault_error_wrong
    hasTarget -> R.string.vault_error_wrong_note
    else -> R.string.vault_error_wrong_all
}

// Segundos a "m:ss", por ejemplo 75 -> "1:15".
internal fun formatWait(seconds: Long): String =
    "%d:%02d".format(seconds / SECONDS_PER_MINUTE, seconds % SECONDS_PER_MINUTE)

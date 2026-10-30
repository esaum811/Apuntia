package com.apuntia.app.ui.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicSecureTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apuntia.app.R
import com.apuntia.app.security.PinRules
import com.apuntia.app.security.VaultState
import com.apuntia.app.ui.AppViewModelProvider
import com.apuntia.app.ui.common.SecureWindow
import com.apuntia.app.ui.components.ApuntiaIcons
import com.apuntia.app.ui.components.IconSquareButton
import com.apuntia.app.ui.components.PrimaryButton
import com.apuntia.app.ui.theme.ApuntiaTheme
import kotlinx.coroutines.delay

private const val TICK_MS = 1_000L
private const val SECONDS_PER_MINUTE = 60

// Solo deja escribir números del 0 al 9 y como máximo 8.
private val PinInput = InputTransformation {
    val text = asCharSequence()
    if (text.length > PinRules.MAX_LENGTH || !text.all { it in '0'..'9' }) revertAllChanges()
}

/**
 * Pantalla completa para crear el PIN (la primera vez) o desbloquear las notas confidenciales.
 * Se dibuja encima de lo que haya y no deja hacer capturas de pantalla.
 *
 * @param onCancel se llama al tocar el botón de volver.
 */
@Composable
fun VaultGate(onCancel: () -> Unit, viewModel: VaultViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    SecureWindow()
    val vaultState by viewModel.vaultState.collectAsStateWithLifecycle()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val creating = vaultState == VaultState.NO_PIN
    val waitSeconds by produceState(0L, state.lockedUntil) {
        while (true) {
            value = ((state.lockedUntil - System.currentTimeMillis()) / TICK_MS).coerceAtLeast(0L)
            if (value == 0L) break
            delay(TICK_MS)
        }
    }
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        IconSquareButton(ApuntiaIcons.back, stringResource(R.string.editor_back), onCancel)
        Text(
            stringResource(if (creating) R.string.vault_create_title else R.string.vault_unlock_title),
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (creating) WarningCard()
        state.finalAttemptsLeft?.let { WipeWarning(it) }
        PinField(stringResource(R.string.vault_pin_label), viewModel.pinState, autoFocus = true)
        if (creating) PinField(stringResource(R.string.vault_confirm_label), viewModel.confirmState)
        Message(state.message, waitSeconds)
        PrimaryButton(
            stringResource(if (creating) R.string.vault_create_action else R.string.vault_unlock_action),
            onClick = viewModel::submit,
        )
    }
}

@Composable
private fun WarningCard() {
    val colors = ApuntiaTheme.colors
    Column(
        Modifier.fillMaxWidth().background(colors.dock, RoundedCornerShape(22.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            stringResource(R.string.vault_warning_title),
            style = MaterialTheme.typography.labelLarge,
            color = colors.dockSelected,
        )
        Text(
            stringResource(R.string.vault_warning_message),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.dockMuted,
        )
    }
}

@Composable
private fun WipeWarning(attemptsLeft: Int) {
    Text(
        stringResource(R.string.vault_wipe_warning, attemptsLeft),
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.error, RoundedCornerShape(22.dp))
            .padding(16.dp),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onError,
    )
}

@Composable
private fun PinField(label: String, state: TextFieldState, autoFocus: Boolean = false) {
    val scheme = MaterialTheme.colorScheme
    val focus = remember { FocusRequester() }
    if (autoFocus) LaunchedEffect(Unit) { focus.requestFocus() }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
        BasicSecureTextField(
            state = state,
            inputTransformation = PinInput,
            textStyle = MaterialTheme.typography.titleMedium.copy(color = scheme.onBackground),
            cursorBrush = SolidColor(scheme.onBackground),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 60.dp)
                .background(scheme.surfaceVariant, RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .focusRequester(focus)
                .semantics { contentDescription = label },
        )
    }
}

@Composable
private fun Message(message: VaultMessage?, waitSeconds: Long) {
    val text = when {
        waitSeconds > 0 -> stringResource(R.string.vault_locked_out, formatWait(waitSeconds))
        message == null -> return
        else -> messageText(message)
    }
    Row(Modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun messageText(message: VaultMessage): String = when (message) {
    VaultMessage.InvalidPin ->
        stringResource(R.string.vault_error_invalid, PinRules.MIN_LENGTH, PinRules.MAX_LENGTH)
    VaultMessage.PinsDontMatch -> stringResource(R.string.vault_error_mismatch)
    is VaultMessage.WrongPin -> stringResource(
        if (message.willWipe) R.string.vault_error_wrong_final else R.string.vault_error_wrong,
        message.attemptsLeft,
    )
    VaultMessage.Failed -> stringResource(R.string.vault_error_failed)
}

// Segundos a "m:ss", por ejemplo 75 -> "1:15".
private fun formatWait(seconds: Long): String =
    "%d:%02d".format(seconds / SECONDS_PER_MINUTE, seconds % SECONDS_PER_MINUTE)

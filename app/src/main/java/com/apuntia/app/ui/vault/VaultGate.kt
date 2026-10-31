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
import com.apuntia.app.ui.components.SecondaryButton
import com.apuntia.app.ui.common.findActivity
import androidx.fragment.app.FragmentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
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

/**
 * Pantalla completa para crear el PIN (la primera vez) o desbloquear las notas confidenciales.
 * Se dibuja encima de lo que haya y no deja hacer capturas de pantalla.
 *
 * @param targetId nota que se quiere abrir, o nulo si no es para abrir una nota concreta.
 * @param onCancel se llama al tocar el botón de volver.
 * @param onWiped se llama cuando se agotan los intentos y se borra contenido; recibe verdadero si fue todo.
 */
@Composable
fun VaultGate(
    targetId: String?,
    onCancel: () -> Unit,
    onWiped: (everything: Boolean) -> Unit,
    viewModel: VaultViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    SecureWindow()
    val vaultState by viewModel.vaultState.collectAsStateWithLifecycle()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val creating = vaultState == VaultState.NO_PIN
    LaunchedEffect(state.wipedEverything) { state.wipedEverything?.let(onWiped) }
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
        state.finalAttemptsLeft?.let { WipeWarning(it, targetId != null) }
        PinField(stringResource(R.string.vault_pin_label), viewModel.pinState, autoFocus = true)
        if (creating) PinField(stringResource(R.string.vault_confirm_label), viewModel.confirmState)
        Message(state.message, waitSeconds, targetId != null)
        PrimaryButton(
            stringResource(if (creating) R.string.vault_create_action else R.string.vault_unlock_action),
            onClick = { viewModel.submit(targetId) },
        )
        if (!creating && state.hasBiometric && isBiometricAvailable(LocalContext.current)) {
            BiometricButton(viewModel)
        }
    }
}

// Ofrece la huella: se muestra sola al entrar, y el botón permite repetirla si se cancela.
@Composable
private fun BiometricButton(viewModel: VaultViewModel) {
    val activity = LocalContext.current.findActivity() as? FragmentActivity ?: return
    val title = stringResource(R.string.vault_biometric_title)
    val negative = stringResource(R.string.vault_biometric_negative)
    val prompt = {
        viewModel.prepareBiometricCipher()?.let { cipher ->
            authenticateWithBiometric(activity, cipher, title, negative, onSuccess = viewModel::unlockWithBiometric)
        }
        Unit
    }
    var prompted by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!prompted) {
            prompted = true
            prompt()
        }
    }
    SecondaryButton(stringResource(R.string.vault_biometric_action), onClick = prompt)
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
private fun WipeWarning(attemptsLeft: Int, hasTarget: Boolean) {
    Text(
        stringResource(
            if (hasTarget) R.string.vault_wipe_warning_note else R.string.vault_wipe_warning_all,
            attemptsLeft,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.error, RoundedCornerShape(22.dp))
            .padding(16.dp),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onError,
    )
}

@Composable
private fun Message(message: VaultMessage?, waitSeconds: Long, hasTarget: Boolean) {
    val text = when {
        waitSeconds > 0 -> stringResource(R.string.vault_locked_out, formatWait(waitSeconds))
        message == null -> return
        else -> messageText(message, hasTarget)
    }
    Row(Modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
    }
}

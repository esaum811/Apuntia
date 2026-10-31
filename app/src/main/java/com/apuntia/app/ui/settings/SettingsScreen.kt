package com.apuntia.app.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apuntia.app.R
import com.apuntia.app.security.VaultState
import com.apuntia.app.ui.AppViewModelProvider
import com.apuntia.app.ui.common.SecureWindow
import com.apuntia.app.ui.common.findActivity
import com.apuntia.app.ui.components.ApuntiaSwitch
import com.apuntia.app.ui.vault.PinField
import com.apuntia.app.ui.vault.VaultGate
import com.apuntia.app.ui.vault.authenticateWithBiometric
import com.apuntia.app.ui.vault.isBiometricAvailable
import com.apuntia.app.ui.vault.messageText

// Espacio al final de la lista para que la barra inferior no tape la última opción.
private val DockClearance = 130.dp

/** Pantalla de Ajustes. Por ahora tiene la sección de seguridad: huella y cambio de PIN. */
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val vaultState by viewModel.vaultState.collectAsStateWithLifecycle()
    val vaultOpen = vaultState == VaultState.UNLOCKED
    var enableRequested by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }
    EnrollEffect(vaultOpen, enableRequested, viewModel) { enableRequested = false }
    LaunchedEffect(state.wipedEverything) {
        if (state.wipedEverything) {
            Toast.makeText(context, R.string.vault_wiped_all_toast, Toast.LENGTH_LONG).show()
        }
    }
    Box(Modifier.fillMaxSize()) {
        SettingsList(state, viewModel) { enableRequested = true }
        if (enableRequested && !vaultOpen) {
            // Para activar la huella primero hay que demostrar que se conoce el PIN.
            VaultGate(targetId = null, onCancel = { enableRequested = false }, onWiped = { enableRequested = false })
        }
        state.changePin?.let { ChangePinDialog(it, viewModel) }
    }
}

// Con la bóveda abierta por el PIN, pide la huella para crear la llave y activarla.
@Composable
private fun EnrollEffect(vaultOpen: Boolean, requested: Boolean, viewModel: SettingsViewModel, onStarted: () -> Unit) {
    val activity = LocalContext.current.findActivity() as? FragmentActivity
    val title = stringResource(R.string.settings_biometric_enroll_title)
    val cancel = stringResource(R.string.dialog_cancel)
    LaunchedEffect(vaultOpen, requested) {
        if (vaultOpen && requested && activity != null) {
            onStarted()
            val cipher = viewModel.prepareEnrollCipher()
            if (cipher == null) {
                viewModel.lockVault()
            } else {
                authenticateWithBiometric(
                    activity,
                    cipher,
                    title,
                    cancel,
                    onError = viewModel::lockVault,
                    onSuccess = viewModel::completeEnableBiometric,
                )
            }
        }
    }
}

@Composable
private fun SettingsList(state: SettingsUiState, viewModel: SettingsViewModel, onEnableBiometric: () -> Unit) {
    val navigationBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = 8.dp,
            end = 20.dp,
            bottom = DockClearance + navigationBarHeight,
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(key = "title") {
            Text(
                stringResource(R.string.nav_settings),
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        item(key = "security-title") { SectionTitle(stringResource(R.string.settings_security)) }
        if (!state.hasPin) {
            item(key = "no-pin") { InfoCard(stringResource(R.string.settings_no_pin)) }
        } else {
            item(key = "biometric") { BiometricRow(state.biometricEnabled, viewModel, onEnableBiometric) }
            item(key = "change-pin") {
                ActionRow(stringResource(R.string.settings_change_pin), viewModel::openChangePin)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        modifier = Modifier.padding(top = 8.dp).semantics { heading() },
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
private fun InfoCard(text: String) {
    Text(
        text,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(22.dp))
            .padding(16.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ActionRow(title: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 64.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(22.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun BiometricRow(enabled: Boolean, viewModel: SettingsViewModel, onEnable: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val available = isBiometricAvailable(LocalContext.current)
    val title = stringResource(R.string.settings_biometric)
    Row(
        Modifier
            .fillMaxWidth()
            .background(scheme.surfaceVariant, RoundedCornerShape(22.dp))
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = scheme.onBackground)
            val hint = if (available) R.string.settings_biometric_hint else R.string.settings_biometric_missing
            Text(
                stringResource(hint),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
            )
        }
        if (available || enabled) {
            ApuntiaSwitch(
                checked = enabled,
                onCheckedChange = { if (it) onEnable() else viewModel.disableBiometric() },
                description = title,
            )
        }
    }
}

@Composable
private fun ChangePinDialog(state: ChangePinState, viewModel: SettingsViewModel) {
    SecureWindow()
    AlertDialog(
        onDismissRequest = viewModel::closeChangePin,
        title = { Text(stringResource(R.string.settings_change_pin)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PinField(stringResource(R.string.settings_pin_current), viewModel.currentPinState, autoFocus = true)
                PinField(stringResource(R.string.settings_pin_new), viewModel.newPinState)
                PinField(stringResource(R.string.vault_confirm_label), viewModel.confirmPinState)
                state.message?.let {
                    Text(messageText(it, hasTarget = false), color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = viewModel::changePin, enabled = !state.isWorking) {
                Text(stringResource(R.string.settings_change_pin_action))
            }
        },
        dismissButton = {
            TextButton(onClick = viewModel::closeChangePin) { Text(stringResource(R.string.dialog_cancel)) }
        },
    )
}

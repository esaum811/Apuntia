package com.apuntia.app.ui.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import com.apuntia.app.ui.vault.PinField
import com.apuntia.app.ui.vault.VaultGate
import com.apuntia.app.ui.vault.authenticateWithBiometric
import com.apuntia.app.ui.vault.messageText

// Espacio al final de la lista para que la barra inferior no tape la última opción.
private val DockClearance = 130.dp

/**
 * Pantalla de Ajustes: seguridad, alarmas, apariencia y datos.
 *
 * @param onOpenTrash abre la papelera.
 */
@Composable
fun SettingsScreen(
    onOpenTrash: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val vaultState by viewModel.vaultState.collectAsStateWithLifecycle()
    val vaultOpen = vaultState == VaultState.UNLOCKED
    var enableRequested by rememberSaveable { mutableStateOf(false) }
    var dialog by rememberSaveable { mutableStateOf<SettingsDialog?>(null) }
    // Sube cada vez que se vuelve a la pantalla, para releer los permisos que el usuario acaba de conceder.
    var resumes by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refresh()
        resumes++
    }
    EnrollEffect(vaultOpen, enableRequested, viewModel) { enableRequested = false }
    LaunchedEffect(state.wipedEverything) {
        if (state.wipedEverything) {
            Toast.makeText(context, R.string.vault_wiped_all_toast, Toast.LENGTH_LONG).show()
        }
    }
    val permissionsOk = remember(resumes) { alarmPermissionsGranted(context) }
    Box(Modifier.fillMaxSize()) {
        SettingsList(viewModel, state, permissionsOk, onOpenTrash) { dialog = it }
        if (enableRequested && !vaultOpen) {
            // Para activar la huella primero hay que demostrar que se conoce el PIN.
            VaultGate(targetId = null, onCancel = { enableRequested = false }, onWiped = { enableRequested = false })
        }
        SettingsDialogs(dialog, viewModel, state, onEnableBiometric = { enableRequested = true }) { dialog = null }
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
private fun SettingsList(
    viewModel: SettingsViewModel,
    state: SettingsUiState,
    permissionsOk: Boolean,
    onOpenTrash: () -> Unit,
    onDialog: (SettingsDialog) -> Unit,
) {
    val context = LocalContext.current
    val navigationBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val cleanup by viewModel.doneCleanup.collectAsStateWithLifecycle()
    val ringtone by viewModel.defaultRingtone.collectAsStateWithLifecycle()
    val trashCount by viewModel.trashCount.collectAsStateWithLifecycle()
    val systemSound = stringResource(R.string.prefs_system)
    val themeLabel = theme.label()
    val cleanupLabel = cleanup.label()
    val soundName = remember(ringtone) { ringtoneTitle(context, ringtone) } ?: systemSound
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
        securitySection(state, viewModel::openChangePin, onDialog)
        alarmsSection(soundName, permissionsOk, onDialog)
        appearanceSection(themeLabel, onDialog)
        dataSection(trashCount, cleanupLabel, onOpenTrash, onDialog)
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

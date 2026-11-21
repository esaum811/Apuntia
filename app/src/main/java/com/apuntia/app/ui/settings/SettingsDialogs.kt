@file:Suppress("MatchingDeclarationName")

package com.apuntia.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apuntia.app.R
import com.apuntia.app.alarm.ScreenPermission
import com.apuntia.app.settings.AutoLock
import com.apuntia.app.settings.DoneCleanup
import com.apuntia.app.settings.ThemeMode
import com.apuntia.app.ui.components.ApuntiaSwitch
import com.apuntia.app.ui.vault.isBiometricAvailable

// Diálogos que se abren desde las filas de Ajustes.
internal enum class SettingsDialog { BIOMETRIC, AUTO_LOCK, SOUND, PERMISSIONS, THEME, LANGUAGE, CLEANUP }

@Composable
internal fun SettingsDialogs(
    dialog: SettingsDialog?,
    viewModel: SettingsViewModel,
    state: SettingsUiState,
    onEnableBiometric: () -> Unit,
    onClose: () -> Unit,
) {
    when (dialog) {
        SettingsDialog.BIOMETRIC -> BiometricDialog(state, viewModel, onEnableBiometric, onClose)
        SettingsDialog.AUTO_LOCK -> OptionsDialog(
            title = stringResource(R.string.prefs_auto_lock),
            options = AutoLock.entries,
            selected = viewModel.autoLock.collectAsStateWithLifecycle().value,
            label = { it.label() },
            message = stringResource(R.string.prefs_auto_lock_hint),
            onPick = viewModel::setAutoLock,
            onClose = onClose,
        )
        SettingsDialog.THEME -> OptionsDialog(
            title = stringResource(R.string.prefs_theme),
            options = ThemeMode.entries,
            selected = viewModel.theme.collectAsStateWithLifecycle().value,
            label = { it.label() },
            onPick = viewModel::setTheme,
            onClose = onClose,
        )
        SettingsDialog.CLEANUP -> OptionsDialog(
            title = stringResource(R.string.prefs_cleanup),
            options = DoneCleanup.entries,
            selected = viewModel.doneCleanup.collectAsStateWithLifecycle().value,
            label = { it.label() },
            message = stringResource(R.string.settings_cleanup_hint),
            onPick = viewModel::setDoneCleanup,
            onClose = onClose,
        )
        SettingsDialog.SOUND -> SoundDialog(
            current = viewModel.defaultRingtone.collectAsStateWithLifecycle().value,
            onSave = {
                viewModel.setDefaultRingtone(it)
                onClose()
            },
            onDismiss = onClose,
        )
        SettingsDialog.PERMISSIONS -> AlarmPermissionsDialog(onClose)
        SettingsDialog.LANGUAGE -> ChoiceDialog(
            title = stringResource(R.string.prefs_language),
            options = listOf(stringResource(R.string.prefs_language_spanish)),
            selectedIndex = 0,
            onSelect = { onClose() },
            onDismiss = onClose,
            message = stringResource(R.string.prefs_language_hint),
        )
        null -> Unit
    }
}

@Composable
private fun <T> OptionsDialog(
    title: String,
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onPick: (T) -> Unit,
    onClose: () -> Unit,
    message: String? = null,
) {
    ChoiceDialog(
        title = title,
        options = options.map { label(it) },
        selectedIndex = options.indexOf(selected),
        onSelect = {
            onPick(options[it])
            onClose()
        },
        onDismiss = onClose,
        message = message,
    )
}

// Huella: se activa o desactiva desde aquí; sin PIN solo explica cómo crearlo.
@Composable
private fun BiometricDialog(
    state: SettingsUiState,
    viewModel: SettingsViewModel,
    onEnable: () -> Unit,
    onClose: () -> Unit,
) {
    val available = isBiometricAvailable(LocalContext.current)
    val title = stringResource(R.string.settings_biometric)
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(stringResource(R.string.prefs_pin_and_biometric)) },
        text = {
            if (!state.hasPin) {
                Text(stringResource(R.string.settings_no_pin), color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(title, style = MaterialTheme.typography.labelLarge)
                        val hint = if (available) {
                            R.string.settings_biometric_hint
                        } else {
                            R.string.settings_biometric_missing
                        }
                        Text(
                            stringResource(hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (available || state.biometricEnabled) {
                        ApuntiaSwitch(
                            checked = state.biometricEnabled,
                            onCheckedChange = {
                                if (it) onEnable() else viewModel.disableBiometric()
                                onClose()
                            },
                            description = title,
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onClose) { Text(stringResource(R.string.prefs_close)) } },
    )
}

@Composable
internal fun AutoLock.label(): String = stringResource(
    when (this) {
        AutoLock.IMMEDIATE -> R.string.prefs_lock_now
        AutoLock.SECONDS_30 -> R.string.prefs_lock_30s
        AutoLock.MINUTE_1 -> R.string.prefs_lock_1m
        AutoLock.MINUTES_2 -> R.string.prefs_lock_2m
        AutoLock.MINUTES_5 -> R.string.prefs_lock_5m
    },
)

@Composable
internal fun ThemeMode.label(): String = stringResource(
    when (this) {
        ThemeMode.SYSTEM -> R.string.prefs_theme_system
        ThemeMode.LIGHT -> R.string.prefs_theme_light
        ThemeMode.DARK -> R.string.prefs_theme_dark
    },
)

@Composable
internal fun DoneCleanup.label(): String = stringResource(
    when (this) {
        DoneCleanup.NEVER -> R.string.settings_cleanup_never
        DoneCleanup.AFTER_7_DAYS -> R.string.settings_cleanup_7
        DoneCleanup.AFTER_30_DAYS -> R.string.settings_cleanup_30
    },
)

// Los dos permisos que hacen aparecer la pantalla de alarma; al tocar uno se abre su pantalla del sistema.
@Composable
private fun AlarmPermissionsDialog(onClose: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(stringResource(R.string.prefs_alarm_permission)) },
        text = {
            Column {
                ScreenPermission.entries.forEach { permission ->
                    val granted = isGranted(context, permission)
                    val label = if (granted) R.string.prefs_allowed else R.string.prefs_missing
                    SettingsRow(
                        title = stringResource(permission.titleRes()),
                        onClick = {
                            onClose()
                            openAlarmPermission(context, permission)
                        },
                        badge = stringResource(label) to granted,
                        showChevron = false,
                        maxLines = 2,
                        horizontalPadding = 0.dp,
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onClose) { Text(stringResource(R.string.prefs_close)) } },
    )
}

private fun ScreenPermission.titleRes(): Int = when (this) {
    ScreenPermission.OVERLAY -> R.string.prefs_perm_overlay
    ScreenPermission.FULL_SCREEN -> R.string.prefs_perm_fullscreen
}

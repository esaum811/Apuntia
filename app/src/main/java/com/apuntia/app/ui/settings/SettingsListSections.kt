package com.apuntia.app.ui.settings

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.apuntia.app.R

internal fun LazyListScope.securitySection(
    state: SettingsUiState,
    onChangePin: () -> Unit,
    onDialog: (SettingsDialog) -> Unit,
) {
    item(key = "security-title") { SectionTitle(stringResource(R.string.settings_security)) }
    item(key = "security") {
        SettingsGroup {
            val title = stringResource(R.string.prefs_pin_and_biometric)
            val openBiometric = { onDialog(SettingsDialog.BIOMETRIC) }
            SettingsRow(title, openBiometric)
            if (state.hasPin) {
                SettingsDivider()
                SettingsRow(stringResource(R.string.settings_change_pin), onChangePin)
            }
            SettingsDivider()
            SettingsRow(stringResource(R.string.prefs_auto_lock), { onDialog(SettingsDialog.AUTO_LOCK) })
        }
    }
}

internal fun LazyListScope.alarmsSection(
    soundName: String,
    permissionsOk: Boolean,
    onDialog: (SettingsDialog) -> Unit,
) {
    item(key = "alarms-title") { SectionTitle(stringResource(R.string.settings_alarms)) }
    item(key = "alarms") {
        SettingsGroup {
            SettingsRow(
                stringResource(R.string.settings_sound_title),
                { onDialog(SettingsDialog.SOUND) },
                value = soundName,
            )
            SettingsDivider()
            val badge = if (permissionsOk) R.string.prefs_allowed to true else R.string.prefs_missing to false
            SettingsRow(
                stringResource(R.string.prefs_alarm_permission),
                { onDialog(SettingsDialog.PERMISSIONS) },
                badge = stringResource(badge.first) to badge.second,
                showChevron = false,
            )
        }
    }
}

internal fun LazyListScope.appearanceSection(themeLabel: String, onDialog: (SettingsDialog) -> Unit) {
    item(key = "appearance-title") { SectionTitle(stringResource(R.string.prefs_appearance)) }
    item(key = "appearance") {
        SettingsGroup {
            SettingsRow(stringResource(R.string.prefs_theme), { onDialog(SettingsDialog.THEME) }, value = themeLabel)
            SettingsDivider()
            SettingsRow(
                stringResource(R.string.prefs_language),
                { onDialog(SettingsDialog.LANGUAGE) },
                value = stringResource(R.string.prefs_language_spanish),
            )
        }
    }
}

internal fun LazyListScope.dataSection(
    trashCount: Int,
    cleanupLabel: String,
    onOpenTrash: () -> Unit,
    onDialog: (SettingsDialog) -> Unit,
) {
    item(key = "data-title") { SectionTitle(stringResource(R.string.prefs_data)) }
    item(key = "data") {
        SettingsGroup {
            val resources = LocalContext.current.resources
            val count = resources.getQuantityString(R.plurals.prefs_trash_count, trashCount, trashCount)
            SettingsRow(stringResource(R.string.prefs_trash), onOpenTrash, value = count)
            SettingsDivider()
            SettingsRow(
                stringResource(R.string.prefs_cleanup),
                { onDialog(SettingsDialog.CLEANUP) },
                value = cleanupLabel,
            )
        }
    }
}

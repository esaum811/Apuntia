package com.apuntia.app.ui.alarms

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apuntia.app.R
import com.apuntia.app.ui.AppViewModelProvider
import com.apuntia.app.ui.common.displayTime
import com.apuntia.app.ui.components.ApuntiaSwitch
import com.apuntia.app.ui.components.CategoryShapeIcon
import com.apuntia.app.ui.theme.ApuntiaTheme
import com.apuntia.app.ui.theme.cardInk

// Espacio al final de la lista para que la barra inferior no tape la última alarma.
private val DockClearance = 130.dp

/**
 * Pantalla de alarmas: lista con interruptor para encender o apagar cada una.
 *
 * @param onOpenAlarm se llama con el id de la alarma tocada.
 */
@Composable
fun AlarmsScreen(
    onOpenAlarm: (String) -> Unit,
    viewModel: AlarmsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    // Al volver de los ajustes del sistema, el permiso pudo haber cambiado.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshPermission() }
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
                stringResource(R.string.alarms_title),
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        if (!state.canScheduleExact) {
            item(key = "permission") { PermissionBanner { openExactAlarmSettings(context) } }
        }
        if (!state.isLoading && state.items.isEmpty()) {
            item(key = "empty") { EmptyAlarms() }
        }
        items(state.items, key = { it.reminder.id }, contentType = { "alarm" }) { item ->
            AlarmCard(item, onOpenAlarm) { viewModel.setEnabled(item.reminder, it) }
        }
    }
}

private fun openExactAlarmSettings(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        // Algunos teléfonos no tienen esa pantalla; la alarma seguirá sin programarse y el aviso se queda.
    }
}

@Composable
private fun PermissionBanner(onAllow: () -> Unit) {
    val colors = ApuntiaTheme.colors
    Row(
        Modifier.fillMaxWidth().background(colors.dock, RoundedCornerShape(22.dp)).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            stringResource(R.string.alarms_permission_message),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.dockMuted,
        )
        Box(
            Modifier
                .defaultMinSize(minHeight = 48.dp)
                .background(colors.accent, RoundedCornerShape(14.dp))
                .clickable(role = Role.Button, onClick = onAllow)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                stringResource(R.string.alarms_permission_action),
                style = MaterialTheme.typography.labelMedium,
                color = colors.onAccent,
            )
        }
    }
}

@Composable
private fun EmptyAlarms() {
    Column(Modifier.fillMaxWidth().padding(top = 32.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(R.string.alarms_empty_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            stringResource(R.string.alarms_empty_hint),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AlarmCard(item: AlarmItem, onOpen: (String) -> Unit, onToggle: (Boolean) -> Unit) {
    val reminder = item.reminder
    val scheme = MaterialTheme.colorScheme
    val enabled = reminder.isEnabled
    val onCard = if (enabled) cardInk else scheme.onBackground
    val time = displayTime(LocalContext.current, reminder.triggerAt)
    val summary = repeatSummary(reminder.repeatRule, reminder.triggerAt)
    val disabledSuffix = stringResource(R.string.alarm_disabled_suffix)
    val name = reminder.label.ifBlank { item.category.name }
    val detail = "$name · $summary${if (enabled) "" else disabledSuffix}"
    val timeText = time.period?.let { "${time.digits} $it" } ?: time.digits
    Row(
        Modifier
            .fillMaxWidth()
            .background(if (enabled) Color(item.category.color) else scheme.surfaceVariant, RoundedCornerShape(26.dp))
            .clickable(role = Role.Button) { onOpen(reminder.id) }
            .padding(start = 18.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(timeText, style = MaterialTheme.typography.displayLarge, color = onCard)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CategoryShapeIcon(item.category.shape, if (enabled) cardInk else Color(item.category.color))
                Text(
                    detail,
                    style = MaterialTheme.typography.labelMedium,
                    color = onCard,
                )
            }
        }
        ApuntiaSwitch(enabled, onToggle, stringResource(R.string.alarm_switch_description, timeText))
    }
}

package com.apuntia.app.ui.alarms

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apuntia.app.R
import com.apuntia.app.domain.model.RepeatRule
import com.apuntia.app.ui.AppViewModelProvider
import com.apuntia.app.ui.common.displayTime
import com.apuntia.app.ui.components.ApuntiaIcons
import com.apuntia.app.ui.components.CategoryChip
import com.apuntia.app.ui.components.IconSquareButton
import com.apuntia.app.ui.theme.ApuntiaTheme

/**
 * Editor para crear o modificar una alarma.
 *
 * @param onClose se llama al volver, guardar o borrar la alarma.
 */
@Composable
fun AlarmEditorScreen(
    onClose: () -> Unit,
    viewModel: AlarmEditorViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.isFinished) { if (state.isFinished) onClose() }
    if (!state.isLoaded) return
    // En Android 13 o más hace falta permiso para mostrar la notificación de la alarma.
    // Se pide al guardar; si lo niegan, la alarma suena igual pero sin botones en la notificación.
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        viewModel.save()
    }
    val context = LocalContext.current
    val onSave = {
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        if (needsPermission) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS) else viewModel.save()
    }
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TopBar(state.isEditing, onBack = onClose, onSave = onSave)
        TimeRow(state.hour, state.minute, viewModel::setTime)
        LabelField(viewModel)
        CategoryPicker(state, viewModel::selectCategory)
        RepeatPicker(state.repeatRule, viewModel::selectRepeat)
        if (state.isEditing) {
            Box(
                Modifier.defaultMinSize(minHeight = 48.dp).clickable(role = Role.Button, onClick = viewModel::delete),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    stringResource(R.string.alarm_delete),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun TopBar(isEditing: Boolean, onBack: () -> Unit, onSave: () -> Unit) {
    val colors = ApuntiaTheme.colors
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconSquareButton(ApuntiaIcons.back, stringResource(R.string.editor_back), onBack)
        Text(
            stringResource(if (isEditing) R.string.alarm_edit_title else R.string.alarm_new_title),
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp).semantics { heading() },
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Box(
            Modifier
                .height(48.dp)
                .background(colors.accent, RoundedCornerShape(16.dp))
                .clickable(role = Role.Button, onClick = onSave)
                .padding(horizontal = 22.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                stringResource(R.string.editor_save),
                style = MaterialTheme.typography.labelMedium,
                color = colors.onAccent,
            )
        }
    }
}

@Composable
private fun TimeRow(hour: Int, minute: Int, onChange: (Int, Int) -> Unit) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val time = displayTime(context, hour, minute)
    Column(
        Modifier
            .fillMaxWidth()
            .background(scheme.surfaceVariant, RoundedCornerShape(22.dp))
            .clickable(role = Role.Button) {
                val is24Hour = DateFormat.is24HourFormat(context)
                TimePickerDialog(context, { _, h, m -> onChange(h, m) }, hour, minute, is24Hour).show()
            }
            .padding(16.dp),
    ) {
        Text(
            stringResource(R.string.alarm_time_label),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
        )
        Text(
            listOfNotNull(time.digits, time.period).joinToString(" "),
            style = MaterialTheme.typography.displayLarge,
            color = scheme.onBackground,
        )
    }
}

@Composable
private fun LabelField(viewModel: AlarmEditorViewModel) {
    val scheme = MaterialTheme.colorScheme
    val label = stringResource(R.string.alarm_label_label)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
        BasicTextField(
            state = viewModel.labelState,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = scheme.onBackground),
            cursorBrush = SolidColor(scheme.onBackground),
            lineLimits = TextFieldLineLimits.SingleLine,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 56.dp)
                .background(scheme.surfaceVariant, RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .semantics { contentDescription = label },
        )
    }
}

@Composable
private fun CategoryPicker(state: AlarmEditorUiState, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(R.string.editor_category_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            state.categories.forEach { category ->
                CategoryChip(
                    label = category.name,
                    selected = category.id == state.categoryId,
                    onClick = { onSelect(category.id) },
                    shape = category.shape,
                    color = Color(category.color),
                )
            }
        }
    }
}

@Composable
private fun RepeatPicker(selected: RepeatRule, onSelect: (RepeatRule) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(R.string.alarm_repeat_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            RepeatRule.entries.forEach { rule ->
                CategoryChip(label = rule.optionLabel(), selected = rule == selected, onClick = { onSelect(rule) })
            }
        }
    }
}

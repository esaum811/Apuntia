package com.apuntia.app.ui.editor

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apuntia.app.R
import com.apuntia.app.domain.model.Category
import com.apuntia.app.domain.model.EntryType
import com.apuntia.app.security.VaultState
import com.apuntia.app.ui.AppViewModelProvider
import com.apuntia.app.ui.common.SecureWindow
import com.apuntia.app.ui.common.dueDateLabel
import com.apuntia.app.ui.components.ApuntiaIcons
import com.apuntia.app.ui.components.ApuntiaSwitch
import com.apuntia.app.ui.components.CategoryChip
import com.apuntia.app.ui.components.CategoryShapeIcon
import com.apuntia.app.ui.components.IconSquareButton
import com.apuntia.app.ui.components.SegmentedControl
import com.apuntia.app.ui.theme.ApuntiaTheme
import com.apuntia.app.ui.theme.cardInk
import com.apuntia.app.ui.theme.cardInkSoft
import com.apuntia.app.ui.vault.VaultGate

private const val BODY_MIN_LINES = 6
private const val DISABLED_ALPHA = 0.4f
private const val BODY_TINT = 0.4f

/**
 * Editor para crear o modificar una nota.
 *
 * @param onClose se llama al volver, guardar o enviar la nota a la papelera.
 */
@Composable
fun EditorScreen(onClose: () -> Unit, viewModel: EditorViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val vaultState by viewModel.vaultState.collectAsStateWithLifecycle()
    val vaultOpen = vaultState == VaultState.UNLOCKED
    var enableRequested by rememberSaveable { mutableStateOf(false) }
    VaultEffects(vaultOpen, state.isBodyLocked, enableRequested, viewModel) { enableRequested = false }
    // Se cierra cuando el ViewModel avisa que terminó; así no depende de funciones que se pierdan al rotar.
    LaunchedEffect(state.isFinished) { if (state.isFinished) onClose() }
    if (!state.isLoaded) return
    if (state.isConfidential || state.isBodyLocked) SecureWindow()
    val needsPin = state.isBodyLocked || state.isConfidential || enableRequested
    Box(Modifier.fillMaxSize()) {
        EditorForm(state, vaultOpen, viewModel, onClose, onRequestConfidential = { enableRequested = true })
        if (!vaultOpen && needsPin) {
            // Pide el PIN encima del editor. Si cancela, deja de pedirlo o cierra el editor.
            VaultGate(onCancel = { if (enableRequested) enableRequested = false else onClose() })
        }
    }
}

// Con la bóveda abierta se descifra el contenido pendiente o se activa lo que el usuario pidió.
@Composable
private fun VaultEffects(
    vaultOpen: Boolean,
    isBodyLocked: Boolean,
    enableRequested: Boolean,
    viewModel: EditorViewModel,
    onEnabled: () -> Unit,
) {
    LaunchedEffect(vaultOpen, isBodyLocked) {
        if (vaultOpen && isBodyLocked) viewModel.loadConfidentialBody()
    }
    LaunchedEffect(vaultOpen, enableRequested) {
        if (vaultOpen && enableRequested) {
            viewModel.setConfidential(true)
            onEnabled()
        }
    }
}

@Composable
private fun EditorForm(
    state: EditorUiState,
    vaultOpen: Boolean,
    viewModel: EditorViewModel,
    onClose: () -> Unit,
    onRequestConfidential: () -> Unit,
) {
    val canSave by remember { derivedStateOf { viewModel.titleState.text.isNotBlank() && !state.isSaving } }
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
        EditorTopBar(state.isEditing, canSave, onBack = onClose, onSave = viewModel::save)
        if (state.hasError) {
            Text(
                stringResource(if (state.isBodyLocked) R.string.editor_error_open else R.string.editor_error_save),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
        TypeSelector(state.type, viewModel::selectType)
        state.categories.firstOrNull { it.id == state.categoryId }?.let { category ->
            EditorCard(category, viewModel)
        }
        CategoryPicker(state, viewModel::selectCategory)
        if (state.type == EntryType.TASK) {
            DueDateRow(state.dueAt, onChange = viewModel::setDueAt)
        } else {
            ConfidentialRow(state.isConfidential) { wanted ->
                if (wanted && !vaultOpen) onRequestConfidential() else viewModel.setConfidential(wanted)
            }
        }
        if (state.isEditing) TrashLink(viewModel::moveToTrash)
    }
}

@Composable
private fun TrashLink(onClick: () -> Unit) {
    Box(
        Modifier
            .defaultMinSize(minHeight = 48.dp)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            stringResource(R.string.editor_trash),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun ConfidentialRow(checked: Boolean, onChange: (Boolean) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val label = stringResource(R.string.editor_confidential_label)
    Row(
        Modifier
            .fillMaxWidth()
            .background(scheme.surfaceVariant, RoundedCornerShape(22.dp))
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(ApuntiaIcons.lock, contentDescription = null, tint = scheme.onBackground, modifier = Modifier.size(24.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = scheme.onBackground)
            Text(
                stringResource(R.string.editor_confidential_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
            )
        }
        ApuntiaSwitch(checked, onChange, label)
    }
}

@Composable
private fun EditorTopBar(isEditing: Boolean, canSave: Boolean, onBack: () -> Unit, onSave: () -> Unit) {
    val colors = ApuntiaTheme.colors
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconSquareButton(ApuntiaIcons.back, stringResource(R.string.editor_back), onBack)
        Text(
            stringResource(if (isEditing) R.string.editor_edit_title else R.string.editor_new_title),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
                .semantics { heading() },
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Box(
            Modifier
                .height(48.dp)
                .alpha(if (canSave) 1f else DISABLED_ALPHA)
                .background(colors.accent, RoundedCornerShape(16.dp))
                .clickable(enabled = canSave, role = Role.Button, onClick = onSave)
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
private fun TypeSelector(type: EntryType, onSelect: (EntryType) -> Unit) {
    SegmentedControl(
        options = listOf(stringResource(R.string.editor_type_note), stringResource(R.string.editor_type_task)),
        selectedIndex = if (type == EntryType.NOTE) 0 else 1,
        onSelect = { onSelect(if (it == 0) EntryType.NOTE else EntryType.TASK) },
    )
}

@Composable
private fun EditorCard(category: Category, viewModel: EditorViewModel) {
    val color = Color(category.color)
    Column {
        Row(
            Modifier
                .background(color, RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            CategoryShapeIcon(category.shape, cardInk)
            Text(category.name, style = MaterialTheme.typography.labelMedium, color = cardInk)
        }
        Column(
            Modifier
                .fillMaxWidth()
                .background(color, RoundedCornerShape(topEnd = 22.dp, bottomEnd = 22.dp, bottomStart = 22.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val titleLabel = stringResource(R.string.editor_title_label)
            val bodyLabel = stringResource(R.string.editor_body_label)
            FieldLabel(titleLabel)
            BasicTextField(
                state = viewModel.titleState,
                textStyle = MaterialTheme.typography.titleMedium.copy(color = cardInk),
                cursorBrush = SolidColor(cardInk),
                lineLimits = TextFieldLineLimits.SingleLine,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 8.dp)
                    .semantics { contentDescription = titleLabel },
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(cardInk),
            )
            FieldLabel(bodyLabel, Modifier.padding(top = 14.dp))
            BasicTextField(
                state = viewModel.bodyState,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = cardInk),
                cursorBrush = SolidColor(cardInk),
                lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = BODY_MIN_LINES),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(lerp(color, Color.White, BODY_TINT), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .semantics { contentDescription = bodyLabel },
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier, style = MaterialTheme.typography.labelSmall, color = cardInkSoft)
}

@Composable
private fun CategoryPicker(state: EditorUiState, onSelect: (String) -> Unit) {
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
private fun DueDateRow(dueAt: Long?, onChange: (Long?) -> Unit) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .background(scheme.surfaceVariant, RoundedCornerShape(22.dp))
            .padding(start = 16.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            Modifier
                .weight(1f)
                .defaultMinSize(minHeight = 64.dp)
                .clickable(role = Role.Button) { showDueDatePicker(context, dueAt, onChange) },
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                stringResource(R.string.editor_due_label),
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
            )
            Text(
                if (dueAt == null) stringResource(R.string.editor_due_none) else dueDateLabel(dueAt),
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onBackground,
            )
        }
        if (dueAt != null) {
            Box(
                Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .clickable(role = Role.Button) { onChange(null) }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.editor_due_clear),
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onBackground,
                )
            }
        }
    }
}

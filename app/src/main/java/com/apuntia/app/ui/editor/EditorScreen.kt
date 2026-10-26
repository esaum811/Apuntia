package com.apuntia.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apuntia.app.R
import com.apuntia.app.domain.model.Category
import com.apuntia.app.domain.model.EntryType
import com.apuntia.app.ui.AppViewModelProvider
import com.apuntia.app.ui.components.ApuntiaIcons
import com.apuntia.app.ui.components.CategoryChip
import com.apuntia.app.ui.components.CategoryShapeIcon
import com.apuntia.app.ui.components.SegmentedControl
import com.apuntia.app.ui.theme.ApuntiaTheme
import com.apuntia.app.ui.theme.cardInk
import com.apuntia.app.ui.theme.cardInkSoft

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
    if (!state.isLoaded) return
    val canSave = viewModel.titleState.text.isNotBlank()
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
        EditorTopBar(state.isEditing, canSave, onBack = onClose, onSave = { viewModel.save(onClose) })
        TypeSelector(state.type, viewModel::selectType)
        state.categories.firstOrNull { it.id == state.categoryId }?.let { category ->
            EditorCard(category, viewModel)
        }
        CategoryPicker(state, viewModel::selectCategory)
        if (state.isEditing) {
            Text(
                stringResource(R.string.editor_trash),
                modifier = Modifier
                    .clickable(role = Role.Button) { viewModel.moveToTrash(onClose) }
                    .padding(vertical = 12.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun EditorTopBar(isEditing: Boolean, canSave: Boolean, onBack: () -> Unit, onSave: () -> Unit) {
    val colors = ApuntiaTheme.colors
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                .clickable(role = Role.Button, onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                ApuntiaIcons.back,
                contentDescription = stringResource(R.string.editor_back),
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(26.dp),
            )
        }
        Text(
            stringResource(if (isEditing) R.string.editor_edit_title else R.string.editor_new_title),
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
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
            FieldLabel(stringResource(R.string.editor_title_label))
            BasicTextField(
                state = viewModel.titleState,
                textStyle = MaterialTheme.typography.titleMedium.copy(color = cardInk),
                cursorBrush = SolidColor(cardInk),
                lineLimits = TextFieldLineLimits.SingleLine,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
            )
            Box(Modifier.fillMaxWidth().height(3.dp).background(cardInk))
            FieldLabel(stringResource(R.string.editor_body_label), Modifier.padding(top = 14.dp))
            BasicTextField(
                state = viewModel.bodyState,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = cardInk),
                cursorBrush = SolidColor(cardInk),
                lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = BODY_MIN_LINES),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(lerp(color, Color.White, BODY_TINT), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
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

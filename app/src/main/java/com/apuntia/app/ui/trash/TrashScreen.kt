package com.apuntia.app.ui.trash

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apuntia.app.R
import com.apuntia.app.ui.AppViewModelProvider
import com.apuntia.app.ui.common.entryDateLabel
import com.apuntia.app.ui.components.ApuntiaIcons
import com.apuntia.app.ui.components.CategoryShapeIcon
import com.apuntia.app.ui.components.ConfirmDialog
import com.apuntia.app.ui.components.IconSquareButton

/**
 * Pantalla de la papelera: ver lo borrado, restaurarlo o eliminarlo para siempre.
 *
 * @param onBack se llama al tocar el botón de volver.
 */
@Composable
fun TrashScreen(onBack: () -> Unit, viewModel: TrashViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmEmpty by rememberSaveable { mutableStateOf(false) }
    var pendingDeleteId by rememberSaveable { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            IconSquareButton(ApuntiaIcons.back, stringResource(R.string.editor_back), onBack)
            Text(
                stringResource(R.string.trash_title),
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        LazyColumn(
            Modifier.navigationBarsPadding(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "hint") {
                Text(
                    stringResource(R.string.trash_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (state.items.isEmpty() && !state.isLoading) item(key = "empty") { EmptyTrash() }
            if (state.items.isNotEmpty()) {
                item(key = "empty-action") { EmptyTrashButton { confirmEmpty = true } }
            }
            items(state.items, key = { it.entry.id }) { item ->
                TrashRow(
                    item,
                    onRestore = { viewModel.restore(item.entry.id) },
                    onDelete = { pendingDeleteId = item.entry.id },
                )
            }
        }
    }
    if (confirmEmpty) {
        ConfirmDialog(
            title = stringResource(R.string.confirm_empty_trash_title),
            message = stringResource(R.string.confirm_empty_trash_message),
            confirmLabel = stringResource(R.string.confirm_empty_action),
            onConfirm = {
                viewModel.emptyTrash()
                confirmEmpty = false
            },
            onDismiss = { confirmEmpty = false },
        )
    }
    pendingDeleteId?.let { id ->
        ConfirmDialog(
            title = stringResource(R.string.confirm_delete_title),
            message = stringResource(R.string.confirm_delete_message),
            confirmLabel = stringResource(R.string.confirm_delete_action),
            onConfirm = {
                viewModel.deleteForever(id)
                pendingDeleteId = null
            },
            onDismiss = { pendingDeleteId = null },
        )
    }
}

@Composable
private fun EmptyTrash() {
    Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(R.string.trash_empty_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            stringResource(R.string.trash_empty_hint),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmptyTrashButton(onClick: () -> Unit) {
    Box(Modifier.defaultMinSize(minHeight = 48.dp).clickable(role = Role.Button, onClick = onClick)) {
        Text(
            stringResource(R.string.trash_empty_action),
            modifier = Modifier.align(Alignment.CenterStart),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun TrashRow(item: TrashItem, onRestore: () -> Unit, onDelete: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxWidth().background(scheme.surfaceVariant, RoundedCornerShape(22.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(item.entry.title, style = MaterialTheme.typography.bodyLarge, color = scheme.onBackground)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CategoryShapeIcon(item.category.shape, Color(item.category.color), outline = scheme.onBackground)
            Text(item.category.name, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
            item.entry.deletedAt?.let {
                Text(
                    stringResource(R.string.trash_deleted_on, entryDateLabel(it)),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RowAction(stringResource(R.string.trash_restore), scheme.onBackground, onRestore)
            RowAction(stringResource(R.string.trash_delete), scheme.error, onDelete)
        }
    }
}

@Composable
private fun RowAction(label: String, color: Color, onClick: () -> Unit) {
    Box(
        Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp).clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            label,
            modifier = Modifier.padding(end = 12.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color,
        )
    }
}

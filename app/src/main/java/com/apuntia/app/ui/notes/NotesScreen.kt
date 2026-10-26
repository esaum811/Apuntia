package com.apuntia.app.ui.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apuntia.app.R
import com.apuntia.app.ui.AppViewModelProvider
import com.apuntia.app.ui.common.entryDateLabel
import com.apuntia.app.ui.components.CategoryCard
import com.apuntia.app.ui.components.CategoryChip
import com.apuntia.app.ui.components.PrimaryButton
import com.apuntia.app.ui.theme.cardInk
import com.apuntia.app.ui.theme.cardInkSoft

// Espacio al final de la lista para que la barra inferior no tape la última nota.
private val DockClearance = 130.dp

/**
 * Pantalla de inicio: filtros por categoría y lista de notas.
 *
 * @param onOpenEntry se llama con el id de la nota tocada.
 * @param onCreate se llama al pedir una nota nueva desde la pantalla vacía.
 */
@Composable
fun NotesScreen(
    onOpenEntry: (String) -> Unit,
    onCreate: () -> Unit,
    viewModel: NotesViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(top = 8.dp, bottom = DockClearance),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "title") {
            Text(
                stringResource(R.string.notes_title),
                modifier = Modifier.padding(horizontal = 20.dp),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        item(key = "filters") { CategoryFilters(state, viewModel::selectCategory) }
        if (!state.isLoading && state.notes.isEmpty()) {
            item(key = "empty") { EmptyNotes(onCreate) }
        }
        items(state.notes, key = { it.entry.id }, contentType = { "note" }) { item ->
            NoteCard(item, onClick = { onOpenEntry(item.entry.id) })
        }
    }
}

@Composable
private fun CategoryFilters(state: NotesUiState, onSelect: (String?) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        item(key = "all") {
            CategoryChip(stringResource(R.string.filter_all), state.selectedCategoryId == null, { onSelect(null) })
        }
        items(state.categories, key = { it.id }) { category ->
            CategoryChip(
                label = category.name,
                selected = state.selectedCategoryId == category.id,
                onClick = { onSelect(category.id) },
                shape = category.shape,
                color = Color(category.color),
            )
        }
    }
}

@Composable
private fun NoteCard(item: NoteItem, onClick: () -> Unit) {
    val entry = item.entry
    CategoryCard(
        categoryName = item.category.name,
        shape = item.category.shape,
        color = Color(item.category.color),
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 20.dp),
    ) {
        Text(entry.title, style = MaterialTheme.typography.titleMedium, color = cardInk)
        if (entry.body.isNotBlank()) {
            Text(
                entry.body,
                style = MaterialTheme.typography.bodyMedium,
                color = cardInkSoft,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(entryDateLabel(entry.updatedAt), style = MaterialTheme.typography.labelMedium, color = cardInk)
    }
}

@Composable
private fun EmptyNotes(onCreate: () -> Unit) {
    Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(R.string.empty_notes_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            stringResource(R.string.empty_notes_hint),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        PrimaryButton(stringResource(R.string.empty_notes_action), onCreate, Modifier.padding(top = 8.dp))
    }
}

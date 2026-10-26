package com.apuntia.app.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apuntia.app.domain.model.Category
import com.apuntia.app.domain.model.Entry
import com.apuntia.app.domain.model.EntryType
import com.apuntia.app.domain.repository.CategoryRepository
import com.apuntia.app.domain.repository.EntryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

private const val STOP_TIMEOUT_MS = 5_000L

/**
 * Una nota junto con su categoría, lista para dibujarse.
 *
 * @property entry la nota.
 * @property category su categoría.
 */
data class NoteItem(val entry: Entry, val category: Category)

/**
 * Lo que la pantalla de notas necesita para dibujarse.
 *
 * @property categories categorías para los filtros.
 * @property selectedCategoryId categoría filtrada, o nulo para ver todas.
 * @property notes notas visibles.
 * @property isLoading verdadero mientras llegan los primeros datos.
 */
data class NotesUiState(
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: String? = null,
    val notes: List<NoteItem> = emptyList(),
    val isLoading: Boolean = true,
)

/** Prepara los datos de la pantalla de notas y atiende el filtro por categoría. */
@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModel(entries: EntryRepository, categories: CategoryRepository) : ViewModel() {
    private val selectedCategoryId = MutableStateFlow<String?>(null)

    /** Estado actual de la pantalla; se actualiza solo cuando cambia la base de datos. */
    val uiState: StateFlow<NotesUiState> = combine(
        categories.observeAll(),
        selectedCategoryId,
        selectedCategoryId.flatMapLatest { entries.observeActive(it) },
    ) { allCategories, selected, list ->
        val byId = allCategories.associateBy { it.id }
        val notes = list
            .filter { it.type == EntryType.NOTE }
            .mapNotNull { entry -> byId[entry.categoryId]?.let { NoteItem(entry, it) } }
        NotesUiState(allCategories, selected, notes, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), NotesUiState())

    /** Filtra por [id] de categoría; con nulo muestra todas. */
    fun selectCategory(id: String?) {
        selectedCategoryId.value = id
    }
}

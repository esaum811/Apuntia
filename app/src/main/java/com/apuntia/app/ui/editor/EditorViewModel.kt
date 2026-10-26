package com.apuntia.app.ui.editor

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apuntia.app.core.IdGenerator
import com.apuntia.app.domain.model.Category
import com.apuntia.app.domain.model.Entry
import com.apuntia.app.domain.model.EntryType
import com.apuntia.app.domain.repository.CategoryRepository
import com.apuntia.app.domain.repository.EntryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Lo que el editor necesita para dibujarse (el texto vive aparte, en [EditorViewModel.titleState]).
 *
 * @property isLoaded verdadero cuando ya se cargaron las categorías y la nota a editar.
 * @property isEditing verdadero si se edita una nota existente; falso si es una nueva.
 * @property type nota o tarea.
 * @property categoryId categoría elegida.
 * @property categories categorías disponibles.
 */
data class EditorUiState(
    val isLoaded: Boolean = false,
    val isEditing: Boolean = false,
    val type: EntryType = EntryType.NOTE,
    val categoryId: String = "",
    val categories: List<Category> = emptyList(),
)

/**
 * Crea una nota nueva o edita una existente.
 *
 * @param entryId id de la nota a editar, o nulo para crear una nueva.
 */
class EditorViewModel(
    private val entryId: String?,
    private val entries: EntryRepository,
    categories: CategoryRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(EditorUiState())
    private var existing: Entry? = null

    /** Texto del título. */
    val titleState = TextFieldState()

    /** Texto del contenido. */
    val bodyState = TextFieldState()

    /** Estado actual del editor. */
    val uiState: StateFlow<EditorUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            val allCategories = categories.observeAll().first()
            val entry = entryId?.let { entries.get(it) }
            existing = entry
            titleState.setTextAndPlaceCursorAtEnd(entry?.title.orEmpty())
            bodyState.setTextAndPlaceCursorAtEnd(entry?.body.orEmpty())
            mutableState.value = EditorUiState(
                isLoaded = true,
                isEditing = entry != null,
                type = entry?.type ?: EntryType.NOTE,
                categoryId = entry?.categoryId ?: allCategories.firstOrNull()?.id.orEmpty(),
                categories = allCategories,
            )
        }
    }

    /** Cambia entre nota y tarea. */
    fun selectType(type: EntryType) = mutableState.update { it.copy(type = type) }

    /** Cambia la categoría de la nota. */
    fun selectCategory(id: String) = mutableState.update { it.copy(categoryId = id) }

    /** Guarda la nota y avisa con [onDone] cuando termina. No guarda si el título está vacío. */
    fun save(onDone: () -> Unit) {
        val title = titleState.text.toString().trim()
        if (title.isEmpty()) return
        val state = mutableState.value
        val body = bodyState.text.toString().trim()
        viewModelScope.launch {
            val base = existing ?: newEntry(state.categoryId)
            entries.save(base.copy(title = title, body = body, type = state.type, categoryId = state.categoryId))
            onDone()
        }
    }

    /** Envía la nota a la papelera y avisa con [onDone]. Solo aplica si se está editando. */
    fun moveToTrash(onDone: () -> Unit) {
        val id = existing?.id ?: return
        viewModelScope.launch {
            entries.moveToTrash(id)
            onDone()
        }
    }

    private fun newEntry(categoryId: String): Entry {
        val now = System.currentTimeMillis()
        return Entry(
            id = IdGenerator.newId(),
            categoryId = categoryId,
            type = EntryType.NOTE,
            title = "",
            body = "",
            isDone = false,
            dueAt = null,
            isConfidential = false,
            isPinned = false,
            createdAt = now,
            updatedAt = now,
            deletedAt = null,
        )
    }
}

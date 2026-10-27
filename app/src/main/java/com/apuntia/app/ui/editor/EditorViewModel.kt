package com.apuntia.app.ui.editor

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apuntia.app.core.IdGenerator
import com.apuntia.app.domain.model.Category
import com.apuntia.app.domain.model.Entry
import com.apuntia.app.domain.model.EntryType
import com.apuntia.app.domain.repository.CategoryRepository
import com.apuntia.app.domain.repository.EntryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "Apuntia"

/**
 * Lo que el editor necesita para dibujarse (el texto vive aparte, en [EditorViewModel.titleState]).
 *
 * @property isLoaded verdadero cuando ya se cargaron las categorías y la nota a editar.
 * @property isEditing verdadero si se edita una nota existente; falso si es una nueva.
 * @property type nota o tarea.
 * @property categoryId categoría elegida.
 * @property categories categorías disponibles.
 * @property isSaving verdadero mientras se guarda; evita guardar dos veces con un doble toque.
 * @property isFinished verdadero cuando terminó de guardar o borrar y hay que cerrar el editor.
 * @property hasError verdadero si no se pudo guardar y hay que avisar al usuario.
 */
data class EditorUiState(
    val isLoaded: Boolean = false,
    val isEditing: Boolean = false,
    val type: EntryType = EntryType.NOTE,
    val categoryId: String = "",
    val categories: List<Category> = emptyList(),
    val isSaving: Boolean = false,
    val isFinished: Boolean = false,
    val hasError: Boolean = false,
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
            guarded(onError = { mutableState.value = EditorUiState(isLoaded = true, hasError = true) }) {
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
    }

    /** Cambia entre nota y tarea. */
    fun selectType(type: EntryType) = mutableState.update { it.copy(type = type) }

    /** Cambia la categoría de la nota. */
    fun selectCategory(id: String) = mutableState.update { it.copy(categoryId = id) }

    /**
     * Guarda la nota. Al terminar, el estado queda con `isFinished` verdadero.
     * No hace nada si el título está vacío o si ya se está guardando (doble toque).
     */
    fun save() {
        val title = titleState.text.toString().trim()
        val state = mutableState.value
        if (title.isEmpty() || state.categoryId.isEmpty() || state.isSaving) return
        val body = bodyState.text.toString().trim()
        // La entrada nueva se crea una sola vez; si el guardado falla y se reintenta, conserva su id.
        val base = existing ?: newEntry(state.categoryId).also { existing = it }
        runSaving {
            entries.save(base.copy(title = title, body = body, type = state.type, categoryId = state.categoryId))
        }
    }

    /** Envía la nota a la papelera. Solo aplica si se está editando una nota existente. */
    fun moveToTrash() {
        val id = existing?.id ?: return
        if (mutableState.value.isSaving) return
        runSaving { entries.moveToTrash(id) }
    }

    private fun runSaving(action: suspend () -> Unit) {
        mutableState.update { it.copy(isSaving = true, hasError = false) }
        viewModelScope.launch {
            guarded(onError = { mutableState.update { it.copy(isSaving = false, hasError = true) } }) {
                action()
                mutableState.update { it.copy(isFinished = true) }
            }
        }
    }

    // Convierte cualquier fallo de la base de datos en un aviso en pantalla en vez de cerrar la app.
    @Suppress("TooGenericExceptionCaught")
    private suspend fun guarded(onError: () -> Unit, block: suspend () -> Unit) {
        try {
            block()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Log.e(TAG, "Error en el editor", error)
            onError()
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

package com.apuntia.app.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apuntia.app.domain.model.Category
import com.apuntia.app.domain.model.Entry
import com.apuntia.app.domain.repository.CategoryRepository
import com.apuntia.app.domain.repository.EntryRepository
import com.apuntia.app.ui.common.launchSafely
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

private const val STOP_TIMEOUT_MS = 5_000L

/**
 * Una entrada de la papelera junto con su categoría.
 *
 * @property entry la nota o tarea borrada.
 * @property category su categoría.
 */
data class TrashItem(val entry: Entry, val category: Category)

/**
 * Lo que la pantalla de la papelera necesita para dibujarse.
 *
 * @property items entradas borradas, la más reciente primero.
 * @property isLoading verdadero mientras llegan los primeros datos.
 */
data class TrashUiState(val items: List<TrashItem> = emptyList(), val isLoading: Boolean = true)

/** Prepara los datos de la papelera y atiende restaurar y borrar para siempre. */
class TrashViewModel(private val entries: EntryRepository, categories: CategoryRepository) : ViewModel() {
    /** Estado actual de la pantalla; se actualiza solo cuando cambia la base de datos. */
    val uiState: StateFlow<TrashUiState> = combine(categories.observeAll(), entries.observeTrash()) { all, trashed ->
        val byId = all.associateBy { it.id }
        TrashUiState(trashed.mapNotNull { entry -> byId[entry.categoryId]?.let { TrashItem(entry, it) } }, false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), TrashUiState())

    /** Saca la entrada [id] de la papelera. */
    fun restore(id: String) {
        viewModelScope.launchSafely { entries.restore(id) }
    }

    /** Borra la entrada [id] para siempre. */
    fun deleteForever(id: String) {
        viewModelScope.launchSafely { entries.deleteForever(id) }
    }

    /** Borra todo lo que hay en la papelera para siempre. */
    fun emptyTrash() {
        viewModelScope.launchSafely { entries.emptyTrash() }
    }
}

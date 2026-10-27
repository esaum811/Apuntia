package com.apuntia.app.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apuntia.app.domain.model.Category
import com.apuntia.app.domain.model.Entry
import com.apuntia.app.domain.model.TaskSectionType
import com.apuntia.app.domain.repository.CategoryRepository
import com.apuntia.app.domain.repository.EntryRepository
import com.apuntia.app.domain.usecase.GroupTasksByDueDate
import com.apuntia.app.ui.common.launchSafely
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

private const val STOP_TIMEOUT_MS = 5_000L

/**
 * Una tarea junto con su categoría, lista para dibujarse.
 *
 * @property entry la tarea.
 * @property category su categoría.
 */
data class TaskItem(val entry: Entry, val category: Category)

/**
 * Un grupo de tareas pendientes con la misma [type] de fecha límite.
 *
 * @property type a qué grupo pertenecen.
 * @property items tareas del grupo.
 */
data class TaskGroupUi(val type: TaskSectionType, val items: List<TaskItem>)

/**
 * Lo que la pantalla de tareas necesita para dibujarse.
 *
 * @property showDone verdadero si se ve la pestaña "Hechas"; falso para "Pendientes".
 * @property pendingGroups tareas pendientes agrupadas por fecha límite.
 * @property pendingCount cuántas tareas pendientes hay.
 * @property done tareas terminadas, las más recientes primero.
 * @property isLoading verdadero mientras llegan los primeros datos.
 */
data class TasksUiState(
    val showDone: Boolean = false,
    val pendingGroups: List<TaskGroupUi> = emptyList(),
    val pendingCount: Int = 0,
    val done: List<TaskItem> = emptyList(),
    val isLoading: Boolean = true,
)

/**
 * Prepara los datos de la pantalla de tareas y atiende marcar tareas como hechas.
 *
 * @param clock devuelve la hora actual en milisegundos; se cambia en las pruebas.
 */
class TasksViewModel(
    private val entries: EntryRepository,
    categories: CategoryRepository,
    private val groupTasks: GroupTasksByDueDate = GroupTasksByDueDate(),
    private val clock: () -> Long = System::currentTimeMillis,
) : ViewModel() {
    private val showDone = MutableStateFlow(false)

    /** Estado actual de la pantalla; se actualiza solo cuando cambia la base de datos. */
    val uiState: StateFlow<TasksUiState> = combine(
        categories.observeAll(),
        entries.observeTasks(isDone = false),
        entries.observeTasks(isDone = true),
        showDone,
    ) { allCategories, pending, done, showingDone ->
        val byId = allCategories.associateBy { it.id }
        fun Entry.toItem() = byId[categoryId]?.let { TaskItem(this, it) }
        val groups = groupTasks(pending, clock(), ZoneId.systemDefault()).map { section ->
            TaskGroupUi(section.type, section.tasks.mapNotNull { it.toItem() })
        }
        TasksUiState(
            showDone = showingDone,
            pendingGroups = groups,
            pendingCount = groups.sumOf { it.items.size },
            done = done.mapNotNull { it.toItem() },
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), TasksUiState())

    /** Cambia entre las pestañas "Pendientes" y "Hechas". */
    fun selectTab(showDoneTab: Boolean) {
        showDone.value = showDoneTab
    }

    /** Marca la tarea [id] como terminada o pendiente. */
    fun setDone(id: String, isDone: Boolean) {
        viewModelScope.launchSafely { entries.setDone(id, isDone) }
    }

    /** Envía a la papelera todas las tareas hechas; se pueden recuperar desde ahí. */
    fun moveDoneToTrash() {
        viewModelScope.launchSafely { entries.moveDoneTasksToTrash() }
    }
}

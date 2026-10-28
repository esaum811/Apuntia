package com.apuntia.app.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.apuntia.app.ApuntiaApplication
import com.apuntia.app.di.AppContainer
import com.apuntia.app.domain.model.EntryType
import com.apuntia.app.ui.alarms.AlarmEditorViewModel
import com.apuntia.app.ui.alarms.AlarmsViewModel
import com.apuntia.app.ui.editor.EditorViewModel
import com.apuntia.app.ui.navigation.Routes
import com.apuntia.app.ui.notes.NotesViewModel
import com.apuntia.app.ui.tasks.TasksViewModel
import com.apuntia.app.ui.trash.TrashViewModel

private fun CreationExtras.container(): AppContainer = (this[APPLICATION_KEY] as ApuntiaApplication).container

/** Crea los ViewModel de las pantallas entregándoles los repositorios que necesitan. */
object AppViewModelProvider {
    /** Fábrica que se pasa a `viewModel(factory = ...)` en cada pantalla. */
    val Factory = viewModelFactory {
        initializer { NotesViewModel(container().entries, container().categories) }
        initializer { TasksViewModel(container().entries, container().categories) }
        initializer { TrashViewModel(container().entries, container().categories) }
        initializer { AlarmsViewModel(container().reminders, container().categories, container().alarmScheduler) }
        initializer {
            AlarmEditorViewModel(
                alarmId = createSavedStateHandle()[Routes.ALARM_ID_ARG],
                reminders = container().reminders,
                categories = container().categories,
                scheduler = container().alarmScheduler,
            )
        }
        initializer {
            EditorViewModel(
                entryId = createSavedStateHandle()[Routes.ENTRY_ID_ARG],
                initialType = EntryType.entries.firstOrNull { it.name == createSavedStateHandle()[Routes.TYPE_ARG] },
                entries = container().entries,
                categories = container().categories,
            )
        }
    }
}

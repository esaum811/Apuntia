package com.apuntia.app.ui.alarms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apuntia.app.alarm.AlarmScheduler
import com.apuntia.app.domain.model.Category
import com.apuntia.app.domain.model.Reminder
import com.apuntia.app.domain.repository.CategoryRepository
import com.apuntia.app.domain.repository.ReminderRepository
import com.apuntia.app.domain.usecase.NextAlarmTime
import com.apuntia.app.ui.common.launchSafely
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

private const val STOP_TIMEOUT_MS = 5_000L

/**
 * Una alarma junto con su categoría.
 *
 * @property reminder la alarma.
 * @property category su categoría.
 */
data class AlarmItem(val reminder: Reminder, val category: Category)

/**
 * Lo que la pantalla de alarmas necesita para dibujarse.
 *
 * @property items alarmas, de la que suena primero a la última.
 * @property canScheduleExact falso si falta el permiso de "Alarmas y recordatorios".
 * @property isLoading verdadero mientras llegan los primeros datos.
 */
data class AlarmsUiState(
    val items: List<AlarmItem> = emptyList(),
    val canScheduleExact: Boolean = true,
    val isLoading: Boolean = true,
)

/** Prepara la lista de alarmas y atiende encender y apagar. */
class AlarmsViewModel(
    private val reminders: ReminderRepository,
    categories: CategoryRepository,
    private val scheduler: AlarmScheduler,
    private val nextAlarmTime: NextAlarmTime = NextAlarmTime(),
    private val clock: () -> Long = System::currentTimeMillis,
) : ViewModel() {
    private val canSchedule = MutableStateFlow(scheduler.canScheduleExact())

    /** Estado actual de la pantalla. */
    val uiState: StateFlow<AlarmsUiState> = combine(
        categories.observeAll(),
        reminders.observeAll(),
        canSchedule,
    ) { all, alarms, can ->
        val byId = all.associateBy { it.id }
        AlarmsUiState(alarms.mapNotNull { r -> byId[r.categoryId]?.let { AlarmItem(r, it) } }, can, false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AlarmsUiState())

    /** Vuelve a revisar el permiso; se llama al regresar de los ajustes del sistema. */
    fun refreshPermission() {
        canSchedule.value = scheduler.canScheduleExact()
    }

    /** Enciende o apaga la alarma [reminder]. Al encender calcula la próxima vez que sonará. */
    fun setEnabled(reminder: Reminder, enabled: Boolean) {
        viewModelScope.launchSafely {
            if (enabled) {
                val next = nextAlarmTime(reminder.triggerAt, reminder.repeatRule, clock(), ZoneId.systemDefault())
                val updated = reminder.copy(triggerAt = next, isEnabled = true)
                reminders.save(updated)
                scheduler.schedule(updated)
            } else {
                reminders.setEnabled(reminder.id, false)
                scheduler.cancel(reminder.id)
            }
        }
    }
}

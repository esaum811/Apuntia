package com.apuntia.app.ui.alarms

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apuntia.app.alarm.AlarmScheduler
import com.apuntia.app.core.IdGenerator
import com.apuntia.app.domain.model.Category
import com.apuntia.app.domain.model.Reminder
import com.apuntia.app.domain.model.RepeatRule
import com.apuntia.app.domain.repository.CategoryRepository
import com.apuntia.app.domain.repository.ReminderRepository
import com.apuntia.app.domain.usecase.NextAlarmTime
import com.apuntia.app.ui.common.launchSafely
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update

private const val DEFAULT_HOUR = 7

/**
 * Lo que el editor de alarmas necesita para dibujarse.
 *
 * @property isLoaded falso mientras se leen la categoría y la alarma.
 * @property isEditing verdadero si se modifica una alarma existente.
 * @property hour hora (0 a 23).
 * @property minute minuto (0 a 59).
 * @property categoryId categoría elegida.
 * @property repeatRule repetición elegida.
 * @property categories categorías disponibles.
 * @property isFinished verdadero cuando la pantalla debe cerrarse.
 */
data class AlarmEditorUiState(
    val isLoaded: Boolean = false,
    val isEditing: Boolean = false,
    val hour: Int = DEFAULT_HOUR,
    val minute: Int = 0,
    val categoryId: String = "",
    val repeatRule: RepeatRule = RepeatRule.NONE,
    val categories: List<Category> = emptyList(),
    val isFinished: Boolean = false,
)

/** Atiende crear, modificar y borrar una alarma. */
class AlarmEditorViewModel(
    private val alarmId: String?,
    private val reminders: ReminderRepository,
    private val categories: CategoryRepository,
    private val scheduler: AlarmScheduler,
    private val nextAlarmTime: NextAlarmTime = NextAlarmTime(),
    private val clock: () -> Long = System::currentTimeMillis,
) : ViewModel() {
    /** Texto de la etiqueta de la alarma. */
    val labelState = TextFieldState()

    private val mutableState = MutableStateFlow(AlarmEditorUiState(isEditing = alarmId != null))
    private var existing: Reminder? = null
    private var isSaving = false

    /** Estado actual del editor. */
    val uiState: StateFlow<AlarmEditorUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launchSafely { load() }
    }

    private suspend fun load() {
        val all = categories.observeAll().first()
        val reminder = alarmId?.let { reminders.get(it) }
        existing = reminder
        val zone = ZoneId.systemDefault()
        val time = reminder?.let { Instant.ofEpochMilli(it.triggerAt).atZone(zone) }
        reminder?.let { labelState.setTextAndPlaceCursorAtEnd(it.label) }
        mutableState.update {
            it.copy(
                isLoaded = true,
                categories = all,
                categoryId = reminder?.categoryId ?: all.firstOrNull()?.id.orEmpty(),
                hour = time?.hour ?: it.hour,
                minute = time?.minute ?: it.minute,
                repeatRule = reminder?.repeatRule ?: it.repeatRule,
            )
        }
    }

    /** Cambia la hora de la alarma. */
    fun setTime(hour: Int, minute: Int) = mutableState.update { it.copy(hour = hour, minute = minute) }

    /** Elige la categoría con id [id]. */
    fun selectCategory(id: String) = mutableState.update { it.copy(categoryId = id) }

    /** Elige cada cuánto se repite. */
    fun selectRepeat(rule: RepeatRule) = mutableState.update { it.copy(repeatRule = rule) }

    /** Guarda la alarma, la enciende y la programa en el sistema. */
    fun save() {
        val state = mutableState.value
        if (isSaving || state.categoryId.isEmpty()) return
        isSaving = true
        viewModelScope.launchSafely {
            try {
                val now = clock()
                val zone = ZoneId.systemDefault()
                val base = baseDate(state.repeatRule, zone).atTime(state.hour, state.minute).atZone(zone)
                    .toInstant().toEpochMilli()
                val reminder = Reminder(
                    id = existing?.id ?: IdGenerator.newId(),
                    entryId = existing?.entryId,
                    categoryId = state.categoryId,
                    label = labelState.text.toString().trim(),
                    triggerAt = nextAlarmTime(base, state.repeatRule, now, zone),
                    repeatRule = state.repeatRule,
                    ringtoneUri = existing?.ringtoneUri,
                    isEnabled = true,
                    createdAt = existing?.createdAt ?: now,
                )
                reminders.save(reminder)
                scheduler.schedule(reminder)
                mutableState.update { it.copy(isFinished = true) }
            } finally {
                isSaving = false
            }
        }
    }

    // Una alarma semanal conserva su día de la semana; las demás parten de hoy.
    private fun baseDate(rule: RepeatRule, zone: ZoneId): LocalDate {
        val previous = existing?.takeIf { it.repeatRule == RepeatRule.WEEKLY && rule == RepeatRule.WEEKLY }
        return previous?.let { Instant.ofEpochMilli(it.triggerAt).atZone(zone).toLocalDate() } ?: LocalDate.now(zone)
    }

    /** Borra la alarma que se está editando. */
    fun delete() {
        val id = existing?.id ?: return
        viewModelScope.launchSafely {
            scheduler.cancel(id)
            reminders.delete(id)
            mutableState.update { it.copy(isFinished = true) }
        }
    }
}

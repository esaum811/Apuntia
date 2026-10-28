package com.apuntia.app.alarm

import com.apuntia.app.domain.model.RepeatRule
import com.apuntia.app.domain.repository.ReminderRepository
import com.apuntia.app.domain.usecase.NextAlarmTime
import java.time.ZoneId

/**
 * Vuelve a programar las alarmas activas. Android las borra al apagar el teléfono, así que se
 * usa al reiniciar y al actualizar la app.
 */
class AlarmRescheduler(
    private val reminders: ReminderRepository,
    private val scheduler: AlarmScheduler,
    private val nextAlarmTime: NextAlarmTime = NextAlarmTime(),
    private val clock: () -> Long = System::currentTimeMillis,
) {
    /** Reprograma cada alarma activa; las de una sola vez que ya pasaron se desactivan. */
    suspend fun rescheduleAll() {
        val now = clock()
        reminders.getEnabled().forEach { reminder ->
            if (reminder.repeatRule == RepeatRule.NONE && reminder.triggerAt <= now) {
                reminders.setEnabled(reminder.id, false)
            } else {
                val next = nextAlarmTime(reminder.triggerAt, reminder.repeatRule, now, ZoneId.systemDefault())
                val updated = reminder.copy(triggerAt = next)
                reminders.save(updated)
                scheduler.schedule(updated)
            }
        }
    }
}

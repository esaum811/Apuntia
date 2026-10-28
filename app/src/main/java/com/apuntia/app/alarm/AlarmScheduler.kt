package com.apuntia.app.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.getSystemService
import com.apuntia.app.MainActivity
import com.apuntia.app.domain.model.Reminder

/** Programa y cancela alarmas en el sistema Android. */
interface AlarmScheduler {
    /** Indica si Android permite programar alarmas exactas ("Alarmas y recordatorios"). */
    fun canScheduleExact(): Boolean

    /**
     * Programa [reminder] para su hora [Reminder.triggerAt].
     * Devuelve falso si el sistema no lo permite (falta el permiso de alarmas exactas).
     */
    fun schedule(reminder: Reminder): Boolean

    /** Programa que la alarma [id] vuelva a sonar a las [triggerAt] (posponer). */
    fun scheduleSnooze(id: String, triggerAt: Long): Boolean

    /** Cancela la alarma [id] y cualquier posposición pendiente. */
    fun cancel(id: String)
}

/** Implementación con `AlarmManager`: suena aunque la app esté cerrada. */
class AndroidAlarmScheduler(private val context: Context) : AlarmScheduler {
    private val alarmManager: AlarmManager = checkNotNull(context.getSystemService())

    override fun canScheduleExact(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    override fun schedule(reminder: Reminder): Boolean = set(reminder.id, reminder.triggerAt, isSnooze = false)

    override fun scheduleSnooze(id: String, triggerAt: Long): Boolean = set(id, triggerAt, isSnooze = true)

    override fun cancel(id: String) {
        alarmManager.cancel(fireIntent(id, isSnooze = false))
        alarmManager.cancel(fireIntent(id, isSnooze = true))
    }

    private fun set(id: String, triggerAt: Long, isSnooze: Boolean): Boolean {
        if (!canScheduleExact()) return false
        // setAlarmClock es la forma más fiable: Android la respeta aunque el teléfono esté en reposo.
        val showApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAt, showApp), fireIntent(id, isSnooze))
        return true
    }

    private fun fireIntent(id: String, isSnooze: Boolean): PendingIntent {
        val suffix = if (isSnooze) "/snooze" else ""
        val intent = Intent(context, AlarmReceiver::class.java)
            .setAction(AlarmIntents.ACTION_FIRE)
            // El dato distingue una alarma de otra y la normal de la pospuesta.
            .setData(Uri.parse("apuntia://alarm/$id$suffix"))
            .putExtra(AlarmIntents.EXTRA_REMINDER_ID, id)
            .putExtra(AlarmIntents.EXTRA_IS_SNOOZE, isSnooze)
        val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        return PendingIntent.getBroadcast(context, 0, intent, flags)
    }
}

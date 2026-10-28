package com.apuntia.app.domain.usecase

import com.apuntia.app.domain.model.RepeatRule
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private const val DAYS_TO_SEARCH = 8

/** Calcula la próxima vez que debe sonar una alarma. */
class NextAlarmTime {
    /**
     * Devuelve el primer momento posterior a [nowMillis] en que suena la alarma.
     *
     * La hora del día sale de [triggerAt]; [rule] decide en qué días puede sonar. Si [triggerAt]
     * todavía no llega y cae en un día permitido, se devuelve tal cual.
     *
     * @param triggerAt fecha y hora programada (milisegundos UTC); de ella se toma la hora del día.
     * @param rule cada cuánto se repite.
     * @param nowMillis momento actual en milisegundos UTC.
     * @param zone zona horaria con la que se decide el día y la hora.
     */
    operator fun invoke(triggerAt: Long, rule: RepeatRule, nowMillis: Long, zone: ZoneId): Long {
        val scheduled = Instant.ofEpochMilli(triggerAt).atZone(zone)
        if (triggerAt > nowMillis && allows(rule, scheduled.dayOfWeek, scheduled.dayOfWeek)) return triggerAt
        val time = scheduled.toLocalTime()
        val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
        for (offset in 0 until DAYS_TO_SEARCH) {
            val date: LocalDate = today.plusDays(offset.toLong())
            val candidate = date.atTime(time).atZone(zone).toInstant().toEpochMilli()
            if (candidate > nowMillis && allows(rule, date.dayOfWeek, scheduled.dayOfWeek)) return candidate
        }
        error("No se encontró una próxima fecha para la alarma")
    }

    private fun allows(rule: RepeatRule, day: DayOfWeek, scheduledDay: DayOfWeek): Boolean = when (rule) {
        RepeatRule.NONE, RepeatRule.DAILY -> true
        RepeatRule.WEEKDAYS -> day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY
        RepeatRule.WEEKLY -> day == scheduledDay
    }
}

package com.apuntia.app.domain.usecase

import com.apuntia.app.domain.model.RepeatRule
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class NextAlarmTimeTest {
    private val zone = ZoneId.of("America/Mexico_City")
    private val next = NextAlarmTime()

    private fun millis(day: Int, hour: Int, minute: Int, month: Int = 10) =
        LocalDateTime.of(2026, month, day, hour, minute).atZone(zone).toInstant().toEpochMilli()

    // "Ahora": miércoles 21 de octubre de 2026, 10:00.
    private val now = millis(21, 10, 0)

    @Test
    fun oneTimeAlarmInTheFutureKeepsItsDate() {
        val at = millis(21, 16, 0)
        assertEquals(at, next(at, RepeatRule.NONE, now, zone))
    }

    @Test
    fun oneTimeAlarmThatAlreadyPassedMovesToTheNextOccurrenceOfThatTime() {
        // 8:00 de hoy ya pasó: la próxima vez es mañana a las 8:00.
        assertEquals(millis(22, 8, 0), next(millis(21, 8, 0), RepeatRule.NONE, now, zone))
    }

    @Test
    fun alarmAtTheExactCurrentMomentMovesToTomorrow() {
        assertEquals(millis(22, 10, 0), next(now, RepeatRule.NONE, now, zone))
    }

    @Test
    fun timeEarlierTodayIsStillAheadWhenLaterTodayExists() {
        // 18:30 de un día pasado: hoy a las 18:30 todavía no llega.
        assertEquals(millis(21, 18, 30), next(millis(10, 18, 30), RepeatRule.DAILY, now, zone))
    }

    @Test
    fun dailyAlarmThatPassedTodayRingsTomorrow() {
        assertEquals(millis(22, 7, 30), next(millis(21, 7, 30), RepeatRule.DAILY, now, zone))
    }

    @Test
    fun weekdaysAlarmOnFridayEveningSkipsTheWeekend() {
        // Viernes 23 de octubre, 20:00: la siguiente es el lunes 26 a las 7:30.
        val friday = millis(23, 20, 0)
        assertEquals(millis(26, 7, 30), next(millis(23, 7, 30), RepeatRule.WEEKDAYS, friday, zone))
    }

    @Test
    fun weekdaysAlarmSavedWithASaturdayDateRingsOnTheNextWeekday() {
        // La fecha guardada es sábado, pero solo importa la hora: la próxima vez es el jueves 22 a las 9:00.
        val saturdayAlarm = millis(24, 9, 0)
        assertEquals(millis(22, 9, 0), next(saturdayAlarm, RepeatRule.WEEKDAYS, now, zone))
    }

    @Test
    fun weeklyAlarmRepeatsOnTheSameWeekday() {
        // Programada un lunes 19 a las 9:00; hoy es miércoles 21: la próxima es el lunes 26.
        assertEquals(millis(26, 9, 0), next(millis(19, 9, 0), RepeatRule.WEEKLY, now, zone))
    }

    @Test
    fun weeklyAlarmEarlierToday() {
        // Programada un miércoles pasado a las 15:00: hoy a las 15:00 todavía no llega.
        assertEquals(millis(21, 15, 0), next(millis(14, 15, 0), RepeatRule.WEEKLY, now, zone))
    }
}

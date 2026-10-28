package com.apuntia.app.ui.alarms

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.apuntia.app.R
import com.apuntia.app.domain.model.RepeatRule
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

/** Nombre de cada opción de repetición, para los botones del editor. */
@Composable
fun RepeatRule.optionLabel(): String = stringResource(
    when (this) {
        RepeatRule.NONE -> R.string.alarm_repeat_none
        RepeatRule.DAILY -> R.string.alarm_repeat_daily
        RepeatRule.WEEKDAYS -> R.string.alarm_repeat_weekdays
        RepeatRule.WEEKLY -> R.string.alarm_repeat_weekly
    },
)

/** Texto de la tarjeta: "Hoy", "Mañana", "Lunes a viernes", "Todos los días" o "Cada lunes". */
@Composable
fun repeatSummary(rule: RepeatRule, triggerAt: Long): String {
    val zone = ZoneId.systemDefault()
    val date = Instant.ofEpochMilli(triggerAt).atZone(zone).toLocalDate()
    val weekday = weekdayName(date.dayOfWeek)
    return when (rule) {
        RepeatRule.NONE -> when (date) {
            LocalDate.now(zone) -> stringResource(R.string.alarm_today)
            LocalDate.now(zone).plusDays(1) -> stringResource(R.string.alarm_tomorrow)
            else -> weekday
        }
        RepeatRule.DAILY -> stringResource(R.string.alarm_repeat_daily)
        RepeatRule.WEEKDAYS -> stringResource(R.string.alarm_repeat_weekdays)
        RepeatRule.WEEKLY -> stringResource(R.string.alarm_every_weekday, weekday.lowercase(Locale.getDefault()))
    }
}

private fun weekdayName(day: DayOfWeek): String =
    day.getDisplayName(TextStyle.FULL, Locale.getDefault()).replaceFirstChar { it.titlecase(Locale.getDefault()) }

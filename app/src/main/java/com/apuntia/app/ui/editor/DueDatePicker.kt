package com.apuntia.app.ui.editor

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.text.format.DateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

private const val DEFAULT_HOUR = 9
private const val MONTH_OFFSET = 1

/**
 * Muestra el selector de fecha y, al elegirla, el de hora. Entrega el resultado en milisegundos UTC.
 *
 * @param context contexto de la pantalla (actividad).
 * @param initial fecha límite actual, o nulo para empezar en la fecha de hoy a las 9:00.
 * @param onPicked se llama con la fecha y hora elegidas.
 */
fun showDueDatePicker(context: Context, initial: Long?, onPicked: (Long) -> Unit) {
    val zone = ZoneId.systemDefault()
    val start = initial?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), zone) }
        ?: LocalDateTime.now(zone).withHour(DEFAULT_HOUR).withMinute(0)
    DatePickerDialog(
        context,
        { _, year, month, day ->
            // Android cuenta los meses desde 0.
            val date = start.withYear(year).withMonth(month + MONTH_OFFSET).withDayOfMonth(day)
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    onPicked(date.withHour(hour).withMinute(minute).atZone(zone).toInstant().toEpochMilli())
                },
                start.hour,
                start.minute,
                DateFormat.is24HourFormat(context),
            ).show()
        },
        start.year,
        start.monthValue - MONTH_OFFSET,
        start.dayOfMonth,
    ).show()
}

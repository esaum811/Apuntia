package com.apuntia.app.ui.common

import android.content.Context
import android.text.format.DateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

private const val NOON_HOUR = 12
private const val AM_PM_FALLBACK = 12

/**
 * Hora lista para mostrar, separada en cifras y período.
 *
 * @property digits la hora, por ejemplo "4:30".
 * @property period "a. m." o "p. m."; nulo si el teléfono usa formato de 24 horas.
 */
data class DisplayTime(val digits: String, val period: String?)

/** Convierte [hour] y [minute] al formato de 12 o 24 horas que use el teléfono. */
fun displayTime(context: Context, hour: Int, minute: Int): DisplayTime {
    val minutes = "%02d".format(Locale.ROOT, minute)
    if (DateFormat.is24HourFormat(context)) return DisplayTime("%02d:%s".format(Locale.ROOT, hour, minutes), null)
    val hour12 = (hour % NOON_HOUR).let { if (it == 0) AM_PM_FALLBACK else it }
    val period = if (hour < NOON_HOUR) "a. m." else "p. m."
    return DisplayTime("$hour12:$minutes", period)
}

/** Igual que [displayTime], pero a partir de una marca de tiempo en milisegundos. */
fun displayTime(context: Context, millis: Long): DisplayTime {
    val time = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
    return displayTime(context, time.hour, time.minute)
}

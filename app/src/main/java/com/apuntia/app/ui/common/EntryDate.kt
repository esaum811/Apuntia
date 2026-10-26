package com.apuntia.app.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.apuntia.app.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
private val dayFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())

/** Texto corto para una fecha: "Hoy, 4:30 p. m.", "Ayer" o "12 oct". */
@Composable
fun entryDateLabel(millis: Long): String {
    val zone = ZoneId.systemDefault()
    val date = Instant.ofEpochMilli(millis).atZone(zone)
    val today = LocalDate.now(zone)
    return when (date.toLocalDate()) {
        today -> stringResource(R.string.date_today, timeFormatter.format(date))
        today.minusDays(1) -> stringResource(R.string.date_yesterday)
        else -> dayFormatter.format(date)
    }
}

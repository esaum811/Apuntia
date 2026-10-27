package com.apuntia.app.domain.usecase

import com.apuntia.app.domain.model.Entry
import com.apuntia.app.domain.model.TaskSection
import com.apuntia.app.domain.model.TaskSectionType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private const val DAYS_IN_THIS_WEEK = 6L

/** Agrupa tareas por su fecha límite (atrasadas, hoy, esta semana, más adelante y sin fecha). */
class GroupTasksByDueDate {
    /**
     * Devuelve solo los grupos que tienen tareas, en orden de urgencia.
     *
     * @param tasks tareas a agrupar.
     * @param nowMillis momento actual en milisegundos UTC.
     * @param zone zona horaria con la que se decide qué día es cada fecha.
     */
    operator fun invoke(tasks: List<Entry>, nowMillis: Long, zone: ZoneId): List<TaskSection> {
        val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
        val byType = tasks.groupBy { sectionOf(it.dueAt, today, zone) }
        return TaskSectionType.entries.mapNotNull { type ->
            byType[type]?.let { TaskSection(type, it.sortedWith(compareBy(nullsLast<Long>()) { task -> task.dueAt })) }
        }
    }

    private fun sectionOf(dueAt: Long?, today: LocalDate, zone: ZoneId): TaskSectionType {
        if (dueAt == null) return TaskSectionType.NO_DATE
        val day = Instant.ofEpochMilli(dueAt).atZone(zone).toLocalDate()
        return when {
            day.isBefore(today) -> TaskSectionType.OVERDUE
            day == today -> TaskSectionType.TODAY
            !day.isAfter(today.plusDays(DAYS_IN_THIS_WEEK)) -> TaskSectionType.THIS_WEEK
            else -> TaskSectionType.LATER
        }
    }
}

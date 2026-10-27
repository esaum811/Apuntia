package com.apuntia.app.domain.usecase

import com.apuntia.app.domain.model.Entry
import com.apuntia.app.domain.model.EntryType
import com.apuntia.app.domain.model.TaskSectionType
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GroupTasksByDueDateTest {
    private val zone = ZoneId.of("America/Mexico_City")
    private val group = GroupTasksByDueDate()

    // "Ahora" fijo: miércoles 21 de octubre de 2026, 10:00 hora de Ciudad de México.
    private val now = millis(2026, 10, 21, 10, 0)

    private fun millis(year: Int, month: Int, day: Int, hour: Int, minute: Int) =
        LocalDateTime.of(year, month, day, hour, minute).atZone(zone).toInstant().toEpochMilli()

    private fun task(id: String, dueAt: Long?) = Entry(
        id = id, categoryId = "school", type = EntryType.TASK, title = id, body = "", isDone = false,
        dueAt = dueAt, isConfidential = false, isPinned = false, createdAt = 0, updatedAt = 0, deletedAt = null,
    )

    private fun typeOf(dueAt: Long?) = group(listOf(task("a", dueAt)), now, zone).single().type

    @Test
    fun emptyListGivesNoSections() {
        assertTrue(group(emptyList(), now, zone).isEmpty())
    }

    @Test
    fun taskWithoutDateGoesToNoDate() {
        assertEquals(TaskSectionType.NO_DATE, typeOf(null))
    }

    @Test
    fun yesterdayLateNightIsOverdue() {
        assertEquals(TaskSectionType.OVERDUE, typeOf(millis(2026, 10, 20, 23, 59)))
    }

    @Test
    fun earlierTodayStillCountsAsToday() {
        assertEquals(TaskSectionType.TODAY, typeOf(millis(2026, 10, 21, 8, 0)))
    }

    @Test
    fun laterTodayIsToday() {
        assertEquals(TaskSectionType.TODAY, typeOf(millis(2026, 10, 21, 23, 59)))
    }

    @Test
    fun tomorrowAndSixDaysAheadAreThisWeek() {
        assertEquals(TaskSectionType.THIS_WEEK, typeOf(millis(2026, 10, 22, 0, 0)))
        assertEquals(TaskSectionType.THIS_WEEK, typeOf(millis(2026, 10, 27, 23, 59)))
    }

    @Test
    fun sevenDaysAheadIsLater() {
        assertEquals(TaskSectionType.LATER, typeOf(millis(2026, 10, 28, 0, 0)))
    }

    @Test
    fun sectionsComeInOrderOfUrgencyAndSkipEmptyOnes() {
        val sections = group(
            listOf(
                task("later", millis(2026, 12, 1, 9, 0)),
                task("none", null),
                task("today", millis(2026, 10, 21, 18, 0)),
                task("overdue", millis(2026, 10, 1, 9, 0)),
            ),
            now,
            zone,
        )
        assertEquals(
            listOf(TaskSectionType.OVERDUE, TaskSectionType.TODAY, TaskSectionType.LATER, TaskSectionType.NO_DATE),
            sections.map { it.type },
        )
    }

    @Test
    fun tasksInsideASectionAreSortedByDueDate() {
        val sections = group(
            listOf(
                task("evening", millis(2026, 10, 21, 20, 0)),
                task("noon", millis(2026, 10, 21, 12, 0)),
            ),
            now,
            zone,
        )
        assertEquals(listOf("noon", "evening"), sections.single().tasks.map { it.id })
    }

    @Test
    fun dayIsDecidedInTheGivenTimeZoneNotInUtc() {
        // 23:30 del 21 en México ya es el día 22 en UTC; para el usuario sigue siendo "hoy".
        assertEquals(TaskSectionType.TODAY, typeOf(millis(2026, 10, 21, 23, 30)))
    }
}

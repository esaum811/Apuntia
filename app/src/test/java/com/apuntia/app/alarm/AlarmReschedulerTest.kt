package com.apuntia.app.alarm

import com.apuntia.app.domain.model.Reminder
import com.apuntia.app.domain.model.RepeatRule
import com.apuntia.app.domain.repository.ReminderRepository
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas de la reprogramación de alarmas tras reiniciar el teléfono. */
class AlarmReschedulerTest {
    private val zone = ZoneId.systemDefault()
    private val now = ZonedDateTime.of(2026, 10, 21, 10, 0, 0, 0, zone).toInstant().toEpochMilli()
    private val repo = FakeReminderRepository()
    private val scheduler = FakeScheduler()
    private val rescheduler = AlarmRescheduler(repo, scheduler, clock = { now })

    private fun reminder(id: String, at: ZonedDateTime, rule: RepeatRule, enabled: Boolean = true) = Reminder(
        id = id, entryId = null, categoryId = "c", label = "", triggerAt = at.toInstant().toEpochMilli(),
        repeatRule = rule, ringtoneUri = null, isEnabled = enabled, createdAt = 0,
    )

    @Test
    fun futureOneShotIsScheduledAgain() = runBlocking {
        val at = ZonedDateTime.of(2026, 10, 21, 18, 0, 0, 0, zone)
        repo.items += reminder("a", at, RepeatRule.NONE)
        rescheduler.rescheduleAll()
        assertEquals(listOf("a"), scheduler.scheduled)
    }

    @Test
    fun pastOneShotIsDisabledNotScheduled() = runBlocking {
        val at = ZonedDateTime.of(2026, 10, 21, 7, 0, 0, 0, zone)
        repo.items += reminder("a", at, RepeatRule.NONE)
        rescheduler.rescheduleAll()
        assertTrue(scheduler.scheduled.isEmpty())
        assertFalse(repo.items.single().isEnabled)
    }

    @Test
    fun pastDailyAlarmMovesToNextOccurrence() = runBlocking {
        val at = ZonedDateTime.of(2026, 10, 20, 7, 0, 0, 0, zone)
        repo.items += reminder("a", at, RepeatRule.DAILY)
        rescheduler.rescheduleAll()
        val expected = ZonedDateTime.of(2026, 10, 22, 7, 0, 0, 0, zone).toInstant().toEpochMilli()
        assertEquals(expected, repo.items.single().triggerAt)
        assertEquals(listOf("a"), scheduler.scheduled)
    }

    @Test
    fun disabledAlarmsAreIgnored() = runBlocking {
        val at = ZonedDateTime.of(2026, 10, 21, 18, 0, 0, 0, zone)
        repo.items += reminder("a", at, RepeatRule.DAILY, enabled = false)
        rescheduler.rescheduleAll()
        assertTrue(scheduler.scheduled.isEmpty())
    }
}

private class FakeScheduler : AlarmScheduler {
    val scheduled = mutableListOf<String>()
    override fun canScheduleExact() = true
    override fun schedule(reminder: Reminder): Boolean = scheduled.add(reminder.id)
    override fun scheduleSnooze(id: String, triggerAt: Long) = true
    override fun cancel(id: String) = Unit
}

private class FakeReminderRepository : ReminderRepository {
    val items = mutableListOf<Reminder>()
    override fun observeAll(): Flow<List<Reminder>> = MutableStateFlow(items.toList())
    override suspend fun getEnabled(): List<Reminder> = items.filter { it.isEnabled }
    override suspend fun get(id: String): Reminder? = items.firstOrNull { it.id == id }
    override suspend fun save(reminder: Reminder) {
        items.removeAll { it.id == reminder.id }
        items += reminder
    }
    override suspend fun setEnabled(id: String, isEnabled: Boolean) {
        items.replaceAll { if (it.id == id) it.copy(isEnabled = isEnabled) else it }
    }
    override suspend fun delete(id: String) {
        items.removeAll { it.id == id }
    }
}

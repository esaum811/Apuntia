package com.apuntia.app.data.repository

import com.apuntia.app.data.local.dao.ReminderDao
import com.apuntia.app.data.mapper.toDomain
import com.apuntia.app.data.mapper.toEntity
import com.apuntia.app.domain.model.Reminder
import com.apuntia.app.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Guarda las alarmas en la base de datos local. */
class RoomReminderRepository(private val dao: ReminderDao) : ReminderRepository {
    override fun observeAll(): Flow<List<Reminder>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getEnabled(): List<Reminder> = dao.getEnabled().map { it.toDomain() }

    override suspend fun get(id: String): Reminder? = dao.getById(id)?.toDomain()

    override suspend fun save(reminder: Reminder) = dao.upsert(reminder.toEntity())

    override suspend fun setEnabled(id: String, isEnabled: Boolean) = dao.setEnabled(id, isEnabled)

    override suspend fun delete(id: String) = dao.delete(id)
}

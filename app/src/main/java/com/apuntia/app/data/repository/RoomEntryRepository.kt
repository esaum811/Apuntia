package com.apuntia.app.data.repository

import com.apuntia.app.data.local.dao.EntryDao
import com.apuntia.app.data.mapper.toDomain
import com.apuntia.app.data.mapper.toEntity
import com.apuntia.app.domain.model.Entry
import com.apuntia.app.domain.repository.EntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val TRASH_DAYS = 30L
private const val MILLIS_PER_DAY = 24L * 60 * 60 * 1000

/**
 * Guarda las notas y tareas en la base de datos local.
 *
 * @param dao acceso a la tabla de entradas.
 * @param clock devuelve la hora actual en milisegundos; se cambia en las pruebas.
 */
class RoomEntryRepository(
    private val dao: EntryDao,
    private val clock: () -> Long = System::currentTimeMillis,
) : EntryRepository {
    override fun observeActive(categoryId: String?): Flow<List<Entry>> =
        (if (categoryId == null) dao.observeActive() else dao.observeByCategory(categoryId))
            .map { list -> list.map { it.toDomain() } }

    override fun observeTasks(isDone: Boolean): Flow<List<Entry>> =
        dao.observeTasks(isDone).map { list -> list.map { it.toDomain() } }

    override fun observeTrash(): Flow<List<Entry>> = dao.observeTrash().map { list -> list.map { it.toDomain() } }

    override suspend fun get(id: String): Entry? = dao.getById(id)?.toDomain()

    override suspend fun save(entry: Entry) {
        // Un contenido confidencial nunca debe quedar en texto plano; se habilita con el bloqueo por PIN.
        check(!entry.isConfidential) { "Las entradas confidenciales todavía no se pueden guardar" }
        dao.upsert(entry.copy(updatedAt = clock()).toEntity())
    }

    override suspend fun setDone(id: String, isDone: Boolean) = dao.setDone(id, isDone, clock())

    override suspend fun moveToTrash(id: String) = dao.moveToTrash(id, clock())

    override suspend fun restore(id: String) = dao.restore(id)

    override suspend fun deleteForever(id: String) = dao.deleteForever(id)

    override suspend fun purgeOldTrash() = dao.purgeTrashOlderThan(clock() - TRASH_DAYS * MILLIS_PER_DAY)
}

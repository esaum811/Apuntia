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

    override suspend fun save(entry: Entry, encryptedBody: ByteArray?) {
        // Un contenido confidencial nunca debe quedar en texto plano en la base de datos.
        require(entry.isConfidential == (encryptedBody != null)) {
            "Una entrada confidencial se guarda con su contenido cifrado, y una normal sin él"
        }
        require(!entry.isConfidential || entry.body.isEmpty()) { "El texto de una entrada confidencial va cifrado" }
        dao.upsert(entry.copy(updatedAt = clock()).toEntity(encryptedBody))
    }

    override suspend fun getEncryptedBody(id: String): ByteArray? = dao.getById(id)?.bodyEncrypted

    override suspend fun setDone(id: String, isDone: Boolean) = dao.setDone(id, isDone, clock())

    override suspend fun moveToTrash(id: String) = dao.moveToTrash(id, clock())

    override suspend fun moveDoneTasksToTrash() = dao.moveDoneTasksToTrash(clock())

    override suspend fun emptyTrash() = dao.emptyTrash()

    override suspend fun restore(id: String) = dao.restore(id)

    override suspend fun deleteForever(id: String) = dao.deleteForever(id)

    override suspend fun deleteConfidential() = dao.deleteConfidential()

    override suspend fun purgeOldTrash() = dao.purgeTrashOlderThan(clock() - TRASH_DAYS * MILLIS_PER_DAY)
}

package com.apuntia.app.data.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.apuntia.app.data.local.entity.EntryEntity
import kotlinx.coroutines.flow.Flow

/** Acceso a la tabla de notas y tareas. */
@Dao
interface EntryDao {

    /** Entradas activas (fuera de la papelera): primero las fijas y luego las más recientes. */
    @Query("SELECT * FROM entries WHERE deletedAt IS NULL ORDER BY isPinned DESC, updatedAt DESC")
    fun observeActive(): Flow<List<EntryEntity>>

    /** Entradas activas de una categoría, con el mismo orden que [observeActive]. */
    @Query(
        "SELECT * FROM entries WHERE deletedAt IS NULL AND categoryId = :categoryId " +
            "ORDER BY isPinned DESC, updatedAt DESC",
    )
    fun observeByCategory(categoryId: String): Flow<List<EntryEntity>>

    /** Tareas activas según estén terminadas o pendientes; las que vencen antes van primero. */
    @Query(
        "SELECT * FROM entries WHERE deletedAt IS NULL AND type = 'TASK' AND isDone = :isDone " +
            "ORDER BY dueAt IS NULL, dueAt ASC, updatedAt DESC",
    )
    fun observeTasks(isDone: Boolean): Flow<List<EntryEntity>>

    /** Entradas en la papelera, de la que se envió más recientemente a la más antigua. */
    @Query("SELECT * FROM entries WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    fun observeTrash(): Flow<List<EntryEntity>>

    /** Busca una entrada por su [id], o devuelve nulo si no existe. */
    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun getById(id: String): EntryEntity?

    /** Crea o actualiza una [entry]. */
    @Upsert
    suspend fun upsert(entry: EntryEntity)

    /** Marca una tarea como terminada o pendiente y actualiza su fecha de modificación. */
    @Query("UPDATE entries SET isDone = :isDone, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setDone(id: String, isDone: Boolean, updatedAt: Long)

    /** Envía una entrada a la papelera marcando el momento [deletedAt]. */
    @Query("UPDATE entries SET deletedAt = :deletedAt WHERE id = :id")
    suspend fun moveToTrash(id: String, deletedAt: Long)

    /** Envía a la papelera todas las tareas terminadas que siguen activas. */
    @Query("UPDATE entries SET deletedAt = :deletedAt WHERE type = 'TASK' AND isDone = 1 AND deletedAt IS NULL")
    suspend fun moveDoneTasksToTrash(deletedAt: Long)

    /** Borra para siempre todo lo que está en la papelera. */
    @Query("DELETE FROM entries WHERE deletedAt IS NOT NULL")
    suspend fun emptyTrash()

    /** Saca una entrada de la papelera. */
    @Query("UPDATE entries SET deletedAt = NULL WHERE id = :id")
    suspend fun restore(id: String)

    /** Borra una entrada para siempre. Sus alarmas se borran con ella. */
    @Query("DELETE FROM entries WHERE id = :id")
    suspend fun deleteForever(id: String)

    /** Borra para siempre todas las entradas confidenciales, incluidas las de la papelera. */
    @Query("DELETE FROM entries WHERE isConfidential = 1")
    suspend fun deleteConfidential()

    /** Borra para siempre lo que lleva en la papelera desde antes de [olderThan]. */
    @Query("DELETE FROM entries WHERE deletedAt IS NOT NULL AND deletedAt < :olderThan")
    suspend fun purgeTrashOlderThan(olderThan: Long)
}

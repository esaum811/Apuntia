package com.apuntia.app.domain.repository

import com.apuntia.app.domain.model.Entry
import kotlinx.coroutines.flow.Flow

/** Acceso a las notas y tareas sin depender de cómo se guardan. */
interface EntryRepository {
    /** Entradas activas, de todas las categorías o solo de [categoryId]; las fijas primero. */
    fun observeActive(categoryId: String? = null): Flow<List<Entry>>

    /** Tareas activas, pendientes o terminadas según [isDone]. */
    fun observeTasks(isDone: Boolean): Flow<List<Entry>>

    /** Entradas en la papelera. */
    fun observeTrash(): Flow<List<Entry>>

    /** Busca una entrada por [id], o devuelve nulo. */
    suspend fun get(id: String): Entry?

    /**
     * Guarda una entrada nueva o modificada y actualiza su fecha de modificación.
     * Las entradas confidenciales todavía no se pueden guardar: se habilitan con el bloqueo por PIN.
     */
    suspend fun save(entry: Entry)

    /** Marca una tarea como terminada o pendiente. */
    suspend fun setDone(id: String, isDone: Boolean)

    /** Envía una entrada a la papelera. */
    suspend fun moveToTrash(id: String)

    /** Envía a la papelera todas las tareas terminadas. */
    suspend fun moveDoneTasksToTrash()

    /** Borra para siempre todo lo que está en la papelera. */
    suspend fun emptyTrash()

    /** Saca una entrada de la papelera. */
    suspend fun restore(id: String)

    /** Borra una entrada para siempre. */
    suspend fun deleteForever(id: String)

    /** Borra para siempre lo que lleva más de 30 días en la papelera. */
    suspend fun purgeOldTrash()
}

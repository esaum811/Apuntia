package com.apuntia.app.domain.repository

import com.apuntia.app.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

/** Acceso a las alarmas sin depender de cómo se guardan. */
interface ReminderRepository {
    /** Todas las alarmas, de la que suena primero a la última. */
    fun observeAll(): Flow<List<Reminder>>

    /** Alarmas activas; se usa al reiniciar el teléfono para volver a programarlas. */
    suspend fun getEnabled(): List<Reminder>

    /** Busca una alarma por [id], o devuelve nulo. */
    suspend fun get(id: String): Reminder?

    /** Crea o actualiza una alarma. */
    suspend fun save(reminder: Reminder)

    /** Activa o desactiva una alarma. */
    suspend fun setEnabled(id: String, isEnabled: Boolean)

    /** Borra una alarma. */
    suspend fun delete(id: String)
}

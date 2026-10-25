package com.apuntia.app.data.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.apuntia.app.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

/** Acceso a la tabla de alarmas. */
@Dao
interface ReminderDao {

    /** Todas las alarmas, de la que suena primero a la última. */
    @Query("SELECT * FROM reminders ORDER BY triggerAt ASC")
    fun observeAll(): Flow<List<ReminderEntity>>

    /**
     * Alarmas activas. Se usa al reiniciar el teléfono para volver a programarlas,
     * porque Android las borra al apagarse.
     */
    @Query("SELECT * FROM reminders WHERE isEnabled = 1")
    suspend fun getEnabled(): List<ReminderEntity>

    /** Busca una alarma por su [id], o devuelve nulo si no existe. */
    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getById(id: String): ReminderEntity?

    /** Crea o actualiza una [reminder]. */
    @Upsert
    suspend fun upsert(reminder: ReminderEntity)

    /** Activa o desactiva la alarma con el [id] indicado. */
    @Query("UPDATE reminders SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setEnabled(id: String, isEnabled: Boolean)

    /** Borra la alarma con el [id] indicado. */
    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun delete(id: String)
}

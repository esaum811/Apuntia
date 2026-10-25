package com.apuntia.app.data.local.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.apuntia.app.data.local.dao.CategoryDao
import com.apuntia.app.data.local.dao.EntryDao
import com.apuntia.app.data.local.dao.ReminderDao
import com.apuntia.app.data.local.entity.CategoryEntity
import com.apuntia.app.data.local.entity.EntryEntity
import com.apuntia.app.data.local.entity.ReminderEntity

/**
 * Base de datos local de Apuntia (cifrada con SQLCipher).
 *
 * Versión 1: categorías, entradas y alarmas. Los adjuntos (Entrega 2) y el registro de acciones
 * de la IA (Entrega 3) se agregan después con una migración.
 */
@Database(
    entities = [CategoryEntity::class, EntryEntity::class, ReminderEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class ApuntiaDatabase : RoomDatabase() {

    /** Acceso a las categorías. */
    abstract fun categoryDao(): CategoryDao

    /** Acceso a las notas y tareas. */
    abstract fun entryDao(): EntryDao

    /** Acceso a las alarmas. */
    abstract fun reminderDao(): ReminderDao
}

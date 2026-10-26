package com.apuntia.app.di

import android.content.Context
import com.apuntia.app.data.local.database.ApuntiaDatabase
import com.apuntia.app.data.local.database.ApuntiaDatabaseFactory
import com.apuntia.app.data.repository.RoomCategoryRepository
import com.apuntia.app.data.repository.RoomEntryRepository
import com.apuntia.app.data.repository.RoomReminderRepository
import com.apuntia.app.domain.repository.CategoryRepository
import com.apuntia.app.domain.repository.EntryRepository
import com.apuntia.app.domain.repository.ReminderRepository
import com.apuntia.app.security.DatabaseKeyProvider

/**
 * Crea y guarda las piezas compartidas de la app (base de datos y repositorios).
 *
 * Todo se crea la primera vez que se pide, no al abrir la app, para no frenar el arranque.
 * La primera petición abre la base de datos cifrada: hazla fuera del hilo principal.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val database: ApuntiaDatabase by lazy {
        ApuntiaDatabaseFactory.create(appContext, DatabaseKeyProvider(appContext).getOrCreatePassphrase())
    }

    /** Categorías. */
    val categories: CategoryRepository by lazy { RoomCategoryRepository(database.categoryDao()) }

    /** Notas y tareas. */
    val entries: EntryRepository by lazy { RoomEntryRepository(database.entryDao()) }

    /** Alarmas. */
    val reminders: ReminderRepository by lazy { RoomReminderRepository(database.reminderDao()) }
}

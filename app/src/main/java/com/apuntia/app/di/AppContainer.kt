package com.apuntia.app.di

import android.content.Context
import android.util.Log
import com.apuntia.app.alarm.AlarmScheduler
import com.apuntia.app.alarm.AndroidAlarmScheduler
import com.apuntia.app.data.local.database.ApuntiaDatabase
import com.apuntia.app.data.local.database.ApuntiaDatabaseFactory
import com.apuntia.app.data.repository.RoomCategoryRepository
import com.apuntia.app.data.repository.RoomEntryRepository
import com.apuntia.app.data.repository.RoomReminderRepository
import com.apuntia.app.domain.repository.CategoryRepository
import com.apuntia.app.domain.repository.EntryRepository
import com.apuntia.app.domain.repository.ReminderRepository
import com.apuntia.app.security.DatabaseKeyProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

private const val TAG = "Apuntia"

/**
 * Crea y guarda las piezas compartidas de la app (base de datos y repositorios).
 *
 * La base de datos se abre con [open], fuera del hilo principal. La interfaz solo debe pedir los
 * repositorios cuando [status] sea [DatabaseStatus.READY].
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val mutableStatus = MutableStateFlow(DatabaseStatus.LOADING)

    /** Estado de la base de datos; la interfaz lo observa para decidir qué mostrar. */
    val status: StateFlow<DatabaseStatus> = mutableStatus.asStateFlow()

    private val database: ApuntiaDatabase by lazy {
        ApuntiaDatabaseFactory.create(appContext, DatabaseKeyProvider(appContext).getOrCreatePassphrase())
    }

    /** Programa y cancela las alarmas en el sistema. */
    val alarmScheduler: AlarmScheduler by lazy { AndroidAlarmScheduler(appContext) }

    /** Categorías. */
    val categories: CategoryRepository by lazy { RoomCategoryRepository(database.categoryDao()) }

    /** Notas y tareas. */
    val entries: EntryRepository by lazy { RoomEntryRepository(database.entryDao()) }

    /** Alarmas. */
    val reminders: ReminderRepository by lazy { RoomReminderRepository(database.reminderDao()) }

    /**
     * Abre la base de datos, crea las categorías por defecto y vacía la papelera vieja.
     * Actualiza [status]; si algo falla queda en [DatabaseStatus.FAILED] y se puede volver a llamar.
     */
    suspend fun open() = withContext(Dispatchers.IO) {
        mutableStatus.value = DatabaseStatus.LOADING
        // Se captura todo error de arranque (Keystore, SQLCipher, disco) para mostrar un aviso en vez de cerrarse.
        @Suppress("TooGenericExceptionCaught")
        try {
            categories.ensureDefaults()
            entries.purgeOldTrash()
            mutableStatus.value = DatabaseStatus.READY
        } catch (error: Exception) {
            Log.e(TAG, "No se pudo abrir la base de datos", error)
            mutableStatus.value = DatabaseStatus.FAILED
        }
    }
}

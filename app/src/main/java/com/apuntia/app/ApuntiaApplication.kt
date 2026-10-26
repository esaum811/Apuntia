package com.apuntia.app

import android.app.Application
import android.content.pm.ApplicationInfo
import android.os.StrictMode
import android.util.Log
import com.apuntia.app.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val TAG = "Apuntia"

/**
 * Clase que Android crea antes que cualquier pantalla.
 *
 * Crea el [container] con los repositorios, prepara las categorías por defecto en segundo plano
 * y, en la versión de depuración, activa StrictMode para avisar si algo bloquea la pantalla.
 */
class ApuntiaApplication : Application() {
    /** Piezas compartidas de la app. */
    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        val isDebuggable = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        if (isDebuggable) enableStrictMode()
        container = AppContainer(this)
        appScope.launch {
            container.categories.ensureDefaults()
            container.entries.purgeOldTrash()
            Log.i(TAG, "Base de datos lista: ${container.categories.observeAll().first().size} categorías")
        }
    }

    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build(),
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build(),
        )
    }
}

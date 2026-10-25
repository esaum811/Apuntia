package com.apuntia.app

import android.app.Application
import android.content.pm.ApplicationInfo
import android.os.StrictMode

/**
 * Clase que Android crea antes que cualquier pantalla.
 *
 * Por ahora solo activa StrictMode en la versión de depuración, que avisa en el Logcat cuando
 * la app hace trabajo pesado (leer disco o red) en el hilo principal.
 */
class ApuntiaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val isDebuggable = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        if (isDebuggable) enableStrictMode()
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

package com.apuntia.app.ui.common

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val TAG = "Apuntia"

/**
 * Lanza una acción en segundo plano sin que un fallo de la base de datos cierre la app.
 * Si algo falla, queda registrado en el Logcat y la pantalla sigue como estaba.
 */
fun CoroutineScope.launchSafely(block: suspend () -> Unit): Job = launch {
    @Suppress("TooGenericExceptionCaught")
    try {
        block()
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Log.e(TAG, "Error en una acción de la pantalla", error)
    }
}

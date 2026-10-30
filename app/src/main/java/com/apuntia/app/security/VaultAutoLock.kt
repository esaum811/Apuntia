package com.apuntia.app.security

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val DEFAULT_GRACE_MS = 60_000L

/**
 * Cierra la bóveda cuando la app pasa un rato en segundo plano.
 * Un cambio rápido a otra app (menos de [graceMillis]) no obliga a poner el PIN otra vez.
 *
 * @param lockAction cierra la bóveda.
 * @param scope donde corre la cuenta regresiva mientras la app está en segundo plano.
 * @param graceMillis tiempo en segundo plano que se tolera antes de cerrar.
 * @param clock hora actual en milisegundos; se cambia en las pruebas.
 */
class VaultAutoLock(
    private val lockAction: () -> Unit,
    private val scope: CoroutineScope,
    private val graceMillis: Long = DEFAULT_GRACE_MS,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private var job: Job? = null
    private var backgroundedAt: Long? = null

    /** Avisa que la app pasó a segundo plano. */
    fun onBackgrounded() {
        backgroundedAt = clock()
        job?.cancel()
        job = scope.launch {
            delay(graceMillis)
            lockAction()
        }
    }

    /** Avisa que la app volvió a primer plano. Si estuvo fuera demasiado tiempo, cierra la bóveda. */
    fun onForegrounded() {
        job?.cancel()
        job = null
        val since = backgroundedAt
        backgroundedAt = null
        if (since != null && clock() - since >= graceMillis) lockAction()
    }
}

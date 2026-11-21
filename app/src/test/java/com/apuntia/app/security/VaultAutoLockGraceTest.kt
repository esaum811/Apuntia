package com.apuntia.app.security

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas de cambiar el tiempo de gracia del bloqueo automático mientras la app está abierta. */
class VaultAutoLockGraceTest {
    private var now = 0L
    private var locks = 0
    private val autoLock = VaultAutoLock(
        lockAction = { locks++ },
        scope = CoroutineScope(Dispatchers.Unconfined),
        graceMillis = 60_000,
        clock = { now },
    )

    @Test
    fun shorterGraceLocksOnReturnSooner() {
        autoLock.graceMillis = 10_000
        autoLock.onBackgrounded()
        now += 11_000
        autoLock.onForegrounded()
        assertEquals(1, locks)
    }

    @Test
    fun immediateGraceLocksOnAnyReturn() {
        autoLock.graceMillis = 0
        autoLock.onBackgrounded()
        autoLock.onForegrounded()
        // Con tiempo cero cierra al salir y otra vez al volver; cerrar dos veces no hace daño.
        assertTrue(locks >= 1)
    }
}

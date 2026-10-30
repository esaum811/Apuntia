package com.apuntia.app.security

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.junit.Assert.assertEquals
import org.junit.Test

/** Pruebas del cierre automático al pasar tiempo en segundo plano. */
class VaultAutoLockTest {
    private var now = 0L
    private var locks = 0
    private val autoLock = VaultAutoLock(
        lockAction = { locks++ },
        scope = CoroutineScope(Job()),
        graceMillis = 60_000,
        clock = { now },
    )

    @Test
    fun quickReturnKeepsTheVaultOpen() {
        autoLock.onBackgrounded()
        now += 10_000
        autoLock.onForegrounded()
        assertEquals(0, locks)
    }

    @Test
    fun longAbsenceLocksTheVaultOnReturn() {
        autoLock.onBackgrounded()
        now += 61_000
        autoLock.onForegrounded()
        assertEquals(1, locks)
    }

    @Test
    fun foregroundWithoutGoingToBackgroundDoesNothing() {
        autoLock.onForegrounded()
        assertEquals(0, locks)
    }
}

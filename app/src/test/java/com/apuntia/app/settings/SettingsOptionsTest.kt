package com.apuntia.app.settings

import org.junit.Assert.assertEquals
import org.junit.Test

/** Pruebas de las opciones guardadas en las preferencias. */
class SettingsOptionsTest {
    @Test
    fun unknownThemeFallsBackToSystem() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromName(null))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromName("otro"))
        assertEquals(ThemeMode.DARK, ThemeMode.fromName("DARK"))
    }

    @Test
    fun unknownAutoLockFallsBackToOneMinute() {
        assertEquals(AutoLock.MINUTE_1, AutoLock.fromName(null))
        assertEquals(AutoLock.MINUTE_1, AutoLock.fromName("otro"))
        assertEquals(AutoLock.MINUTES_5, AutoLock.fromName("MINUTES_5"))
    }

    @Test
    fun autoLockTimesGrowInOrder() {
        val times = AutoLock.entries.map { it.millis }
        assertEquals(times.sorted(), times)
        assertEquals(0L, AutoLock.IMMEDIATE.millis)
        assertEquals(60_000L, AutoLock.MINUTE_1.millis)
    }
}

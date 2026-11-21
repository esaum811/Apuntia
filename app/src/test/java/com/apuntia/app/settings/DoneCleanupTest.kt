package com.apuntia.app.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DoneCleanupTest {
    @Test
    fun neverHasNoDays() = assertNull(DoneCleanup.NEVER.days)

    @Test
    fun optionsMapToDays() {
        assertEquals(7, DoneCleanup.AFTER_7_DAYS.days)
        assertEquals(30, DoneCleanup.AFTER_30_DAYS.days)
    }

    @Test
    fun unknownNameFallsBackToNever() {
        assertEquals(DoneCleanup.NEVER, DoneCleanup.fromName("otra"))
        assertEquals(DoneCleanup.NEVER, DoneCleanup.fromName(null))
        assertEquals(DoneCleanup.AFTER_7_DAYS, DoneCleanup.fromName("AFTER_7_DAYS"))
    }
}

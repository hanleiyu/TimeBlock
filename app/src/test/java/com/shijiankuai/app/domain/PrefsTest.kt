package com.shijiankuai.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrefsTest {
    @Test
    fun showAllTimeDefaultsOn() {
        assertTrue(Prefs.showAllTimeEnabled(null))
        assertTrue(Prefs.showAllTimeEnabled("1"))
        assertFalse(Prefs.showAllTimeEnabled("0"))
    }

    @Test
    fun autoBackupDefaultsOn() {
        assertTrue(Prefs.autoBackupEnabled(null))
        assertTrue(Prefs.autoBackupEnabled("1"))
        assertFalse(Prefs.autoBackupEnabled("0"))
    }

    @Test
    fun darkThemeDefaultsOn() {
        assertTrue(Prefs.darkThemeEnabled(null))
        assertTrue(Prefs.darkThemeEnabled("1"))
        assertFalse(Prefs.darkThemeEnabled("0"))
    }

    @Test
    fun lastAutoBackupAtParsesMillis() {
        assertEquals(null, Prefs.lastAutoBackupAt(null))
        assertEquals(null, Prefs.lastAutoBackupAt("nope"))
        assertEquals(1_700_000_000_000L, Prefs.lastAutoBackupAt("1700000000000")?.toEpochMilli())
    }
}

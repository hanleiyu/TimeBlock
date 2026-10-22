package com.shijiankuai.app.domain

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
}

package com.shijiankuai.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class PresetsTest {
    @Test
    fun typeColorsMatchFinalPalette() {
        val byId = Presets.types.associate { it.id to it.color }
        assertEquals(0xFF808BD0, byId.getValue(SLEEP_TYPE_ID))
        assertEquals(0xFF8EBFD2, byId.getValue("work"))
        assertEquals(0xFFE5AD8C, byId.getValue("eat"))
        assertEquals(0xFFE3C783, byId.getValue("play"))
        assertEquals(0xFFDE8B9D, byId.getValue("read"))
        assertEquals(0xFF9CCB97, byId.getValue("social"))
        assertEquals(0xFFA996CF, byId.getValue("study"))
        assertEquals(0xFF74B3AE, byId.getValue("sport"))
    }

    @Test
    fun swatchesStayInTheSoftFamily() {
        assertEquals(Presets.types.map { it.color } + Presets.extraColors, Presets.swatches)
        assertEquals(12, Presets.swatches.size)
    }
}

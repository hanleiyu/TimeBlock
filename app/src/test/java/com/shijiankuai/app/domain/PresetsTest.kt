package com.shijiankuai.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PresetsTest {
    @Test
    fun typeColorsMatchFinalPalette() {
        val byId = Presets.types.associate { it.id to it.color }
        assertEquals(0xFF808BD0, byId.getValue(SLEEP_TYPE_ID))
        assertEquals(0xFF8EBFD2, byId.getValue("work"))
        assertEquals(0xFFA8A6A3, byId.getValue("commute"))
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
        assertEquals(13, Presets.swatches.size)
    }

    @Test
    fun commuteSitsAfterWorkAndIsDistinct() {
        assertEquals(listOf("work", "commute"), Presets.types.take(2).map { it.id })
        val commute = Presets.types.single { it.id == "commute" }
        assertEquals("通勤", commute.name)
        assertTrue(rgbDist(commute.color, 0xFFEDE6E0) > 50)
        assertTrue(rgbDist(commute.color, 0xFF8EBFD2) > 40)
    }

    private fun rgbDist(a: Long, b: Long): Double {
        fun chan(rgb: Long, shift: Int) = ((rgb shr shift) and 0xFF).toDouble()
        val dr = chan(a, 16) - chan(b, 16)
        val dg = chan(a, 8) - chan(b, 8)
        val db = chan(a, 0) - chan(b, 0)
        return kotlin.math.sqrt(dr * dr + dg * dg + db * db)
    }
}

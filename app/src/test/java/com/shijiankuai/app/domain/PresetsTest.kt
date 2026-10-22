package com.shijiankuai.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PresetsTest {
    @Test
    fun typeColorsMatchFinalPalette() {
        val byId = Presets.types.associate { it.id to it.color }
        assertEquals(0xFF808BD0L, byId.getValue(SLEEP_TYPE_ID))
        assertEquals(0xFF8EBFD2L, byId.getValue("work"))
        assertEquals(0xFFE5AD8CL, byId.getValue("eat"))
        assertEquals(0xFFE3C783L, byId.getValue("play"))
        assertEquals(0xFFDE8B9DL, byId.getValue("read"))
        assertEquals(0xFF9CCB97L, byId.getValue("social"))
        assertEquals(0xFFA996CFL, byId.getValue("study"))
        assertEquals(0xFF74B3AEL, byId.getValue("sport"))
        assertEquals(0xFFA8A6A3L, byId.getValue("other"))
    }

    @Test
    fun swatchesStayInTheSoftFamily() {
        assertEquals(Presets.types.map { it.color } + Presets.extraColors, Presets.swatches)
        assertEquals(13, Presets.swatches.size)
    }

    @Test
    fun otherSitsLastAfterSportAndIsDistinct() {
        assertEquals(listOf("sport", "other"), Presets.types.takeLast(2).map { it.id })
        val other = Presets.types.single { it.id == "other" }
        assertEquals("其他", other.name)
        assertTrue(Presets.types.none { it.id == "commute" || it.name == "通勤" })
        assertTrue(rgbDist(other.color, 0xFFEDE6E0) > 50)
        assertTrue(rgbDist(other.color, 0xFF8EBFD2) > 40)
    }

    private fun rgbDist(a: Long, b: Long): Double {
        fun chan(rgb: Long, shift: Int) = ((rgb shr shift) and 0xFF).toDouble()
        val dr = chan(a, 16) - chan(b, 16)
        val dg = chan(a, 8) - chan(b, 8)
        val db = chan(a, 0) - chan(b, 0)
        return kotlin.math.sqrt(dr * dr + dg * dg + db * db)
    }
}

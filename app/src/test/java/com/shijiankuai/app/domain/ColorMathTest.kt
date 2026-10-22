package com.shijiankuai.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class ColorMathTest {
    @Test
    fun primaryHuesMapToRgb() {
        assertEquals(0xFFFF0000L, ColorMath.hsvToArgb(0f, 1f, 1f))
        assertEquals(0xFF00FF00L, ColorMath.hsvToArgb(120f, 1f, 1f))
        assertEquals(0xFF0000FFL, ColorMath.hsvToArgb(240f, 1f, 1f))
        assertEquals(0xFF000000L, ColorMath.hsvToArgb(0f, 0f, 0f))
        assertEquals(0xFFFFFFFFL, ColorMath.hsvToArgb(0f, 0f, 1f))
    }

    @Test
    fun hsvRoundtripKeepsPresetSwatches() {
        Presets.swatches.forEach { argb ->
            val hsv = ColorMath.argbToHsv(argb)
            val back = ColorMath.hsvToArgb(hsv.h, hsv.s, hsv.v)
            assertTrue(
                "roundtrip $argb -> $back",
                channelDist(argb, back) <= 3,
            )
        }
    }

    @Test
    fun hexParseAndFormat() {
        assertEquals(0xFF8EBFD2L, ColorMath.parseHex("#8EBFD2"))
        assertEquals(0xFF8EBFD2L, ColorMath.parseHex("8ebfd2"))
        assertEquals(0xFF8EBFD2L, ColorMath.parseHex("#FF8EBFD2"))
        assertEquals("#8EBFD2", ColorMath.toHex(0xFF8EBFD2L))
        assertNull(ColorMath.parseHex("zz"))
        assertNull(ColorMath.parseHex(""))
    }

    @Test
    fun signExtendedLongMatchesCanonicalArgb() {
        val stored = 0xFF8EBFD2.toLong()
        val picked = ColorMath.parseHex("#8EBFD2")!!
        assertTrue(ColorMath.same(stored, picked))
        assertEquals(0xFF8EBFD2L, ColorMath.normalize(stored))
        val hsv = ColorMath.argbToHsv(stored)
        assertTrue(channelDist(ColorMath.hsvToArgb(hsv.h, hsv.s, hsv.v), picked) <= 3)
    }

    private fun channelDist(a: Long, b: Long): Int {
        fun ch(c: Long, shift: Int) = ((c shr shift) and 0xFF).toInt()
        return abs(ch(a, 16) - ch(b, 16)) +
            abs(ch(a, 8) - ch(b, 8)) +
            abs(ch(a, 0) - ch(b, 0))
    }
}

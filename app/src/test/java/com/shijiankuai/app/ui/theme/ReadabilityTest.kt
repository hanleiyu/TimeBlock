package com.shijiankuai.app.ui.theme

import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadabilityTest {
    private val paper = 0xFBF5F1L
    private val darkPaper = 0x000000L

    @Test
    fun inkTokensMeetWcagAaOnPaper() {
        assertTrue(Contrast.ratio(0x2B2E45, paper) >= 4.5)
        assertTrue(Contrast.ratio(0x5C5F75, paper) >= 4.5)
        assertTrue(Contrast.ratio(0x5E6178, paper) >= 4.5)
    }

    @Test
    fun darkInkTokensMeetWcagAaOnBlack() {
        assertTrue(Contrast.ratio(0xF4F4F5, darkPaper) >= 4.5)
        assertTrue(Contrast.ratio(0xB4B4BB, darkPaper) >= 4.5)
        assertTrue(Contrast.ratio(0x8A8A93, darkPaper) >= 4.5)
    }

    @Test
    fun darkSelectedGrayFillReadableOnChip() {
        val chip = 0x1C1C1FL
        val selected = 0xE8E8EDL
        assertTrue(Contrast.ratio(selected, chip) >= 3.0)
        assertTrue(Contrast.ratio(0x000000, selected) >= 4.5)
    }

    @Test
    fun darkDaySelectRingReadableOnBlack() {
        assertTrue(Contrast.ratio(0x00E676, darkPaper) >= 3.0)
        assertTrue(Contrast.ratio(0x6B8F71, paper) >= 3.0)
    }

    @Test
    fun healthMarksReadableOnDarkBlack() {
        assertTrue(Contrast.ratio(0x00E676, darkPaper) >= 3.0)
        assertTrue(Contrast.ratio(0x8B82F6, darkPaper) >= 3.0)
        assertTrue(Contrast.ratio(0xF5A524, darkPaper) >= 3.0)
        assertTrue(Contrast.ratio(0xFF4D6D, darkPaper) >= 3.0)
        assertTrue(Contrast.ratio(0x5EB0FF, darkPaper) >= 3.0)
        assertTrue(Contrast.ratio(0xFFDB58, darkPaper) >= 3.0)
        assertTrue(Contrast.ratio(0xFFC14A, darkPaper) >= 3.0)
    }

    @Test
    fun typographyBumpsOneStep() {
        assertEquals(FontWeight.Medium, bumped(FontWeight.Normal))
        assertEquals(FontWeight.SemiBold, bumped(FontWeight.Medium))
        assertEquals(FontWeight.Bold, bumped(FontWeight.SemiBold))
        assertEquals(FontWeight.Medium, bumped(null))
    }
}

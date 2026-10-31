package com.shijiankuai.app.ui.theme

import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadabilityTest {
    private val paper = 0xFBF5F1L
    private val darkPaper = 0x1C1A1FL

    @Test
    fun inkTokensMeetWcagAaOnPaper() {
        assertTrue(Contrast.ratio(0x2B2E45, paper) >= 4.5)
        assertTrue(Contrast.ratio(0x5C5F75, paper) >= 4.5)
        assertTrue(Contrast.ratio(0x5E6178, paper) >= 4.5)
    }

    @Test
    fun darkInkTokensMeetWcagAaOnPaper() {
        assertTrue(Contrast.ratio(0xF4EFE8, darkPaper) >= 4.5)
        assertTrue(Contrast.ratio(0xC4BEC8, darkPaper) >= 4.5)
        assertTrue(Contrast.ratio(0x9E98A6, darkPaper) >= 4.5)
    }

    @Test
    fun healthMarksReadableOnDarkPaper() {
        assertTrue(Contrast.ratio(0xC45B5B, darkPaper) >= 3.0)
        assertTrue(Contrast.ratio(0x6A8C9E, darkPaper) >= 3.0)
        assertTrue(Contrast.ratio(0xE4C35A, darkPaper) >= 3.0)
        assertTrue(Contrast.ratio(0x6B8F71, darkPaper) >= 3.0)
        assertTrue(Contrast.ratio(0x7A7EB0, darkPaper) >= 3.0)
        assertTrue(Contrast.ratio(0xD99A76, darkPaper) >= 3.0)
    }

    @Test
    fun typographyBumpsOneStep() {
        assertEquals(FontWeight.Medium, bumped(FontWeight.Normal))
        assertEquals(FontWeight.SemiBold, bumped(FontWeight.Medium))
        assertEquals(FontWeight.Bold, bumped(FontWeight.SemiBold))
        assertEquals(FontWeight.Medium, bumped(null))
    }
}

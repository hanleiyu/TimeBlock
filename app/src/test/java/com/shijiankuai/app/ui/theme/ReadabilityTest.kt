package com.shijiankuai.app.ui.theme

import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadabilityTest {
    private val paper = 0xFBF5F1L

    @Test
    fun inkTokensMeetWcagAaOnPaper() {
        assertTrue(Contrast.ratio(0x2B2E45, paper) >= 4.5)
        assertTrue(Contrast.ratio(0x5C5F75, paper) >= 4.5)
        assertTrue(Contrast.ratio(0x5E6178, paper) >= 4.5)
    }

    @Test
    fun typographyBumpsOneStep() {
        assertEquals(FontWeight.Medium, bumped(FontWeight.Normal))
        assertEquals(FontWeight.SemiBold, bumped(FontWeight.Medium))
        assertEquals(FontWeight.Bold, bumped(FontWeight.SemiBold))
        assertEquals(FontWeight.Medium, bumped(null))
    }
}

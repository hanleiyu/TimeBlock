package com.shijiankuai.app.ui.chrome

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayFabSpecTest {
    @Test
    fun overlayFabDoesNotConsumeColumnHeight() {
        assertFalse(OverlayFabSpec.consumesLayoutHeight)
        assertEquals(56, OverlayFabSpec.sizeDp)
        assertEquals(20, OverlayFabSpec.endPaddingDp)
        assertEquals(20, OverlayFabSpec.bottomPaddingDp)
        assertTrue(OverlayFabSpec.sizeDp >= OverlayFabSpec.minTouchTargetDp)
        assertTrue(OverlayFabSpec.minTouchTargetDp >= 48)
        assertEquals(88, OverlayFabSpec.scrollClearanceDp)
        assertEquals("＋ 记录", OverlayFabSpec.healthRecordDescription)
        assertEquals("记录事件", OverlayFabSpec.lifeEventDescription)
    }
}

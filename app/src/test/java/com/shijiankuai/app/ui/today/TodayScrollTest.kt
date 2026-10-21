package com.shijiankuai.app.ui.today

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TodayScrollTest {
    private val day = LocalDate.of(2026, 10, 21)

    @Test
    fun snapOnlyOnFirstOpenOfThatDay() {
        assertTrue(TodayScroll.shouldSnapToNow(alreadySnappedOn = null, viewing = day, isToday = true))
        assertFalse(TodayScroll.shouldSnapToNow(alreadySnappedOn = day, viewing = day, isToday = true))
        assertTrue(TodayScroll.shouldSnapToNow(alreadySnappedOn = day, viewing = day.plusDays(1), isToday = true))
        assertFalse(TodayScroll.shouldSnapToNow(alreadySnappedOn = null, viewing = day, isToday = false))
    }

    @Test
    fun nowOffsetIsInstantAndDoesNotStartAtTop() {
        val row = 44f
        val y = TodayScroll.nowOffsetPx(nowIndex = 43, rowHeightPx = row, topPadPx = 80f)
        assertEquals((7 * 44 - 80).coerceAtLeast(0), y)
        assertTrue(y > 0)
    }
}

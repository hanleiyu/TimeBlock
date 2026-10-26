package com.shijiankuai.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class StatsNavTest {
    private val picked = LocalDate.of(2026, 10, 21)
    private val today = LocalDate.of(2026, 10, 26)

    @Test
    fun pickKeepsAnchorAndKindSelectsRange() {
        assertEquals(picked, StatsNav.pick(picked))
        assertEquals(picked to picked, StatsCalc.periodBounds(PeriodKind.DAY, StatsNav.pick(picked)))
        assertEquals(
            LocalDate.of(2026, 10, 19) to LocalDate.of(2026, 10, 25),
            StatsCalc.periodBounds(PeriodKind.WEEK, StatsNav.pick(picked)),
        )
        assertEquals(
            LocalDate.of(2026, 10, 1) to LocalDate.of(2026, 10, 31),
            StatsCalc.periodBounds(PeriodKind.MONTH, StatsNav.pick(picked)),
        )
        assertEquals(
            LocalDate.of(2026, 1, 1) to LocalDate.of(2026, 12, 31),
            StatsCalc.periodBounds(PeriodKind.YEAR, StatsNav.pick(picked)),
        )
    }

    @Test
    fun switchingKindKeepsTheSameAnchor() {
        val anchor = StatsNav.pick(picked)
        val day = StatsCalc.periodBounds(PeriodKind.DAY, anchor)
        val week = StatsCalc.periodBounds(PeriodKind.WEEK, anchor)
        assertEquals(picked, day.first)
        assertTrue(!picked.isBefore(week.first) && !picked.isAfter(week.second))
        assertEquals(picked, anchor)
    }

    @Test
    fun jumpTodayLandsOnCurrentPeriod() {
        val anchor = StatsNav.jumpToday(today)
        assertEquals(today, anchor)
        assertTrue(StatsNav.contains(PeriodKind.DAY, anchor, today))
        assertTrue(StatsNav.contains(PeriodKind.WEEK, anchor, today))
        assertTrue(StatsNav.contains(PeriodKind.MONTH, anchor, today))
        assertTrue(StatsNav.contains(PeriodKind.YEAR, anchor, today))
        assertFalse(StatsNav.contains(PeriodKind.DAY, StatsNav.pick(picked), today))
    }

    @Test
    fun shiftStepsByCurrentKind() {
        assertEquals(picked.plusDays(1), StatsNav.shift(PeriodKind.DAY, picked, 1))
        assertEquals(picked.minusWeeks(1), StatsNav.shift(PeriodKind.WEEK, picked, -1))
        assertEquals(picked.plusMonths(1), StatsNav.shift(PeriodKind.MONTH, picked, 1))
        assertEquals(picked.minusYears(1), StatsNav.shift(PeriodKind.YEAR, picked, -1))
    }
}

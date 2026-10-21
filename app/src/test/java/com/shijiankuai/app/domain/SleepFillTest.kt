package com.shijiankuai.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class SleepFillTest {
    private val zone: ZoneId = ZoneId.of("Asia/Shanghai")
    private val day = LocalDate.of(2026, 10, 21)

    private fun instant(date: LocalDate, hour: Int, minute: Int, second: Int = 0) =
        LocalDateTime.of(date, java.time.LocalTime.of(hour, minute, second))
            .atZone(zone)
            .toInstant()

    @Test
    fun sleepStartingAt0042_doesNotFillMidnight() {
        val start = instant(day, 0, 42)
        val wake = instant(day, 7, 12)
        val filled = SleepFill.blocks(start, wake, zone)

        assertFalse("00:00–00:40 must stay empty", filled.any { it.index < 4 && it.epochDay == day.toEpochDay() })
        assertTrue("00:40–00:50 is majority sleep (8 min)", BlockId(day.toEpochDay(), 4) in filled)
        assertTrue("07:00–07:10 is full sleep", BlockId(day.toEpochDay(), 42) in filled)
        assertFalse("07:10–07:20 is only 2 min", filled.any { it.index == 43 })
        assertEquals(39, filled.size)
        assertTrue(filled.all { it.epochDay == day.toEpochDay() })
    }

    @Test
    fun sleepCrossingMidnight_fillsPreviousDayTail() {
        val prev = day.minusDays(1)
        val start = instant(prev, 23, 30)
        val wake = instant(day, 7, 12)
        val filled = SleepFill.blocks(start, wake, zone)

        val prevBlocks = filled.filter { it.epochDay == prev.toEpochDay() }.map { it.index }.sorted()
        assertEquals(listOf(141, 142, 143), prevBlocks)

        val todayBlocks = filled.filter { it.epochDay == day.toEpochDay() }
        assertEquals(43, todayBlocks.size)
        assertTrue(BlockId(day.toEpochDay(), 0) in filled)
        assertTrue(BlockId(day.toEpochDay(), 42) in filled)
        assertFalse(filled.any { it.index == 43 && it.epochDay == day.toEpochDay() })
        assertEquals(46, filled.size)
    }

    @Test
    fun majorityThreshold_fiveMinutesIncluded() {
        val start = instant(day, 0, 42)
        val wake = instant(day, 0, 47)
        val filled = SleepFill.blocks(start, wake, zone)
        assertEquals(listOf(BlockId(day.toEpochDay(), 4)), filled)
    }

    @Test
    fun majorityThreshold_fourMinutesExcluded() {
        val start = instant(day, 0, 42)
        val wake = instant(day, 0, 46)
        assertTrue(SleepFill.blocks(start, wake, zone).isEmpty())
    }

    @Test
    fun emptyIfWakeNotAfterStart() {
        val t = instant(day, 7, 0)
        assertTrue(SleepFill.blocks(t, t, zone).isEmpty())
        assertTrue(SleepFill.blocks(t, t.minusSeconds(60), zone).isEmpty())
    }
}

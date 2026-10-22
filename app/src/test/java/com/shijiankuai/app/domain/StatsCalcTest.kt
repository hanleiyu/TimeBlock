package com.shijiankuai.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class StatsCalcTest {
    private val zone: ZoneId = ZoneId.of("Asia/Shanghai")
    private val day = LocalDate.of(2026, 10, 21)
    private val now = LocalDateTime.of(2026, 10, 21, 14, 20).atZone(zone).toInstant()
    private val types = Presets.types

    private fun bid(date: LocalDate, index: Int) = BlockId(date.toEpochDay(), index)

    @Test
    fun todayIgnoresFutureUnfilledAndFutureFilled() {
        val blocks = mutableMapOf<BlockId, String>()
        repeat(10) { blocks[bid(day, it)] = SLEEP_TYPE_ID }
        // planned future work — must not appear
        blocks[bid(day, 100)] = "work"

        val stats = StatsCalc.aggregate(PeriodKind.DAY, day, now, zone, blocks, types)
        assertEquals(86, stats.totalElapsed)
        assertEquals(10, stats.filled)
        assertEquals(76, stats.unfilled)
        assertEquals(1, stats.byType.size)
        assertEquals(SLEEP_TYPE_ID, stats.byType[0].typeId)
        assertEquals(10, stats.byType[0].blocks)
    }

    @Test
    fun pastDayCountsAll144() {
        val past = day.minusDays(1)
        val blocks = (0 until 40).associate { bid(past, it) to SLEEP_TYPE_ID }
        val stats = StatsCalc.aggregate(PeriodKind.DAY, past, now, zone, blocks, types)
        assertEquals(144, stats.totalElapsed)
        assertEquals(40, stats.filled)
        assertEquals(104, stats.unfilled)
    }

    @Test
    fun futureDayIsEmpty() {
        val stats = StatsCalc.aggregate(PeriodKind.DAY, day.plusDays(1), now, zone, emptyMap(), types)
        assertEquals(0, stats.totalElapsed)
        assertEquals(0, stats.filled)
        assertEquals(0, stats.unfilled)
    }

    @Test
    fun weekStartsMondayAndSumsElapsed() {
        // 2026-10-21 is Wednesday. Week is Mon 19 – Sun 25.
        val mon = LocalDate.of(2026, 10, 19)
        val (start, end) = StatsCalc.periodBounds(PeriodKind.WEEK, day)
        assertEquals(mon, start)
        assertEquals(LocalDate.of(2026, 10, 25), end)

        val blocks = buildMap {
            // Monday full sleep night leftover 20 blocks
            repeat(20) { put(bid(mon, it), SLEEP_TYPE_ID) }
            // Today 10 work in the morning
            repeat(10) { put(bid(day, 50 + it), "work") }
        }
        val stats = StatsCalc.aggregate(PeriodKind.WEEK, day, now, zone, blocks, types)
        // Mon 144 + Tue 144 + Wed 86 = 374
        assertEquals(374, stats.totalElapsed)
        assertEquals(30, stats.filled)
        assertEquals(344, stats.unfilled)
        assertEquals(listOf(SLEEP_TYPE_ID, "work"), stats.byType.map { it.typeId })
    }

    @Test
    fun hoursFromBlocks() {
        assertEquals(46 * 10 / 60.0, TypeCount(SLEEP_TYPE_ID, "睡觉", 0L, 46).hours, 1e-9)
    }

    @Test
    fun fullSpanDayIsAlways144AndKeepsFutureRecorded() {
        val blocks = mutableMapOf<BlockId, String>()
        repeat(10) { blocks[bid(day, it)] = SLEEP_TYPE_ID }
        blocks[bid(day, 100)] = "work"

        val stats = StatsCalc.aggregate(PeriodKind.DAY, day, now, zone, blocks, types, fullSpan = true)
        assertEquals(144, stats.totalElapsed)
        assertEquals(11, stats.filled)
        assertEquals(133, stats.unfilled)
        assertEquals(true, stats.fullSpan)
        assertEquals(listOf(SLEEP_TYPE_ID, "work"), stats.byType.map { it.typeId })
    }

    @Test
    fun fullSpanWeekIsSevenDaysAndFutureDayIsEmptySlice() {
        val empty = StatsCalc.aggregate(PeriodKind.WEEK, day, now, zone, emptyMap(), types, fullSpan = true)
        assertEquals(7 * 144, empty.totalElapsed)
        assertEquals(0, empty.filled)
        assertEquals(7 * 144, empty.unfilled)

        val tomorrow = StatsCalc.aggregate(PeriodKind.DAY, day.plusDays(1), now, zone, emptyMap(), types, fullSpan = true)
        assertEquals(144, tomorrow.totalElapsed)
        assertEquals(144, tomorrow.unfilled)
    }
}

package com.shijiankuai.app.domain

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class StatsObserveTest {
    private val zone: ZoneId = ZoneId.of("Asia/Shanghai")
    private val day = LocalDate.of(2026, 10, 21)
    private val now = LocalDateTime.of(2026, 10, 21, 14, 20).atZone(zone).toInstant()

    @Test
    fun flowRecomputesAfterBlockInsert() = runTest {
        val blocks = MutableStateFlow<Map<BlockId, String>>(emptyMap())
        val types = MutableStateFlow(Presets.types)
        val tick = MutableStateFlow(0L)
        val flow = StatsObserve.flow(
            kind = PeriodKind.DAY,
            anchor = day,
            now = { now },
            zone = zone,
            blocks = blocks,
            types = types,
            tick = tick,
        )
        val seen = mutableListOf<PeriodStats>()
        val job = launch { flow.collect { seen.add(it) } }
        advanceUntilIdle()
        assertEquals(0, seen.last().filled)

        blocks.value = mapOf(BlockId(day.toEpochDay(), 10) to "work")
        advanceUntilIdle()
        assertEquals(1, seen.last().filled)
        assertEquals("work", seen.last().byType.single().typeId)
        job.cancel()
    }

    @Test
    fun tickRefreshesNowForCurrentPeriod() = runTest {
        var current = LocalDateTime.of(2026, 10, 21, 10, 0).atZone(zone).toInstant()
        val blocks = MutableStateFlow<Map<BlockId, String>>(emptyMap())
        val types = MutableStateFlow(Presets.types)
        val tick = MutableStateFlow(0L)
        val flow = StatsObserve.flow(
            kind = PeriodKind.DAY,
            anchor = day,
            now = { current },
            zone = zone,
            blocks = blocks,
            types = types,
            tick = tick,
        )
        val seen = mutableListOf<PeriodStats>()
        val job = launch { flow.collect { seen.add(it) } }
        advanceUntilIdle()
        assertEquals(60, seen.last().totalElapsed)

        current = LocalDateTime.of(2026, 10, 21, 14, 20).atZone(zone).toInstant()
        tick.value = 1L
        advanceUntilIdle()
        assertEquals(86, seen.last().totalElapsed)
        job.cancel()
    }

    @Test
    fun togglingShowAllTimeSwitchesToFullSpan() = runTest {
        val blocks = MutableStateFlow(mapOf(BlockId(day.toEpochDay(), 10) to "work"))
        val types = MutableStateFlow(Presets.types)
        val tick = MutableStateFlow(0L)
        val showAll = MutableStateFlow(false)
        val flow = StatsObserve.flow(
            kind = PeriodKind.DAY,
            anchor = day,
            now = { now },
            zone = zone,
            blocks = blocks,
            types = types,
            tick = tick,
            showAllTime = showAll,
        )
        val seen = mutableListOf<PeriodStats>()
        val job = launch { flow.collect { seen.add(it) } }
        advanceUntilIdle()
        assertEquals(86, seen.last().totalElapsed)
        assertEquals(false, seen.last().fullSpan)

        showAll.value = true
        advanceUntilIdle()
        assertEquals(144, seen.last().totalElapsed)
        assertEquals(143, seen.last().unfilled)
        assertEquals(true, seen.last().fullSpan)
        job.cancel()
    }
}

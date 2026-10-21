package com.shijiankuai.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class WakeFillTest {
    private val zone: ZoneId = ZoneId.of("Asia/Shanghai")
    private val day = LocalDate.of(2026, 10, 21)

    private fun instant(hour: Int, minute: Int) =
        LocalDateTime.of(day, java.time.LocalTime.of(hour, minute)).atZone(zone).toInstant()

    @Test
    fun wakeAt0712AfterSleep0042_fillsOnlyOverlap_toastAndSleptMatch() {
        val start = instant(0, 42)
        val wake = instant(7, 12)
        val result = WakeFill.compute(start, wake, zone)
        val toast = WakeFill.toast(result, zone)
        val slept = WakeFill.sleptOn(day, result.filled)

        assertEquals((4..42).map { BlockId(day.toEpochDay(), it) }, result.filled)
        assertFalse(result.filled.any { it.index < 4 })
        assertEquals(39, result.filled.size)
        assertEquals(39, slept)
        assertEquals("00:42 入睡 · 已记 39 块睡觉", toast)
    }
}

package com.shijiankuai.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class SleepSessionsTest {
    private val zone: ZoneId = ZoneId.of("Asia/Shanghai")
    private val day = LocalDate.of(2026, 10, 21)
    private val prev = day.minusDays(1)

    private fun instant(date: LocalDate, hour: Int, minute: Int) =
        LocalDateTime.of(date, LocalTime.of(hour, minute)).atZone(zone).toInstant()

    private val overnight = SleepSession(
        startedAt = instant(prev, 23, 30),
        endedAt = instant(day, 7, 12),
    )
    private val nap = SleepSession(
        startedAt = instant(day, 13, 0),
        endedAt = instant(day, 13, 40),
    )

    @Test
    fun overnightSessionShowsOnBothDays() {
        assertEquals(overnight, SleepSessions.closedForDate(prev, listOf(overnight), zone))
        assertEquals(overnight, SleepSessions.closedForDate(day, listOf(overnight), zone))
        assertNull(SleepSessions.closedForDate(day.plusDays(1), listOf(overnight), zone))
    }

    @Test
    fun laterNapWinsOnWakeDay() {
        val picked = SleepSessions.closedForDate(day, listOf(overnight, nap), zone)
        assertEquals(nap, picked)
        assertEquals(overnight, SleepSessions.closedForDate(prev, listOf(overnight, nap), zone))
    }

    @Test
    fun openSessionIsIgnored() {
        val open = SleepSession(startedAt = instant(day, 22, 0), endedAt = null)
        assertEquals(overnight, SleepSessions.closedForDate(day, listOf(open, overnight), zone))
    }

    @Test
    fun editKeepsOvernightDates() {
        val (start, end) = SleepSessions.editInstants(
            start = LocalTime.of(22, 30),
            end = LocalTime.of(7, 15),
            originalStart = overnight.startedAt,
            originalEnd = overnight.endedAt!!,
            zone = zone,
        )
        assertEquals(instant(prev, 22, 30), start)
        assertEquals(instant(day, 7, 15), end)
    }

    @Test
    fun editSameDayKeepsSameDay() {
        val (start, end) = SleepSessions.editInstants(
            start = LocalTime.of(0, 50),
            end = LocalTime.of(7, 0),
            originalStart = instant(day, 0, 42),
            originalEnd = instant(day, 7, 12),
            zone = zone,
        )
        assertEquals(instant(day, 0, 50), start)
        assertEquals(instant(day, 7, 0), end)
    }
}

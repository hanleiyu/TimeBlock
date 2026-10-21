package com.shijiankuai.app.domain

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object SleepSessions {
    /** Latest closed session that overlaps the calendar [date]. */
    fun closedForDate(
        date: LocalDate,
        sessions: List<SleepSession>,
        zone: ZoneId,
    ): SleepSession? {
        val dayStart = date.atStartOfDay(zone).toInstant()
        val dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant()
        return sessions
            .mapNotNull { s -> s.endedAt?.let { s to it } }
            .filter { (s, end) -> s.startedAt < dayEnd && end > dayStart }
            .maxByOrNull { it.second }
            ?.first
    }

    /**
     * Rebuild instants after the user edits clock times.
     * Dates come from the original session so overnight spans stay overnight.
     */
    fun editInstants(
        start: LocalTime,
        end: LocalTime,
        originalStart: Instant,
        originalEnd: Instant,
        zone: ZoneId,
    ): Pair<Instant, Instant> {
        val startDate = originalStart.atZone(zone).toLocalDate()
        val endDate = originalEnd.atZone(zone).toLocalDate()
        val s = startDate.atTime(start).atZone(zone).toInstant()
        var e = endDate.atTime(end).atZone(zone).toInstant()
        if (!e.isAfter(s)) e = e.plusSeconds(24 * 3600)
        return s to e
    }
}

package com.shijiankuai.app.domain

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.min

/**
 * Block i of a calendar day covers
 * `[00:00 + i*10min, 00:00 + (i+1)*10min)` in [zone].
 */
object BlockMath {
    fun minutesSinceMidnight(time: LocalTime): Int =
        time.toSecondOfDay() / 60

    /** Index of the block that *contains* [time], 0..143. */
    fun indexContaining(time: LocalTime): Int =
        (minutesSinceMidnight(time) / BLOCK_MINUTES).coerceIn(0, BLOCKS_PER_DAY - 1)

    /**
     * How many blocks of [date] have fully started before [now].
     * At 14:20 this is 86 (00:00–14:20). The in-progress block is not elapsed.
     */
    fun elapsedCount(date: LocalDate, now: Instant, zone: ZoneId): Int {
        val z = now.atZone(zone)
        val today = z.toLocalDate()
        return when {
            date.isBefore(today) -> BLOCKS_PER_DAY
            date.isAfter(today) -> 0
            else -> minutesSinceMidnight(z.toLocalTime()) / BLOCK_MINUTES
        }
    }

    fun blockStart(date: LocalDate, index: Int, zone: ZoneId): Instant =
        date.atTime(LocalTime.MIDNIGHT)
            .plusMinutes(index.toLong() * BLOCK_MINUTES)
            .atZone(zone)
            .toInstant()

    fun blockEnd(date: LocalDate, index: Int, zone: ZoneId): Instant =
        blockStart(date, index, zone).plus(BLOCK_MINUTES.toLong(), ChronoUnit.MINUTES)

    fun overlapMillis(
        aStart: Instant,
        aEnd: Instant,
        bStart: Instant,
        bEnd: Instant,
    ): Long {
        val start = max(aStart.toEpochMilli(), bStart.toEpochMilli())
        val end = min(aEnd.toEpochMilli(), bEnd.toEpochMilli())
        return max(0L, end - start)
    }
}

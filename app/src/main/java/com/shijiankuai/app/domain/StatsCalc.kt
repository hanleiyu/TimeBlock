package com.shijiankuai.app.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.time.DayOfWeek

object StatsCalc {
    fun periodBounds(kind: PeriodKind, anchor: LocalDate): Pair<LocalDate, LocalDate> {
        return when (kind) {
            PeriodKind.DAY -> anchor to anchor
            PeriodKind.WEEK -> {
                val start = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                start to start.plusDays(6)
            }
            PeriodKind.MONTH -> {
                val start = anchor.with(TemporalAdjusters.firstDayOfMonth())
                start to anchor.with(TemporalAdjusters.lastDayOfMonth())
            }
            PeriodKind.YEAR -> {
                val start = anchor.with(TemporalAdjusters.firstDayOfYear())
                start to anchor.with(TemporalAdjusters.lastDayOfYear())
            }
        }
    }

    /**
     * [blocks] maps BlockId → typeId. Future blocks (index >= elapsed that day)
     * are ignored: they are neither filled nor unfilled.
     */
    fun aggregate(
        kind: PeriodKind,
        anchor: LocalDate,
        now: Instant,
        zone: ZoneId,
        blocks: Map<BlockId, String>,
        types: List<BlockType>,
        fullSpan: Boolean = false,
    ): PeriodStats {
        val (start, end) = periodBounds(kind, anchor)
        val typeById = types.associateBy { it.id }
        val counts = linkedMapOf<String, Int>()
        var elapsed = 0
        var filled = 0

        var day = start
        while (!day.isAfter(end)) {
            val n = if (fullSpan) BLOCKS_PER_DAY else BlockMath.elapsedCount(day, now, zone)
            elapsed += n
            for (i in 0 until n) {
                val id = blocks[BlockId(day.toEpochDay(), i)]
                if (id != null) {
                    filled += 1
                    counts[id] = (counts[id] ?: 0) + 1
                }
            }
            day = day.plusDays(1)
        }

        val byType = counts.entries
            .sortedByDescending { it.value }
            .mapNotNull { (id, n) ->
                val t = typeById[id] ?: return@mapNotNull TypeCount(id, id, 0xFF9A9288, n)
                TypeCount(id, t.name, t.color, n)
            }

        return PeriodStats(
            kind = kind,
            start = start,
            endInclusive = end,
            totalElapsed = elapsed,
            filled = filled,
            unfilled = elapsed - filled,
            byType = byType,
            fullSpan = fullSpan,
        )
    }
}

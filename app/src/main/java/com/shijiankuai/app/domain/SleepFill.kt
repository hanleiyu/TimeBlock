package com.shijiankuai.app.domain

import java.time.Instant
import java.time.ZoneId

/**
 * Maps a sleep interval onto 10-minute calendar blocks.
 *
 * Rule (majority): a block becomes 睡觉 if at least 5 minutes of it
 * overlap `[sleepStart, wakeAt)`. Partial edges are not rounded out
 * to midnight — sleep that starts at 00:42 does **not** fill 00:00–00:40.
 */
object SleepFill {
    private const val MAJORITY_MS = 5L * 60L * 1000L

    fun blocks(
        sleepStart: Instant,
        wakeAt: Instant,
        zone: ZoneId,
    ): List<BlockId> {
        if (!wakeAt.isAfter(sleepStart)) return emptyList()

        val startDay = sleepStart.atZone(zone).toLocalDate()
        val endDay = wakeAt.atZone(zone).toLocalDate()
        val out = ArrayList<BlockId>()

        var day = startDay
        while (!day.isAfter(endDay)) {
            for (i in 0 until BLOCKS_PER_DAY) {
                val bStart = BlockMath.blockStart(day, i, zone)
                val bEnd = BlockMath.blockEnd(day, i, zone)
                val overlap = BlockMath.overlapMillis(bStart, bEnd, sleepStart, wakeAt)
                if (overlap >= MAJORITY_MS) {
                    out += BlockId(day.toEpochDay(), i)
                }
            }
            day = day.plusDays(1)
        }
        return out
    }
}

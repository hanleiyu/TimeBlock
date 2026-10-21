package com.shijiankuai.app.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** The wake path: fill [sleepStart, wakeAt), then toast / 已睡 use the same list. */
object WakeFill {
    fun compute(sleepStart: Instant, wakeAt: Instant, zone: ZoneId): WakeFillResult =
        WakeFillResult(sleepStart, wakeAt, SleepFill.blocks(sleepStart, wakeAt, zone))

    fun toast(result: WakeFillResult, zone: ZoneId): String {
        val hm = result.sleepStart.atZone(zone).toLocalTime()
            .format(DateTimeFormatter.ofPattern("HH:mm"))
        return "$hm 入睡 · 已记 ${result.filled.size} 块睡觉"
    }

    fun sleptOn(date: LocalDate, filled: List<BlockId>): Int =
        filled.count { it.epochDay == date.toEpochDay() }
}

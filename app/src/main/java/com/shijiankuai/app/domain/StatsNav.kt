package com.shijiankuai.app.domain

import java.time.LocalDate

object StatsNav {
    fun pick(date: LocalDate): LocalDate = date

    fun jumpToday(today: LocalDate): LocalDate = today

    fun shift(kind: PeriodKind, anchor: LocalDate, delta: Int): LocalDate = when (kind) {
        PeriodKind.DAY -> anchor.plusDays(delta.toLong())
        PeriodKind.WEEK -> anchor.plusWeeks(delta.toLong())
        PeriodKind.MONTH -> anchor.plusMonths(delta.toLong())
        PeriodKind.YEAR -> anchor.plusYears(delta.toLong())
    }

    fun contains(kind: PeriodKind, anchor: LocalDate, day: LocalDate): Boolean {
        val (start, end) = StatsCalc.periodBounds(kind, anchor)
        return !day.isBefore(start) && !day.isAfter(end)
    }
}

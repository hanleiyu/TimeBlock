package com.shijiankuai.app.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

object TodoCalendar {
    val WEEKDAY_LABELS = listOf("一", "二", "三", "四", "五", "六", "日")

    fun daysWithCompleted(items: Iterable<TodoItem>): Set<LocalDate> =
        items.asSequence()
            .filter { it.done }
            .map { LocalDate.ofEpochDay(it.epochDay) }
            .toSet()

    fun datesFromEpochDays(days: Iterable<Long>): Set<LocalDate> =
        days.map(LocalDate::ofEpochDay).toSet()

    fun hasCompletionDot(date: LocalDate, completedDays: Set<LocalDate>): Boolean =
        date in completedDays

    fun monthCells(month: YearMonth): List<LocalDate?> {
        val first = month.atDay(1)
        val lead = first.dayOfWeek.value - 1
        val days = month.lengthOfMonth()
        val cells = ArrayList<LocalDate?>(42)
        repeat(lead) { cells.add(null) }
        for (d in 1..days) cells.add(month.atDay(d))
        while (cells.size % 7 != 0) cells.add(null)
        return cells
    }

    fun monthLabel(month: YearMonth): String = "${month.year}年${month.monthValue}月"

    fun cellIndex(date: LocalDate, firstDayOfWeek: DayOfWeek): Int {
        val lead = (YearMonth.from(date).atDay(1).dayOfWeek.value - firstDayOfWeek.value + 7) % 7
        return lead + date.dayOfMonth - 1
    }
}

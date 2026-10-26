package com.shijiankuai.app.domain

import java.time.LocalDate
import java.time.YearMonth

object TodoMonth {
    val CHECKLIST_KINDS = listOf(PeriodKind.DAY, PeriodKind.MONTH, PeriodKind.YEAR)

    fun coerceKind(kind: PeriodKind): PeriodKind =
        if (kind == PeriodKind.WEEK) PeriodKind.MONTH else kind

    fun monthGrid(month: YearMonth): List<LocalDate> {
        val first = month.atDay(1)
        val lead = first.dayOfWeek.value - 1
        val weeks = (lead + month.lengthOfMonth() + 6) / 7
        val start = first.minusDays(lead.toLong())
        return List(weeks * 7) { start.plusDays(it.toLong()) }
    }

    fun queryBounds(anchor: LocalDate): Pair<LocalDate, LocalDate> {
        val grid = monthGrid(YearMonth.from(anchor))
        return grid.first() to grid.last()
    }

    fun selectedDayInMonth(today: LocalDate, month: YearMonth): LocalDate {
        val day = today.dayOfMonth.coerceAtMost(month.lengthOfMonth())
        return month.atDay(day)
    }

    fun monthCounts(items: List<TodoItem>, month: YearMonth): Map<TodoCategory, Int> {
        val inMonth = items.filter { item ->
            item.done && YearMonth.from(LocalDate.ofEpochDay(item.epochDay)) == month
        }
        return TodoRange.SUMMARY_ORDER.associateWith { cat ->
            inMonth.count { it.category == cat }
        }
    }

    fun dayDots(items: List<TodoItem>): Map<LocalDate, List<TodoCategory>> =
        items.asSequence()
            .filter { it.done }
            .groupBy { LocalDate.ofEpochDay(it.epochDay) }
            .mapValues { (_, dayItems) ->
                TodoRange.SUMMARY_ORDER
                    .filter { cat -> dayItems.any { it.category == cat } }
                    .take(3)
            }
            .filterValues { it.isNotEmpty() }

    fun doneOn(items: List<TodoItem>, date: LocalDate): List<TodoItem> =
        items
            .filter { it.done && it.epochDay == date.toEpochDay() }
            .sortedWith(compareBy<TodoItem> { it.sortOrder }.thenBy { it.id })

    fun summaryTitle(date: LocalDate): String =
        "${date.monthValue}月${date.dayOfMonth}日已完成"

    fun summaryCount(n: Int): String = "${n}项完成"

    fun summaryEmpty(): String = "当天暂无已完成"
}

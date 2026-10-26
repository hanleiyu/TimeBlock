package com.shijiankuai.app.domain

import java.time.LocalDate

object TodoRange {
    val SUMMARY_ORDER = listOf(
        TodoCategory.WORK,
        TodoCategory.HEALTH,
        TodoCategory.LIFE,
        TodoCategory.STUDY,
    )

    fun weekCards(items: List<TodoItem>): List<TodoCard> =
        SUMMARY_ORDER.mapNotNull { cat ->
            val rows = items
                .filter { it.category == cat && it.done }
                .sortedWith(compareBy<TodoItem> { it.epochDay }.thenBy { it.sortOrder })
            if (rows.isEmpty()) null else TodoCard(cat, rows)
        }

    fun completedCounts(items: List<TodoItem>): Map<TodoCategory, Int> =
        SUMMARY_ORDER.associateWith { cat -> items.count { it.done && it.category == cat } }

    fun completedTotal(items: List<TodoItem>): Int = items.count { it.done }

    data class MonthStack(val month: Int, val counts: Map<TodoCategory, Int>) {
        val total: Int get() = counts.values.sum()
    }

    fun yearStacks(items: List<TodoItem>, year: Int): List<MonthStack> =
        (1..12).map { month ->
            val inMonth = items.filter { item ->
                if (!item.done) return@filter false
                val day = LocalDate.ofEpochDay(item.epochDay)
                day.year == year && day.monthValue == month
            }
            MonthStack(month, SUMMARY_ORDER.associateWith { cat -> inMonth.count { it.category == cat } })
        }

    fun periodLabel(kind: PeriodKind, start: LocalDate, end: LocalDate): String = when (kind) {
        PeriodKind.DAY -> {
            val w = listOf("一", "二", "三", "四", "五", "六", "日")[start.dayOfWeek.value - 1]
            "${start.monthValue}月${start.dayOfMonth}日周$w"
        }
        PeriodKind.WEEK -> "${start.monthValue}月${start.dayOfMonth}日-${end.monthValue}月${end.dayOfMonth}日"
        PeriodKind.MONTH -> "${start.year}年${start.monthValue}月"
        PeriodKind.YEAR -> "${start.year}年"
    }
}

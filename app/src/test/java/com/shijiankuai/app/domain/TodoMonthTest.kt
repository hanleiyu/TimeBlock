package com.shijiankuai.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class TodoMonthTest {
    private val today = LocalDate.of(2026, 10, 26)

    private fun item(
        id: String,
        cat: TodoCategory,
        title: String,
        done: Boolean,
        day: LocalDate,
        sort: Int = 0,
    ) = TodoItem(id, day.toEpochDay(), cat, title, done, sort)

    @Test
    fun monthGridIsMondayFirstAndKeepsNeighborDates() {
        val oct = TodoMonth.monthGrid(YearMonth.of(2026, 10))
        assertEquals(35, oct.size)
        assertEquals(LocalDate.of(2026, 9, 28), oct.first())
        assertEquals(LocalDate.of(2026, 10, 1), oct[3])
        assertEquals(LocalDate.of(2026, 10, 26), oct[28])
        assertEquals(LocalDate.of(2026, 10, 31), oct[33])
        assertEquals(LocalDate.of(2026, 11, 1), oct.last())
        assertEquals(0, oct.size % 7)
    }

    @Test
    fun queryBoundsCoverVisibleNeighborDays() {
        val (start, end) = TodoMonth.queryBounds(today)
        assertEquals(LocalDate.of(2026, 9, 28), start)
        assertEquals(LocalDate.of(2026, 11, 1), end)
    }

    @Test
    fun dayDotsMergeSameCategoryCapAtThreeInSummaryOrder() {
        val day = LocalDate.of(2026, 10, 19)
        val items = listOf(
            item("w1", TodoCategory.WORK, "回邮件", true, day),
            item("w2", TodoCategory.WORK, "开会", true, day, 1),
            item("s1", TodoCategory.STUDY, "读书", true, day),
            item("h1", TodoCategory.HEALTH, "散步", true, day),
            item("l1", TodoCategory.LIFE, "倒垃圾", true, day),
            item("x1", TodoCategory.WORK, "未完成", false, day),
        )
        val dots = TodoMonth.dayDots(items)
        assertEquals(
            listOf(TodoCategory.WORK, TodoCategory.HEALTH, TodoCategory.LIFE),
            dots[day],
        )
        assertTrue(TodoMonth.dayDots(items.filter { !it.done }).isEmpty())
    }

    @Test
    fun monthCountsIgnoreOpenTasksAndNeighborMonths() {
        val items = listOf(
            item("w1", TodoCategory.WORK, "回邮件", true, LocalDate.of(2026, 10, 26)),
            item("w2", TodoCategory.WORK, "开会", false, LocalDate.of(2026, 10, 26)),
            item("h1", TodoCategory.HEALTH, "散步", true, LocalDate.of(2026, 10, 10)),
            item("l1", TodoCategory.LIFE, "买菜", true, LocalDate.of(2026, 9, 30)),
            item("s1", TodoCategory.STUDY, "去年", true, LocalDate.of(2026, 11, 1)),
        )
        val counts = TodoMonth.monthCounts(items, YearMonth.of(2026, 10))
        assertEquals(1, counts[TodoCategory.WORK])
        assertEquals(1, counts[TodoCategory.HEALTH])
        assertEquals(0, counts[TodoCategory.LIFE])
        assertEquals(0, counts[TodoCategory.STUDY])
    }

    @Test
    fun doneOnDayIsFlatCompletedListInStableOrder() {
        val day = LocalDate.of(2026, 10, 26)
        val items = listOf(
            item("w1", TodoCategory.WORK, "回邮件", true, day, 1),
            item("l1", TodoCategory.LIFE, "倒垃圾", true, day, 0),
            item("s1", TodoCategory.STUDY, "读书", false, day),
            item("h1", TodoCategory.HEALTH, "散步", true, LocalDate.of(2026, 10, 25)),
        )
        val done = TodoMonth.doneOn(items, day)
        assertEquals(listOf("倒垃圾", "回邮件"), done.map { it.title })
        assertTrue(done.all { it.done })
        assertTrue(TodoMonth.doneOn(items, LocalDate.of(2026, 10, 5)).isEmpty())
    }

    @Test
    fun selectedDayInMonthUsesTodayDayNumberAndClamps() {
        assertEquals(LocalDate.of(2026, 10, 26), TodoMonth.selectedDayInMonth(today, YearMonth.of(2026, 10)))
        assertEquals(LocalDate.of(2026, 11, 26), TodoMonth.selectedDayInMonth(today, YearMonth.of(2026, 11)))
        assertEquals(LocalDate.of(2026, 2, 26), TodoMonth.selectedDayInMonth(today, YearMonth.of(2026, 2)))
        assertEquals(
            LocalDate.of(2026, 2, 28),
            TodoMonth.selectedDayInMonth(LocalDate.of(2026, 1, 31), YearMonth.of(2026, 2)),
        )
    }

    @Test
    fun summaryCopyMatchesMonthPanel() {
        val day = LocalDate.of(2026, 10, 26)
        assertEquals("10月26日已完成", TodoMonth.summaryTitle(day))
        assertEquals("3项完成", TodoMonth.summaryCount(3))
        assertEquals("当天暂无已完成", TodoMonth.summaryEmpty())
    }

    @Test
    fun checklistKindsDropWeek() {
        assertEquals(
            listOf(PeriodKind.DAY, PeriodKind.MONTH, PeriodKind.YEAR),
            TodoMonth.CHECKLIST_KINDS,
        )
        assertEquals(PeriodKind.MONTH, TodoMonth.coerceKind(PeriodKind.WEEK))
        assertEquals(PeriodKind.DAY, TodoMonth.coerceKind(PeriodKind.DAY))
    }
}

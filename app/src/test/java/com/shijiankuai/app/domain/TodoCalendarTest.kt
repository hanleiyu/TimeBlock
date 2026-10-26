package com.shijiankuai.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class TodoCalendarTest {
    private fun item(epochDay: Long, done: Boolean, id: String = "t$epochDay") = TodoItem(
        id = id,
        epochDay = epochDay,
        category = TodoCategory.WORK,
        title = "任务",
        done = done,
        sortOrder = 0,
    )

    @Test
    fun daysWithCompletedMarksOnlyDatesThatHaveAtLeastOneDoneTask() {
        val oct3 = LocalDate.of(2026, 10, 3)
        val oct10 = LocalDate.of(2026, 10, 10)
        val oct12 = LocalDate.of(2026, 10, 12)
        val items = listOf(
            item(oct3.toEpochDay(), done = true, id = "a"),
            item(oct3.toEpochDay(), done = true, id = "b"),
            item(oct10.toEpochDay(), done = true, id = "c"),
            item(oct12.toEpochDay(), done = false, id = "d"),
            item(LocalDate.of(2026, 10, 4).toEpochDay(), done = false, id = "e"),
        )
        val marked = TodoCalendar.daysWithCompleted(items)
        assertEquals(setOf(oct3, oct10), marked)
        assertTrue(TodoCalendar.hasCompletionDot(oct3, marked))
        assertTrue(TodoCalendar.hasCompletionDot(oct10, marked))
        assertFalse(TodoCalendar.hasCompletionDot(oct12, marked))
        assertFalse(TodoCalendar.hasCompletionDot(LocalDate.of(2026, 10, 4), marked))
        assertFalse(TodoCalendar.hasCompletionDot(LocalDate.of(2026, 10, 26), emptySet()))
    }

    @Test
    fun datesFromEpochDaysMapsDistinctDoneDays() {
        val a = LocalDate.of(2026, 10, 3)
        val b = LocalDate.of(2026, 10, 21)
        assertEquals(setOf(a, b), TodoCalendar.datesFromEpochDays(listOf(a.toEpochDay(), b.toEpochDay())))
        assertTrue(TodoCalendar.datesFromEpochDays(emptyList()).isEmpty())
    }

    @Test
    fun monthCellsAreMondayFirstWithLeadingBlanks() {
        val oct = TodoCalendar.monthCells(YearMonth.of(2026, 10))
        assertEquals(0, oct.size % 7)
        assertNull(oct[0])
        assertNull(oct[1])
        assertNull(oct[2])
        assertEquals(LocalDate.of(2026, 10, 1), oct[3])
        assertEquals(LocalDate.of(2026, 10, 26), oct[28])
        assertEquals(LocalDate.of(2026, 10, 31), oct[33])
        assertTrue(oct.drop(34).all { it == null })
        assertEquals(listOf("一", "二", "三", "四", "五", "六", "日"), TodoCalendar.WEEKDAY_LABELS)
        assertEquals("2026年10月", TodoCalendar.monthLabel(YearMonth.of(2026, 10)))
    }
}

package com.shijiankuai.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TodoRecurrenceTest {
    private val monday = LocalDate.of(2026, 10, 26)

    private fun item(
        id: String,
        epochDay: Long = monday.toEpochDay(),
        rec: TodoRecurrence = TodoRecurrence.None,
        done: Boolean = false,
        doneDays: Set<Long> = emptySet(),
        skipDays: Set<Long> = emptySet(),
    ) = TodoItem(
        id = id,
        epochDay = epochDay,
        category = TodoCategory.WORK,
        title = id,
        done = done,
        sortOrder = 0,
        seriesId = id,
        recurrence = rec,
        doneDays = doneDays,
        skipDays = skipDays,
    )

    @Test
    fun weeklyMaskUsesMondayAsBitZero() {
        val weekdays = TodoRecurrence.weekly(setOf(1, 2, 3, 4, 5))
        assertEquals(31, weekdays.daysMask)
        assertEquals(setOf(1, 2, 3, 4, 5), TodoRecurrence.weeklyDays(31))
        assertTrue(weekdays.matches(monday))
        assertFalse(weekdays.matches(monday.plusDays(5)))
        assertTrue(weekdays.hasDays())
        assertFalse(TodoRecurrence.Weekly(0).hasDays())
    }

    @Test
    fun monthlyClampsPastEndOfMonth() {
        val rec = TodoRecurrence.Monthly(31)
        assertTrue(rec.matches(LocalDate.of(2026, 1, 31)))
        assertTrue(rec.matches(LocalDate.of(2026, 2, 28)))
        assertFalse(rec.matches(LocalDate.of(2026, 2, 27)))
    }

    @Test
    fun expandMaterializesWeeklyOccurrencesAndSkips() {
        val skip = monday.plusDays(1).toEpochDay()
        val done = monday.toEpochDay()
        val series = item(
            "t_week",
            rec = TodoRecurrence.weekly(setOf(1, 2)),
            doneDays = setOf(done),
            skipDays = setOf(skip),
        )
        val out = TodoRecurrence.expand(listOf(series), monday, monday.plusDays(7))
        assertEquals(listOf(monday.toEpochDay(), monday.plusDays(7).toEpochDay()), out.map { it.epochDay })
        assertEquals(TodoRecurrence.occId("t_week", monday.toEpochDay()), out[0].id)
        assertTrue(out[0].done)
        assertFalse(out[1].done)
        assertEquals("t_week", out[0].seriesId)
    }

    @Test
    fun expandKeepsOneOffInsideRangeOnly() {
        val keep = item("keep", monday.toEpochDay())
        val before = item("before", monday.minusDays(1).toEpochDay())
        val out = TodoRecurrence.expand(listOf(keep, before), monday, monday.plusDays(2))
        assertEquals(listOf("keep"), out.map { it.id })
    }

    @Test
    fun expandMonthlyHitsEachMonth() {
        val series = item("rent", monday.toEpochDay(), rec = TodoRecurrence.Monthly(26))
        val out = TodoRecurrence.expand(listOf(series), monday, LocalDate.of(2026, 12, 31))
        assertEquals(
            listOf(
                LocalDate.of(2026, 10, 26),
                LocalDate.of(2026, 11, 26),
                LocalDate.of(2026, 12, 26),
            ).map { it.toEpochDay() },
            out.map { it.epochDay },
        )
    }

    @Test
    fun occIdRoundtripAndCompletedDays() {
        val id = TodoRecurrence.occId("t_1", 20_000L)
        assertEquals("t_1" to 20_000L, TodoRecurrence.parseOcc(id))
        assertEquals("plain" to null, TodoRecurrence.parseOcc("plain"))
        val items = listOf(
            item("once", 20_000L, done = true),
            item("week", 20_001L, rec = TodoRecurrence.weekly(setOf(1)), doneDays = setOf(20_008L)),
        )
        assertEquals(setOf(20_000L, 20_008L), TodoRecurrence.completedEpochDays(items))
    }

    @Test
    fun encodeDecodeDaySets() {
        assertEquals(setOf(1L, 3L, 9L), TodoRecurrence.decodeDays("1,3,9"))
        assertEquals("1,3,9", TodoRecurrence.encodeDays(setOf(9L, 1L, 3L)))
        assertTrue(TodoRecurrence.decodeDays("").isEmpty())
        assertTrue(TodoRecurrence.decodeDays(null).isEmpty())
    }

    @Test
    fun deletingMiddleOccurrenceKeepsEarlierDoneAndOpenAndStopsLater() {
        val tue = monday.plusDays(1)
        val wed = monday.plusDays(2)
        val series = item(
            "stand",
            rec = TodoRecurrence.weekly(setOf(1, 2, 3, 4, 5)),
            doneDays = setOf(monday.toEpochDay(), wed.toEpochDay()),
        )
        val after = TodoRecurrence.deleteOccurrence(
            listOf(series),
            TodoRecurrence.occId("stand", wed.toEpochDay()),
        )
        val kept = after.single()
        val week = TodoRecurrence.expand(after, monday, monday.plusDays(10))
        assertEquals(listOf(monday.toEpochDay(), tue.toEpochDay()), week.map { it.epochDay })
        assertTrue(week[0].done)
        assertFalse(week[1].done)
        assertEquals(setOf(monday.toEpochDay()), kept.doneDays)
        assertFalse(week.any { it.epochDay >= wed.toEpochDay() })
        assertEquals(setOf(monday.toEpochDay()), TodoRecurrence.completedEpochDays(after))
    }

    @Test
    fun deletingFirstOccurrenceRemovesTheWholeFutureAndKeepsTheSeriesRow() {
        val series = item("rent", rec = TodoRecurrence.Monthly(26))
        val after = TodoRecurrence.deleteOccurrence(
            listOf(series),
            TodoRecurrence.occId("rent", monday.toEpochDay()),
        )
        assertEquals(1, after.size)
        assertTrue(
            TodoRecurrence.expand(after, monday.minusMonths(1), monday.plusMonths(6)).isEmpty(),
        )
    }

    @Test
    fun deletingOneOffRemovesOnlyThatItem() {
        val once = item("once")
        val other = item("keep", monday.plusDays(1).toEpochDay())
        val after = TodoRecurrence.deleteOccurrence(listOf(once, other), "once")
        assertEquals(listOf("keep"), after.map { it.id })
    }

    @Test
    fun cutoffUsesTheOccurrenceDateNotSystemToday() {
        val historical = LocalDate.of(2026, 9, 7)
        val series = item(
            "old",
            epochDay = historical.toEpochDay(),
            rec = TodoRecurrence.weekly(setOf(1)),
            doneDays = setOf(historical.toEpochDay()),
        )
        val cut = historical.plusWeeks(2)
        val after = TodoRecurrence.deleteOccurrence(
            listOf(series),
            TodoRecurrence.occId("old", cut.toEpochDay()),
        )
        val range = TodoRecurrence.expand(after, historical, LocalDate.of(2026, 12, 1))
        assertEquals(
            listOf(historical, historical.plusWeeks(1)).map { it.toEpochDay() },
            range.map { it.epochDay },
        )
        assertTrue(range[0].done)
        assertFalse(range[1].done)
        assertTrue(range.none { it.epochDay >= cut.toEpochDay() })
    }

    @Test
    fun cutoffSurvivesReExpandLikeARestart() {
        val series = item(
            "loop",
            rec = TodoRecurrence.weekly(setOf(1)),
            doneDays = setOf(monday.toEpochDay()),
        )
        val after = TodoRecurrence.deleteOccurrence(
            listOf(series),
            TodoRecurrence.occId("loop", monday.plusWeeks(1).toEpochDay()),
        )
        val again = TodoRecurrence.expand(after, monday, monday.plusWeeks(8))
        assertEquals(listOf(monday.toEpochDay()), again.map { it.epochDay })
        assertTrue(again.single().done)
        assertEquals(setOf(monday.toEpochDay()), TodoRecurrence.completedEpochDays(after))
    }
}

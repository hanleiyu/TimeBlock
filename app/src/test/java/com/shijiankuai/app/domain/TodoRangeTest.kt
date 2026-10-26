package com.shijiankuai.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TodoRangeTest {
    private val mon = LocalDate.of(2026, 10, 26)
    private val tue = LocalDate.of(2026, 10, 27)

    private fun item(
        id: String,
        cat: TodoCategory,
        title: String,
        done: Boolean,
        day: LocalDate = mon,
        sort: Int = 0,
    ) = TodoItem(id, day.toEpochDay(), cat, title, done, sort)

    @Test
    fun weekCardsKeepOnlyDoneAndHideEmptyCategoriesInSummaryOrder() {
        val items = listOf(
            item("w1", TodoCategory.WORK, "写周报", done = true),
            item("w2", TodoCategory.WORK, "回邮件", done = true, sort = 1),
            item("w3", TodoCategory.WORK, "开会", done = false, sort = 2),
            item("s1", TodoCategory.STUDY, "读书", done = false),
            item("h1", TodoCategory.HEALTH, "散步", done = true, day = tue),
            item("l1", TodoCategory.LIFE, "买菜", done = true),
        )
        val cards = TodoRange.weekCards(items)
        assertEquals(
            listOf(TodoCategory.WORK, TodoCategory.HEALTH, TodoCategory.LIFE),
            cards.map { it.category },
        )
        assertEquals(listOf("写周报", "回邮件"), cards[0].items.map { it.title })
        assertEquals(listOf("散步"), cards[1].items.map { it.title })
        assertTrue(cards.none { it.category == TodoCategory.STUDY })
        assertTrue(cards.all { card -> card.items.all { it.done } })
    }

    @Test
    fun completedCountsAndTotalIgnoreOpenTasks() {
        val items = listOf(
            item("w1", TodoCategory.WORK, "写周报", done = true),
            item("w2", TodoCategory.WORK, "开会", done = false),
            item("h1", TodoCategory.HEALTH, "散步", done = true),
            item("h2", TodoCategory.HEALTH, "再散", done = true),
            item("l1", TodoCategory.LIFE, "买菜", done = false),
        )
        val counts = TodoRange.completedCounts(items)
        assertEquals(1, counts[TodoCategory.WORK])
        assertEquals(2, counts[TodoCategory.HEALTH])
        assertEquals(0, counts[TodoCategory.LIFE])
        assertEquals(0, counts[TodoCategory.STUDY])
        assertEquals(3, TodoRange.completedTotal(items))
        assertEquals(
            listOf(TodoCategory.WORK, TodoCategory.HEALTH, TodoCategory.LIFE, TodoCategory.STUDY),
            TodoRange.SUMMARY_ORDER,
        )
    }

    @Test
    fun yearStacksAreTwelveMonthsAndOnlyCountDone() {
        val items = listOf(
            item("w1", TodoCategory.WORK, "写周报", done = true, day = LocalDate.of(2026, 4, 2)),
            item("w2", TodoCategory.WORK, "开会", done = false, day = LocalDate.of(2026, 4, 3)),
            item("h1", TodoCategory.HEALTH, "散步", done = true, day = LocalDate.of(2026, 4, 8)),
            item("l1", TodoCategory.LIFE, "买菜", done = true, day = LocalDate.of(2026, 12, 1)),
            item("x1", TodoCategory.STUDY, "去年", done = true, day = LocalDate.of(2025, 4, 1)),
        )
        val stacks = TodoRange.yearStacks(items, 2026)
        assertEquals(12, stacks.size)
        assertEquals((1..12).toList(), stacks.map { it.month })
        assertEquals(1, stacks[3].counts[TodoCategory.WORK])
        assertEquals(1, stacks[3].counts[TodoCategory.HEALTH])
        assertEquals(0, stacks[3].counts[TodoCategory.LIFE])
        assertEquals(2, stacks[3].total)
        assertEquals(1, stacks[11].counts[TodoCategory.LIFE])
        assertEquals(0, stacks[0].total)
    }

    @Test
    fun periodLabelMatchesChecklistCopy() {
        val start = LocalDate.of(2026, 10, 20)
        val end = LocalDate.of(2026, 10, 26)
        assertEquals("10月26日周一", TodoRange.periodLabel(PeriodKind.DAY, end, end))
        assertEquals("10月20日-10月26日", TodoRange.periodLabel(PeriodKind.WEEK, start, end))
        assertEquals("2026年10月", TodoRange.periodLabel(PeriodKind.MONTH, start, end))
        assertEquals("2026年", TodoRange.periodLabel(PeriodKind.YEAR, start, end))
    }
}

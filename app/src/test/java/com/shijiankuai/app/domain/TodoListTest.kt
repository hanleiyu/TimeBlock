package com.shijiankuai.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TodoListTest {
    private val day = 20_000L

    private fun item(
        id: String,
        category: TodoCategory,
        title: String,
        sort: Int,
        done: Boolean = false,
        epochDay: Long = day,
    ) = TodoItem(id, epochDay, category, title, done, sort)

    @Test
    fun cardsHideEmptyCategoriesAndKeepFixedOrder() {
        val items = listOf(
            item("l1", TodoCategory.LIFE, "买菜", 0),
            item("w1", TodoCategory.WORK, "周报", 0),
            item("w2", TodoCategory.WORK, "邮件", 1, done = true),
        )
        val cards = TodoList.cards(items)
        assertEquals(listOf(TodoCategory.WORK, TodoCategory.LIFE), cards.map { it.category })
        assertTrue(cards.none { it.category == TodoCategory.STUDY })
        assertTrue(cards.none { it.category == TodoCategory.HEALTH })
        assertEquals(listOf("w1", "w2"), cards[0].items.map { it.id })
    }

    @Test
    fun completedItemsStayInPlaceNotSunk() {
        val items = listOf(
            item("a", TodoCategory.WORK, "周报", 0, done = false),
            item("b", TodoCategory.WORK, "邮件", 1, done = true),
            item("c", TodoCategory.WORK, "会议", 2, done = false),
        )
        val card = TodoList.cards(items).single()
        assertEquals(listOf("a", "b", "c"), card.items.map { it.id })
        assertEquals(listOf(false, true, false), card.items.map { it.done })
        val look = TodoList.doneLook(true)
        assertTrue(look.strike)
        assertTrue(look.faded)
        assertFalse(TodoList.doneLook(false).strike)
    }

    @Test
    fun reorderOnlyMovesWithinTheSameCategory() {
        val items = listOf(
            item("w1", TodoCategory.WORK, "周报", 0),
            item("w2", TodoCategory.WORK, "邮件", 1),
            item("w3", TodoCategory.WORK, "会议", 2),
            item("h1", TodoCategory.HEALTH, "散步", 0),
        )
        val next = TodoList.reorderWithin(items, TodoCategory.WORK, listOf("w3", "w1", "w2"))
        val work = next.filter { it.category == TodoCategory.WORK }.sortedBy { it.sortOrder }
        assertEquals(listOf("w3", "w1", "w2"), work.map { it.id })
        assertEquals(listOf(0, 1, 2), work.map { it.sortOrder })
        val health = next.single { it.id == "h1" }
        assertEquals(0, health.sortOrder)
        assertEquals(TodoCategory.HEALTH, health.category)
    }

    @Test
    fun deferMovesOpenTaskToTomorrowAtCategoryEnd() {
        val today = item("w1", TodoCategory.WORK, "周报", 0)
        val tomorrow = listOf(
            item("w9", TodoCategory.WORK, "已在明天", 0, epochDay = day + 1),
            item("h1", TodoCategory.HEALTH, "散步", 0, epochDay = day + 1),
        )
        val moved = TodoList.deferToNextDay(today, tomorrow)
        assertEquals(day + 1, moved.epochDay)
        assertEquals(TodoCategory.WORK, moved.category)
        assertEquals("周报", moved.title)
        assertFalse(moved.done)
        assertEquals(1, moved.sortOrder)
        assertEquals("w1", moved.id)
    }

    @Test
    fun emptyDayHasNoCards() {
        assertTrue(TodoList.cards(emptyList()).isEmpty())
    }
}

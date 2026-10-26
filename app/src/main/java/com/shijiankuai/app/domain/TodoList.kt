package com.shijiankuai.app.domain

enum class TodoCategory(val id: String, val label: String) {
    WORK("work", "工作"),
    STUDY("study", "学习"),
    HEALTH("health", "健康"),
    LIFE("life", "生活");

    companion object {
        fun fromId(id: String): TodoCategory =
            entries.firstOrNull { it.id == id } ?: WORK
    }
}

data class TodoItem(
    val id: String,
    val epochDay: Long,
    val category: TodoCategory,
    val title: String,
    val done: Boolean,
    val sortOrder: Int,
)

data class TodoCard(
    val category: TodoCategory,
    val items: List<TodoItem>,
)

data class TodoDoneLook(
    val strike: Boolean,
    val faded: Boolean,
)

object TodoList {
    fun cards(items: List<TodoItem>): List<TodoCard> =
        TodoCategory.entries.mapNotNull { cat ->
            val rows = items.filter { it.category == cat }.sortedBy { it.sortOrder }
            if (rows.isEmpty()) null else TodoCard(cat, rows)
        }

    fun reorderWithin(
        items: List<TodoItem>,
        category: TodoCategory,
        orderedIds: List<String>,
    ): List<TodoItem> {
        val inCat = items.filter { it.category == category }.sortedBy { it.sortOrder }
        val others = items.filter { it.category != category }
        val seen = linkedSetOf<String>()
        val next = mutableListOf<TodoItem>()
        orderedIds.forEach { id ->
            val row = inCat.find { it.id == id } ?: return@forEach
            if (seen.add(id)) next += row
        }
        inCat.forEach { if (seen.add(it.id)) next += it }
        return others + next.mapIndexed { i, t -> t.copy(sortOrder = i) }
    }

    fun deferToNextDay(item: TodoItem, existing: List<TodoItem>): TodoItem {
        val target = item.epochDay + 1
        val max = existing
            .filter { it.epochDay == target && it.category == item.category }
            .maxOfOrNull { it.sortOrder } ?: -1
        return item.copy(epochDay = target, sortOrder = max + 1, done = false)
    }

    fun doneLook(done: Boolean) = TodoDoneLook(strike = done, faded = done)
}

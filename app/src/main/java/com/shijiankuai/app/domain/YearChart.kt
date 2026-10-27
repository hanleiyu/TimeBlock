package com.shijiankuai.app.domain

import kotlin.math.hypot

object YearChart {
    data class Point(
        val month: Int,
        val category: TodoCategory,
        val count: Int,
        val x: Float,
        val y: Float,
    )

    fun points(
        stacks: List<TodoRange.MonthStack>,
        width: Float,
        height: Float,
        max: Int,
        padY: Float,
    ): List<Point> {
        if (stacks.isEmpty() || width <= 0f || height <= 0f) return emptyList()
        val plotH = (height - padY * 2).coerceAtLeast(1f)
        val slot = width / stacks.size
        val ceiling = max.coerceAtLeast(1).toFloat()
        return stacks.flatMapIndexed { i, stack ->
            TodoRange.SUMMARY_ORDER.map { cat ->
                val n = stack.counts[cat] ?: 0
                Point(
                    month = stack.month,
                    category = cat,
                    count = n,
                    x = slot * (i + 0.5f),
                    y = padY + plotH * (1f - n / ceiling),
                )
            }
        }
    }

    fun hit(points: List<Point>, x: Float, y: Float, radius: Float): Point? =
        points
            .map { it to hypot(it.x - x, it.y - y) }
            .filter { it.second <= radius }
            .minWithOrNull(
                compareBy<Pair<Point, Float>> { it.second }
                    .thenByDescending { TodoRange.SUMMARY_ORDER.indexOf(it.first.category) },
            )
            ?.first

    fun tooltip(point: Point): String = "${point.month}月 · ${point.category.label} ${point.count}"
}

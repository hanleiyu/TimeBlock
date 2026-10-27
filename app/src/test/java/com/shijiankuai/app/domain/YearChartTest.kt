package com.shijiankuai.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class YearChartTest {
    private val stacks = listOf(
        TodoRange.MonthStack(1, mapOf(TodoCategory.WORK to 0, TodoCategory.HEALTH to 2, TodoCategory.LIFE to 0, TodoCategory.STUDY to 0)),
        TodoRange.MonthStack(4, mapOf(TodoCategory.WORK to 4, TodoCategory.HEALTH to 4, TodoCategory.LIFE to 1, TodoCategory.STUDY to 0)),
    )

    @Test
    fun pointsSitInMonthSlotCenters() {
        val pts = YearChart.points(stacks, width = 200f, height = 100f, max = 4, padY = 10f)
        assertEquals(8, pts.size)
        val janHealth = pts.single { it.month == 1 && it.category == TodoCategory.HEALTH }
        assertEquals(50f, janHealth.x, 0.01f)
        assertEquals(2, janHealth.count)
        val aprWork = pts.single { it.month == 4 && it.category == TodoCategory.WORK }
        assertEquals(150f, aprWork.x, 0.01f)
        assertEquals(10f, aprWork.y, 0.01f)
    }

    @Test
    fun hitPicksNearestPointInsideRadius() {
        val pts = YearChart.points(stacks, width = 200f, height = 100f, max = 4, padY = 10f)
        val hit = YearChart.hit(pts, x = 52f, y = 50f, radius = 20f)!!
        assertEquals(1, hit.month)
        assertEquals(TodoCategory.HEALTH, hit.category)
        assertEquals(2, hit.count)
        assertNull(YearChart.hit(pts, x = 100f, y = 10f, radius = 12f))
    }

    @Test
    fun overlappingPointsPreferLaterSummaryOrder() {
        val pts = YearChart.points(stacks, width = 200f, height = 100f, max = 4, padY = 10f)
        val hit = YearChart.hit(pts, x = 150f, y = 10f, radius = 8f)!!
        assertEquals(TodoCategory.HEALTH, hit.category)
        assertEquals(4, hit.count)
    }

    @Test
    fun tooltipNamesMonthCategoryAndCount() {
        val pt = YearChart.Point(10, TodoCategory.WORK, 3, 0f, 0f)
        assertEquals("10月 · 工作 3", YearChart.tooltip(pt))
    }
}

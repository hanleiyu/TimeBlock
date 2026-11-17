package com.shijiankuai.app.exercise.ui.month

import com.shijiankuai.app.domain.TodoMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class HealthMonthLayoutTest {
    @Test
    fun healthCalendarGridMatchesChecklistIncludingNeighborDays() {
        val march = YearMonth.of(2026, 3)
        val oct = YearMonth.of(2026, 10)
        assertEquals(TodoMonth.monthGrid(march), HealthMonthLayout.grid(march))
        assertEquals(TodoMonth.monthGrid(oct), HealthMonthLayout.grid(oct))
        val octGrid = HealthMonthLayout.grid(oct)
        assertEquals(LocalDate.of(2026, 9, 28), octGrid.first())
        assertEquals(LocalDate.of(2026, 11, 1), octGrid.last())
        assertNotEquals(oct, YearMonth.from(octGrid.first()))
        assertNotEquals(oct, YearMonth.from(octGrid.last()))
    }
}

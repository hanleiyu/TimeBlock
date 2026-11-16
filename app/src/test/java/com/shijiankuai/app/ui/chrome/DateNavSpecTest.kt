package com.shijiankuai.app.ui.chrome

import com.shijiankuai.app.domain.PeriodKind
import com.shijiankuai.app.domain.TodoRange
import com.shijiankuai.app.ui.toCn
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DateNavSpecTest {
    @Test
    fun dateBarsShareOneUncircledCenteredLayout() {
        assertFalse(DateNavSpec.enclosesChevrons)
        assertTrue(DateNavSpec.labelCentered)
        assertTrue(DateNavSpec.chevronsAtOuterEnds)
        assertEquals(16, DateNavSpec.labelSizeSp)
        assertEquals(22, DateNavSpec.chevronSizeDp)
        assertEquals(48, DateNavSpec.minTouchTargetDp)
        assertEquals(8, DateNavSpec.verticalPaddingDp)
        assertTrue(DateNavSpec.minTouchTargetDp >= 48)
        assertTrue(DateNavSpec.chevronSizeDp < DateNavSpec.minTouchTargetDp)
        assertEquals("上一", DateNavSpec.prevContentDescription)
        assertEquals("下一", DateNavSpec.nextContentDescription)
    }

    @Test
    fun periodLabelsStayKindSpecific() {
        val day = LocalDate.of(2026, 11, 16)
        val weekEnd = LocalDate.of(2026, 11, 22)
        assertEquals("11月16日 周一", PeriodKind.DAY.barLabel(day, day))
        assertEquals("11/16 – 11/22", PeriodKind.WEEK.barLabel(day, weekEnd))
        assertEquals("2026年11月", PeriodKind.MONTH.barLabel(day, day))
        assertEquals("2026年", PeriodKind.YEAR.barLabel(day, day))
        assertEquals(day.toCn(), PeriodKind.DAY.barLabel(day, day))
    }

    @Test
    fun checklistLabelsStayKindSpecific() {
        val day = LocalDate.of(2026, 11, 16)
        val weekStart = LocalDate.of(2026, 11, 16)
        val weekEnd = LocalDate.of(2026, 11, 22)
        assertEquals("11月16日周一", TodoRange.periodLabel(PeriodKind.DAY, day, day))
        assertEquals("11月16日-11月22日", TodoRange.periodLabel(PeriodKind.WEEK, weekStart, weekEnd))
        assertEquals("2026年11月", TodoRange.periodLabel(PeriodKind.MONTH, day, day))
        assertEquals("2026年", TodoRange.periodLabel(PeriodKind.YEAR, day, day))
    }
}

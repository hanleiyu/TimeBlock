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
    fun dayChecklistAndMonthHealthUseCanonicalBarLabels() {
        val day = LocalDate.of(2026, 11, 16)
        val weekEnd = LocalDate.of(2026, 11, 22)
        val month = java.time.YearMonth.of(2026, 11)
        assertEquals(PeriodKind.DAY.barLabel(day, day), DateNavSpec.checklistLabel(PeriodKind.DAY, day, day))
        assertEquals(day.toCn(), DateNavSpec.checklistLabel(PeriodKind.DAY, day, day))
        assertEquals("11月16日 周一", DateNavSpec.checklistLabel(PeriodKind.DAY, day, day))
        assertEquals("11/16 – 11/22", DateNavSpec.checklistLabel(PeriodKind.WEEK, day, weekEnd))
        assertEquals("2026年11月", DateNavSpec.monthHealthLabel(month))
        assertEquals(PeriodKind.MONTH.barLabel(month.atDay(1), month.atEndOfMonth()), DateNavSpec.monthHealthLabel(month))
        assertEquals(DateNavSpec.checklistLabel(PeriodKind.DAY, day, day), TodoRange.periodLabel(PeriodKind.DAY, day, day))
        assertEquals(DateNavSpec.checklistLabel(PeriodKind.WEEK, day, weekEnd), TodoRange.periodLabel(PeriodKind.WEEK, day, weekEnd))
        assertEquals(DateNavSpec.checklistLabel(PeriodKind.MONTH, day, day), TodoRange.periodLabel(PeriodKind.MONTH, day, day))
        assertEquals(DateNavSpec.checklistLabel(PeriodKind.YEAR, day, day), TodoRange.periodLabel(PeriodKind.YEAR, day, day))
    }
}

package com.shijiankuai.app.ui.chrome

import org.junit.Assert.assertEquals
import org.junit.Test

class MonthCardSpecTest {
    @Test
    fun healthMonthChromeMatchesChecklistCard() {
        assertEquals(16, MonthCardSpec.radiusDp)
        assertEquals(1, MonthCardSpec.borderWidthDp)
        assertEquals(10, MonthCardSpec.calendarPadHDp)
        assertEquals(12, MonthCardSpec.calendarPadVDp)
        assertEquals(16, MonthCardSpec.sectionPadHDp)
        assertEquals(14, MonthCardSpec.sectionPadVDp)
        assertEquals(12, MonthCardSpec.gapDp)
        assertEquals(12, MonthCardSpec.weekdaySp)
        assertEquals(14, MonthCardSpec.dayNumberSp)
        assertEquals(34, MonthCardSpec.dayCircleDp)
        assertEquals(15, MonthCardSpec.selectedStrokeTenthsDp)
        assertEquals(1, MonthCardSpec.todayStrokeDp)
        assertEquals(92, MonthCardSpec.cellAspectTimes100)
        assertEquals(14, MonthCardSpec.countChipRadiusDp)
        assertEquals(6, MonthCardSpec.countChipPadVDp)
        assertEquals(8, MonthCardSpec.countChipGapDp)
        assertEquals(0.92f, MonthCardSpec.cellAspect, 0.0001f)
        assertEquals(1.5f, MonthCardSpec.selectedStrokeDp, 0.0001f)
    }
}

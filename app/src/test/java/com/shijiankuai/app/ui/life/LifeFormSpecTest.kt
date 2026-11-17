package com.shijiankuai.app.ui.life

import com.shijiankuai.app.domain.LifeCalendar
import com.shijiankuai.app.domain.LifeMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LifeFormSpecTest {
    private val jan = LifeMonth(2001, 1)
    private val feb = LifeMonth(2001, 2)
    private val mar = LifeMonth(2001, 3)
    private val jun = LifeMonth(2001, 6)

    @Test
    fun newPlusAndEmptyMonthRecordingsStartSingleOn() {
        assertTrue(LifeFormSpec.initialSingle(jan, jan))
        val plus = LifeTap.blankEvent(jan, jan)
        assertTrue(LifeFormSpec.initialSingle(plus.start, plus.end))
        val emptyTap = LifeTap.fromHit(feb, emptyList()) as LifeTap.OpenForm
        assertTrue(LifeFormSpec.initialSingle(emptyTap.start, emptyTap.end))
        assertEquals("单个月", LifeFormSpec.singleSwitchLabel)
    }

    @Test
    fun existingAndDragRangesInitializeFromInterval() {
        assertFalse(LifeFormSpec.initialSingle(jan, mar))
        assertTrue(LifeFormSpec.initialSingle(feb, feb))
        val drag = LifeCalendar.normalizeRange(mar, jan)
        assertFalse(LifeFormSpec.initialSingle(drag.first, drag.second))
        val singleDrag = LifeTap.OpenForm(feb, feb)
        assertTrue(LifeFormSpec.initialSingle(singleDrag.start, singleDrag.end))
        val overlapAdd = LifeTap.addOverlap(jun)
        assertTrue(LifeFormSpec.initialSingle(overlapAdd.start, overlapAdd.end))
    }

    @Test
    fun toggleOnCollapsesToStartAndOffRestoresValidEnd() {
        val (collapsed, mem) = LifeFormSpec.collapseToStart(jan, mar, null)
        assertEquals(jan, collapsed)
        assertEquals(mar, mem)
        assertEquals(mar, LifeFormSpec.expandFromStart(jan, mem))
        assertEquals(jun, LifeFormSpec.expandFromStart(jun, mem))
        assertEquals(jun, LifeFormSpec.expandFromStart(jun, null))
        val alreadySingle = LifeFormSpec.collapseToStart(feb, feb, mar)
        assertEquals(feb, alreadySingle.first)
        assertEquals(mar, alreadySingle.second)
    }

    @Test
    fun startChangeKeepsRangeValid() {
        assertEquals(jan, LifeFormSpec.endAfterStartChange(single = true, start = jan, end = mar))
        assertEquals(mar, LifeFormSpec.endAfterStartChange(single = false, start = jan, end = mar))
        assertEquals(jun, LifeFormSpec.endAfterStartChange(single = false, start = jun, end = mar))
    }

    @Test
    fun saveWritesStartEqualsEndWhenSingle() {
        assertEquals(jan to jan, LifeFormSpec.saveBounds(single = true, start = jan, end = mar))
        assertEquals(jan to mar, LifeFormSpec.saveBounds(single = false, start = jan, end = mar))
        assertEquals(jan to mar, LifeFormSpec.saveBounds(single = false, start = mar, end = jan))
    }
}

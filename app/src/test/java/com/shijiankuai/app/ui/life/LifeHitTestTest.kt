package com.shijiankuai.app.ui.life

import com.shijiankuai.app.domain.LifeMonth
import com.shijiankuai.app.domain.LifeOrigin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LifeHitTestTest {
    private val origin = LifeOrigin(1998, 6)
    private val layout = LifeHitLayout(
        rowHeightPx = 40f,
        labelWidthPx = 36f,
        trailingWidthPx = 40f,
        gapPx = 4f,
        gridWidthPx = 36f + 12 * 20f + 11 * 4f + 40f,
    )

    @Test
    fun tapMapsYearRowAndMonthColumn() {
        assertEquals(LifeMonth(1998, 1), LifeHitTest.monthAt(40f, 10f, 0, origin, layout))
        assertEquals(LifeMonth(1998, 6), LifeHitTest.monthAt(40f + 5 * 24f, 10f, 0, origin, layout))
        assertEquals(LifeMonth(1999, 1), LifeHitTest.monthAt(40f, 50f, 0, origin, layout))
    }

    @Test
    fun scrollShiftsYearOnly() {
        assertEquals(LifeMonth(1998, 1), LifeHitTest.monthAt(40f, 10f, 0, origin, layout))
        assertEquals(LifeMonth(2008, 1), LifeHitTest.monthAt(40f, 10f, 400, origin, layout))
    }

    @Test
    fun trailingTitleColumnHitsLastMonthOfRow() {
        val x = layout.gridWidthPx - 10f
        assertEquals(LifeMonth(1998, 12), LifeHitTest.monthAt(x, 8f, 0, origin, layout))
    }

    @Test
    fun outOfGridIsNull() {
        assertNull(LifeHitTest.monthAt(40f, -5f, 0, origin, layout))
        assertNull(LifeHitTest.monthAt(40f, 10f, 40 * 90, origin, layout))
    }
}

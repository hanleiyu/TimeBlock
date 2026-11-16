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
        contentTopPx = 0f,
    )

    private fun specLayout() = LifeHitLayout(
        rowHeightPx = LifeGridSpec.rowHeightDp.toFloat(),
        labelWidthPx = LifeGridSpec.ageGutterDp.toFloat(),
        trailingWidthPx = LifeGridSpec.trailingGutterDp.toFloat(),
        gapPx = LifeGridSpec.gapDp.toFloat(),
        gridWidthPx = (LifeGridSpec.circleFitsCellOnCompactWidthDp - 2 * LifeGridSpec.pagePaddingDp).toFloat(),
        contentTopPx = LifeGridSpec.contentTopDp.toFloat(),
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

    @Test
    fun contentTopInset_mapsVisualRowNotRawViewportY() {
        val grid = specLayout()
        val janX = grid.labelWidthPx + grid.cellWidthPx / 2f
        val topPad = grid.contentTopPx
        assertTrue("expected a content top inset like LifeScreen scroll padding", topPad > 0f)
        assertNull(LifeHitTest.monthAt(janX, topPad - 1f, 0, origin, grid))
        assertEquals(
            LifeMonth(1998, 1),
            LifeHitTest.monthAt(janX, topPad + 0.5f, 0, origin, grid),
        )
        assertEquals(
            LifeMonth(1998, 1),
            LifeHitTest.monthAt(janX, topPad + grid.rowHeightPx - 0.1f, 0, origin, grid),
        )
        assertEquals(
            LifeMonth(1999, 1),
            LifeHitTest.monthAt(janX, topPad + grid.rowHeightPx, 0, origin, grid),
        )
    }

    @Test
    fun circleCenterAndCellEdgesMapToSameMonth() {
        val grid = specLayout()
        val janLeft = grid.labelWidthPx
        val janRight = janLeft + grid.cellWidthPx
        val y = grid.contentTopPx + grid.rowHeightPx / 2f
        assertEquals(LifeMonth(1998, 1), LifeHitTest.monthAt(janLeft + 0.5f, y, 0, origin, grid))
        assertEquals(LifeMonth(1998, 1), LifeHitTest.monthAt(janLeft + grid.cellWidthPx / 2f, y, 0, origin, grid))
        assertEquals(LifeMonth(1998, 1), LifeHitTest.monthAt(janRight - 0.1f, y, 0, origin, grid))
        val junX = grid.labelWidthPx + 5 * (grid.cellWidthPx + grid.gapPx) + grid.cellWidthPx / 2f
        assertEquals(LifeMonth(1998, 6), LifeHitTest.monthAt(junX, y, 0, origin, grid))
    }

    @Test
    fun scrollPlusContentTopStillHitsTheVisibleRow() {
        val grid = specLayout()
        val janX = grid.labelWidthPx + grid.cellWidthPx / 2f
        val y = grid.contentTopPx + 4f
        assertEquals(LifeMonth(1998, 1), LifeHitTest.monthAt(janX, y, 0, origin, grid))
        assertEquals(
            LifeMonth(1999, 1),
            LifeHitTest.monthAt(janX, y, grid.rowHeightPx.toInt(), origin, grid),
        )
        assertEquals(
            LifeMonth(2008, 1),
            LifeHitTest.monthAt(janX, y, (10 * grid.rowHeightPx).toInt(), origin, grid),
        )
    }

    @Test
    fun lastYearLastMonthAndBeyondHorizon() {
        val grid = specLayout()
        val decX = grid.gridWidthPx - grid.trailingWidthPx / 2f
        val lastRowY = grid.contentTopPx + 89 * grid.rowHeightPx + grid.rowHeightPx / 2f
        assertEquals(LifeMonth(2087, 12), LifeHitTest.monthAt(decX, lastRowY, 0, origin, grid))
        assertNull(
            LifeHitTest.monthAt(
                decX,
                grid.contentTopPx + 90 * grid.rowHeightPx + 1f,
                0,
                origin,
                grid,
            ),
        )
    }

    private fun assertTrue(message: String, condition: Boolean) {
        org.junit.Assert.assertTrue(message, condition)
    }
}

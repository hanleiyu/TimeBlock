package com.shijiankuai.app.ui.today

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BlockHitTestTest {
    private val layout = BlockHitLayout(
        rowHeightPx = 100f,
        labelWidthPx = 40f,
        gapPx = 10f,
        gridWidthPx = 40f + 6 * 50f + 5 * 10f, // label + 6 cells + 5 gaps
    )

    @Test
    fun viewportTap_mapsColumnAndHour() {
        assertEquals(0, BlockHitTest.indexAt(viewportX = 45f, viewportY = 10f, scrollY = 0, layout))
        assertEquals(1, BlockHitTest.indexAt(viewportX = 105f, viewportY = 10f, scrollY = 0, layout))
        assertEquals(5, BlockHitTest.indexAt(viewportX = 360f, viewportY = 10f, scrollY = 0, layout))
        assertEquals(6, BlockHitTest.indexAt(viewportX = 45f, viewportY = 110f, scrollY = 0, layout))
    }

    @Test
    fun scrollOffset_shiftsHourOnly() {
        val x = 45f
        val y = 20f
        assertEquals(0, BlockHitTest.indexAt(x, y, scrollY = 0, layout))
        assertEquals(18, BlockHitTest.indexAt(x, y, scrollY = 300, layout))
        assertEquals(42, BlockHitTest.indexAt(x, y, scrollY = 700, layout))
    }

    @Test
    fun sameCoordinatesAfterClearAssignCancel_stillHitTheSameBlock() {
        val x = 165f
        val y = 55f
        val scroll = 200
        val before = BlockHitTest.indexAt(x, y, scroll, layout)
        val afterClear = BlockHitTest.indexAt(x, y, scroll, layout)
        val afterAssign = BlockHitTest.indexAt(x, y, scroll, layout)
        val afterCancel = BlockHitTest.indexAt(x, y, scroll, layout)
        assertEquals(14, before)
        assertEquals(before, afterClear)
        assertEquals(before, afterAssign)
        assertEquals(before, afterCancel)
    }

    @Test
    fun labelColumn_selectsFirstBlockOfThatHour() {
        assertEquals(12, BlockHitTest.indexAt(viewportX = 8f, viewportY = 210f, scrollY = 0, layout))
    }

    @Test
    fun outsideGrid_isIgnored() {
        assertNull(BlockHitTest.indexAt(viewportX = 45f, viewportY = -1f, scrollY = 0, layout))
        assertNull(BlockHitTest.indexAt(viewportX = 45f, viewportY = 20f, scrollY = 2400, layout))
    }
}

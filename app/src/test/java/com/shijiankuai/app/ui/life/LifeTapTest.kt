package com.shijiankuai.app.ui.life

import com.shijiankuai.app.domain.LifeCalendar
import com.shijiankuai.app.domain.LifeEvent
import com.shijiankuai.app.domain.LifeMonth
import com.shijiankuai.app.domain.LifeOrigin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LifeTapTest {
    private val origin = LifeOrigin(1998, 6)
    private val school = LifeEvent(
        id = "school",
        title = "小学",
        note = "note",
        start = LifeMonth(2004, 9),
        end = LifeMonth(2010, 6),
        color = 0xFF74B3AEL,
        createdAt = 1L,
        updatedAt = 2L,
    )
    private val camp = LifeEvent(
        id = "camp",
        title = "夏令营",
        note = "overlap",
        start = LifeMonth(2008, 7),
        end = LifeMonth(2008, 8),
        color = 0xFFE07A5FL,
        createdAt = 3L,
        updatedAt = 4L,
    )

    @Test
    fun emptyValidMonthTapOpensFormPrefillsThatMonth() {
        val month = LifeMonth(2012, 3)
        val action = LifeTap.fromHit(month, emptyList())
        assertEquals(LifeTap.OpenForm(month, month), action)
        val draft = LifeTap.blankEvent(month, month)
        assertEquals(month, draft.start)
        assertEquals(month, draft.end)
        assertEquals("", draft.id)
        assertEquals("", draft.title)
    }

    @Test
    fun recordedMonthTapOpensDetailsWithAllCovering() {
        val month = LifeMonth(2008, 7)
        val action = LifeTap.fromHit(month, listOf(school, camp))
        val details = action as LifeTap.OpenDetails
        assertEquals(month, details.month)
        assertEquals(listOf(camp, school), details.covering)
    }

    @Test
    fun detailsAddOpensFormForSameMonthWithoutDroppingOthers() {
        val month = LifeMonth(2008, 7)
        val covering = LifeCalendar.covering(listOf(school, camp), month)
        assertEquals(2, covering.size)
        assertEquals(LifeTap.OpenForm(month, month), LifeTap.addOverlap(month))
        assertEquals(listOf(camp, school), LifeCalendar.covering(listOf(school, camp), month))
    }

    @Test
    fun missedHitOrTopInsetDoesNotOpenForm() {
        assertNull(LifeTap.fromHit(null, emptyList()))
        val layout = LifeHitLayout(
            rowHeightPx = LifeGridSpec.rowHeightDp.toFloat(),
            labelWidthPx = LifeGridSpec.ageGutterDp.toFloat(),
            trailingWidthPx = LifeGridSpec.trailingGutterDp.toFloat(),
            gapPx = LifeGridSpec.gapDp.toFloat(),
            gridWidthPx = 328f,
            contentTopPx = LifeGridSpec.contentTopDp.toFloat(),
        )
        val miss = LifeHitTest.monthAt(40f, layout.contentTopPx - 1f, 0, origin, layout)
        assertNull(miss)
        assertNull(LifeTap.fromHit(miss, emptyList()))
    }

    @Test
    fun birthAndPrebirthEmptyCellsStillOpenForm() {
        val birth = LifeMonth(1998, 6)
        val prebirth = LifeMonth(1998, 1)
        assertEquals(LifeTap.OpenForm(birth, birth), LifeTap.fromHit(birth, emptyList()))
        assertEquals(LifeTap.OpenForm(prebirth, prebirth), LifeTap.fromHit(prebirth, emptyList()))
    }

    @Test
    fun outOfHorizonHitIsIgnored() {
        val layout = LifeHitLayout(
            rowHeightPx = 28f,
            labelWidthPx = 24f,
            trailingWidthPx = 24f,
            gapPx = 4f,
            gridWidthPx = 328f,
            contentTopPx = 12f,
        )
        val beyond = LifeHitTest.monthAt(40f, 12f + 90 * 28f + 1f, 0, origin, layout)
        assertNull(beyond)
        assertNull(LifeTap.fromHit(beyond, emptyList()))
    }

    @Test
    fun longPressRangeStillNormalizesToFormBounds() {
        val a = LifeMonth(2001, 1)
        val b = LifeMonth(2001, 4)
        val range = LifeCalendar.normalizeRange(b, a)
        assertEquals(a to b, range)
        assertEquals(LifeTap.OpenForm(a, b), LifeTap.OpenForm(range.first, range.second))
        assertFalse(LifeFormSpec.initialSingle(range.first, range.second))
        assertTrue(LifeFormSpec.initialSingle(a, a))
        assertTrue(LifeCalendar.index(origin, a) != null)
        assertTrue(LifeCalendar.index(origin, b) != null)
    }
}

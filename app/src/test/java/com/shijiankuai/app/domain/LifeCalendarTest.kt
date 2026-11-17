package com.shijiankuai.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LifeCalendarTest {
    private val origin = LifeOrigin(1998, 6)
    private val today = LifeMonth(2026, 11)

    private fun event(
        id: String,
        startY: Int,
        startM: Int,
        endY: Int,
        endM: Int,
        title: String = id,
        color: Long = 0xFF74B3AEL,
        updatedAt: Long = 1L,
        createdAt: Long = 1L,
        note: String = "note-$id",
    ) = LifeEvent(
        id = id,
        title = title,
        note = note,
        start = LifeMonth(startY, startM),
        end = LifeMonth(endY, endM),
        color = color,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    @Test
    fun originParseNeverGuesses() {
        assertNull(LifeOrigin.parse(null))
        assertNull(LifeOrigin.parse(""))
        assertNull(LifeOrigin.parse("1998"))
        assertNull(LifeOrigin.parse("1998-13"))
        assertEquals(LifeOrigin(1998, 6), LifeOrigin.parse("1998-06"))
        assertEquals("1998-06", LifeOrigin(1998, 6).encode())
    }

    @Test
    fun horizonIsNinetyCalendarYearsFromBirthYear() {
        val years = LifeCalendar.years(origin)
        assertEquals(1998, years.first)
        assertEquals(2087, years.last)
        assertEquals(90, years.count())
        assertEquals(90 * 12, LifeCalendar.MONTHS_IN_HORIZON)
    }

    @Test
    fun indexMapsCalendarMonthsAndRejectsOutOfWindow() {
        assertEquals(5, LifeCalendar.index(origin, LifeMonth(1998, 6)))
        assertEquals(0, LifeCalendar.index(origin, LifeMonth(1998, 1)))
        assertEquals(11, LifeCalendar.index(origin, LifeMonth(1998, 12)))
        assertEquals(12, LifeCalendar.index(origin, LifeMonth(1999, 1)))
        assertNull(LifeCalendar.index(origin, LifeMonth(1997, 12)))
        assertNull(LifeCalendar.index(origin, LifeMonth(2088, 1)))
        assertEquals(LifeMonth(2026, 11), LifeCalendar.monthAt(origin, LifeCalendar.index(origin, today)!!))
    }

    @Test
    fun cellsMarkPrebirthPastCurrentFutureWithoutSplittingColors() {
        val school = event("school", 2004, 9, 2010, 6, title = "小学", color = 0xFF74B3AEL)
        val cells = LifeCalendar.cells(origin, today, listOf(school))
        assertEquals(90 * 12, cells.size)

        val jan98 = cells[0]
        assertEquals(LifeTone.PREBIRTH, jan98.tone)
        assertNull(jan98.display)
        assertFalse(jan98.birth)

        val birth = cells[5]
        assertTrue(birth.birth)
        assertEquals(LifeTone.PAST, birth.tone)

        val current = cells[LifeCalendar.index(origin, today)!!]
        assertEquals(LifeTone.CURRENT, current.tone)
        assertNull(current.display)

        val future = cells[LifeCalendar.index(origin, LifeMonth(2026, 12))!!]
        assertEquals(LifeTone.FUTURE, future.tone)

        val schoolStart = cells[LifeCalendar.index(origin, LifeMonth(2004, 9))!!]
        val schoolEnd = cells[LifeCalendar.index(origin, LifeMonth(2010, 6))!!]
        assertEquals("小学", schoolStart.display?.title)
        assertEquals(0xFF74B3AEL, schoolStart.display?.color)
        assertEquals("小学", schoolEnd.eventLabel)
        assertNull(schoolStart.eventLabel)
        assertEquals(schoolStart.display, schoolEnd.display)
    }

    @Test
    fun sparseDecadeAgeLabelsSitOnTheAgeRowNotEveryYear() {
        val cells = LifeCalendar.cells(origin, today, emptyList())
        fun labelOn(year: Int) = cells
            .filter { it.month.year == year }
            .map { it.ageLabel }
            .firstOrNull { it != null }
        assertNull(labelOn(1998))
        assertEquals("10岁", labelOn(2008))
        assertEquals("20岁", labelOn(2018))
        assertEquals("30岁", labelOn(2028))
        assertNull(labelOn(2007))
        assertNull(labelOn(2009))
    }

    @Test
    fun elapsedCaptionUsesYearMonthDeltaNotInclusivePad() {
        assertEquals("已过 28 年 5 个月", LifeCalendar.elapsedCaption(origin, today))
        assertEquals("已过 0 年 0 个月", LifeCalendar.elapsedCaption(origin, LifeMonth(1998, 6)))
        assertEquals("已过 0 年 0 个月", LifeCalendar.elapsedCaption(origin, LifeMonth(1998, 5)))
        assertEquals("已过 1 年 0 个月", LifeCalendar.elapsedCaption(origin, LifeMonth(1999, 6)))
    }

    @Test
    fun progressSegmentsMatchCellFills() {
        val school = event("school", 2004, 9, 2010, 6, color = 0xFF74B3AEL)
        val cells = LifeCalendar.cells(origin, today, listOf(school))
        val segs = LifeCalendar.progress(origin, today, listOf(school))
        assertEquals(cells.size, segs.sumOf { it.length })
        assertEquals(cells.map { it.fillKey() }, segs.flatMap { seg -> List(seg.length) { seg.fillKey } })
    }

    @Test
    fun singleMonthAndCrossYearSpansAreContiguous() {
        val one = event("one", 2020, 3, 2020, 3)
        assertEquals(listOf(LifeMonth(2020, 3)), LifeCalendar.monthsIn(one.start, one.end))
        val span = event("span", 2004, 11, 2005, 2)
        assertEquals(
            listOf(
                LifeMonth(2004, 11),
                LifeMonth(2004, 12),
                LifeMonth(2005, 1),
                LifeMonth(2005, 2),
            ),
            LifeCalendar.monthsIn(span.start, span.end),
        )
    }

    @Test
    fun overlappingRecordsAreKeptAndSameMonthShowsOneWinningColor() {
        val school = event("school", 2004, 9, 2010, 6, title = "小学", color = 0xFF74B3AEL, updatedAt = 10)
        val camp = event("camp", 2008, 7, 2008, 8, title = "夏令营", color = 0xFFE3C783L, updatedAt = 20)
        val events = listOf(school, camp)
        assertTrue(LifeCalendar.validate(events, camp, excludeId = null) is LifeSave.Ok)

        val july = LifeMonth(2008, 7)
        val covering = LifeCalendar.covering(events, july)
        assertEquals(setOf("camp", "school"), covering.map { it.id }.toSet())
        assertEquals(2, covering.size)
        assertTrue(covering.any { it.id == "school" })
        assertTrue(covering.any { it.id == "camp" })
        assertEquals(camp, LifeCalendar.displayEvent(events, july))
        assertEquals(0xFFE3C783L, LifeCalendar.displayEvent(events, july)?.color)
        assertEquals(school, LifeCalendar.displayEvent(events, LifeMonth(2008, 6)))
        assertEquals(school, LifeCalendar.displayEvent(events, LifeMonth(2008, 9)))
    }

    @Test
    fun newerUpdateWinsColorAndStableIdBreaksTies() {
        val a = event("aaa", 2011, 1, 2011, 12, color = 0xFFDE8B9DL, updatedAt = 5)
        val b = event("bbb", 2011, 1, 2011, 12, color = 0xFF8EBFD2L, updatedAt = 5)
        assertEquals(b, LifeCalendar.displayEvent(listOf(a, b), LifeMonth(2011, 6)))
        val bumped = a.copy(updatedAt = 6, color = 0xFF9CCB97L)
        assertEquals(bumped, LifeCalendar.displayEvent(listOf(bumped, b), LifeMonth(2011, 6)))
        assertEquals(0xFF9CCB97L, LifeCalendar.displayEvent(listOf(bumped, b), LifeMonth(2011, 6))?.color)
    }

    @Test
    fun deletingWinnerRevealsNextColorAndEditDoesNotMutateOthers() {
        val school = event("school", 2004, 9, 2010, 6, color = 0xFF74B3AEL, updatedAt = 10)
        val camp = event("camp", 2008, 7, 2008, 8, color = 0xFFE3C783L, updatedAt = 20)
        val afterDelete = listOf(school)
        assertEquals(school, LifeCalendar.displayEvent(afterDelete, LifeMonth(2008, 7)))
        val renamed = school.copy(title = "小学时光")
        val afterEdit = listOf(renamed, camp)
        assertEquals("小学时光", afterEdit.single { it.id == "school" }.title)
        assertEquals("camp", afterEdit.single { it.id == "camp" }.id)
        assertEquals(camp, LifeCalendar.displayEvent(afterEdit, LifeMonth(2008, 7)))
        assertEquals(0xFF74B3AEL, renamed.color)
    }

    @Test
    fun progressUsesTheSameOverlapResolver() {
        val school = event("school", 2004, 9, 2010, 6, color = 0xFF74B3AEL, updatedAt = 10)
        val camp = event("camp", 2008, 7, 2008, 8, color = 0xFFE3C783L, updatedAt = 20)
        val events = listOf(school, camp)
        val cells = LifeCalendar.cells(origin, today, events)
        val segs = LifeCalendar.progress(origin, today, events)
        assertEquals(cells.map { it.fillKey() }, segs.flatMap { seg -> List(seg.length) { seg.fillKey } })
        val july = cells[LifeCalendar.index(origin, LifeMonth(2008, 7))!!]
        assertEquals("e:camp", july.fillKey())
        assertEquals(0xFFE3C783L, july.display?.color)
        assertEquals(2, july.covering.size)
    }

    @Test
    fun validateRejectsEmptyTitleAndBackwardsRangeButAllowsOverlap() {
        val school = event("school", 2004, 9, 2010, 6)
        assertTrue(
            LifeCalendar.validate(listOf(school), event("job", 2010, 6, 2012, 1), null) is LifeSave.Ok,
        )
        assertEquals(
            LifeSave.EmptyTitle,
            LifeCalendar.validate(emptyList(), school.copy(title = "  "), school.id),
        )
        assertEquals(
            LifeSave.InvalidRange,
            LifeCalendar.validate(emptyList(), event("bad", 2012, 1, 2010, 6), null),
        )
    }

    @Test
    fun outOfWindowEventsStayAddressableButAreNotPainted() {
        val early = event("early", 1990, 1, 1990, 6)
        val late = event("late", 2090, 1, 2090, 3)
        val cells = LifeCalendar.cells(origin, today, listOf(early, late))
        assertTrue(cells.none { it.display?.id == "early" || it.display?.id == "late" })
        assertEquals(listOf(early), LifeCalendar.covering(listOf(early, late), LifeMonth(1990, 3)))
        assertEquals(listOf(late), LifeCalendar.covering(listOf(early, late), LifeMonth(2090, 2)))
    }

    @Test
    fun coveringReturnsEveryRecordOnAMonth() {
        val school = event("school", 2004, 9, 2010, 6, title = "小学")
        val extra = event("extra", 2010, 6, 2010, 6, title = "毕业", updatedAt = 9)
        val events = listOf(school, extra)
        val both = LifeCalendar.covering(events, LifeMonth(2010, 6))
        assertEquals(setOf("school", "extra"), both.map { it.id }.toSet())
        assertEquals(extra, LifeCalendar.displayEvent(events, LifeMonth(2010, 6)))
        assertEquals(listOf(school), LifeCalendar.covering(events, LifeMonth(2010, 5)))
        assertTrue(LifeCalendar.covering(events, LifeMonth(2010, 7)).isEmpty())
    }

    @Test
    fun dragRangeNormalizesAcrossRows() {
        val a = LifeMonth(2004, 11)
        val b = LifeMonth(2005, 2)
        assertEquals(a to b, LifeCalendar.normalizeRange(b, a))
        assertEquals(a to a, LifeCalendar.normalizeRange(a, a))
    }

    @Test
    fun rangeLabelKeepsMonthPrecision() {
        assertEquals("2004年9月", LifeCalendar.rangeLabel(LifeMonth(2004, 9), LifeMonth(2004, 9)))
        assertEquals("2004年9月 – 2010年6月", LifeCalendar.rangeLabel(LifeMonth(2004, 9), LifeMonth(2010, 6)))
    }

    @Test
    fun presetPaletteIsTheTypeSwatches() {
        assertEquals(Presets.swatches, LifeCalendar.colors)
        assertTrue(LifeCalendar.colors.size >= 8)
        assertTrue(0xFFEEAE23L in LifeCalendar.colors)
        assertTrue(0xFF9CCB97L in LifeCalendar.colors)
    }
}

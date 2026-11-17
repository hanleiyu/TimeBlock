package com.shijiankuai.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LifeDotPaintTest {
    private fun event(
        id: String,
        color: Long,
        updatedAt: Long,
        start: LifeMonth = LifeMonth(2008, 7),
        end: LifeMonth = LifeMonth(2008, 8),
    ) = LifeEvent(id, id, "", start, end, color, createdAt = 1L, updatedAt = updatedAt)

    @Test
    fun zeroCoveringYieldsNoSectorsAndKeepsEmptyAppearance() {
        assertTrue(LifeCalendar.dotColors(emptyList()).isEmpty())
        assertTrue(LifeCalendar.dotSectors(emptyList()).isEmpty())
    }

    @Test
    fun oneColorIsAFullDisk() {
        val green = 0xFF74B3AEL
        val colors = LifeCalendar.dotColors(listOf(event("only", green, 1)))
        assertEquals(listOf(ColorMath.normalize(green)), colors)
        val sectors = LifeCalendar.dotSectors(colors)
        assertEquals(1, sectors.size)
        assertEquals(360f, sectors.single().sweepDeg, 0.001f)
        assertEquals(LifeCalendar.DOT_START_DEG, sectors.single().startDeg, 0.001f)
    }

    @Test
    fun duplicateColorsCollapseToOneSector() {
        val green = 0xFF74B3AEL
        val sameAsUnsigned = 4285838254L
        val covering = listOf(
            event("new", green, updatedAt = 20),
            event("old", sameAsUnsigned, updatedAt = 10),
        )
        val colors = LifeCalendar.dotColors(covering)
        assertEquals(listOf(ColorMath.normalize(green)), colors)
        assertEquals(1, LifeCalendar.dotSectors(colors).size)
    }

    @Test
    fun twoDistinctColorsAreLeftThenRightHalvesInCoverOrder() {
        val camp = event("camp", 0xFFE3C783L, updatedAt = 20)
        val school = event("school", 0xFF74B3AEL, updatedAt = 10)
        val colors = LifeCalendar.dotColors(listOf(school, camp), LifeMonth(2008, 7))
        assertEquals(
            listOf(ColorMath.normalize(camp.color), ColorMath.normalize(school.color)),
            colors,
        )
        val sectors = LifeCalendar.dotSectors(colors)
        assertEquals(2, sectors.size)
        assertEquals(180f, sectors[0].sweepDeg, 0.001f)
        assertEquals(180f, sectors[1].sweepDeg, 0.001f)
        assertEquals(90f, sectors[0].startDeg, 0.001f)
        assertEquals(270f, sectors[1].startDeg, 0.001f)
        assertEquals(360f, sectors.sumOf { it.sweepDeg.toDouble() }.toFloat(), 0.001f)
    }

    @Test
    fun threeDistinctColorsAreEqualThirdsAndKeepStableOrder() {
        val a = event("aaa", 0xFFDE8B9DL, updatedAt = 5)
        val b = event("bbb", 0xFF8EBFD2L, updatedAt = 5)
        val c = event("ccc", 0xFFEEAE23L, updatedAt = 4)
        val shuffled = listOf(c, a, b)
        val colors = LifeCalendar.dotColors(shuffled, LifeMonth(2008, 7))
        assertEquals(
            listOf(
                ColorMath.normalize(b.color),
                ColorMath.normalize(a.color),
                ColorMath.normalize(c.color),
            ),
            colors,
        )
        val sectors = LifeCalendar.dotSectors(colors)
        assertEquals(3, sectors.size)
        sectors.forEach { assertEquals(120f, it.sweepDeg, 0.001f) }
        assertEquals(90f, sectors[0].startDeg, 0.001f)
        assertEquals(210f, sectors[1].startDeg, 0.001f)
        assertEquals(330f, sectors[2].startDeg, 0.001f)
    }

    @Test
    fun fourColorsAreNotTruncated() {
        val events = listOf(
            event("d", 0xFF808BD0L, 4),
            event("c", 0xFFC4B5E3L, 3),
            event("b", 0xFFE5AD8CL, 2),
            event("a", 0xFFDE8B9DL, 1),
        )
        val colors = LifeCalendar.dotColors(events)
        assertEquals(4, colors.size)
        val sectors = LifeCalendar.dotSectors(colors)
        assertEquals(4, sectors.size)
        sectors.forEach { assertEquals(90f, it.sweepDeg, 0.001f) }
    }
}

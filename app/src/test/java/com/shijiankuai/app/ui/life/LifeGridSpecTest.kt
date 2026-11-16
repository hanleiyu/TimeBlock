package com.shijiankuai.app.ui.life

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LifeGridSpecTest {
    @Test
    fun guttersAreSymmetricAndNarrowerThanLegacyReserve() {
        assertEquals(LifeGridSpec.ageGutterDp, LifeGridSpec.trailingGutterDp)
        assertTrue(LifeGridSpec.ageGutterDp < 36)
        assertTrue(LifeGridSpec.trailingGutterDp < 48)
        assertTrue(LifeGridSpec.ageGutterDp >= 20)
        assertEquals(16, LifeGridSpec.pagePaddingDp)
        assertTrue(LifeGridSpec.headingsAlignWithCircles)
        assertEquals(LifeGridSpec.circleDp, 18)
        assertEquals(LifeGridSpec.gapDp, 4)
        assertEquals(28, LifeGridSpec.rowHeightDp)
        assertEquals(LifeGridSpec.ageGutterDp, LifeGridSpec.progressStartInsetDp)
        assertEquals(LifeGridSpec.trailingGutterDp, LifeGridSpec.progressEndInsetDp)
    }

    @Test
    fun compactWidthCellsFitCirclesAndStayCentered() {
        val gridWidth = LifeGridSpec.circleFitsCellOnCompactWidthDp - 2 * LifeGridSpec.pagePaddingDp
        val layout = LifeHitLayout(
            rowHeightPx = LifeGridSpec.rowHeightDp.toFloat(),
            labelWidthPx = LifeGridSpec.ageGutterDp.toFloat(),
            trailingWidthPx = LifeGridSpec.trailingGutterDp.toFloat(),
            gapPx = LifeGridSpec.gapDp.toFloat(),
            gridWidthPx = gridWidth.toFloat(),
            contentTopPx = LifeGridSpec.contentTopDp.toFloat(),
        )
        assertTrue(layout.cellWidthPx + 0.01f >= LifeGridSpec.circleDp)
        assertEquals(layout.labelWidthPx, layout.trailingWidthPx)
        val leftMargin = layout.labelWidthPx
        val rightMargin = layout.trailingWidthPx
        assertEquals(leftMargin, rightMargin, 0.01f)
    }

    @Test
    fun legacyGuttersOnCompactWidthMakeCellsNarrowerThanCircles() {
        val gridWidth = (360 - 2 * 16).toFloat()
        val legacy = LifeHitLayout(
            rowHeightPx = 28f,
            labelWidthPx = 36f,
            trailingWidthPx = 48f,
            gapPx = 4f,
            gridWidthPx = gridWidth,
            contentTopPx = 12f,
        )
        assertTrue(legacy.cellWidthPx < 18f)
    }
}

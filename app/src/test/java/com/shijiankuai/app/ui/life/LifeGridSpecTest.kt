package com.shijiankuai.app.ui.life

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LifeGridSpecTest {
    @Test
    fun rightTitleGutterIsGoneAndDotsAreLarger() {
        assertEquals(24, LifeGridSpec.ageGutterDp)
        assertEquals(0, LifeGridSpec.trailingGutterDp)
        assertEquals(16, LifeGridSpec.pagePaddingDp)
        assertEquals(20, LifeGridSpec.circleDp)
        assertEquals(2, LifeGridSpec.gapDp)
        assertEquals(30, LifeGridSpec.rowHeightDp)
        assertTrue(LifeGridSpec.circleDp > 18)
        assertTrue(LifeGridSpec.gapDp < 4)
        assertTrue(LifeGridSpec.headingsAlignWithCircles)
        assertEquals(LifeGridSpec.ageGutterDp, LifeGridSpec.progressStartInsetDp)
        assertEquals(LifeGridSpec.trailingGutterDp, LifeGridSpec.progressEndInsetDp)
        assertEquals(0, LifeGridSpec.progressEndInsetDp)
    }

    @Test
    fun compactAndNarrowWidthsFitCirclesWithoutClipping() {
        listOf(LifeGridSpec.minPhoneWidthDp, LifeGridSpec.circleFitsCellOnCompactWidthDp, 390, 412).forEach { width ->
            val layout = layoutForWidth(width)
            assertTrue(
                "cell ${layout.cellWidthPx} must fit circle ${LifeGridSpec.circleDp} at ${width}dp",
                layout.cellWidthPx + 0.01f >= LifeGridSpec.circleDp,
            )
            assertEquals(0f, layout.trailingWidthPx, 0.01f)
        }
    }

    @Test
    fun reclaimingTrailingGutterWidensCellsPastTheOldCircle() {
        val now = layoutForWidth(360)
        val old = LifeHitLayout(
            rowHeightPx = 28f,
            labelWidthPx = 24f,
            trailingWidthPx = 24f,
            gapPx = 4f,
            gridWidthPx = (360 - 2 * 16).toFloat(),
            contentTopPx = 12f,
        )
        assertTrue(now.cellWidthPx > old.cellWidthPx)
        assertTrue(now.cellWidthPx + 0.01f >= LifeGridSpec.circleDp)
    }

    private fun layoutForWidth(widthDp: Int) = LifeHitLayout(
        rowHeightPx = LifeGridSpec.rowHeightDp.toFloat(),
        labelWidthPx = LifeGridSpec.ageGutterDp.toFloat(),
        trailingWidthPx = LifeGridSpec.trailingGutterDp.toFloat(),
        gapPx = LifeGridSpec.gapDp.toFloat(),
        gridWidthPx = (widthDp - 2 * LifeGridSpec.pagePaddingDp).toFloat(),
        contentTopPx = LifeGridSpec.contentTopDp.toFloat(),
    )
}

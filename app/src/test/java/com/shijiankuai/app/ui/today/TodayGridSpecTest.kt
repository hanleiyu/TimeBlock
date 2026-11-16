package com.shijiankuai.app.ui.today

import com.shijiankuai.app.ui.life.LifeGridSpec
import org.junit.Assert.assertEquals
import org.junit.Test

class TodayGridSpecTest {
    @Test
    fun progressTrackMatchesFirstAndLastBlockCellOuterEdges() {
        listOf(320, 360, 390, 412).forEach { widthDp ->
            val first = TodayGridSpec.firstCellLeftDp.toFloat()
            val last = widthDp - TodayGridSpec.lastCellRightInsetDp
            val progressLeft = TodayGridSpec.progressStartInsetDp.toFloat()
            val progressRight = widthDp - TodayGridSpec.progressEndInsetDp
            assertEquals("left @${widthDp}dp", first, progressLeft, 0.01f)
            assertEquals("right @${widthDp}dp", last.toFloat(), progressRight.toFloat(), 0.01f)
            assertEquals(TodayGridSpec.startPadDp + TodayGridSpec.labelWidthDp, TodayGridSpec.progressStartInsetDp)
            assertEquals(TodayGridSpec.endPadDp, TodayGridSpec.progressEndInsetDp)
        }
    }

    @Test
    fun lifeProgressTrackMatchesFirstAndLastMonthCellOuterEdges() {
        listOf(320, 360, 390, 412).forEach { widthDp ->
            val page = LifeGridSpec.pagePaddingDp
            val first = page + LifeGridSpec.ageGutterDp
            val last = widthDp - page - LifeGridSpec.trailingGutterDp
            val progressLeft = page + LifeGridSpec.progressStartInsetDp
            val progressRight = widthDp - page - LifeGridSpec.progressEndInsetDp
            assertEquals("life left @${widthDp}dp", first, progressLeft)
            assertEquals("life right @${widthDp}dp", last, progressRight)
        }
    }
}

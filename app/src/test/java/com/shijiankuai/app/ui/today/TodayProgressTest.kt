package com.shijiankuai.app.ui.today

import com.shijiankuai.app.domain.SLEEP_TYPE_ID
import org.junit.Assert.assertEquals
import org.junit.Test

class TodayProgressTest {
    @Test
    fun segmentsMergeRunsAndSplitAtNow() {
        val blocks = (4..42).associateWith { SLEEP_TYPE_ID }
        val segs = TodayProgress.segments(blocks, elapsed = 43)
        assertEquals(
            listOf(
                DayProgressSeg(typeId = null, length = 4, future = false),
                DayProgressSeg(typeId = SLEEP_TYPE_ID, length = 39, future = false),
                DayProgressSeg(typeId = null, length = 101, future = true),
            ),
            segs,
        )
        assertEquals(144, segs.sumOf { it.length })
    }
}

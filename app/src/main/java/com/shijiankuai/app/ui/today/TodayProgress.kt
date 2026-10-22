package com.shijiankuai.app.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shijiankuai.app.domain.BLOCKS_PER_DAY
import com.shijiankuai.app.domain.BlockType
import com.shijiankuai.app.ui.theme.EmptyFuture
import com.shijiankuai.app.ui.theme.EmptyPast

data class DayProgressSeg(
    val typeId: String?,
    val length: Int,
    val future: Boolean,
)

object TodayProgress {
    fun segments(
        blocks: Map<Int, String>,
        elapsed: Int,
        total: Int = BLOCKS_PER_DAY,
    ): List<DayProgressSeg> {
        if (total <= 0) return emptyList()
        val raw = (0 until total).map { i ->
            DayProgressSeg(
                typeId = blocks[i],
                length = 1,
                future = i >= elapsed,
            )
        }
        val out = mutableListOf<DayProgressSeg>()
        raw.forEach { seg ->
            val last = out.lastOrNull()
            if (last != null && last.typeId == seg.typeId && last.future == seg.future) {
                out[out.lastIndex] = last.copy(length = last.length + 1)
            } else {
                out += seg
            }
        }
        return out
    }
}

@Composable
fun TodayProgressBar(
    blocks: Map<Int, String>,
    types: List<BlockType>,
    elapsed: Int,
    modifier: Modifier = Modifier,
) {
    val colors = types.associate { it.id to Color(it.color.toInt()) }
    val segs = TodayProgress.segments(blocks, elapsed)
    Row(
        modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(EmptyFuture),
    ) {
        segs.forEach { seg ->
            val color = when {
                seg.future -> EmptyFuture
                seg.typeId != null -> colors[seg.typeId] ?: EmptyPast
                else -> EmptyPast
            }
            Box(
                Modifier
                    .weight(seg.length.toFloat())
                    .fillMaxHeight()
                    .background(color),
            )
        }
    }
}

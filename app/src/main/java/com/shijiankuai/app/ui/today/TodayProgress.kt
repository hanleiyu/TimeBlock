package com.shijiankuai.app.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shijiankuai.app.domain.BLOCKS_PER_DAY
import com.shijiankuai.app.domain.BlockType
import com.shijiankuai.app.ui.theme.Accent
import com.shijiankuai.app.ui.theme.EmptyFuture
import com.shijiankuai.app.ui.theme.EmptyPast
import com.shijiankuai.app.ui.theme.Ink
import com.shijiankuai.app.ui.theme.PaperDeep
import java.time.LocalTime
import kotlin.math.roundToInt

enum class TodayProgressStyle { A, B, C }

object TodayProgressFlags {
    val style: TodayProgressStyle = TodayProgressStyle.A
}

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

    fun percent(elapsed: Int, total: Int = BLOCKS_PER_DAY): Int {
        if (total <= 0) return 0
        return ((elapsed * 100f) / total).roundToInt().coerceIn(0, 100)
    }
}

@Composable
fun TodayProgressBar(
    style: TodayProgressStyle,
    blocks: Map<Int, String>,
    types: List<BlockType>,
    elapsed: Int,
    nowTime: LocalTime?,
    modifier: Modifier = Modifier,
) {
    val colors = types.associate { it.id to Color(it.color.toInt()) }
    when (style) {
        TodayProgressStyle.A -> ProgressA(blocks, colors, elapsed, modifier)
        TodayProgressStyle.B -> ProgressB(elapsed, nowTime, modifier)
        TodayProgressStyle.C -> ProgressC(elapsed, modifier)
    }
}

@Composable
private fun ProgressA(
    blocks: Map<Int, String>,
    colors: Map<String, Color>,
    elapsed: Int,
    modifier: Modifier,
) {
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

@Composable
private fun ProgressB(
    elapsed: Int,
    nowTime: LocalTime?,
    modifier: Modifier,
) {
    val pct = TodayProgress.percent(elapsed)
    val clock = nowTime?.let { "%02d:%02d".format(it.hour, it.minute) }
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(PaperDeep),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(pct / 100f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(999.dp))
                    .background(Accent),
            )
        }
        Text(
            listOfNotNull("$pct%", clock).joinToString("  "),
            color = Ink,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 10.dp),
        )
    }
}

@Composable
private fun ProgressC(
    elapsed: Int,
    modifier: Modifier,
) {
    val frac = (elapsed / BLOCKS_PER_DAY.toFloat()).coerceIn(0f, 1f)
    Box(
        modifier
            .fillMaxWidth()
            .height(14.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(PaperDeep),
    ) {
        Box(
            Modifier
                .fillMaxWidth(frac)
                .fillMaxHeight()
                .background(Ink),
        )
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth(frac),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Box(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Ink),
            )
        }
    }
}

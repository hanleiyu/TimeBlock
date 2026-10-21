package com.shijiankuai.app.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shijiankuai.app.domain.PeriodKind
import com.shijiankuai.app.domain.PeriodStats
import com.shijiankuai.app.ui.hoursLabel
import com.shijiankuai.app.ui.theme.Card
import com.shijiankuai.app.ui.theme.Ink
import com.shijiankuai.app.ui.theme.Ink2
import com.shijiankuai.app.ui.theme.Ink3
import com.shijiankuai.app.ui.theme.Line
import com.shijiankuai.app.ui.theme.Paper
import com.shijiankuai.app.ui.theme.PaperDeep
import com.shijiankuai.app.ui.theme.Unfilled
import com.shijiankuai.app.ui.toCn
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun StatsScreen(
    kind: PeriodKind,
    anchor: LocalDate,
    stats: PeriodStats?,
    onKind: (PeriodKind) -> Unit,
    onShift: (Int) -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Paper)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Text("统计", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE8E0D4))
                .padding(3.dp),
        ) {
            PeriodKind.entries.forEach { k ->
                val label = when (k) {
                    PeriodKind.DAY -> "日"
                    PeriodKind.WEEK -> "周"
                    PeriodKind.MONTH -> "月"
                    PeriodKind.YEAR -> "年"
                }
                val on = k == kind
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (on) Card else Color.Transparent)
                        .clickable { onKind(k) }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(label, fontSize = 13.sp, color = if (on) Ink else Ink2, fontWeight = if (on) FontWeight.Medium else FontWeight.Normal)
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                Modifier.size(28.dp).clip(CircleShape).background(Card).clickable { onShift(-1) },
                contentAlignment = Alignment.Center,
            ) { Text("‹", color = Ink2) }
            Text(periodLabel(kind, stats?.start ?: anchor, stats?.endInclusive ?: anchor), fontSize = 14.sp, color = Ink)
            Box(
                Modifier.size(28.dp).clip(CircleShape).background(Card).clickable { onShift(1) },
                contentAlignment = Alignment.Center,
            ) { Text("›", color = Ink2) }
        }

        val s = stats
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Sum("总块", s?.totalElapsed ?: 0)
            Sum("已填", s?.filled ?: 0)
            Sum("未填", s?.unfilled ?: 0)
        }

        Spacer(Modifier.height(8.dp))
        Donut(s)
        Spacer(Modifier.height(8.dp))

        val total = (s?.totalElapsed ?: 0).coerceAtLeast(1)
        s?.byType.orEmpty().forEach { row ->
            StatRow(Color(row.color.toInt()), row.name, row.blocks, total)
        }
        if (s != null && s.unfilled > 0) {
            StatRow(Unfilled, "未填", s.unfilled, total)
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun Sum(label: String, n: Int) {
    Column {
        Text("$n", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        Text(label, fontSize = 12.sp, color = Ink2)
    }
}

@Composable
private fun Donut(stats: PeriodStats?) {
    val slices = stats?.byType.orEmpty()
    val filled = stats?.filled ?: 0
    val total = stats?.totalElapsed ?: 0
    Box(Modifier.fillMaxWidth().padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(148.dp)) {
            val stroke = Stroke(width = 36.dp.toPx(), cap = StrokeCap.Butt)
            if (slices.isEmpty() || filled == 0) {
                drawArc(Color(0xFFD8D0C4), 0f, 360f, false, style = stroke)
            } else {
                var start = -90f
                slices.forEach { sl ->
                    val sweep = 360f * sl.blocks / filled.toFloat()
                    drawArc(Color(sl.color.toInt()), start, sweep, false, style = stroke)
                    start += sweep
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$filled", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink)
            Text("已填 / $total", fontSize = 10.sp, color = Ink3)
        }
    }
}

@Composable
private fun StatRow(color: Color, name: String, blocks: Int, total: Int) {
    val pct = if (total == 0) 0 else (blocks * 100f / total).roundToIntSafe()
    Row(
        Modifier
            .fillMaxWidth()
            .height(36.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Text(name, fontSize = 13.sp, color = Ink, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 8.dp).weight(1f))
        Text(
            "$blocks 块 · ${hoursLabel(blocks)} · $pct%",
            fontSize = 12.sp,
            color = Ink2,
        )
    }
}

private fun Float.roundToIntSafe(): Int = kotlin.math.round(this).toInt()

private fun periodLabel(kind: PeriodKind, start: LocalDate, end: LocalDate): String = when (kind) {
    PeriodKind.DAY -> start.toCn()
    PeriodKind.WEEK -> "${start.monthValue}/${start.dayOfMonth} – ${end.monthValue}/${end.dayOfMonth}"
    PeriodKind.MONTH -> "${start.year}年${start.monthValue}月"
    PeriodKind.YEAR -> "${start.year}年"
}

package com.shijiankuai.app.exercise.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shijiankuai.app.ui.theme.Accent
import com.shijiankuai.app.ui.theme.Card
import com.shijiankuai.app.ui.theme.HighlightWash
import com.shijiankuai.app.ui.theme.Ink
import com.shijiankuai.app.ui.theme.Ink2
import com.shijiankuai.app.ui.theme.Ink3
import com.shijiankuai.app.ui.theme.Line
import com.shijiankuai.app.ui.theme.PaperDeep
import com.shijiankuai.app.ui.theme.Unfilled
import kotlin.math.max

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    border: Color? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Card)
            .then(if (border != null) Modifier.border(1.dp, border, shape) else Modifier)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(14.dp),
        content = content,
    )
}

@Composable
fun ScreenHeader(title: String, subtitle: String? = null, onBack: (() -> Unit)? = null) {
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                Text(
                    text = "‹",
                    fontSize = 28.sp,
                    color = Ink,
                    modifier = Modifier
                        .clickable(onClick = onBack)
                        .padding(end = 8.dp),
                )
            }
            Text(title, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        }
        if (!subtitle.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(subtitle, fontSize = 13.sp, color = Ink2)
        }
    }
}

@Composable
fun ProgressBar(progress: Float, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(99.dp))
            .background(PaperDeep),
    ) {
        Box(
            Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(8.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(color),
        )
    }
}

@Composable
fun StatusPill(text: String, bg: Color, fg: Color) {
    Text(
        text = text,
        color = fg,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChoiceChipRow(
    options: List<String>,
    selected: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            val on = option == selected
            Text(
                text = option,
                color = if (on) Color.White else Ink,
                fontSize = 13.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(if (on) Accent else PaperDeep)
                    .clickable { onSelect(option) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
fun Segmented(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PaperDeep)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { option ->
            val on = option == selected
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (on) Card else Color.Transparent)
                    .clickable { onSelect(option) }
                    .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    option,
                    color = if (on) Ink else Ink2,
                    fontWeight = if (on) FontWeight.SemiBold else FontWeight.Medium,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun DurationStepper(
    value: Int,
    onChange: (Int) -> Unit,
    chips: List<Int>,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircleIconButton { onChange(max(5, value - 5)) }
            Text(
                text = "${value}分钟",
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ink,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            CircleIconButton(plus = true) { onChange(value + 5) }
        }
        Spacer(Modifier.height(14.dp))
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            chips.forEach { chip ->
                val on = chip == value
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (on) Accent else PaperDeep),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        chip.toString(),
                        color = if (on) Color.White else Ink2,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { onChange(chip) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CircleIconButton(plus: Boolean = false, onClick: () -> Unit) {
    Box(
        Modifier
            .size(36.dp)
            .clip(CircleShape)
            .border(1.dp, Line, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (plus) Icons.Outlined.Add else Icons.Outlined.Remove,
            contentDescription = null,
            tint = if (plus) Accent else Ink2,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
fun PrimaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Accent,
            disabledContainerColor = PaperDeep,
            contentColor = Color.White,
            disabledContentColor = Ink3,
        ),
        contentPadding = PaddingValues(0.dp),
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SettingsRow(label: String, value: String, onClick: (() -> Unit)? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = Ink, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(value, color = Ink2, fontSize = 14.sp)
        if (onClick != null) {
            Icon(Icons.Outlined.ChevronRight, null, tint = Ink3, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun EmptyHint(text: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Ink3, fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun LineChart(
    values: List<Float>,
    labels: List<String>,
    color: Color,
    yMaxHint: Float? = null,
    modifier: Modifier = Modifier.height(160.dp),
    highlightLast: Boolean = false,
) {
    if (values.isEmpty() || values.all { it == 0f }) {
        EmptyHint("记录几周后这里会显示趋势")
        return
    }
    val maxY = max(yMaxHint ?: 0f, values.max())
        .let { if (it <= 0f) 1f else it }
    Column(modifier.fillMaxWidth()) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            val left = 48.dp.toPx()
            val right = 8.dp.toPx()
            val top = 12.dp.toPx()
            val bottom = size.height - 8.dp.toPx()
            val width = size.width - left - right
            val height = bottom - top
            val grid = 3
            for (i in 0..grid) {
                val y = top + height * i / grid
                drawLine(Unfilled, Offset(left, y), Offset(size.width - right, y), strokeWidth = 2f)
            }
            val step = if (values.size == 1) 0f else width / (values.size - 1)
            val points = values.mapIndexed { i, v ->
                val x = left + step * i
                val y = bottom - (v / maxY) * height
                Offset(x, y)
            }
            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { lineTo(it.x, it.y) }
            }
            drawPath(path, color, style = Stroke(width = 5f, cap = StrokeCap.Round))
            points.forEachIndexed { i, p ->
                val last = highlightLast && i == points.lastIndex
                drawCircle(if (last) Card else color, radius = if (last) 9.dp.toPx() else 5.dp.toPx(), center = p)
                if (last) drawCircle(color, radius = 9.dp.toPx(), center = p, style = Stroke(width = 4f))
                else drawCircle(Card, radius = 2.2.dp.toPx(), center = p)
            }
        }
        if (labels.isNotEmpty()) {
            Row(Modifier.fillMaxWidth().padding(start = 40.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(labels.first(), color = Ink3, fontSize = 12.sp)
                if (labels.size > 1) Text(labels.last(), color = Ink3, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun SoftHintCard(text: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(HighlightWash)
            .padding(14.dp),
    ) {
        Column {
            Text("升级提示", color = Accent, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(6.dp))
            Text(text, color = Ink, fontSize = 13.sp, lineHeight = 20.sp)
        }
    }
}

package com.shijiankuai.app.ui.chrome

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shijiankuai.app.domain.PeriodKind
import com.shijiankuai.app.ui.PeriodFeature
import com.shijiankuai.app.ui.theme.Card
import com.shijiankuai.app.ui.theme.Ink
import com.shijiankuai.app.ui.theme.Ink2
import com.shijiankuai.app.ui.theme.OnSelected
import com.shijiankuai.app.ui.theme.Paper
import com.shijiankuai.app.ui.theme.PaperDeep
import com.shijiankuai.app.ui.theme.SelectedFill
import com.shijiankuai.app.ui.toCn
import java.time.LocalDate

@Composable
fun PageTitle(
    title: String,
    modifier: Modifier = Modifier,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        trailing()
    }
}

@Composable
fun JumpTodayPill(onClick: () -> Unit) {
    Text(
        "今天",
        color = Ink2,
        fontSize = 12.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Card)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

@Composable
fun <T> SegmentTabs(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PaperDeep)
            .padding(3.dp),
    ) {
        options.forEach { option ->
            val on = option == selected
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (on) SelectedFill else Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelect(option) },
                    )
                    .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label(option),
                    fontSize = 13.sp,
                    color = if (on) OnSelected else Ink2,
                    fontWeight = if (on) FontWeight.Bold else FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
fun PeriodScaffold(
    title: String,
    features: List<PeriodFeature>,
    selected: PeriodFeature,
    onSelect: (PeriodFeature) -> Unit,
    showJumpToday: Boolean,
    onJumpToday: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Paper),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            PageTitle(title) {
                if (showJumpToday) JumpTodayPill(onJumpToday)
            }
            Spacer(Modifier.height(12.dp))
            SegmentTabs(
                options = features,
                selected = selected,
                onSelect = onSelect,
                label = { it.label },
            )
        }
        Box(Modifier.weight(1f).fillMaxWidth(), content = content)
    }
}

@Composable
fun CircledDateBar(
    label: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onLabelClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            Modifier.size(28.dp).clip(CircleShape).background(Card).clickable(onClick = onPrev),
            contentAlignment = Alignment.Center,
        ) { Text("‹", color = Ink2) }
        Text(
            label,
            fontSize = 14.sp,
            color = Ink,
            modifier = Modifier.clickable(onClick = onLabelClick),
        )
        Box(
            Modifier.size(28.dp).clip(CircleShape).background(Card).clickable(onClick = onNext),
            contentAlignment = Alignment.Center,
        ) { Text("›", color = Ink2) }
    }
}

@Composable
fun PlainDateBar(
    label: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onLabelClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text("‹", fontSize = 22.sp, color = Ink2, modifier = Modifier.clickable(onClick = onPrev))
        Text(
            label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Ink,
            modifier = Modifier.clickable(onClick = onLabelClick),
        )
        Text("›", fontSize = 22.sp, color = Ink2, modifier = Modifier.clickable(onClick = onNext))
    }
}

fun PeriodKind.tabLabel(): String = when (this) {
    PeriodKind.DAY -> "日"
    PeriodKind.WEEK -> "周"
    PeriodKind.MONTH -> "月"
    PeriodKind.YEAR -> "年"
}

fun PeriodKind.barLabel(start: LocalDate, end: LocalDate): String = when (this) {
    PeriodKind.DAY -> start.toCn()
    PeriodKind.WEEK -> "${start.monthValue}/${start.dayOfMonth} – ${end.monthValue}/${end.dayOfMonth}"
    PeriodKind.MONTH -> "${start.year}年${start.monthValue}月"
    PeriodKind.YEAR -> "${start.year}年"
}

package com.shijiankuai.app.ui.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shijiankuai.app.domain.TodoCalendar
import com.shijiankuai.app.domain.TodoCategory
import com.shijiankuai.app.domain.TodoItem
import com.shijiankuai.app.domain.TodoMonth
import com.shijiankuai.app.domain.TodoRange
import com.shijiankuai.app.ui.theme.Card
import com.shijiankuai.app.ui.theme.HighlightWash
import com.shijiankuai.app.ui.theme.Ink
import com.shijiankuai.app.ui.theme.Ink2
import com.shijiankuai.app.ui.theme.Ink3
import com.shijiankuai.app.ui.theme.Line
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun TodoMonthPanel(
    date: LocalDate,
    today: LocalDate,
    items: List<TodoItem>,
    onSetDate: (LocalDate) -> Unit,
) {
    val month = YearMonth.from(date)
    val counts = remember(items, month) { TodoMonth.monthCounts(items, month) }
    val dots = remember(items) { TodoMonth.dayDots(items) }
    val done = remember(items, date) { TodoMonth.doneOn(items, date) }
    val grid = remember(month) { TodoMonth.monthGrid(month) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MonthKalendarCard(
            month = month,
            selected = date,
            today = today,
            grid = grid,
            dots = dots,
            onSelect = onSetDate,
        )
        DayDoneSummary(date = date, done = done)
        MonthCountRow(counts)
    }
}

@Composable
private fun MonthCountRow(counts: Map<TodoCategory, Int>) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TodoRange.SUMMARY_ORDER.forEach { cat ->
            Column(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Card)
                    .border(1.dp, Line, RoundedCornerShape(14.dp))
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    categoryIcon(cat),
                    contentDescription = cat.label,
                    tint = categoryTint(cat),
                    modifier = Modifier.size(18.dp),
                )
                Text(cat.label, fontSize = 11.sp, color = Ink2, modifier = Modifier.padding(top = 4.dp))
                Text(
                    "${counts[cat] ?: 0}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = categoryTint(cat),
                    modifier = Modifier.padding(top = 1.dp),
                )
            }
        }
    }
}

@Composable
private fun MonthKalendarCard(
    month: YearMonth,
    selected: LocalDate,
    today: LocalDate,
    grid: List<LocalDate>,
    dots: Map<LocalDate, List<TodoCategory>>,
    onSelect: (LocalDate) -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Card)
            .border(1.dp, Line, RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 12.dp),
    ) {
        Row(Modifier.fillMaxWidth()) {
            TodoCalendar.WEEKDAY_LABELS.forEach { label ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(label, fontSize = 12.sp, color = Ink3)
                }
            }
        }
        grid.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    MonthDayCell(
                        day = day,
                        inMonth = YearMonth.from(day) == month,
                        selected = day == selected,
                        isToday = day == today,
                        cats = dots[day].orEmpty(),
                        onClick = { onSelect(day) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthDayCell(
    day: LocalDate,
    inMonth: Boolean,
    selected: Boolean,
    isToday: Boolean,
    cats: List<TodoCategory>,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    val numberColor = when {
        selected -> Ink
        inMonth -> Ink
        else -> Ink3.copy(alpha = 0.38f)
    }
    Column(
        modifier
            .aspectRatio(0.92f)
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .size(32.dp)
                .clip(CircleShape)
                .then(
                    when {
                        selected -> Modifier.background(HighlightWash)
                        isToday && inMonth -> Modifier.border(1.dp, Ink3.copy(alpha = 0.35f), CircleShape)
                        else -> Modifier
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "${day.dayOfMonth}",
                fontSize = 14.sp,
                fontWeight = if (selected || isToday) FontWeight.SemiBold else FontWeight.Medium,
                color = numberColor,
            )
        }
        Row(
            Modifier.height(10.dp).padding(top = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            cats.take(3).forEach { cat ->
                Box(
                    Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(categoryTint(cat).copy(alpha = if (inMonth) 1f else 0.45f)),
                )
            }
        }
    }
}

@Composable
private fun DayDoneSummary(date: LocalDate, done: List<TodoItem>) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Card)
            .border(1.dp, Line, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(
            TodoMonth.summaryTitle(date),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Ink,
        )
        if (done.isEmpty()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 28.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(TodoMonth.summaryEmpty(), fontSize = 14.sp, color = Ink2)
            }
        } else {
            Text(
                TodoMonth.summaryCount(done.size),
                fontSize = 13.sp,
                color = Ink2,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            )
            done.forEach { item ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("✓", color = DoneGreen, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        item.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Ink,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 10.dp),
                    )
                }
            }
        }
    }
}

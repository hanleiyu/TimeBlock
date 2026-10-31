package com.shijiankuai.app.exercise.ui.month

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shijiankuai.app.exercise.domain.Category
import com.shijiankuai.app.exercise.domain.WeekCalculator
import com.shijiankuai.app.exercise.domain.Workout
import com.shijiankuai.app.exercise.domain.formatYuan
import com.shijiankuai.app.exercise.domain.toZhDateWeek
import com.shijiankuai.app.exercise.ui.RecordsViewModel
import com.shijiankuai.app.exercise.ui.components.AppCard
import com.shijiankuai.app.ui.theme.Accent
import com.shijiankuai.app.ui.theme.Aero
import com.shijiankuai.app.ui.theme.DaySelect
import com.shijiankuai.app.ui.theme.BalanceHue
import com.shijiankuai.app.ui.theme.BowelMark
import com.shijiankuai.app.ui.theme.Card
import com.shijiankuai.app.ui.theme.EarlySleepMark
import com.shijiankuai.app.ui.theme.Ink
import com.shijiankuai.app.ui.theme.Ink2
import com.shijiankuai.app.ui.theme.Ink3
import com.shijiankuai.app.ui.theme.Line
import com.shijiankuai.app.ui.theme.PeriodMark
import com.shijiankuai.app.ui.theme.StrengthHue
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun MonthScreen(
    vm: RecordsViewModel,
    onOpen: (Long) -> Unit,
    onAdd: (LocalDate) -> Unit,
) {
    val workouts by vm.workouts.collectAsStateWithLifecycle()
    val month by vm.month.collectAsStateWithLifecycle()
    val selected by vm.selected.collectAsStateWithLifecycle()
    val monthStats = WeekCalculator.statsForMonth(workouts, month)
    val monthEarn = WeekCalculator.monthEarnYuan(workouts, month)
    val totalEarn = WeekCalculator.totalEarnYuan(workouts)
    val dayWorkouts = workouts.filter { it.date == selected }
    val dayTotal = dayWorkouts.filter { it.category.countsTowardStats }.sumOf { it.rawMinutes }
    val dotsByDay = workouts
        .filter { YearMonth.from(it.date) == month }
        .groupBy { it.date }
        .mapValues { (_, list) -> list.map { it.category }.toSet() }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            AppCard {
                MonthHeader(month, onPrev = vm::prevMonth, onNext = vm::nextMonth)
                Spacer(Modifier.height(12.dp))
                WeekdayRow()
                Spacer(Modifier.height(6.dp))
                MonthGrid(
                    month = month,
                    selected = selected,
                    dotsByDay = dotsByDay,
                    onSelect = vm::select,
                )
            }
        }
        item {
            AppCard {
                Text("本月", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MonthStat("有氧", "${monthStats.rawAerobic}分钟")
                    MonthStat("力量", "${monthStats.rawStrength}分钟")
                    MonthStat("平衡", "${monthStats.balanceSessions}次")
                }
                Spacer(Modifier.height(14.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(Line))
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                    Column(Modifier.weight(1f)) {
                        Text("本月净赚", color = Ink, fontSize = 13.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            formatYuan(monthEarn),
                            color = Aero,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("累计", color = Ink, fontSize = 13.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            formatYuan(totalEarn),
                            color = Ink,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    selected.toZhDateWeek(),
                    color = Ink,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Text("共${dayTotal}分钟", color = Ink2, fontSize = 13.sp, modifier = Modifier.padding(end = 10.dp))
                Text(
                    "＋ 记录",
                    color = Accent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Card)
                        .clickable { onAdd(selected) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
        }
        if (dayWorkouts.isEmpty()) {
            item {
                AppCard { Text("没有记录", color = Ink, fontSize = 14.sp) }
            }
        } else {
            items(dayWorkouts, key = { it.id }) { w ->
                DayWorkoutRow(w) { onOpen(w.id) }
            }
        }
    }
}

@Composable
private fun MonthHeader(month: YearMonth, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("‹", color = Ink, fontSize = 22.sp, modifier = Modifier.clickable(onClick = onPrev).padding(8.dp))
        Text(
            "${month.year}年${month.monthValue}月",
            color = Ink,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        Text("›", color = Ink, fontSize = 22.sp, modifier = Modifier.clickable(onClick = onNext).padding(8.dp))
    }
}

@Composable
private fun WeekdayRow() {
    Row(Modifier.fillMaxWidth()) {
        listOf("一", "二", "三", "四", "五", "六", "日").forEach { label ->
            Text(
                label,
                color = Ink3,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    selected: LocalDate,
    dotsByDay: Map<LocalDate, Set<Category>>,
    onSelect: (LocalDate) -> Unit,
) {
    val first = month.atDay(1)
    val shift = (first.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
    val start = first.minusDays(shift.toLong())
    val rows = WeekCalculator.weekRowsForMonth(month)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(rows) { week ->
            Row(Modifier.fillMaxWidth()) {
                repeat(7) { dow ->
                    val date = start.plusDays((week * 7 + dow).toLong())
                    Box(Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                        if (YearMonth.from(date) == month) {
                            DayCell(
                                date = date,
                                selected = selected,
                                dots = dotsByDay[date].orEmpty(),
                                onSelect = onSelect,
                            )
                        } else {
                            Spacer(Modifier.height(48.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    selected: LocalDate,
    dots: Set<Category>,
    onSelect: (LocalDate) -> Unit,
) {
    val selectedDay = date == selected
    val hasPeriod = Category.PERIOD in dots
    val hasBowel = Category.BOWEL in dots
    val hasEarlySleep = Category.EARLY_SLEEP in dots
    val dayNumberColor = if (hasEarlySleep) EarlySleepMark else Ink
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(date) }
            .padding(vertical = 2.dp),
    ) {
        Box(Modifier.size(34.dp), contentAlignment = Alignment.Center) {
            if (selectedDay) {
                Box(Modifier.size(34.dp).border(1.5.dp, DaySelect, CircleShape))
            }
            Text(
                date.dayOfMonth.toString(),
                color = dayNumberColor,
                fontSize = 14.sp,
                fontWeight = if (selectedDay) FontWeight.SemiBold else FontWeight.Normal,
            )
        }
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
            Category.entries.forEach { cat ->
                if (cat.showsCalendarDot && cat in dots) {
                    Box(Modifier.size(5.dp).clip(CircleShape).background(cat.dotColor()))
                }
            }
            if (hasPeriod) PeriodTriangleMark()
            if (hasBowel) BowelCheckMark()
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun PeriodTriangleMark() {
    val mark = PeriodMark
    Canvas(Modifier.size(6.dp)) {
        val path = Path().apply {
            moveTo(size.width / 2f, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(path, color = mark)
    }
}

@Composable
private fun BowelCheckMark() {
    val mark = BowelMark
    Canvas(Modifier.size(7.dp)) {
        val stroke = Stroke(width = size.minDimension * 0.22f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val path = Path().apply {
            moveTo(size.width * 0.12f, size.height * 0.55f)
            lineTo(size.width * 0.38f, size.height * 0.82f)
            lineTo(size.width * 0.88f, size.height * 0.18f)
        }
        drawPath(path, color = mark, style = stroke)
    }
}

@Composable
private fun MonthStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 4.dp)) {
        Text(label, color = Ink, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        Text(value, color = Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DayWorkoutRow(w: Workout, onClick: () -> Unit) {
    val accent = w.category.dotColor()
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Card)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .width(3.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(accent),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(w.activityName, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(3.dp))
            Text(w.category.label, color = Ink2, fontSize = 12.sp)
        }
        Text(w.listDurationLabel, color = Ink, fontSize = 14.sp)
    }
}

@Composable
fun Category.dotColor(): Color = when (this) {
    Category.AEROBIC -> Aero
    Category.STRENGTH -> StrengthHue
    Category.BALANCE -> BalanceHue
    Category.PERIOD -> PeriodMark
    Category.BOWEL -> BowelMark
    Category.EARLY_SLEEP -> EarlySleepMark
}

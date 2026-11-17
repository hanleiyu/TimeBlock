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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shijiankuai.app.domain.TodoCalendar
import com.shijiankuai.app.exercise.domain.Category
import com.shijiankuai.app.exercise.domain.WeekCalculator
import com.shijiankuai.app.exercise.domain.Workout
import com.shijiankuai.app.exercise.domain.formatYuan
import com.shijiankuai.app.exercise.domain.toZhDateWeek
import com.shijiankuai.app.exercise.ui.RecordsViewModel
import com.shijiankuai.app.ui.DatePick
import com.shijiankuai.app.ui.chrome.DateNavBar
import com.shijiankuai.app.ui.chrome.DateNavSpec
import com.shijiankuai.app.ui.chrome.MonthCardSpec
import com.shijiankuai.app.ui.chrome.MonthFrame
import com.shijiankuai.app.ui.theme.Aero
import com.shijiankuai.app.ui.theme.DaySelect
import com.shijiankuai.app.ui.theme.BalanceHue
import com.shijiankuai.app.ui.theme.BowelMark
import com.shijiankuai.app.ui.theme.EarlySleepMark
import com.shijiankuai.app.ui.theme.Ink
import com.shijiankuai.app.ui.theme.Ink2
import com.shijiankuai.app.ui.theme.Ink3
import com.shijiankuai.app.ui.theme.Line
import com.shijiankuai.app.ui.theme.PeriodMark
import com.shijiankuai.app.ui.theme.StrengthHue
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun MonthScreen(
    vm: RecordsViewModel,
    onOpen: (Long) -> Unit,
    onOpenMetrics: () -> Unit = {},
) {
    val workouts by vm.workouts.collectAsStateWithLifecycle()
    val month by vm.month.collectAsStateWithLifecycle()
    val selected by vm.selected.collectAsStateWithLifecycle()
    val monthStats = WeekCalculator.statsForMonth(workouts, month)
    val monthEarn = WeekCalculator.monthEarnYuan(workouts, month)
    val totalEarn = WeekCalculator.totalEarnYuan(workouts)
    val dayWorkouts = workouts.filter { it.date == selected }
    val dayTotal = dayWorkouts.filter { it.category.countsTowardStats }.sumOf { it.rawMinutes }
    val dotsByDay = remember(workouts) {
        workouts.groupBy { it.date }.mapValues { (_, list) -> list.map { it.category }.toSet() }
    }
    val grid = remember(month) { HealthMonthLayout.grid(month) }
    val today = remember { LocalDate.now() }
    var showDate by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        DateNavBar(
            label = DateNavSpec.monthHealthLabel(month),
            onPrev = vm::prevMonth,
            onNext = vm::nextMonth,
            onLabelClick = { showDate = true },
        )
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(MonthCardSpec.gapDp.dp),
        ) {
            item {
                MonthKalendarCard(
                    month = month,
                    selected = selected,
                    today = today,
                    grid = grid,
                    dotsByDay = dotsByDay,
                    onSelect = vm::select,
                )
            }
            item {
                MonthFrame {
                    Text("本月", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("累计", color = Ink, fontSize = 13.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                formatYuan(totalEarn),
                                color = Ink,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
            item {
                MonthFrame(onClick = onOpenMetrics) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("指标", color = Ink, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        Icon(
                            Icons.Outlined.ChevronRight,
                            contentDescription = null,
                            tint = Ink2,
                        )
                    }
                }
            }
            item {
                MonthFrame {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            selected.toZhDateWeek(),
                            color = Ink,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                        Text("共${dayTotal}分钟", color = Ink2, fontSize = 13.sp)
                    }
                    if (dayWorkouts.isEmpty()) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 28.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("没有记录", color = Ink2, fontSize = 14.sp)
                        }
                    } else {
                        Spacer(Modifier.height(8.dp))
                        dayWorkouts.forEach { w ->
                            DayWorkoutRow(w) { onOpen(w.id) }
                        }
                    }
                }
            }
        }
    }
    if (showDate) {
        DatePick(selected, { vm.select(it); showDate = false }) { showDate = false }
    }
}

@Composable
private fun MonthKalendarCard(
    month: YearMonth,
    selected: LocalDate,
    today: LocalDate,
    grid: List<LocalDate>,
    dotsByDay: Map<LocalDate, Set<Category>>,
    onSelect: (LocalDate) -> Unit,
) {
    MonthFrame(
        padHDp = MonthCardSpec.calendarPadHDp,
        padVDp = MonthCardSpec.calendarPadVDp,
    ) {
        Row(Modifier.fillMaxWidth()) {
            TodoCalendar.WEEKDAY_LABELS.forEach { label ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(label, fontSize = MonthCardSpec.weekdaySp.sp, color = Ink3)
                }
            }
        }
        grid.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    DayCell(
                        date = day,
                        inMonth = YearMonth.from(day) == month,
                        selected = day == selected,
                        isToday = day == today,
                        dots = dotsByDay[day].orEmpty(),
                        onSelect = onSelect,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    inMonth: Boolean,
    selected: Boolean,
    isToday: Boolean,
    dots: Set<Category>,
    onSelect: (LocalDate) -> Unit,
    modifier: Modifier,
) {
    val hasPeriod = Category.PERIOD in dots
    val hasBowel = Category.BOWEL in dots
    val hasEarlySleep = Category.EARLY_SLEEP in dots
    val fade = if (inMonth) 1f else 0.45f
    val numberColor = when {
        selected -> if (hasEarlySleep && inMonth) EarlySleepMark else Ink
        !inMonth -> Ink3.copy(alpha = 0.38f)
        hasEarlySleep -> EarlySleepMark
        else -> Ink
    }
    Column(
        modifier
            .aspectRatio(MonthCardSpec.cellAspect)
            .clickable { onSelect(date) }
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.size(MonthCardSpec.dayCircleDp.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Box(
                    Modifier
                        .size(MonthCardSpec.dayCircleDp.dp)
                        .border(MonthCardSpec.selectedStrokeDp.dp, DaySelect, CircleShape),
                )
            } else if (isToday && inMonth) {
                Box(
                    Modifier
                        .size(MonthCardSpec.dayCircleDp.dp)
                        .border(MonthCardSpec.todayStrokeDp.dp, Ink3.copy(alpha = 0.35f), CircleShape),
                )
            }
            Text(
                "${date.dayOfMonth}",
                color = numberColor,
                fontSize = MonthCardSpec.dayNumberSp.sp,
                fontWeight = if (selected || isToday) FontWeight.Bold else FontWeight.SemiBold,
            )
        }
        Row(
            Modifier.height(10.dp).padding(top = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Category.entries.forEach { cat ->
                if (cat.showsCalendarDot && cat in dots) {
                    Box(
                        Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(cat.dotColor().copy(alpha = fade)),
                    )
                }
            }
            if (hasPeriod) PeriodTriangleMark(alpha = fade)
            if (hasBowel) BowelCheckMark(alpha = fade)
        }
    }
}

@Composable
private fun PeriodTriangleMark(alpha: Float) {
    val mark = PeriodMark.copy(alpha = PeriodMark.alpha * alpha)
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
private fun BowelCheckMark(alpha: Float) {
    val mark = BowelMark.copy(alpha = BowelMark.alpha * alpha)
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
        Text(value, color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DayWorkoutRow(w: Workout, onClick: () -> Unit) {
    val accent = w.category.dotColor()
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
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
            Text(w.activityName, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
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

package com.shijiankuai.app.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shijiankuai.app.domain.BlockType
import com.shijiankuai.app.ui.TodayUi
import com.shijiankuai.app.ui.hoursLabel
import com.shijiankuai.app.ui.theme.Accent
import com.shijiankuai.app.ui.theme.Card
import com.shijiankuai.app.ui.theme.EmptyFuture
import com.shijiankuai.app.ui.theme.EmptyPast
import com.shijiankuai.app.ui.theme.HighlightLine
import com.shijiankuai.app.ui.theme.HighlightWash
import com.shijiankuai.app.ui.theme.Ink
import com.shijiankuai.app.ui.theme.Ink2
import com.shijiankuai.app.ui.theme.Ink3
import com.shijiankuai.app.ui.theme.Line
import com.shijiankuai.app.ui.theme.Paper
import com.shijiankuai.app.ui.theme.Sleep
import com.shijiankuai.app.ui.theme.Wake
import com.shijiankuai.app.ui.toCn
import com.shijiankuai.app.ui.toHm
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    ui: TodayUi,
    zone: java.time.ZoneId,
    onSleep: () -> Unit,
    onWake: () -> Unit,
    onWakeWithStart: (LocalTime) -> Unit,
    onSkipSleepStart: () -> Unit,
    onSetRange: (Int, Int, String?) -> Unit,
    onShiftDate: (Int) -> Unit,
    onSetDate: (LocalDate) -> Unit,
    onEditLast: (LocalTime, LocalTime) -> Unit,
    onAddType: (String, Long) -> Unit,
    needSleepStart: Boolean,
    onDismissToast: () -> Unit,
) {
    var showDate by remember { mutableStateOf(false) }

    if (ui.isToday && ui.isAsleep) {
        AsleepScreen(
            date = ui.date,
            onWake = onWake,
            onShiftDate = onShiftDate,
            onOpenDate = { showDate = true },
        )
        if (showDate) DatePick(ui.date, onSetDate) { showDate = false }
        if (needSleepStart) SleepStartDialog(onConfirm = onWakeWithStart, onSkip = onSkipSleepStart)
        return
    }
    var showEdit by remember { mutableStateOf(false) }
    var sel by remember { mutableStateOf<IntRange?>(null) }
    var showSheet by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Paper),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 16.dp, top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            var dragAcc by remember { mutableStateOf(0f) }
            Text(
                ui.date.toCn(),
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ink,
                modifier = Modifier
                    .weight(1f)
                    .pointerInput(ui.date) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (dragAcc > 64) onShiftDate(-1)
                                else if (dragAcc < -64) onShiftDate(1)
                                dragAcc = 0f
                            },
                            onHorizontalDrag = { _, dx -> dragAcc += dx },
                        )
                    }
                    .clickable { showDate = true },
            )
            if (!ui.isToday) {
                Text(
                    "今天",
                    color = Ink2,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Card)
                        .clickable { onSetDate(LocalDate.now(zone)) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
            if (ui.isToday) {
                Text(
                    "我睡了",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Sleep)
                        .clickable(onClick = onSleep)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }
        Row(
            Modifier.padding(start = 20.dp, end = 16.dp, top = 6.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (ui.lastWake != null) {
                Text(
                    "醒了 ${ui.lastWake.toHm(zone)}",
                    color = Ink2,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { showEdit = true },
                )
            }
            Text("144 块", color = Ink2, fontSize = 12.sp)
        }

        if (ui.toast != null) {
            Row(
                Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Accent)
                    .clickable(onClick = onDismissToast)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFC5C8DC)))
                Text(ui.toast, color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(start = 8.dp))
            }
            Spacer(Modifier.height(10.dp))
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatCard(ui.elapsed.toString(), "已过", highlight = true, modifier = Modifier.weight(1f))
            StatCard(ui.remaining.toString(), "剩余", modifier = Modifier.weight(1f))
            StatCard(ui.slept.toString(), "已睡", modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))

        BlockGrid(
            blocks = ui.blocks,
            types = ui.types,
            elapsed = ui.elapsed,
            nowIndex = ui.nowIndex,
            nowTime = ui.nowTime,
            selection = sel,
            onSelect = { range ->
                sel = range
                showSheet = true
            },
            modifier = Modifier.weight(1f),
        )
    }

    if (showSheet && sel != null) {
        TypePickerSheet(
            count = sel!!.count(),
            types = ui.types.filter { !it.archived },
            onPick = { id ->
                onSetRange(sel!!.first, sel!!.last, id)
                sel = null
                showSheet = false
            },
            onClear = {
                onSetRange(sel!!.first, sel!!.last, null)
                sel = null
                showSheet = false
            },
            onCancel = {
                sel = null
                showSheet = false
            },
            onAddType = onAddType,
        )
    }

    if (showDate) DatePick(ui.date, onSetDate) { showDate = false }

    if (showEdit && ui.lastSleep != null && ui.lastWake != null) {
        EditTimesDialog(
            start = ui.lastSleep.atZone(zone).toLocalTime(),
            end = ui.lastWake.atZone(zone).toLocalTime(),
            onDismiss = { showEdit = false },
            onConfirm = { a, b ->
                onEditLast(a, b)
                showEdit = false
            },
        )
    }

    if (needSleepStart) SleepStartDialog(onConfirm = onWakeWithStart, onSkip = onSkipSleepStart)
}

@Composable
private fun StatCard(value: String, label: String, highlight: Boolean = false, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (highlight) HighlightWash else Card)
            .border(1.dp, if (highlight) HighlightLine else Line, RoundedCornerShape(14.dp))
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink)
        Text(label, fontSize = 11.sp, color = Ink3)
    }
}

@Composable
fun BlockGrid(
    blocks: Map<Int, String>,
    types: List<BlockType>,
    elapsed: Int,
    nowIndex: Int?,
    nowTime: LocalTime?,
    selection: IntRange?,
    onSelect: (IntRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = remember(types) { types.associate { it.id to Color(it.color.toInt()) } }
    val scroll = rememberScrollState()
    val density = LocalDensity.current
    val rowH = 44.dp
    val labelW = 42.dp
    val gap = 5.dp

    var gridWidthPx by remember { mutableStateOf(0f) }
    val layout = BlockHitLayout(
        rowHeightPx = with(density) { rowH.toPx() },
        labelWidthPx = with(density) { labelW.toPx() },
        gapPx = with(density) { gap.toPx() },
        gridWidthPx = gridWidthPx,
    )
    val layoutState = rememberUpdatedState(layout)
    val onSelectState = rememberUpdatedState(onSelect)

    LaunchedEffect(nowIndex) {
        if (nowIndex != null) {
            val hour = nowIndex / 6
            val y = with(density) { (rowH * hour).roundToPx() - 80 }
            scroll.animateScrollTo(y.coerceAtLeast(0))
        }
    }

    var dragStart by remember { mutableStateOf<Int?>(null) }
    var dragEnd by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(selection) {
        if (selection == null) {
            dragStart = null
            dragEnd = null
        }
    }
    val liveSel = if (dragStart != null && dragEnd != null) {
        val a = minOf(dragStart!!, dragEnd!!)
        val b = maxOf(dragStart!!, dragEnd!!)
        a..b
    } else selection

    Column(
        modifier
            .fillMaxSize()
            .padding(start = 10.dp, end = 14.dp)
            .onSizeChanged { gridWidthPx = it.width.toFloat() }
            .pointerInput(Unit) {
                detectTapGestures { off ->
                    val idx = BlockHitTest.indexAt(off.x, off.y, scroll.value, layoutState.value)
                    idx?.let { onSelectState.value(it..it) }
                }
            }
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { off ->
                        val i = BlockHitTest.indexAt(off.x, off.y, scroll.value, layoutState.value)
                        dragStart = i
                        dragEnd = i
                    },
                    onDragEnd = {
                        val a = dragStart
                        val b = dragEnd
                        dragStart = null
                        dragEnd = null
                        if (a != null && b != null) onSelectState.value(minOf(a, b)..maxOf(a, b))
                    },
                    onDragCancel = { dragStart = null; dragEnd = null },
                    onDrag = { change, _ ->
                        dragEnd = BlockHitTest.indexAt(
                            change.position.x,
                            change.position.y,
                            scroll.value,
                            layoutState.value,
                        )
                    },
                )
            }
            .verticalScroll(scroll),
    ) {
        repeat(24) { hour ->
            HourRow(
                hour = hour,
                blocks = blocks,
                colors = colors,
                elapsed = elapsed,
                nowTime = nowTime,
                selection = liveSel,
                rowHeight = rowH,
                labelWidth = labelW,
            )
        }
    }
}

@Composable
private fun HourRow(
    hour: Int,
    blocks: Map<Int, String>,
    colors: Map<String, Color>,
    elapsed: Int,
    nowTime: LocalTime?,
    selection: IntRange?,
    rowHeight: androidx.compose.ui.unit.Dp,
    labelWidth: androidx.compose.ui.unit.Dp,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(rowHeight)
            .padding(bottom = 6.dp),
    ) {
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "%02d:00".format(hour),
                fontSize = 11.sp,
                color = Ink3,
                modifier = Modifier.size(width = labelWidth, height = rowHeight),
                maxLines = 1,
            )
            Row(
                Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                repeat(6) { col ->
                    val idx = hour * 6 + col
                    val typeId = blocks[idx]
                    val future = idx >= elapsed
                    val color = when {
                        typeId != null -> colors[typeId] ?: EmptyPast
                        future -> EmptyFuture
                        else -> EmptyPast
                    }
                    val selected = selection?.contains(idx) == true
                    Box(
                        Modifier
                            .weight(1f)
                            .height(rowHeight - 6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (typeId != null && future) color.copy(alpha = 0.48f) else color)
                            .then(
                                if (typeId == null && future) {
                                    Modifier.border(1.dp, Line, RoundedCornerShape(8.dp))
                                } else Modifier
                            )
                            .then(
                                if (selected) Modifier.border(2.dp, Ink, RoundedCornerShape(8.dp))
                                else Modifier
                            ),
                    )
                }
            }
        }
        if (nowTime != null && nowTime.hour == hour) {
            val yFrac = (nowTime.minute + nowTime.second / 60f) / 60f
            val density = LocalDensity.current
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(start = labelWidth)
                    .align(Alignment.TopStart)
                    .offset {
                        IntOffset(0, (with(density) { (rowHeight - 6.dp).toPx() } * yFrac).roundToInt())
                    },
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(Accent),
                )
                Text(
                    "现在 ${"%02d:%02d".format(nowTime.hour, nowTime.minute)}",
                    color = Color.White,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(y = (-9).dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Accent)
                        .padding(horizontal = 6.dp, vertical = 1.dp),
                )
            }
        }
    }
}

@Composable
fun AsleepScreen(
    date: LocalDate,
    onWake: () -> Unit,
    onShiftDate: (Int) -> Unit = {},
    onOpenDate: () -> Unit = {},
) {
    var dragAcc by remember { mutableStateOf(0f) }
    Column(
        Modifier
            .fillMaxSize()
            .background(Paper)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("时间块", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        }
        Spacer(Modifier.weight(1f))
        Text(
            "${date.year}年${date.monthValue}月${date.dayOfMonth}日 · 星期${listOf("一","二","三","四","五","六","日")[date.dayOfWeek.value - 1]}",
            color = Ink2,
            fontSize = 15.sp,
            modifier = Modifier
                .pointerInput(date) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (dragAcc > 64) onShiftDate(-1)
                            else if (dragAcc < -64) onShiftDate(1)
                            dragAcc = 0f
                        },
                        onHorizontalDrag = { _, dx -> dragAcc += dx },
                    )
                }
                .clickable(onClick = onOpenDate),
        )
        Text("还在睡", fontSize = 30.sp, fontWeight = FontWeight.SemiBold, color = Ink, modifier = Modifier.padding(top = 10.dp))
        Spacer(Modifier.height(36.dp))
        Box(
            Modifier
                .size(188.dp)
                .clip(CircleShape)
                .background(Wake)
                .clickable(onClick = onWake),
            contentAlignment = Alignment.Center,
        ) {
            Text("我醒了", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.weight(1.2f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePick(date: LocalDate, onSetDate: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let {
                    onSetDate(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                }
                onDismiss()
            }) { Text("确定") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    ) { DatePicker(state = state) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SleepStartDialog(onConfirm: (LocalTime) -> Unit, onSkip: () -> Unit) {
    val state = rememberTimePickerState(is24Hour = true)
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onSkip,
        title = { Text("没有入睡记录") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TimePicker(state = state)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }) { Text("按此时入睡") }
        },
        dismissButton = { TextButton(onClick = onSkip) { Text("直接醒来") } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTimesDialog(
    start: LocalTime,
    end: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime, LocalTime) -> Unit,
) {
    var which by remember { mutableStateOf(0) }
    val s = rememberTimePickerState(initialHour = start.hour, initialMinute = start.minute, is24Hour = true)
    val e = rememberTimePickerState(initialHour = end.hour, initialMinute = end.minute, is24Hour = true)
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (which == 0) "入睡时间" else "醒来时间") },
        text = { TimePicker(state = if (which == 0) s else e) },
        confirmButton = {
            Row {
                if (which == 0) {
                    TextButton(onClick = { which = 1 }) { Text("下一步") }
                } else {
                    TextButton(onClick = {
                        onConfirm(LocalTime.of(s.hour, s.minute), LocalTime.of(e.hour, e.minute))
                    }) { Text("保存") }
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

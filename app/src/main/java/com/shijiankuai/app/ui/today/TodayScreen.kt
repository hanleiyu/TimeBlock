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
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
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
import com.shijiankuai.app.ui.DatePick
import com.shijiankuai.app.ui.TodayUi
import com.shijiankuai.app.ui.chrome.DateNavBar
import com.shijiankuai.app.ui.chrome.OverlayFab
import com.shijiankuai.app.ui.chrome.OverlayFabSpec
import com.shijiankuai.app.ui.theme.Accent
import com.shijiankuai.app.ui.theme.GooseYellow
import com.shijiankuai.app.ui.theme.EmptyFuture
import com.shijiankuai.app.ui.theme.EmptyPast
import com.shijiankuai.app.ui.theme.Ink
import com.shijiankuai.app.ui.theme.Ink2
import com.shijiankuai.app.ui.theme.Ink3
import com.shijiankuai.app.ui.theme.Line
import com.shijiankuai.app.ui.theme.OnAccent
import com.shijiankuai.app.ui.theme.Paper
import com.shijiankuai.app.ui.theme.Wake
import com.shijiankuai.app.ui.toCn
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    ui: TodayUi,
    zone: java.time.ZoneId,
    onWake: () -> Unit,
    onWakeWithStart: (LocalTime) -> Unit,
    onSkipSleepStart: () -> Unit,
    onSetRange: (Int, Int, String?) -> Unit,
    onShiftDate: (Int) -> Unit,
    onSetDate: (LocalDate) -> Unit,
    onAddType: (String, Long) -> Unit,
    onAddHealthRecord: () -> Unit,
    needSleepStart: Boolean,
    onDismissToast: () -> Unit,
    gridScroll: ScrollState,
    takeNowSnap: (LocalDate, Boolean) -> Boolean,
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
    var sel by remember { mutableStateOf<IntRange?>(null) }
    var showSheet by remember { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Paper),
    ) {
        Column(Modifier.fillMaxSize()) {
            DateNavBar(
                label = ui.date.toCn(),
                onPrev = { onShiftDate(-1) },
                onNext = { onShiftDate(1) },
                onLabelClick = { showDate = true },
                modifier = Modifier.padding(horizontal = 16.dp),
            )

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
                    Box(Modifier.size(8.dp).clip(CircleShape).background(OnAccent.copy(alpha = 0.7f)))
                    Text(ui.toast, color = OnAccent, fontSize = 13.sp, modifier = Modifier.padding(start = 8.dp))
                }
                Spacer(Modifier.height(10.dp))
            }

            TodayProgressBar(
                blocks = ui.blocks,
                types = ui.types,
                elapsed = ui.elapsed,
                modifier = Modifier.padding(
                    start = TodayGridSpec.progressStartInsetDp.dp,
                    end = TodayGridSpec.progressEndInsetDp.dp,
                    bottom = 10.dp,
                ),
            )

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
                scroll = gridScroll,
                takeNowSnap = { takeNowSnap(ui.date, ui.isToday) },
                modifier = Modifier.weight(1f),
            )
        }
        OverlayFab(
            onClick = onAddHealthRecord,
            contentDescription = OverlayFabSpec.healthRecordDescription,
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

    if (needSleepStart) SleepStartDialog(onConfirm = onWakeWithStart, onSkip = onSkipSleepStart)
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
    scroll: ScrollState,
    takeNowSnap: () -> Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = remember(types) { types.associate { it.id to Color(it.color.toInt()) } }
    val density = LocalDensity.current
    val rowH = TodayGridSpec.rowHeightDp.dp
    val labelW = TodayGridSpec.labelWidthDp.dp
    val gap = TodayGridSpec.gapDp.dp

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
        val idx = nowIndex ?: return@LaunchedEffect
        if (!takeNowSnap()) return@LaunchedEffect
        val y = TodayScroll.nowOffsetPx(
            nowIndex = idx,
            rowHeightPx = with(density) { rowH.toPx() },
            topPadPx = 80f,
        )
        scroll.scrollTo(y)
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
            .padding(start = TodayGridSpec.startPadDp.dp, end = TodayGridSpec.endPadDp.dp)
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
        Spacer(Modifier.height(OverlayFabSpec.scrollClearanceDp.dp))
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
                horizontalArrangement = Arrangement.spacedBy(TodayGridSpec.gapDp.dp),
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
                        .background(GooseYellow),
                )
                Text(
                    "现在 ${"%02d:%02d".format(nowTime.hour, nowTime.minute)}",
                    color = Color.Black,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(y = (-9).dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(GooseYellow)
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
        Text("还在睡", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Ink, modifier = Modifier.padding(top = 10.dp))
        Spacer(Modifier.height(36.dp))
        Box(
            Modifier
                .size(188.dp)
                .clip(CircleShape)
                .background(Wake)
                .clickable(onClick = onWake),
            contentAlignment = Alignment.Center,
        ) {
            Text("我醒了", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.weight(1.2f))
    }
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

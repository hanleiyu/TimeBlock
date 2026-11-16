package com.shijiankuai.app.ui.life

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shijiankuai.app.domain.LifeCalendar
import com.shijiankuai.app.domain.LifeCell
import com.shijiankuai.app.domain.LifeEvent
import com.shijiankuai.app.domain.LifeMonth
import com.shijiankuai.app.domain.LifeOrigin
import com.shijiankuai.app.domain.LifeTone
import com.shijiankuai.app.ui.chrome.OverlayFab
import com.shijiankuai.app.ui.chrome.OverlayFabSpec
import com.shijiankuai.app.ui.theme.Accent
import com.shijiankuai.app.ui.theme.Card
import com.shijiankuai.app.ui.theme.EmptyFuture
import com.shijiankuai.app.ui.theme.EmptyPast
import com.shijiankuai.app.ui.theme.Ink
import com.shijiankuai.app.ui.theme.Ink2
import com.shijiankuai.app.ui.theme.Ink3
import com.shijiankuai.app.ui.theme.Line
import com.shijiankuai.app.ui.theme.OnAccent
import com.shijiankuai.app.ui.theme.OnSelected
import com.shijiankuai.app.ui.theme.Paper
import com.shijiankuai.app.ui.theme.PaperDeep
import com.shijiankuai.app.ui.theme.SelectedFill
import com.shijiankuai.app.ui.theme.Sheet
import java.time.LocalDate
import kotlinx.coroutines.launch

@Composable
fun LifeScreen(
    origin: LifeOrigin?,
    today: LocalDate,
    events: List<LifeEvent>,
    onSetOrigin: (LifeOrigin) -> Unit,
    onSave: (LifeEvent) -> Unit,
    onDelete: (String) -> Unit,
) {
    if (origin == null) {
        OriginSetup(onSetOrigin = onSetOrigin)
        return
    }
    LifeGridPane(
        origin = origin,
        today = LifeMonth(today.year, today.monthValue),
        events = events,
        onSetOrigin = onSetOrigin,
        onSave = onSave,
        onDelete = onDelete,
    )
}

@Composable
private fun OriginSetup(
    initial: LifeOrigin? = null,
    onSetOrigin: (LifeOrigin) -> Unit,
    onCancel: (() -> Unit)? = null,
) {
    var yearText by remember { mutableStateOf(initial?.year?.toString().orEmpty()) }
    var month by remember { mutableStateOf(initial?.month) }
    val year = yearText.toIntOrNull()
    val ready = year != null && year in 1..9999 && month in 1..12
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text("选择出生年月", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ink)
        Spacer(Modifier.height(6.dp))
        Text("只记到月，不会猜测具体日期", fontSize = 13.sp, color = Ink2)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = yearText,
            onValueChange = { yearText = it.filter { ch -> ch.isDigit() }.take(4) },
            placeholder = { Text("年份") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            (1..12).forEach { m ->
                val on = month == m
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (on) SelectedFill else Color.Transparent)
                        .then(if (on) Modifier else Modifier.border(1.dp, Line, RoundedCornerShape(8.dp)))
                        .clickable { month = m }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("$m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (on) OnSelected else Ink2)
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (onCancel != null) {
                SheetBtn("取消", false, Modifier.weight(1f), onClick = onCancel)
            }
            SheetBtn("开始", true, Modifier.weight(1f), enabled = ready) {
                if (year != null && month != null) onSetOrigin(LifeOrigin(year, month!!))
            }
        }
    }
}

@Composable
private fun LifeGridPane(
    origin: LifeOrigin,
    today: LifeMonth,
    events: List<LifeEvent>,
    onSetOrigin: (LifeOrigin) -> Unit,
    onSave: (LifeEvent) -> Unit,
    onDelete: (String) -> Unit,
) {
    val cells = remember(origin, today, events) { LifeCalendar.cells(origin, today, events) }
    val segs = remember(origin, today, events) { LifeCalendar.progress(origin, today, events) }
    val rows = remember(cells) { cells.chunked(12) }
    var form by remember { mutableStateOf<LifeEvent?>(null) }
    var details by remember { mutableStateOf<LifeTap.OpenDetails?>(null) }
    var editingOrigin by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val labelW = LifeGridSpec.ageGutterDp.dp
    val trailingW = LifeGridSpec.trailingGutterDp.dp
    val gap = LifeGridSpec.gapDp.dp
    val rowH = LifeGridSpec.rowHeightDp.dp
    var gridWidthPx by remember { mutableStateOf(0f) }
    var viewportH by remember { mutableIntStateOf(0) }
    val layout = LifeHitLayout(
        rowHeightPx = with(density) { rowH.toPx() },
        labelWidthPx = with(density) { labelW.toPx() },
        trailingWidthPx = with(density) { trailingW.toPx() },
        gapPx = with(density) { gap.toPx() },
        gridWidthPx = gridWidthPx,
        contentTopPx = with(density) { LifeGridSpec.contentTopDp.dp.toPx() },
    )
    val layoutState = rememberUpdatedState(layout)
    val originState = rememberUpdatedState(origin)
    val eventsState = rememberUpdatedState(events)
    var dragStart by remember { mutableStateOf<LifeMonth?>(null) }
    var dragEnd by remember { mutableStateOf<LifeMonth?>(null) }
    val liveSel = if (dragStart != null && dragEnd != null) {
        LifeCalendar.normalizeRange(dragStart!!, dragEnd!!)
    } else null

    Box(Modifier.fillMaxSize()) {
            Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = LifeGridSpec.pagePaddingDp.dp),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(LifeCalendar.elapsedCaption(origin, today), color = Ink2, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Text("展示 90 年", color = Ink2, fontSize = 12.sp)
            }
            Spacer(Modifier.height(8.dp))
            LifeProgressBar(segs)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.size(width = labelW, height = 16.dp))
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(gap)) {
                    (1..12).forEach { n ->
                        Text(
                            "$n",
                            color = Ink3,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                        )
                    }
                }
                Spacer(Modifier.size(width = trailingW, height = 16.dp))
            }
            Spacer(Modifier.height(4.dp))
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .onSizeChanged {
                        gridWidthPx = it.width.toFloat()
                        viewportH = it.height
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { off ->
                            val month = LifeHitTest.monthAt(
                                off.x,
                                off.y,
                                scroll.value,
                                originState.value,
                                layoutState.value,
                            )
                            when (val action = LifeTap.fromHit(month, eventsState.value)) {
                                is LifeTap.OpenForm -> form = LifeTap.blankEvent(action.start, action.end)
                                is LifeTap.OpenDetails -> details = action
                                null -> {}
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { off ->
                                val m = LifeHitTest.monthAt(off.x, off.y, scroll.value, originState.value, layoutState.value)
                                dragStart = m
                                dragEnd = m
                            },
                            onDragEnd = {
                                val a = dragStart
                                val b = dragEnd
                                dragStart = null
                                dragEnd = null
                                if (a != null && b != null) {
                                    val range = LifeCalendar.normalizeRange(a, b)
                                    form = LifeTap.blankEvent(range.first, range.second)
                                }
                            },
                            onDragCancel = { dragStart = null; dragEnd = null },
                            onDrag = { change, _ ->
                                dragEnd = LifeHitTest.monthAt(
                                    change.position.x,
                                    change.position.y,
                                    scroll.value,
                                    originState.value,
                                    layoutState.value,
                                ) ?: dragEnd
                                val y = change.position.y
                                val edge = 48f
                                if (y < edge) scope.launch { scroll.scrollBy(-36f) }
                                else if (viewportH > 0 && y > viewportH - edge) scope.launch { scroll.scrollBy(36f) }
                            },
                        )
                    }
                    .verticalScroll(scroll)
                    .padding(top = LifeGridSpec.contentTopDp.dp, bottom = OverlayFabSpec.scrollClearanceDp.dp),
            ) {
                Column {
                    rows.forEach { row ->
                        LifeYearRow(
                            row = row,
                            selected = liveSel,
                            labelWidth = labelW,
                            trailingWidth = trailingW,
                            gap = gap,
                            rowHeight = rowH,
                            onBirth = { editingOrigin = true },
                        )
                    }
                }
            }
        }
        OverlayFab(
            onClick = {
                val month = when {
                    today < origin.asMonth() -> origin.asMonth()
                    LifeCalendar.index(origin, today) == null ->
                        LifeCalendar.monthAt(origin, LifeCalendar.MONTHS_IN_HORIZON - 1)
                    else -> today
                }
                form = LifeTap.blankEvent(month, month)
            },
            contentDescription = OverlayFabSpec.lifeEventDescription,
        )
    }

    form?.let { draft ->
        LifeEventSheet(
            origin = origin,
            draft = draft,
            onSave = {
                onSave(it)
                form = null
            },
            onCancel = { form = null },
        )
    }
    details?.let { sheet ->
        LifeDetailsSheet(
            events = sheet.covering,
            onAdd = {
                details = null
                form = LifeTap.blankEvent(sheet.month, sheet.month)
            },
            onEdit = { event ->
                details = null
                form = event
            },
            onDelete = { id ->
                onDelete(id)
                val rest = sheet.covering.filter { it.id != id }
                details = if (rest.isEmpty()) null else sheet.copy(covering = rest)
            },
            onCancel = { details = null },
        )
    }
    if (editingOrigin) {
        Box(Modifier.fillMaxSize().background(Paper)) {
            OriginSetup(
                initial = origin,
                onSetOrigin = {
                    onSetOrigin(it)
                    editingOrigin = false
                },
                onCancel = { editingOrigin = false },
            )
        }
    }
}

@Composable
private fun LifeProgressBar(segs: List<com.shijiankuai.app.domain.LifeProgressSeg>) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(
                start = LifeGridSpec.progressStartInsetDp.dp,
                end = LifeGridSpec.progressEndInsetDp.dp,
            )
            .height(8.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(EmptyFuture),
    ) {
        segs.forEach { seg ->
            Box(
                Modifier
                    .weight(seg.length.toFloat())
                    .fillMaxHeight()
                    .background(cellFill(seg.color, seg.tone)),
            )
        }
    }
}

@Composable
private fun LifeYearRow(
    row: List<LifeCell>,
    selected: Pair<LifeMonth, LifeMonth>?,
    labelWidth: androidx.compose.ui.unit.Dp,
    trailingWidth: androidx.compose.ui.unit.Dp,
    gap: androidx.compose.ui.unit.Dp,
    rowHeight: androidx.compose.ui.unit.Dp,
    onBirth: () -> Unit,
) {
    val age = row.firstOrNull { it.ageLabel != null }?.ageLabel.orEmpty()
    val title = row.lastOrNull { it.eventLabel != null }?.eventLabel
    Row(
        Modifier
            .fillMaxWidth()
            .height(rowHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            age,
            color = Ink3,
            fontSize = 11.sp,
            textAlign = TextAlign.End,
            overflow = TextOverflow.Visible,
            modifier = Modifier.size(width = labelWidth, height = rowHeight),
            maxLines = 1,
        )
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(gap), verticalAlignment = Alignment.CenterVertically) {
            row.forEach { cell ->
                val inSel = selected?.let { cell.month in it.first..it.second } == true
                Box(
                    Modifier.weight(1f).height(rowHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        Modifier
                            .size(LifeGridSpec.circleDp.dp)
                            .clip(CircleShape)
                            .background(cellFill(cell.display?.color, cell.tone))
                            .then(
                                when {
                                    inSel -> Modifier.border(2.dp, Ink, CircleShape)
                                    cell.tone == LifeTone.CURRENT -> Modifier.border(1.5.dp, Ink2, CircleShape)
                                    cell.display == null &&
                                        (cell.tone == LifeTone.FUTURE || cell.tone == LifeTone.PREBIRTH) ->
                                        Modifier.border(1.dp, Line, CircleShape)
                                    else -> Modifier
                                },
                            )
                    )
                    if (cell.birth) {
                        Text(
                            "出生",
                            color = Ink2,
                            fontSize = 9.sp,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = (-11).dp)
                                .clickable(onClick = onBirth),
                            maxLines = 1,
                        )
                    }
                }
            }
        }
        Text(
            title.orEmpty(),
            color = Ink2,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.size(width = trailingWidth, height = rowHeight),
        )
    }
}

@Composable
private fun cellFill(color: Long?, tone: LifeTone): Color = when {
    color != null -> Color(color.toInt())
    tone == LifeTone.PAST || tone == LifeTone.CURRENT -> EmptyPast
    else -> EmptyFuture
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LifeEventSheet(
    origin: LifeOrigin,
    draft: LifeEvent,
    onSave: (LifeEvent) -> Unit,
    onCancel: () -> Unit,
) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var start by remember { mutableStateOf(draft.start) }
    var end by remember { mutableStateOf(draft.end) }
    var single by remember { mutableStateOf(LifeFormSpec.initialSingle(draft.start, draft.end)) }
    var rememberedEnd by remember { mutableStateOf(if (draft.start == draft.end) null else draft.end) }
    var title by remember { mutableStateOf(draft.title) }
    var note by remember { mutableStateOf(draft.note) }
    var color by remember { mutableStateOf(draft.color) }
    val years = (minOf(origin.year, start.year, end.year)..maxOf(origin.year + LifeCalendar.HORIZON_YEARS - 1, start.year, end.year))
    ModalBottomSheet(onDismissRequest = onCancel, sheetState = state, containerColor = Sheet) {
        Column(Modifier.padding(horizontal = 18.dp).padding(bottom = 28.dp)) {
            Text("记录事件", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Ink)
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(LifeFormSpec.singleSwitchLabel, fontSize = 14.sp, color = Ink, modifier = Modifier.weight(1f))
                Switch(
                    checked = single,
                    onCheckedChange = { on ->
                        single = on
                        if (on) {
                            val collapsed = LifeFormSpec.collapseToStart(start, end, rememberedEnd)
                            end = collapsed.first
                            rememberedEnd = collapsed.second
                        } else {
                            end = LifeFormSpec.expandFromStart(start, rememberedEnd)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Accent,
                    ),
                )
            }
            Spacer(Modifier.height(12.dp))
            if (single) {
                YearMonthField("年月", start, years, Modifier.fillMaxWidth()) {
                    start = it
                    end = LifeFormSpec.endAfterStartChange(true, it, end)
                }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    YearMonthField("开始年月", start, years, Modifier.weight(1f)) {
                        start = it
                        end = LifeFormSpec.endAfterStartChange(false, it, end)
                    }
                    YearMonthField("结束年月", end, years, Modifier.weight(1f)) { end = it }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("标题 必填", fontSize = 12.sp, color = Accent)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("小学") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            Text("事件内容", fontSize = 12.sp, color = Ink2)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                placeholder = { Text("把这件事写下来") },
                modifier = Modifier.fillMaxWidth().height(96.dp),
            )
            Spacer(Modifier.height(14.dp))
            Text("颜色", fontSize = 12.sp, color = Ink2)
            Spacer(Modifier.height(8.dp))
            LifeCalendar.colors.chunked(8).forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                ) {
                    row.forEach { c ->
                        val on = c == color
                        Box(
                            Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(c.toInt()))
                                .then(if (on) Modifier.border(2.dp, Ink, CircleShape) else Modifier)
                                .clickable { color = c },
                        )
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SheetBtn("取消", false, Modifier.weight(1f), onClick = onCancel)
                SheetBtn("保存", true, Modifier.weight(1f), enabled = title.isNotBlank()) {
                    val range = LifeFormSpec.saveBounds(single, start, end)
                    onSave(
                        draft.copy(
                            title = title.trim(),
                            note = note.trim(),
                            start = range.first,
                            end = range.second,
                            color = color,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun YearMonthField(
    label: String,
    value: LifeMonth,
    years: IntRange,
    modifier: Modifier,
    onChange: (LifeMonth) -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    Column(modifier) {
        Text(label, fontSize = 12.sp, color = Ink2)
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PaperDeep)
                .clickable { open = true }
                .padding(horizontal = 12.dp, vertical = 12.dp),
        ) {
            Text("${value.year}年${value.month}月", color = Ink, fontSize = 15.sp)
        }
    }
    if (open) {
        YearMonthPicker(value = value, years = years, onPick = {
            onChange(it)
            open = false
        }, onDismiss = { open = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YearMonthPicker(
    value: LifeMonth,
    years: IntRange,
    onPick: (LifeMonth) -> Unit,
    onDismiss: () -> Unit,
) {
    var year by remember { mutableIntStateOf(value.year.coerceIn(years.first, years.last)) }
    var month by remember { mutableIntStateOf(value.month) }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Sheet) {
        Column(Modifier.padding(horizontal = 18.dp).padding(bottom = 24.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("‹", fontSize = 22.sp, color = Ink2, modifier = Modifier.clickable { year = (year - 1).coerceAtLeast(years.first) }.padding(8.dp))
                Text("${year}年", fontSize = 16.sp, color = Ink, modifier = Modifier.weight(1f), maxLines = 1)
                Text("›", fontSize = 22.sp, color = Ink2, modifier = Modifier.clickable { year = (year + 1).coerceAtMost(years.last) }.padding(8.dp))
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (1..12).forEach { m ->
                    val on = month == m
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (on) SelectedFill else Color.Transparent)
                            .then(if (on) Modifier else Modifier.border(1.dp, Line, RoundedCornerShape(8.dp)))
                            .clickable { month = m }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("$m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (on) OnSelected else Ink2)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            SheetBtn("确定", true, Modifier.fillMaxWidth()) { onPick(LifeMonth(year, month)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LifeDetailsSheet(
    events: List<LifeEvent>,
    onAdd: () -> Unit,
    onEdit: (LifeEvent) -> Unit,
    onDelete: (String) -> Unit,
    onCancel: () -> Unit,
) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onCancel, sheetState = state, containerColor = Sheet) {
        Column(Modifier.padding(horizontal = 18.dp).padding(bottom = 28.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("事件", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Ink, modifier = Modifier.weight(1f))
                Text(
                    "添加",
                    color = Accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(onClick = onAdd).padding(horizontal = 4.dp, vertical = 6.dp),
                )
            }
            Spacer(Modifier.height(12.dp))
            events.forEach { event ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Card)
                        .padding(14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(12.dp).clip(CircleShape).background(Color(event.color.toInt())))
                        Text(event.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ink, modifier = Modifier.padding(start = 8.dp).weight(1f))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(LifeCalendar.rangeLabel(event.start, event.end), fontSize = 13.sp, color = Ink2)
                    if (event.note.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(event.note, fontSize = 14.sp, color = Ink)
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SheetBtn("编辑", false, Modifier.weight(1f)) { onEdit(event) }
                        SheetBtn("删除", true, Modifier.weight(1f)) { onDelete(event.id) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetBtn(label: String, primary: Boolean, modifier: Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (primary) Accent else PaperDeep)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = if (primary) OnAccent else Ink2, fontWeight = FontWeight.Bold)
    }
}

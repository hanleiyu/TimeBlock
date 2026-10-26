package com.shijiankuai.app.ui.todo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shijiankuai.app.domain.PeriodKind
import com.shijiankuai.app.domain.StatsCalc
import com.shijiankuai.app.domain.StatsNav
import com.shijiankuai.app.domain.TodoCard
import com.shijiankuai.app.domain.TodoCategory
import com.shijiankuai.app.domain.TodoItem
import com.shijiankuai.app.domain.TodoList
import com.shijiankuai.app.domain.TodoMonth
import com.shijiankuai.app.domain.TodoRange
import com.shijiankuai.app.ui.theme.Accent
import com.shijiankuai.app.ui.theme.Card
import com.shijiankuai.app.ui.theme.Ink
import com.shijiankuai.app.ui.theme.Ink2
import com.shijiankuai.app.ui.theme.Ink3
import com.shijiankuai.app.ui.theme.Line
import com.shijiankuai.app.ui.DatePick
import com.shijiankuai.app.ui.theme.Paper
import com.shijiankuai.app.ui.theme.PaperDeep
import com.shijiankuai.app.ui.theme.Sheet
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun TodoScreen(
    date: LocalDate,
    today: LocalDate,
    kind: PeriodKind,
    items: List<TodoItem>,
    onShiftDate: (Int) -> Unit,
    onSetDate: (LocalDate) -> Unit,
    onJumpToday: () -> Unit,
    onKind: (PeriodKind) -> Unit,
    onAdd: (TodoCategory, String) -> Unit,
    onToggle: (String, Boolean) -> Unit,
    onDefer: (String) -> Unit,
    onEdit: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    onReorder: (TodoCategory, List<String>) -> Unit,
) {
    val viewKind = TodoMonth.coerceKind(kind)
    val cards = remember(items) { TodoList.cards(items) }
    val (start, end) = remember(viewKind, date) { StatsCalc.periodBounds(viewKind, date) }
    var adding by remember { mutableStateOf(false) }
    var acting by remember { mutableStateOf<TodoItem?>(null) }
    var editing by remember { mutableStateOf<TodoItem?>(null) }
    var showDate by remember { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Paper),
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "清单",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink,
                    modifier = Modifier.weight(1f),
                )
                if (!StatsNav.contains(viewKind, date, today)) {
                    Text(
                        "今天",
                        color = Ink2,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Card)
                            .clickable {
                                onJumpToday()
                                showDate = false
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }
            PeriodTabs(kind = viewKind, onKind = onKind)
            if (viewKind != PeriodKind.MONTH) {
                DateBar(
                    kind = viewKind,
                    start = start,
                    end = end,
                    onShiftDate = onShiftDate,
                    onOpenCalendar = { showDate = true },
                )
            }
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp, bottom = if (viewKind == PeriodKind.DAY) 88.dp else 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                when (viewKind) {
                    PeriodKind.DAY -> cards.forEach { card ->
                        CategoryCard(
                            card = card,
                            onToggle = onToggle,
                            onOpen = { item -> if (!item.done) acting = item },
                            onReorder = { onReorder(card.category, it) },
                        )
                    }
                    PeriodKind.WEEK, PeriodKind.MONTH -> TodoMonthPanel(
                        date = date,
                        today = today,
                        items = items,
                        onSetDate = onSetDate,
                    )
                    PeriodKind.YEAR -> YearSummary(items, start.year)
                }
            }
        }
        if (viewKind == PeriodKind.DAY) {
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 20.dp)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Accent)
                    .clickable { adding = true },
                contentAlignment = Alignment.Center,
            ) {
                Text("+", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Medium)
            }
        }
    }

    if (adding) {
        AddTodoSheet(
            onAdd = { cat, title ->
                onAdd(cat, title)
                adding = false
            },
            onCancel = { adding = false },
        )
    }
    acting?.let { item ->
        TodoActionSheet(
            title = item.title,
            onDefer = {
                onDefer(item.id)
                acting = null
            },
            onEdit = {
                editing = item
                acting = null
            },
            onDelete = {
                onDelete(item.id)
                acting = null
            },
            onCancel = { acting = null },
        )
    }
    editing?.let { item ->
        EditTodoSheet(
            initial = item.title,
            onSave = { text ->
                onEdit(item.id, text)
                editing = null
            },
            onCancel = { editing = null },
        )
    }
    if (showDate && viewKind != PeriodKind.MONTH) {
        DatePick(date, onSetDate) { showDate = false }
    }
}

@Composable
private fun PeriodTabs(kind: PeriodKind, onKind: (PeriodKind) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PaperDeep)
            .padding(3.dp),
    ) {
        TodoMonth.CHECKLIST_KINDS.forEach { k ->
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
                Text(
                    label,
                    fontSize = 13.sp,
                    color = if (on) Ink else Ink2,
                    fontWeight = if (on) FontWeight.SemiBold else FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun DateBar(
    kind: PeriodKind,
    start: LocalDate,
    end: LocalDate,
    onShiftDate: (Int) -> Unit,
    onOpenCalendar: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text("‹", fontSize = 22.sp, color = Ink2, modifier = Modifier.clickable { onShiftDate(-1) })
        Text(
            TodoRange.periodLabel(kind, start, end),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Ink,
            modifier = Modifier.clickable(onClick = onOpenCalendar),
        )
        Text("›", fontSize = 22.sp, color = Ink2, modifier = Modifier.clickable { onShiftDate(1) })
    }
}

@Composable
private fun YearSummary(items: List<TodoItem>, year: Int) {
    val counts = remember(items) { TodoRange.completedCounts(items) }
    val stacks = remember(items, year) { TodoRange.yearStacks(items, year) }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TodoRange.SUMMARY_ORDER.forEach { cat ->
            Column(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(categoryWash(cat))
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(categoryIcon(cat), contentDescription = cat.label, tint = categoryTint(cat), modifier = Modifier.size(18.dp))
                Text(cat.label, fontSize = 11.sp, color = Ink2, modifier = Modifier.padding(top = 6.dp))
                Text(
                    "${counts[cat] ?: 0}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = categoryTint(cat),
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
    YearStackChart(stacks)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
    ) {
        TodoRange.SUMMARY_ORDER.forEach { cat ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(categoryTint(cat)))
                Text(cat.label, fontSize = 11.sp, color = Ink2, modifier = Modifier.padding(start = 4.dp))
            }
        }
    }
}

@Composable
private fun YearStackChart(stacks: List<TodoRange.MonthStack>) {
    val max = stacks.maxOfOrNull { it.total }?.coerceAtLeast(1) ?: 1
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .height(168.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        stacks.forEach { stack ->
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Canvas(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    val inset = 3.dp.toPx()
                    val barW = (size.width - inset * 2).coerceAtLeast(2f)
                    var top = size.height
                    TodoRange.SUMMARY_ORDER.forEach { cat ->
                        val n = stack.counts[cat] ?: 0
                        if (n <= 0) return@forEach
                        val h = size.height * n / max.toFloat()
                        top -= h
                        drawRect(
                            color = categoryTint(cat),
                            topLeft = Offset(inset, top),
                            size = Size(barW, h),
                        )
                    }
                }
                Text("${stack.month}", fontSize = 10.sp, color = Ink2, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
private fun CategoryCard(
    card: TodoCard,
    onToggle: (String, Boolean) -> Unit,
    onOpen: (TodoItem) -> Unit,
    onReorder: (List<String>) -> Unit,
) {
    val tint = categoryTint(card.category)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Card)
            .border(1.dp, Line, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                categoryIcon(card.category),
                contentDescription = card.category.label,
                tint = tint,
                modifier = Modifier.size(18.dp),
            )
            Text(
                card.category.label,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ink,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Spacer(Modifier.height(6.dp))
        ReorderList(card.items, onToggle, onOpen, onReorder)
    }
}

@Composable
private fun ReorderList(
    items: List<TodoItem>,
    onToggle: (String, Boolean) -> Unit,
    onOpen: (TodoItem) -> Unit,
    onReorder: (List<String>) -> Unit,
) {
    var order by remember(items.map { it.id to it.done to it.title }) {
        mutableStateOf(items.map { it.id })
    }
    val byId = items.associateBy { it.id }
    val rowPx = with(LocalDensity.current) { 48.dp.toPx() }
    var dragging by remember { mutableStateOf<String?>(null) }
    var dragY by remember { mutableFloatStateOf(0f) }

    Column {
        order.forEach { id ->
            val item = byId[id] ?: return@forEach
            val lift = if (dragging == id) dragY else 0f
            TodoRow(
                item = item,
                lift = lift,
                onToggle = { onToggle(item.id, !item.done) },
                onOpen = { onOpen(item) },
                onDrag = { dy ->
                    if (dragging == null) dragging = id
                    dragY += dy
                    val from = order.indexOf(id)
                    val to = (from + (dragY / rowPx).roundToInt()).coerceIn(0, order.lastIndex)
                    if (to != from) {
                        order = order.toMutableList().also {
                            val moved = it.removeAt(from)
                            it.add(to, moved)
                        }
                        dragY -= (to - from) * rowPx
                    }
                },
                onDragEnd = {
                    dragging = null
                    dragY = 0f
                    onReorder(order)
                },
            )
        }
    }
}

@Composable
private fun TodoRow(
    item: TodoItem,
    lift: Float,
    onToggle: () -> Unit,
    onOpen: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
) {
    val look = TodoList.doneLook(item.done)
    Row(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .offset { IntOffset(0, lift.roundToInt()) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(22.dp)
                .clip(CircleShape)
                .then(
                    if (item.done) Modifier.background(Accent)
                    else Modifier.border(1.5.dp, Ink3, CircleShape),
                )
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center,
        ) {
            if (item.done) {
                Icon(Icons.Outlined.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
        Text(
            item.title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = if (look.faded) Ink2.copy(alpha = 0.62f) else Ink,
            textDecoration = if (look.strike) TextDecoration.LineThrough else TextDecoration.None,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
                .clickable(enabled = !item.done, onClick = onOpen),
        )
        Icon(
            Icons.Outlined.DragHandle,
            contentDescription = null,
            tint = Ink3,
            modifier = Modifier
                .size(22.dp)
                .pointerInput(item.id) {
                    detectDragGestures(
                        onDragEnd = onDragEnd,
                        onDragCancel = onDragEnd,
                        onDrag = { change, amount ->
                            change.consume()
                            onDrag(amount.y)
                        },
                    )
                },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTodoSheet(onAdd: (TodoCategory, String) -> Unit, onCancel: () -> Unit) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(TodoCategory.WORK) }
    ModalBottomSheet(
        onDismissRequest = onCancel,
        sheetState = state,
        containerColor = Sheet,
    ) {
        Column(Modifier.padding(horizontal = 18.dp).padding(bottom = 28.dp)) {
            Text("新任务", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Ink, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("输入任务内容") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(14.dp))
            CategoryChips(selected = category, onSelect = { category = it })
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SheetBtn("取消", false, Modifier.weight(1f), onClick = onCancel)
                SheetBtn("添加", true, Modifier.weight(1f), enabled = title.isNotBlank()) {
                    if (title.isNotBlank()) onAdd(category, title.trim())
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTodoSheet(initial: String, onSave: (String) -> Unit, onCancel: () -> Unit) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf(initial) }
    ModalBottomSheet(
        onDismissRequest = onCancel,
        sheetState = state,
        containerColor = Sheet,
    ) {
        Column(Modifier.padding(horizontal = 18.dp).padding(bottom = 28.dp)) {
            Text("编辑", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Ink, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SheetBtn("取消", false, Modifier.weight(1f), onClick = onCancel)
                SheetBtn("保存", true, Modifier.weight(1f), enabled = title.isNotBlank()) {
                    if (title.isNotBlank()) onSave(title.trim())
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoActionSheet(
    title: String,
    onDefer: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onCancel,
        sheetState = state,
        containerColor = Sheet,
    ) {
        Column(Modifier.padding(horizontal = 18.dp).padding(bottom = 28.dp)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Ink)
            Spacer(Modifier.height(14.dp))
            ActionRow("延到明天", Ink, onDefer)
            ActionRow("编辑", Ink, onEdit)
            ActionRow("删除", Color(0xFFB4554A), onDelete)
            Spacer(Modifier.height(8.dp))
            SheetBtn("取消", false, Modifier.fillMaxWidth(), onClick = onCancel)
        }
    }
}

@Composable
private fun ActionRow(label: String, color: Color, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = color)
    }
}

@Composable
private fun CategoryChips(selected: TodoCategory, onSelect: (TodoCategory) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TodoCategory.entries.forEach { cat ->
            val on = cat == selected
            Box(
                Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (on) Accent else Color.Transparent)
                    .then(if (on) Modifier else Modifier.border(1.dp, Line, RoundedCornerShape(999.dp)))
                    .clickable { onSelect(cat) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(cat.label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (on) Color.White else Ink2)
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
        Text(label, color = if (primary) Color.White else Ink2, fontWeight = FontWeight.SemiBold)
    }
}


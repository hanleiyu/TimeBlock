package com.shijiankuai.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.shijiankuai.app.domain.TodoCalendar
import com.shijiankuai.app.ui.theme.Accent
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.temporal.WeekFields
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePick(
    date: LocalDate,
    onSetDate: (LocalDate) -> Unit,
    markedDays: Set<LocalDate> = emptySet(),
    onDismiss: () -> Unit,
) {
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
    ) {
        Box {
            DatePicker(state = state)
            if (markedDays.isNotEmpty() && state.displayMode == DisplayMode.Picker) {
                CompletionDotsOverlay(
                    markedDays = markedDays,
                    displayedMonthMillis = state.displayedMonthMillis,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(Cell * Rows),
                )
            }
        }
    }
}

private val Cell = 48.dp
private const val Rows = 6
private const val Cols = 7

@Composable
private fun CompletionDotsOverlay(
    markedDays: Set<LocalDate>,
    displayedMonthMillis: Long,
    modifier: Modifier = Modifier,
) {
    val month = YearMonth.from(
        Instant.ofEpochMilli(displayedMonthMillis).atZone(ZoneOffset.UTC).toLocalDate(),
    )
    val firstDow = WeekFields.of(Locale.getDefault()).firstDayOfWeek
    BoxWithConstraints(modifier) {
        val extra = maxWidth - Cell * Cols
        val gap = extra / (Cols + 1)
        val rowH = maxHeight / Rows
        markedDays.forEach { day ->
            if (YearMonth.from(day) != month) return@forEach
            if (!TodoCalendar.hasCompletionDot(day, markedDays)) return@forEach
            val index = TodoCalendar.cellIndex(day, firstDow)
            val col = index % Cols
            val row = index / Cols
            Box(
                Modifier
                    .offset(x = gap + Cell * col + gap * col, y = rowH * row)
                    .size(Cell, rowH),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Box(
                    Modifier
                        .padding(bottom = 5.dp)
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(Accent),
                )
            }
        }
    }
}

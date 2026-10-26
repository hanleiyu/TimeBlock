package com.shijiankuai.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.shijiankuai.app.domain.TodoCalendar
import com.shijiankuai.app.ui.theme.Accent
import com.shijiankuai.app.ui.theme.Card
import com.shijiankuai.app.ui.theme.Ink
import com.shijiankuai.app.ui.theme.Ink2
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun DatePick(
    date: LocalDate,
    onSetDate: (LocalDate) -> Unit,
    markedDays: Set<LocalDate> = emptySet(),
    applyOnSelect: Boolean = false,
    onDismiss: () -> Unit,
) {
    var month by remember(date) { mutableStateOf(YearMonth.from(date)) }
    var pending by remember(date) { mutableStateOf(date) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(Card)
                .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "‹",
                    fontSize = 20.sp,
                    color = Ink2,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { month = month.minusMonths(1) },
                )
                Text(
                    TodoCalendar.monthLabel(month),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink,
                )
                Text(
                    "›",
                    fontSize = 20.sp,
                    color = Ink2,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { month = month.plusMonths(1) },
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
            ) {
                TodoCalendar.WEEKDAY_LABELS.forEach { label ->
                    Box(
                        Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Ink2)
                    }
                }
            }
            TodoCalendar.monthCells(month).chunked(7).forEach { week ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp),
                ) {
                    week.forEach { cell ->
                        Box(
                            Modifier
                                .weight(1f)
                                .height(48.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (cell != null) {
                                val selected = cell == pending
                                val marked = TodoCalendar.hasCompletionDot(cell, markedDays)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable {
                                        pending = cell
                                        if (applyOnSelect) {
                                            onSetDate(cell)
                                            onDismiss()
                                        }
                                    },
                                ) {
                                    Box(
                                        Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (selected) Accent else Color.Transparent),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            "${cell.dayOfMonth}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (selected) Color.White else Ink,
                                        )
                                    }
                                    Box(
                                        Modifier
                                            .padding(top = 1.dp)
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    !marked -> Color.Transparent
                                                    selected -> Color.White.copy(alpha = 0.85f)
                                                    else -> Accent
                                                },
                                            ),
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (!applyOnSelect) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text(
                        "取消",
                        color = Accent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable(onClick = onDismiss)
                            .padding(8.dp),
                    )
                    Text(
                        "确定",
                        color = Accent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable {
                                onSetDate(pending)
                                onDismiss()
                            }
                            .padding(8.dp),
                    )
                }
            }
        }
    }
}

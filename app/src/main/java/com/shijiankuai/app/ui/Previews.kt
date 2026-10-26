package com.shijiankuai.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shijiankuai.app.domain.BLOCKS_PER_DAY
import com.shijiankuai.app.domain.Presets
import com.shijiankuai.app.domain.SLEEP_TYPE_ID
import com.shijiankuai.app.ui.settings.SettingsScreen
import com.shijiankuai.app.ui.stats.StatsScreen
import com.shijiankuai.app.ui.theme.TimeBlockTheme
import com.shijiankuai.app.ui.today.AsleepScreen
import com.shijiankuai.app.ui.today.TodayScreen
import com.shijiankuai.app.domain.PeriodKind
import com.shijiankuai.app.domain.PeriodStats
import com.shijiankuai.app.domain.StatsCalc
import com.shijiankuai.app.domain.BlockId
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PreviewAsleep() {
    TimeBlockTheme {
        AsleepScreen(date = LocalDate.of(2026, 10, 21), onWake = {})
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PreviewAwake() {
    val day = LocalDate.of(2026, 10, 21)
    val blocks = (4..42).associateWith { SLEEP_TYPE_ID }
    TimeBlockTheme {
        TodayScreen(
            ui = TodayUi(
                date = day,
                isToday = true,
                isAsleep = false,
                blocks = blocks,
                types = Presets.types,
                elapsed = 43,
                remaining = 101,
                slept = 39,
                nowIndex = 43,
                nowTime = java.time.LocalTime.of(7, 12),
                lastSleep = null,
                lastWake = null,
                toast = "00:42 入睡 · 已记 39 块睡觉",
            ),
            zone = ZoneId.of("Asia/Shanghai"),
            onSleep = {},
            onWake = {},
            onWakeWithStart = {},
            onSkipSleepStart = {},
            onSetRange = { _, _, _ -> },
            onShiftDate = {},
            onSetDate = {},
            onEditLast = { _, _ -> },
            onAddType = { _, _ -> },
            needSleepStart = false,
            onDismissToast = {},
            gridScroll = rememberScrollState(),
            takeNowSnap = { _, _ -> false },
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PreviewStats() {
    val day = LocalDate.of(2026, 10, 21)
    val zone = ZoneId.of("Asia/Shanghai")
    val now = LocalDateTime.of(2026, 10, 21, 14, 20).atZone(zone).toInstant()
    val blocks = (0..42).associate { BlockId(day.toEpochDay(), it) to SLEEP_TYPE_ID }
    val stats = StatsCalc.aggregate(PeriodKind.DAY, day, now, zone, blocks, Presets.types)
    TimeBlockTheme {
        StatsScreen(
            kind = PeriodKind.DAY,
            anchor = day,
            today = day,
            stats = stats,
            onKind = {},
            onShift = {},
            onSetAnchor = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PreviewSettings() {
    TimeBlockTheme {
        SettingsScreen(
            types = Presets.types,
            onOpenTypes = {},
            exportCsv = { "" },
            exportBackup = { "" },
            restore = { false },
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, locale = "zh-rCN")
@Composable
fun PreviewDatePickMarked() {
    val day = LocalDate.of(2026, 10, 26)
    val marked = setOf(3, 10, 12, 15, 18, 21, 26).map { LocalDate.of(2026, 10, it) }.toSet()
    TimeBlockTheme {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0x66000000)),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = com.shijiankuai.app.ui.theme.Card,
                shadowElevation = 8.dp,
            ) {
                Column {
                    DatePickCalendar(date = day, markedDays = marked)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = {}) { Text("取消") }
                        TextButton(onClick = {}) { Text("确定") }
                    }
                }
            }
        }
    }
}

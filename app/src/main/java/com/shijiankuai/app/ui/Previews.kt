package com.shijiankuai.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shijiankuai.app.domain.Presets
import com.shijiankuai.app.domain.SLEEP_TYPE_ID
import com.shijiankuai.app.ui.settings.SettingsScreen
import com.shijiankuai.app.ui.stats.StatsScreen
import com.shijiankuai.app.ui.theme.TimeBlockTheme
import com.shijiankuai.app.ui.today.AsleepScreen
import com.shijiankuai.app.ui.today.TodayScreen
import com.shijiankuai.app.domain.PeriodKind
import com.shijiankuai.app.domain.StatsCalc
import com.shijiankuai.app.domain.BlockId
import com.shijiankuai.app.domain.TodoCategory
import com.shijiankuai.app.domain.TodoItem
import com.shijiankuai.app.ui.todo.TodoMonthPanel
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
fun PreviewTodoMonth() {
    val day = LocalDate.of(2026, 10, 26)
    val items = listOf(
        TodoItem("1", day.toEpochDay(), TodoCategory.WORK, "回邮件", true, 0),
        TodoItem("2", day.toEpochDay(), TodoCategory.LIFE, "倒垃圾", true, 1),
        TodoItem("3", day.toEpochDay(), TodoCategory.STUDY, "阅读30分钟", true, 2),
        TodoItem("4", LocalDate.of(2026, 10, 19).toEpochDay(), TodoCategory.WORK, "周报", true, 0),
        TodoItem("5", LocalDate.of(2026, 10, 19).toEpochDay(), TodoCategory.HEALTH, "散步", true, 1),
        TodoItem("6", LocalDate.of(2026, 10, 10).toEpochDay(), TodoCategory.LIFE, "买菜", true, 0),
    )
    TimeBlockTheme {
        Column(Modifier.padding(16.dp)) {
            TodoMonthPanel(date = day, today = day, items = items, onSetDate = {})
        }
    }
}

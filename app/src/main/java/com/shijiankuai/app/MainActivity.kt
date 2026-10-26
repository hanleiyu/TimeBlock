package com.shijiankuai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shijiankuai.app.ui.AppViewModel
import com.shijiankuai.app.ui.settings.SettingsScreen
import com.shijiankuai.app.ui.settings.TypesScreen
import com.shijiankuai.app.ui.stats.StatsScreen
import com.shijiankuai.app.ui.todo.TodoScreen
import com.shijiankuai.app.ui.theme.Ink
import com.shijiankuai.app.ui.theme.Ink3
import com.shijiankuai.app.ui.theme.Paper
import com.shijiankuai.app.ui.theme.TabBar
import com.shijiankuai.app.ui.theme.TimeBlockTheme
import com.shijiankuai.app.ui.today.TodayScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repo = (application as TimeBlockApp).container.repo
        setContent {
            TimeBlockTheme {
                val vm: AppViewModel = viewModel(factory = AppViewModel.factory(repo))
                AppRoot(vm)
            }
        }
    }
}

@Composable
private fun AppRoot(vm: AppViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    var typesOpen by remember { mutableStateOf(false) }
    val today by vm.today.collectAsStateWithLifecycle()
    val types by vm.types.collectAsStateWithLifecycle()
    val stats by vm.stats.collectAsStateWithLifecycle()
    val kind by vm.statsKind.collectAsStateWithLifecycle()
    val anchor by vm.statsAnchor.collectAsStateWithLifecycle()
    val needStart by vm.needSleepStart.collectAsStateWithLifecycle()
    val showAllTime by vm.showAllTime.collectAsStateWithLifecycle()
    val todoDate by vm.todoDate.collectAsStateWithLifecycle()
    val todoKind by vm.todoKind.collectAsStateWithLifecycle()
    val todos by vm.todos.collectAsStateWithLifecycle()
    val todoCompletedDays by vm.todoCompletedDays.collectAsStateWithLifecycle()
    val zone = remember { java.time.ZoneId.systemDefault() }
    val todayScroll = rememberScrollState()

    Column(
        Modifier
            .fillMaxSize()
            .background(Paper)
            .statusBarsPadding(),
    ) {
        when {
            typesOpen -> TypesScreen(
                types = types,
                onBack = { typesOpen = false },
                onUpdate = vm::updateType,
                onReorder = vm::reorder,
                onAdd = vm::addType,
            )
            else -> Box(Modifier.weight(1f)) {
                Box(
                    Modifier.then(
                        if (tab == 0) Modifier.fillMaxSize() else Modifier.size(0.dp),
                    ),
                ) {
                    TodayScreen(
                        ui = today,
                        zone = zone,
                        onSleep = vm::markSleep,
                        onWake = vm::markWake,
                        onWakeWithStart = vm::markWakeWithStart,
                        onSkipSleepStart = vm::skipSleepStart,
                        onSetRange = vm::setRange,
                        onShiftDate = vm::shiftDate,
                        onSetDate = vm::setDate,
                        onEditLast = vm::editLast,
                        onAddType = vm::addType,
                        needSleepStart = needStart,
                        onDismissToast = vm::dismissToast,
                        gridScroll = todayScroll,
                        takeNowSnap = vm::takeNowSnap,
                    )
                }
                if (tab == 1) {
                    TodoScreen(
                        date = todoDate,
                        kind = todoKind,
                        items = todos,
                        completedDays = todoCompletedDays,
                        onShiftDate = vm::shiftTodoDate,
                        onSetDate = vm::setTodoDate,
                        onJumpToday = vm::jumpTodoToday,
                        onKind = vm::setTodoKind,
                        onAdd = vm::addTodo,
                        onToggle = vm::setTodoDone,
                        onDefer = vm::deferTodo,
                        onEdit = vm::updateTodoTitle,
                        onDelete = vm::deleteTodo,
                        onReorder = vm::reorderTodos,
                    )
                } else if (tab == 2) {
                    StatsScreen(
                        kind = kind,
                        anchor = anchor,
                        today = java.time.LocalDate.now(zone),
                        stats = stats,
                        onKind = vm::setStatsKind,
                        onShift = vm::shiftStats,
                        onSetAnchor = vm::setStatsAnchor,
                    )
                } else if (tab == 3) {
                    SettingsScreen(
                        types = types,
                        onOpenTypes = { typesOpen = true },
                        exportCsv = { vm.exportCsv() },
                        exportBackup = { vm.exportBackup() },
                        restore = { vm.restore(it) },
                        showAllTime = showAllTime,
                        onShowAllTime = vm::setShowAllTime,
                    )
                }
            }
        }
        if (!typesOpen) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(TabBar)
                    .navigationBarsPadding()
                    .height(64.dp)
                    .padding(bottom = 10.dp),
            ) {
                TabItem("今天", tab == 0, Icons.Outlined.CalendarMonth, Modifier.weight(1f)) { tab = 0 }
                TabItem("清单", tab == 1, Icons.Outlined.CheckBox, Modifier.weight(1f)) { tab = 1 }
                TabItem("统计", tab == 2, Icons.Outlined.PieChart, Modifier.weight(1f)) { tab = 2 }
                TabItem("设置", tab == 3, Icons.Outlined.Settings, Modifier.weight(1f)) { tab = 3 }
            }
        }
    }
}

@Composable
private fun TabItem(
    label: String,
    on: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier,
    click: () -> Unit,
) {
    Column(
        modifier.clickable(onClick = click),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(8.dp))
        Icon(icon, contentDescription = label, tint = if (on) Ink else Ink3)
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = if (on) Ink else Ink3)
    }
}

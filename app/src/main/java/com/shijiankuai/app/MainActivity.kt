package com.shijiankuai.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shijiankuai.app.data.DailyBackupScheduler
import com.shijiankuai.app.domain.DailyBackup
import com.shijiankuai.app.exercise.data.ExerciseRepository
import com.shijiankuai.app.exercise.ui.ExerciseVmFactory
import com.shijiankuai.app.exercise.ui.HealthPage
import com.shijiankuai.app.exercise.ui.HealthScreen
import com.shijiankuai.app.exercise.ui.editor.WorkoutEditorScreen
import com.shijiankuai.app.exercise.ui.goals.HealthGoalsScreen
import com.shijiankuai.app.exercise.ui.metrics.MetricDetailScreen
import com.shijiankuai.app.exercise.ui.plan.PlanScreen
import com.shijiankuai.app.ui.AppViewModel
import com.shijiankuai.app.ui.settings.SettingsScreen
import com.shijiankuai.app.ui.settings.TypesScreen
import com.shijiankuai.app.ui.stats.StatsScreen
import com.shijiankuai.app.ui.theme.Ink3
import com.shijiankuai.app.ui.theme.OnSelected
import com.shijiankuai.app.ui.theme.Paper
import com.shijiankuai.app.ui.theme.SelectedFill
import com.shijiankuai.app.ui.theme.TabBar
import com.shijiankuai.app.ui.theme.TimeBlockTheme
import com.shijiankuai.app.ui.today.TodayScreen
import com.shijiankuai.app.ui.todo.TodoScreen
import java.time.LocalDate

private sealed class Overlay {
    data object None : Overlay()
    data object Types : Overlay()
    data object HealthGoals : Overlay()
    data object Plan : Overlay()
    data class Editor(val workoutId: Long?, val epochDay: Long?) : Overlay()
    data class Metric(val id: Long) : Overlay()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestLegacyStorageIfNeeded()
        val container = (application as TimeBlockApp).container
        setContent {
            val vm: AppViewModel = viewModel(factory = AppViewModel.factory(container.repo, container.backup))
            val dark by vm.darkTheme.collectAsStateWithLifecycle()
            TimeBlockTheme(dark = dark) {
                AppRoot(vm, container.exercise)
            }
        }
    }

    private fun requestLegacyStorageIfNeeded() {
        if (Build.VERSION.SDK_INT >= 29) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            17,
        )
    }
}

@Composable
private fun AppRoot(vm: AppViewModel, exercise: ExerciseRepository) {
    var tab by remember { mutableIntStateOf(0) }
    var overlay by remember { mutableStateOf<Overlay>(Overlay.None) }
    var healthPage by remember { mutableStateOf(HealthPage.WEEK) }
    val today by vm.today.collectAsStateWithLifecycle()
    val types by vm.types.collectAsStateWithLifecycle()
    val stats by vm.stats.collectAsStateWithLifecycle()
    val kind by vm.statsKind.collectAsStateWithLifecycle()
    val anchor by vm.statsAnchor.collectAsStateWithLifecycle()
    val needStart by vm.needSleepStart.collectAsStateWithLifecycle()
    val showAllTime by vm.showAllTime.collectAsStateWithLifecycle()
    val autoBackup by vm.autoBackup.collectAsStateWithLifecycle()
    val lastAutoBackupAt by vm.lastAutoBackupAt.collectAsStateWithLifecycle()
    val darkTheme by vm.darkTheme.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    val todoDate by vm.todoDate.collectAsStateWithLifecycle()
    val todoKind by vm.todoKind.collectAsStateWithLifecycle()
    val todos by vm.todos.collectAsStateWithLifecycle()
    val zone = remember { java.time.ZoneId.systemDefault() }
    val todayScroll = rememberScrollState()

    Column(
        Modifier
            .fillMaxSize()
            .background(Paper)
            .statusBarsPadding(),
    ) {
        when (val layer = overlay) {
            Overlay.Types -> TypesScreen(
                types = types,
                onBack = { overlay = Overlay.None },
                onUpdate = vm::updateType,
                onReorder = vm::reorder,
                onAdd = vm::addType,
            )
            Overlay.HealthGoals -> HealthGoalsScreen(
                vm = viewModel(factory = ExerciseVmFactory(exercise)),
                onBack = { overlay = Overlay.None },
            )
            Overlay.Plan -> PlanScreen(
                vm = viewModel(factory = ExerciseVmFactory(exercise)),
                onBack = { overlay = Overlay.None },
            )
            is Overlay.Editor -> WorkoutEditorScreen(
                vm = viewModel(
                    key = "workout-${layer.workoutId}-${layer.epochDay}",
                    factory = ExerciseVmFactory(exercise, layer.workoutId),
                ),
                initialDate = layer.epochDay?.let(LocalDate::ofEpochDay),
                onDone = { overlay = Overlay.None },
            )
            is Overlay.Metric -> MetricDetailScreen(
                vm = viewModel(
                    key = "metric-${layer.id}",
                    factory = ExerciseVmFactory(exercise, layer.id),
                ),
                onBack = { overlay = Overlay.None },
            )
            Overlay.None -> Box(Modifier.weight(1f)) {
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
                    StatsScreen(
                        kind = kind,
                        anchor = anchor,
                        today = java.time.LocalDate.now(zone),
                        stats = stats,
                        onKind = vm::setStatsKind,
                        onShift = vm::shiftStats,
                        onSetAnchor = vm::setStatsAnchor,
                    )
                } else if (tab == 2) {
                    TodoScreen(
                        date = todoDate,
                        today = java.time.LocalDate.now(zone),
                        kind = todoKind,
                        items = todos,
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
                } else if (tab == 3) {
                    HealthScreen(
                        repo = exercise,
                        page = healthPage,
                        onPage = { healthPage = it },
                        onOpenPlan = { overlay = Overlay.Plan },
                        onOpenWorkout = { overlay = Overlay.Editor(it, null) },
                        onAddWorkout = { overlay = Overlay.Editor(null, it.toEpochDay()) },
                        onOpenMetric = { overlay = Overlay.Metric(it) },
                    )
                } else if (tab == 4) {
                    SettingsScreen(
                        types = types,
                        onOpenTypes = { overlay = Overlay.Types },
                        onOpenHealthGoals = { overlay = Overlay.HealthGoals },
                        exportBackup = { vm.exportBackup() },
                        restore = { vm.restore(it) },
                        showAllTime = showAllTime,
                        onShowAllTime = vm::setShowAllTime,
                        autoBackup = autoBackup,
                        lastBackupLabel = DailyBackup.lastBackupLabel(lastAutoBackupAt, zone),
                        onAutoBackup = { on ->
                            vm.setAutoBackup(on)
                            DailyBackupScheduler.setEnabled(ctx, on)
                        },
                        darkTheme = darkTheme,
                        onDarkTheme = vm::setDarkTheme,
                    )
                }
            }
        }
        if (overlay == Overlay.None) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(TabBar)
                    .navigationBarsPadding()
                    .heightIn(min = 72.dp)
                    .padding(top = 8.dp, bottom = 12.dp),
            ) {
                TabItem("今天", tab == 0, Icons.Outlined.CalendarMonth, Modifier.weight(1f)) { tab = 0 }
                TabItem("统计", tab == 1, Icons.Outlined.PieChart, Modifier.weight(1f)) { tab = 1 }
                TabItem("清单", tab == 2, Icons.Outlined.CheckBox, Modifier.weight(1f)) { tab = 2 }
                TabItem("健康", tab == 3, Icons.Outlined.FavoriteBorder, Modifier.weight(1f)) { tab = 3 }
                TabItem("设置", tab == 4, Icons.Outlined.Settings, Modifier.weight(1f)) { tab = 4 }
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
        modifier
            .padding(horizontal = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (on) SelectedFill else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = click,
            )
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = label, tint = if (on) OnSelected else Ink3)
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (on) OnSelected else Ink3)
    }
}

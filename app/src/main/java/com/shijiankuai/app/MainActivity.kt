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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarViewMonth
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.shijiankuai.app.domain.StatsNav
import com.shijiankuai.app.exercise.data.ExerciseRepository
import com.shijiankuai.app.exercise.ui.ExerciseVmFactory
import com.shijiankuai.app.exercise.ui.HealthPane
import com.shijiankuai.app.exercise.ui.editor.WorkoutEditorScreen
import com.shijiankuai.app.exercise.ui.goals.HealthGoalsScreen
import com.shijiankuai.app.exercise.ui.metrics.MetricDetailScreen
import com.shijiankuai.app.exercise.ui.metrics.MetricsScreen
import com.shijiankuai.app.exercise.ui.plan.PlanScreen
import com.shijiankuai.app.ui.AppViewModel
import com.shijiankuai.app.ui.BottomTab
import com.shijiankuai.app.ui.PeriodFeature
import com.shijiankuai.app.ui.AppIa
import com.shijiankuai.app.ui.chrome.PeriodScaffold
import com.shijiankuai.app.exercise.ui.components.ScreenHeader
import com.shijiankuai.app.ui.settings.SettingsScreen
import com.shijiankuai.app.ui.settings.TypesScreen
import com.shijiankuai.app.ui.stats.StatsScreen
import com.shijiankuai.app.ui.theme.Ink
import com.shijiankuai.app.ui.theme.Ink3
import com.shijiankuai.app.ui.theme.Paper
import com.shijiankuai.app.ui.theme.TabBar
import com.shijiankuai.app.ui.theme.TimeBlockTheme
import com.shijiankuai.app.ui.life.LifeScreen
import com.shijiankuai.app.ui.today.TodayScreen
import com.shijiankuai.app.ui.todo.TodoScreen
import java.time.LocalDate

private sealed class Overlay {
    data object None : Overlay()
    data object Types : Overlay()
    data object HealthGoals : Overlay()
    data object Plan : Overlay()
    data object Metrics : Overlay()
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
    var tab by remember { mutableStateOf(BottomTab.DAY) }
    var overlay by remember { mutableStateOf<Overlay>(Overlay.None) }
    var dayFeature by remember { mutableStateOf(PeriodFeature.BLOCKS) }
    var weekFeature by remember { mutableStateOf(PeriodFeature.STATS) }
    var monthFeature by remember { mutableStateOf(PeriodFeature.STATS) }
    var yearFeature by remember { mutableStateOf(PeriodFeature.STATS) }
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
    val todayDate = LocalDate.now(zone)
    val feature = when (tab) {
        BottomTab.DAY -> dayFeature
        BottomTab.WEEK -> weekFeature
        BottomTab.MONTH -> monthFeature
        BottomTab.YEAR -> yearFeature
        BottomTab.SETTINGS -> PeriodFeature.STATS
    }
    val periodKind = AppIa.periodKind(tab)
    val lifeOrigin by vm.lifeOrigin.collectAsStateWithLifecycle()
    val lifeEvents by vm.lifeEvents.collectAsStateWithLifecycle()
    val showJumpToday = tab != BottomTab.SETTINGS &&
        feature != PeriodFeature.HEALTH &&
        feature != PeriodFeature.LIFE &&
        periodKind != null &&
        !StatsNav.contains(periodKind, anchor, todayDate)

    fun selectTab(next: BottomTab) {
        tab = next
        AppIa.periodKind(next)?.let { nextKind ->
            vm.setStatsKind(nextKind)
            vm.setTodoKind(nextKind)
        }
    }

    fun selectFeature(next: PeriodFeature) {
        when (tab) {
            BottomTab.DAY -> dayFeature = next
            BottomTab.WEEK -> weekFeature = next
            BottomTab.MONTH -> monthFeature = next
            BottomTab.YEAR -> yearFeature = next
            BottomTab.SETTINGS -> {}
        }
    }

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
            Overlay.Metrics -> Column(
                Modifier
                    .fillMaxSize()
                    .background(Paper)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            ) {
                ScreenHeader("指标", onBack = { overlay = Overlay.None })
                Spacer(Modifier.height(12.dp))
                Box(Modifier.weight(1f)) {
                    MetricsScreen(
                        vm = viewModel(factory = ExerciseVmFactory(exercise)),
                        onOpen = { overlay = Overlay.Metric(it) },
                    )
                }
            }
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
                onBack = { overlay = Overlay.Metrics },
            )
            Overlay.None -> Box(Modifier.weight(1f)) {
                when (tab) {
                    BottomTab.SETTINGS -> SettingsScreen(
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
                    else -> PeriodScaffold(
                        title = feature.label,
                        features = AppIa.features(tab),
                        selected = feature,
                        onSelect = ::selectFeature,
                        showJumpToday = showJumpToday,
                        onJumpToday = vm::jumpStatsToday,
                    ) {
                        if (tab == BottomTab.DAY) {
                            Box(
                                Modifier.then(
                                    if (feature == PeriodFeature.BLOCKS) Modifier.fillMaxSize()
                                    else Modifier.size(0.dp),
                                ),
                            ) {
                                TodayScreen(
                                    ui = today,
                                    zone = zone,
                                    onWake = vm::markWake,
                                    onWakeWithStart = vm::markWakeWithStart,
                                    onSkipSleepStart = vm::skipSleepStart,
                                    onSetRange = vm::setRange,
                                    onShiftDate = vm::shiftDate,
                                    onSetDate = vm::setDate,
                                    onAddType = vm::addType,
                                    onAddHealthRecord = {
                                        overlay = Overlay.Editor(null, today.date.toEpochDay())
                                    },
                                    needSleepStart = needStart,
                                    onDismissToast = vm::dismissToast,
                                    gridScroll = todayScroll,
                                    takeNowSnap = vm::takeNowSnap,
                                )
                            }
                        }
                        if (feature == PeriodFeature.STATS) {
                            StatsScreen(
                                kind = kind,
                                anchor = anchor,
                                today = todayDate,
                                stats = stats,
                                onShift = vm::shiftStats,
                                onSetAnchor = vm::setStatsAnchor,
                                embedded = true,
                            )
                        } else if (feature == PeriodFeature.TODO) {
                            TodoScreen(
                                date = todoDate,
                                today = todayDate,
                                kind = todoKind,
                                items = todos,
                                onShiftDate = vm::shiftTodoDate,
                                onSetDate = vm::setTodoDate,
                                onJumpToday = vm::jumpTodoToday,
                                onKind = vm::setTodoKind,
                                onAdd = vm::addTodo,
                                onToggle = vm::setTodoDone,
                                onDefer = vm::deferTodo,
                                onEdit = vm::updateTodo,
                                onDelete = vm::deleteTodo,
                                onReorder = vm::reorderTodos,
                                embedded = true,
                            )
                        } else if (feature == PeriodFeature.HEALTH) {
                            HealthPane(
                                repo = exercise,
                                week = tab == BottomTab.WEEK,
                                onOpenPlan = { overlay = Overlay.Plan },
                                onOpenWorkout = { overlay = Overlay.Editor(it, null) },
                                onOpenMetrics = { overlay = Overlay.Metrics },
                            )
                        } else if (feature == PeriodFeature.LIFE) {
                            LifeScreen(
                                origin = lifeOrigin,
                                today = todayDate,
                                events = lifeEvents,
                                onSetOrigin = vm::setLifeOrigin,
                                onSave = vm::saveLifeEvent,
                                onDelete = vm::deleteLifeEvent,
                            )
                        }
                    }
                }
            }
        }
        if (overlay == Overlay.None) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(TabBar)
                    .navigationBarsPadding()
                    .heightIn(min = 56.dp)
                    .padding(top = 6.dp, bottom = 8.dp),
            ) {
                BottomTab.entries.forEach { item ->
                    TabItem(
                        label = item.label,
                        on = tab == item,
                        modifier = Modifier.weight(1f),
                        icon = { tint -> NavIcon(item, tint) },
                        click = { selectTab(item) },
                    )
                }
            }
        }
    }
}

@Composable
private fun NavIcon(tab: BottomTab, tint: Color) {
    when (tab) {
        BottomTab.DAY -> Icon(Icons.Outlined.CalendarMonth, contentDescription = tab.label, tint = tint)
        BottomTab.WEEK -> Icon(Icons.Outlined.DateRange, contentDescription = tab.label, tint = tint)
        BottomTab.MONTH -> Icon(Icons.Outlined.CalendarViewMonth, contentDescription = tab.label, tint = tint)
        BottomTab.YEAR -> Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = tint)
            Text(
                "年",
                color = tint,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.offset(y = 2.dp),
            )
        }
        BottomTab.SETTINGS -> Icon(Icons.Outlined.Settings, contentDescription = tab.label, tint = tint)
    }
}

@Composable
private fun TabItem(
    label: String,
    on: Boolean,
    icon: @Composable (Color) -> Unit,
    modifier: Modifier,
    click: () -> Unit,
) {
    val tint = if (on) Ink else Ink3
    Column(
        modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = click,
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        icon(tint)
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = if (on) FontWeight.Bold else FontWeight.SemiBold,
            color = tint,
        )
    }
}

package com.shijiankuai.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shijiankuai.app.data.Repository
import com.shijiankuai.app.data.WakeOutcome
import com.shijiankuai.app.domain.BLOCK_MINUTES
import com.shijiankuai.app.domain.BLOCKS_PER_DAY
import com.shijiankuai.app.domain.BlockMath
import com.shijiankuai.app.domain.BlockType
import com.shijiankuai.app.domain.PeriodKind
import com.shijiankuai.app.domain.PeriodStats
import com.shijiankuai.app.domain.SLEEP_TYPE_ID
import com.shijiankuai.app.domain.SleepSession
import com.shijiankuai.app.domain.SleepSessions
import com.shijiankuai.app.domain.StatsCalc
import com.shijiankuai.app.domain.StatsNav
import com.shijiankuai.app.domain.TodoCategory
import com.shijiankuai.app.domain.TodoItem
import com.shijiankuai.app.domain.TodoMonth
import com.shijiankuai.app.domain.WakeFill
import com.shijiankuai.app.domain.WakeFillResult
import com.shijiankuai.app.ui.today.TodayScroll
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TodayUi(
    val date: LocalDate,
    val isToday: Boolean,
    val isAsleep: Boolean,
    val blocks: Map<Int, String>,
    val types: List<BlockType>,
    val elapsed: Int,
    val remaining: Int,
    val slept: Int,
    val nowIndex: Int?,
    val nowTime: LocalTime?,
    val lastSleep: Instant?,
    val lastWake: Instant?,
    val toast: String? = null,
)

class AppViewModel(private val repo: Repository) : ViewModel() {
    val viewedDate = MutableStateFlow(repo.clock.today())
    val todoDate = MutableStateFlow(repo.clock.today())
    val toast = MutableStateFlow<String?>(null)
    val needSleepStart = MutableStateFlow(false)

    private val tick = MutableStateFlow(0L)
    private var ticker: Job? = viewModelScope.launch {
        while (true) {
            tick.value = System.currentTimeMillis()
            delay(15_000)
        }
    }

    val types: StateFlow<List<BlockType>> = repo.types.stateIn(
        viewModelScope, SharingStarted.Eagerly, emptyList(),
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val today: StateFlow<TodayUi> = combine(
        viewedDate,
        repo.isAsleep,
        repo.types,
        viewedDate.flatMapLatest { repo.blocksOn(it) },
        repo.sessions,
    ) { date, asleep, typeList, blocks, sessions ->
        Triple(Triple(date, asleep, typeList), blocks, sessions)
    }.combine(tick) { core, _ -> core }
        .combine(toast) { core, t ->
            val (triple, blocks, sessions) = core
            val (date, asleep, typeList) = triple
            val now = repo.clock.now()
            val zone = repo.clock.zone()
            val todayDate = now.atZone(zone).toLocalDate()
            val elapsed = BlockMath.elapsedCount(date, now, zone)
            val nowTime = if (date == todayDate) now.atZone(zone).toLocalTime() else null
            val nowIndex = nowTime?.let(BlockMath::indexContaining)
            val mapped = sessions.map {
                SleepSession(
                    Instant.ofEpochMilli(it.startedAtMillis),
                    it.endedAtMillis?.let(Instant::ofEpochMilli),
                )
            }
            val closed = SleepSessions.closedForDate(date, mapped, zone)
            TodayUi(
                date = date,
                isToday = date == todayDate,
                isAsleep = asleep,
                blocks = blocks,
                types = typeList,
                elapsed = elapsed,
                remaining = BLOCKS_PER_DAY - elapsed,
                slept = blocks.values.count { it == SLEEP_TYPE_ID },
                nowIndex = nowIndex,
                nowTime = nowTime,
                lastSleep = closed?.startedAt,
                lastWake = closed?.endedAt,
                toast = t,
            )
        }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        TodayUi(
            date = repo.clock.today(),
            isToday = true,
            isAsleep = true,
            blocks = emptyMap(),
            types = emptyList(),
            elapsed = 0,
            remaining = BLOCKS_PER_DAY,
            slept = 0,
            nowIndex = null,
            nowTime = null,
            lastSleep = null,
            lastWake = null,
        ),
    )

    val statsKind = MutableStateFlow(PeriodKind.DAY)
    val statsAnchor = MutableStateFlow(repo.clock.today())
    val showAllTime: StateFlow<Boolean> = repo.showAllTime

    val todoKind = MutableStateFlow(PeriodKind.DAY)

    @OptIn(ExperimentalCoroutinesApi::class)
    val todos: StateFlow<List<TodoItem>> = combine(todoDate, todoKind) { date, kind -> date to kind }
        .flatMapLatest { (date, kind) ->
            val (start, end) = if (kind == PeriodKind.MONTH) {
                TodoMonth.queryBounds(date)
            } else {
                StatsCalc.periodBounds(kind, date)
            }
            repo.todosBetween(start, end)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val todoCompletedDays: StateFlow<Set<LocalDate>> = repo.todoCompletedDays()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    @OptIn(ExperimentalCoroutinesApi::class)
    val stats: StateFlow<PeriodStats?> = combine(statsKind, statsAnchor) { kind, anchor -> kind to anchor }
        .flatMapLatest { (kind, anchor) -> repo.statsFlow(kind, anchor, tick) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private var snappedNowOn: LocalDate? = null

    fun takeNowSnap(date: LocalDate, isToday: Boolean): Boolean {
        if (!TodayScroll.shouldSnapToNow(snappedNowOn, date, isToday)) {
            return false
        }
        snappedNowOn = date
        return true
    }

    init {
        viewModelScope.launch { repo.hydrate() }
    }

    fun shiftDate(days: Int) {
        viewedDate.value = viewedDate.value.plusDays(days.toLong())
    }

    fun setDate(date: LocalDate) {
        viewedDate.value = date
    }

    fun shiftTodoDate(delta: Int) {
        todoDate.value = StatsNav.shift(todoKind.value, todoDate.value, delta)
    }

    fun setTodoDate(date: LocalDate) {
        todoDate.value = date
    }

    fun jumpTodoToday() {
        todoDate.value = repo.clock.today()
    }

    fun setTodoKind(kind: PeriodKind) {
        todoKind.value = TodoMonth.coerceKind(kind)
    }

    fun addTodo(category: TodoCategory, title: String) {
        viewModelScope.launch { repo.addTodo(todoDate.value, category, title) }
    }

    fun setTodoDone(id: String, done: Boolean) {
        viewModelScope.launch { repo.setTodoDone(id, done) }
    }

    fun updateTodoTitle(id: String, title: String) {
        viewModelScope.launch { repo.updateTodoTitle(id, title) }
    }

    fun deleteTodo(id: String) {
        viewModelScope.launch { repo.deleteTodo(id) }
    }

    fun reorderTodos(category: TodoCategory, ids: List<String>) {
        viewModelScope.launch { repo.reorderTodos(todoDate.value, category, ids) }
    }

    fun deferTodo(id: String) {
        viewModelScope.launch { repo.deferTodo(id) }
    }

    fun markSleep() {
        viewModelScope.launch { repo.markSleep() }
    }

    fun markWake() {
        viewModelScope.launch {
            when (val out = repo.markWake()) {
                WakeOutcome.NoSleepStart -> needSleepStart.value = true
                is WakeOutcome.Filled -> showWakeToast(out.result)
            }
        }
    }

    fun markWakeWithStart(time: LocalTime) {
        viewModelScope.launch {
            needSleepStart.value = false
            val zone = repo.clock.zone()
            val now = repo.clock.now()
            var start = now.atZone(zone).with(time).toInstant()
            if (!start.isBefore(now)) start = start.minusSeconds(24 * 3600)
            when (val out = repo.markWake(start)) {
                WakeOutcome.NoSleepStart -> {}
                is WakeOutcome.Filled -> showWakeToast(out.result)
            }
        }
    }

    fun skipSleepStart() {
        viewModelScope.launch {
            needSleepStart.value = false
            repo.justWake()
        }
    }

    fun editLast(start: LocalTime, end: LocalTime) {
        viewModelScope.launch {
            val oldStart = today.value.lastSleep ?: return@launch
            val oldEnd = today.value.lastWake ?: return@launch
            val (s, e) = SleepSessions.editInstants(
                start, end, oldStart, oldEnd, repo.clock.zone(),
            )
            repo.editSession(oldStart, oldEnd, s, e)
        }
    }

    fun setRange(from: Int, to: Int, typeId: String?) {
        viewModelScope.launch { repo.setRange(viewedDate.value, from, to, typeId) }
    }

    fun shiftStats(delta: Int) {
        statsAnchor.value = StatsNav.shift(statsKind.value, statsAnchor.value, delta)
    }

    fun setStatsAnchor(date: LocalDate) {
        statsAnchor.value = StatsNav.pick(date)
    }

    fun jumpStatsToday() {
        statsAnchor.value = StatsNav.jumpToday(repo.clock.today())
    }

    fun setStatsKind(kind: PeriodKind) {
        statsKind.value = kind
    }

    fun setShowAllTime(on: Boolean) {
        viewModelScope.launch { repo.setShowAllTime(on) }
    }

    suspend fun exportCsv(): String = repo.exportCsv()
    suspend fun exportBackup(): String = repo.exportBackup()
    suspend fun restore(json: String): Boolean = repo.restoreBackup(json)

    fun addType(name: String, color: Long) {
        viewModelScope.launch { repo.addType(name, color) }
    }

    fun updateType(type: BlockType) {
        viewModelScope.launch { repo.updateType(type) }
    }

    fun reorder(ids: List<String>) {
        viewModelScope.launch { repo.reorder(ids) }
    }

    fun dismissToast() {
        toast.value = null
    }

    private fun showWakeToast(result: WakeFillResult) {
        toast.value = WakeFill.toast(result, repo.clock.zone())
        viewModelScope.launch {
            delay(4000)
            if (toast.value?.contains("入睡") == true) toast.value = null
        }
    }

    companion object {
        fun factory(repo: Repository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T = AppViewModel(repo) as T
            }
    }
}

fun LocalDate.toCn(): String {
    val w = listOf("一", "二", "三", "四", "五", "六", "日")[dayOfWeek.value - 1]
    return "${monthValue}月${dayOfMonth}日 周$w"
}

fun Instant.toHm(zone: ZoneId): String =
    atZone(zone).toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))

fun hoursLabel(blocks: Int): String {
    val h = blocks * BLOCK_MINUTES / 60.0
    return if (h == h.toLong().toDouble()) "${h.toLong()}.0 时" else String.format("%.1f 时", h)
}

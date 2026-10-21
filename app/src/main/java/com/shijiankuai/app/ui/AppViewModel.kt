package com.shijiankuai.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shijiankuai.app.data.Repository
import com.shijiankuai.app.data.SleepRow
import com.shijiankuai.app.data.WakeOutcome
import com.shijiankuai.app.domain.BLOCK_MINUTES
import com.shijiankuai.app.domain.BLOCKS_PER_DAY
import com.shijiankuai.app.domain.BlockMath
import com.shijiankuai.app.domain.BlockType
import com.shijiankuai.app.domain.PeriodKind
import com.shijiankuai.app.domain.PeriodStats
import com.shijiankuai.app.domain.SLEEP_TYPE_ID
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
    val lastSleep: Instant?,
    val lastWake: Instant?,
    val toast: String? = null,
)

class AppViewModel(private val repo: Repository) : ViewModel() {
    val viewedDate = MutableStateFlow(repo.clock.today())
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
            val nowIndex = if (date == todayDate) {
                BlockMath.indexContaining(now.atZone(zone).toLocalTime())
            } else {
                null
            }
            val open = sessions.firstOrNull { it.endedAtMillis == null }
            val lastClosed = sessions.firstOrNull { it.endedAtMillis != null }
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
                lastSleep = open?.startedAtMillis?.let(Instant::ofEpochMilli)
                    ?: lastClosed?.startedAtMillis?.let(Instant::ofEpochMilli),
                lastWake = lastClosed?.endedAtMillis?.let(Instant::ofEpochMilli),
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
            lastSleep = null,
            lastWake = null,
        ),
    )

    val statsKind = MutableStateFlow(PeriodKind.DAY)
    val statsAnchor = MutableStateFlow(repo.clock.today())
    val stats = MutableStateFlow<PeriodStats?>(null)

    init {
        viewModelScope.launch { repo.hydrate() }
        viewModelScope.launch {
            combine(statsKind, statsAnchor, tick) { kind, anchor, _ -> kind to anchor }
                .collect { (kind, anchor) ->
                    stats.value = repo.stats(kind, anchor)
                }
        }
    }

    fun shiftDate(days: Int) {
        viewedDate.value = viewedDate.value.plusDays(days.toLong())
    }

    fun setDate(date: LocalDate) {
        viewedDate.value = date
    }

    fun markSleep() {
        viewModelScope.launch { repo.markSleep() }
    }

    fun markWake() {
        viewModelScope.launch {
            when (val out = repo.markWake()) {
                WakeOutcome.NoSleepStart -> needSleepStart.value = true
                is WakeOutcome.Filled -> showWakeToast(out.result.sleepStart, out.result.filled.size)
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
                is WakeOutcome.Filled -> showWakeToast(out.result.sleepStart, out.result.filled.size)
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
            val zone = repo.clock.zone()
            val date = viewedDate.value
            var s = date.atTime(start).atZone(zone).toInstant()
            var e = date.atTime(end).atZone(zone).toInstant()
            if (!e.isAfter(s)) s = s.minusSeconds(24 * 3600)
            repo.editLastSession(s, e)
        }
    }

    fun setRange(from: Int, to: Int, typeId: String?) {
        viewModelScope.launch { repo.setRange(viewedDate.value, from, to, typeId) }
    }

    fun shiftStats(delta: Int) {
        val kind = statsKind.value
        statsAnchor.value = when (kind) {
            PeriodKind.DAY -> statsAnchor.value.plusDays(delta.toLong())
            PeriodKind.WEEK -> statsAnchor.value.plusWeeks(delta.toLong())
            PeriodKind.MONTH -> statsAnchor.value.plusMonths(delta.toLong())
            PeriodKind.YEAR -> statsAnchor.value.plusYears(delta.toLong())
        }
    }

    fun setStatsKind(kind: PeriodKind) {
        statsKind.value = kind
    }

    suspend fun exportCsv(): String = repo.exportCsv()
    suspend fun exportBackup(): String = repo.exportBackup()
    suspend fun restore(json: String) = repo.restoreBackup(json)

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

    private fun showWakeToast(start: Instant, n: Int) {
        val hm = start.atZone(repo.clock.zone()).toLocalTime()
            .format(DateTimeFormatter.ofPattern("HH:mm"))
        toast.value = "$hm 入睡 · 已记 $n 块睡觉"
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

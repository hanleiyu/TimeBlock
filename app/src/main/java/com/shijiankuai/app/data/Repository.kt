package com.shijiankuai.app.data

import androidx.room.withTransaction
import com.shijiankuai.app.domain.AppClock
import com.shijiankuai.app.domain.BlockId
import com.shijiankuai.app.domain.BlockType
import com.shijiankuai.app.domain.ColorMath
import com.shijiankuai.app.domain.PeriodKind
import com.shijiankuai.app.domain.PeriodStats
import com.shijiankuai.app.domain.Prefs
import com.shijiankuai.app.domain.SLEEP_TYPE_ID
import com.shijiankuai.app.domain.SleepFill
import com.shijiankuai.app.domain.SleepSession
import com.shijiankuai.app.domain.StatsCalc
import com.shijiankuai.app.domain.StatsObserve
import com.shijiankuai.app.domain.WakeFill
import com.shijiankuai.app.domain.WakeFillResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class Repository(
    private val db: AppDatabase,
    val clock: AppClock,
) {
    private val _isAsleep = MutableStateFlow(true)
    val isAsleep: StateFlow<Boolean> = _isAsleep
    private val _showAllTime = MutableStateFlow(true)
    val showAllTime: StateFlow<Boolean> = _showAllTime

    val types: Flow<List<BlockType>> = db.types().observe().map { rows ->
        rows.map { it.toDomain() }
    }

    fun blocksOn(date: LocalDate): Flow<Map<Int, String>> =
        db.blocks().observeDay(date.toEpochDay()).map { rows ->
            rows.associate { it.index to it.typeId }
        }

    val sessions: Flow<List<SleepRow>> = db.sleeps().observe()

    suspend fun hydrate() {
        AppDatabase.seedIfNeeded(db)
        _isAsleep.value = db.kv().get("is_asleep") != "0"
        _showAllTime.value = Prefs.showAllTimeEnabled(db.kv().get(Prefs.SHOW_ALL_TIME))
    }

    suspend fun setShowAllTime(on: Boolean) {
        db.kv().put(KvRow(Prefs.SHOW_ALL_TIME, if (on) "1" else "0"))
        _showAllTime.value = on
    }

    suspend fun markSleep(): Instant {
        val now = clock.now()
        db.sleeps().open()?.let { open ->
            db.sleeps().update(open.copy(endedAtMillis = now.toEpochMilli()))
        }
        db.sleeps().insert(SleepRow(startedAtMillis = now.toEpochMilli(), endedAtMillis = null))
        db.kv().put(KvRow("is_asleep", "1"))
        _isAsleep.value = true
        return now
    }

    suspend fun markWake(sleepStartOverride: Instant? = null): WakeOutcome {
        val now = clock.now()
        val open = db.sleeps().open()
        val start = sleepStartOverride ?: open?.startedAtMillis?.let(Instant::ofEpochMilli)
        db.kv().put(KvRow("is_asleep", "0"))
        _isAsleep.value = false
        if (start == null) {
            return WakeOutcome.NoSleepStart
        }
        val result = applySleepSpan(start, now, previous = null)
        if (open != null) {
            db.sleeps().update(open.copy(endedAtMillis = now.toEpochMilli()))
        } else {
            db.sleeps().insert(
                SleepRow(startedAtMillis = start.toEpochMilli(), endedAtMillis = now.toEpochMilli()),
            )
        }
        return WakeOutcome.Filled(result)
    }

    suspend fun justWake() {
        val now = clock.now()
        db.sleeps().open()?.let { db.sleeps().update(it.copy(endedAtMillis = now.toEpochMilli())) }
        db.kv().put(KvRow("is_asleep", "0"))
        _isAsleep.value = false
    }

    suspend fun editSession(oldStart: Instant, oldEnd: Instant, start: Instant, end: Instant) {
        val row = db.sleeps().all().firstOrNull {
            it.startedAtMillis == oldStart.toEpochMilli() && it.endedAtMillis == oldEnd.toEpochMilli()
        } ?: return
        db.sleeps().update(
            row.copy(startedAtMillis = start.toEpochMilli(), endedAtMillis = end.toEpochMilli()),
        )
        applySleepSpan(start, end, previous = oldStart to oldEnd)
        db.kv().put(KvRow("is_asleep", "0"))
        _isAsleep.value = false
    }

    private suspend fun applySleepSpan(
        start: Instant,
        end: Instant,
        previous: Pair<Instant, Instant>?,
    ): WakeFillResult {
        val zone = clock.zone()
        val result = WakeFill.compute(start, end, zone)
        val next = result.filled.toSet()
        val old = previous?.let { SleepFill.blocks(it.first, it.second, zone).toSet() } ?: emptySet()
        val toClear = old - next
        toClear.forEach { id ->
            val row = db.blocks().range(id.epochDay, id.epochDay)
                .firstOrNull { it.index == id.index }
            if (row?.typeId == SLEEP_TYPE_ID) {
                db.blocks().clear(id.epochDay, id.index)
            }
        }
        db.blocks().upsertAll(next.map { BlockRow(it.epochDay, it.index, SLEEP_TYPE_ID) })
        return result
    }

    suspend fun setRange(date: LocalDate, from: Int, to: Int, typeId: String?) {
        val a = minOf(from, to).coerceIn(0, 143)
        val b = maxOf(from, to).coerceIn(0, 143)
        if (typeId == null) {
            db.blocks().clearRange(date.toEpochDay(), a, b)
        } else {
            db.blocks().upsertAll((a..b).map { BlockRow(date.toEpochDay(), it, typeId) })
        }
    }

    suspend fun addType(name: String, color: Long): String {
        val id = "c_" + UUID.randomUUID().toString().take(8)
        val sort = db.types().maxSort() + 1
        db.types().upsert(
            TypeRow(id, name, ColorMath.normalize(color), sort, archived = false, isPreset = false, isSleep = false),
        )
        return id
    }

    suspend fun updateType(type: BlockType) {
        val color = ColorMath.normalize(type.color)
        if (type.isSleep) {
            db.types().update(
                TypeRow(type.id, type.name, color, type.sortOrder, false, type.isPreset, true),
            )
        } else {
            db.types().update(
                TypeRow(type.id, type.name, color, type.sortOrder, type.archived, type.isPreset, false),
            )
        }
    }

    suspend fun reorder(ids: List<String>) {
        val all = db.types().all().associateBy { it.id }
        ids.forEachIndexed { i, id ->
            val row = all[id] ?: return@forEachIndexed
            db.types().update(row.copy(sortOrder = i))
        }
    }

    fun statsFlow(kind: PeriodKind, anchor: LocalDate, ticks: Flow<Long>): Flow<PeriodStats> {
        val (start, end) = StatsCalc.periodBounds(kind, anchor)
        val blocks = db.blocks().observeRange(start.toEpochDay(), end.toEpochDay()).map { rows ->
            rows.associate { BlockId(it.epochDay, it.index) to it.typeId }
        }
        val typeList = db.types().observe().map { rows -> rows.map { it.toDomain() } }
        return StatsObserve.flow(
            kind, anchor, clock::now, clock.zone(), blocks, typeList, ticks, _showAllTime,
        )
    }

    suspend fun exportCsv(): String = Backup.csv(db.blocks().all(), db.types().all())

    suspend fun exportBackup(): String =
        Backup.exportJson(db.types().all(), db.blocks().all(), db.sleeps().all(), db.kv().all())

    suspend fun restoreBackup(json: String): Boolean {
        val snap = Backup.tryParse(json) ?: return false
        db.withTransaction {
            db.types().clearAll()
            db.blocks().clearAll()
            db.sleeps().clearAll()
            db.kv().clearAll()
            snap.types.forEach { db.types().upsert(it) }
            db.blocks().upsertAll(snap.blocks)
            snap.sleeps.forEach { db.sleeps().insert(it.copy(id = 0)) }
            snap.kv.forEach { db.kv().put(it) }
        }
        _isAsleep.value = db.kv().get("is_asleep") != "0"
        _showAllTime.value = Prefs.showAllTimeEnabled(db.kv().get(Prefs.SHOW_ALL_TIME))
        return true
    }

    fun sleptBlocksToday(date: LocalDate, map: Map<Int, String>): Int =
        map.values.count { it == SLEEP_TYPE_ID }
}

data class SleepUiState(
    val isAsleep: Boolean,
    val openStart: Instant?,
    val lastClosed: SleepSession?,
)

sealed class WakeOutcome {
    data object NoSleepStart : WakeOutcome()
    data class Filled(val result: WakeFillResult) : WakeOutcome()
}

private fun TypeRow.toDomain() = BlockType(id, name, color, sortOrder, archived, isPreset, isSleep)

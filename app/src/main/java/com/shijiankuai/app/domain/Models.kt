package com.shijiankuai.app.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

const val BLOCK_MINUTES = 10
const val BLOCKS_PER_DAY = 144
const val SLEEP_TYPE_ID = "sleep"

data class BlockId(val epochDay: Long, val index: Int) {
    init {
        require(index in 0 until BLOCKS_PER_DAY) { "index $index" }
    }

    val date: LocalDate get() = LocalDate.ofEpochDay(epochDay)
}

data class BlockType(
    val id: String,
    val name: String,
    val color: Long,
    val sortOrder: Int,
    val archived: Boolean,
    val isPreset: Boolean,
    val isSleep: Boolean,
)

data class SleepSession(
    val startedAt: Instant,
    val endedAt: Instant?,
)

enum class PeriodKind { DAY, WEEK, MONTH, YEAR }

data class TypeCount(
    val typeId: String,
    val name: String,
    val color: Long,
    val blocks: Int,
) {
    val hours: Double get() = blocks * BLOCK_MINUTES / 60.0
}

data class PeriodStats(
    val kind: PeriodKind,
    val start: LocalDate,
    val endInclusive: LocalDate,
    val totalElapsed: Int,
    val filled: Int,
    val unfilled: Int,
    val byType: List<TypeCount>,
) {
    val unfilledHours: Double get() = unfilled * BLOCK_MINUTES / 60.0
}

data class WakeFillResult(
    val sleepStart: Instant,
    val wakeAt: Instant,
    val filled: List<BlockId>,
)

fun interface AppClock {
    fun now(): Instant
    fun zone(): ZoneId = ZoneId.systemDefault()
    fun today(): LocalDate = now().atZone(zone()).toLocalDate()
}

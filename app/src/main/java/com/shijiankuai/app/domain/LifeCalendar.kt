package com.shijiankuai.app.domain

data class LifeMonth(val year: Int, val month: Int) : Comparable<LifeMonth> {
    init {
        require(month in 1..12) { "month $month" }
    }

    override fun compareTo(other: LifeMonth) =
        compareValuesBy(this, other, { it.year }, { it.month })

    fun plusMonths(delta: Int): LifeMonth {
        val zero = year * 12 + (month - 1) + delta
        val y = Math.floorDiv(zero, 12)
        val m = Math.floorMod(zero, 12) + 1
        return LifeMonth(y, m)
    }
}

data class LifeOrigin(val year: Int, val month: Int) {
    fun asMonth(): LifeMonth = LifeMonth(year, month)

    fun encode(): String = "%04d-%02d".format(year, month)

    companion object {
        fun parse(raw: String?): LifeOrigin? {
            val m = Regex("""^(\d{4})-(\d{2})$""").matchEntire(raw?.trim().orEmpty()) ?: return null
            val year = m.groupValues[1].toInt()
            val month = m.groupValues[2].toInt()
            if (month !in 1..12) return null
            return LifeOrigin(year, month)
        }
    }
}

data class LifeEvent(
    val id: String,
    val title: String,
    val note: String,
    val start: LifeMonth,
    val end: LifeMonth,
    val color: Long,
    val createdAt: Long,
    val updatedAt: Long,
)

enum class LifeTone { PREBIRTH, PAST, CURRENT, FUTURE }

data class LifeCell(
    val month: LifeMonth,
    val tone: LifeTone,
    val display: LifeEvent?,
    val covering: List<LifeEvent>,
    val birth: Boolean,
    val ageLabel: String?,
    val eventLabel: String?,
) {
    fun fillKey(): String = display?.let { "e:${it.id}" } ?: tone.name
}

data class LifeProgressSeg(
    val fillKey: String,
    val length: Int,
    val color: Long?,
    val tone: LifeTone,
)

data class LifeDotSector(
    val color: Long,
    val startDeg: Float,
    val sweepDeg: Float,
)

sealed class LifeSave {
    data class Ok(val event: LifeEvent) : LifeSave()
    data object EmptyTitle : LifeSave()
    data object InvalidRange : LifeSave()
}

object LifeCalendar {
    const val HORIZON_YEARS = 90
    const val MONTHS_IN_HORIZON = HORIZON_YEARS * 12
    /** 6 o'clock; clockwise so two colors are left then right. */
    const val DOT_START_DEG = 90f
    val colors: List<Long> get() = Presets.swatches

    private val coverOrder = compareByDescending<LifeEvent> { it.updatedAt }
        .thenByDescending { it.id }

    fun years(origin: LifeOrigin): IntRange =
        origin.year until (origin.year + HORIZON_YEARS)

    fun index(origin: LifeOrigin, month: LifeMonth): Int? {
        val i = (month.year - origin.year) * 12 + (month.month - 1)
        return i.takeIf { it in 0 until MONTHS_IN_HORIZON }
    }

    fun monthAt(origin: LifeOrigin, index: Int): LifeMonth {
        require(index in 0 until MONTHS_IN_HORIZON)
        return LifeMonth(origin.year + index / 12, index % 12 + 1)
    }

    fun monthsIn(start: LifeMonth, end: LifeMonth): List<LifeMonth> {
        if (end < start) return emptyList()
        val out = ArrayList<LifeMonth>()
        var cur = start
        while (cur <= end) {
            out += cur
            cur = cur.plusMonths(1)
        }
        return out
    }

    fun covering(events: List<LifeEvent>, month: LifeMonth): List<LifeEvent> =
        events.filter { month in it.start..it.end }.sortedWith(coverOrder)

    fun displayEvent(events: List<LifeEvent>, month: LifeMonth): LifeEvent? =
        covering(events, month).firstOrNull()

    /** Distinct covering colors in cover order (latest-updated, then id). Duplicates drop. */
    fun dotColors(covering: List<LifeEvent>): List<Long> {
        val out = ArrayList<Long>()
        covering.forEach { event ->
            val color = ColorMath.normalize(event.color)
            if (out.none { it == color }) out += color
        }
        return out
    }

    fun dotColors(events: List<LifeEvent>, month: LifeMonth): List<Long> =
        dotColors(covering(events, month))

    fun dotSectors(colors: List<Long>): List<LifeDotSector> {
        if (colors.isEmpty()) return emptyList()
        val sweep = 360f / colors.size
        return colors.mapIndexed { i, color ->
            LifeDotSector(color, DOT_START_DEG + i * sweep, sweep)
        }
    }

    fun normalizeRange(a: LifeMonth, b: LifeMonth): Pair<LifeMonth, LifeMonth> =
        if (a <= b) a to b else b to a

    fun rangeLabel(start: LifeMonth, end: LifeMonth): String {
        val one = "${start.year}年${start.month}月"
        return if (start == end) one else "$one – ${end.year}年${end.month}月"
    }

    fun elapsedCaption(origin: LifeOrigin, today: LifeMonth): String {
        val months = (today.year - origin.year) * 12 + (today.month - origin.month)
        val safe = months.coerceAtLeast(0)
        return "已过 ${safe / 12} 年 ${safe % 12} 个月"
    }

    fun validate(
        @Suppress("UNUSED_PARAMETER") events: List<LifeEvent>,
        candidate: LifeEvent,
        @Suppress("UNUSED_PARAMETER") excludeId: String?,
    ): LifeSave {
        if (candidate.title.trim().isEmpty()) return LifeSave.EmptyTitle
        if (candidate.end < candidate.start) return LifeSave.InvalidRange
        return LifeSave.Ok(candidate.copy(title = candidate.title.trim()))
    }

    fun cells(
        origin: LifeOrigin,
        today: LifeMonth,
        events: List<LifeEvent>,
    ): List<LifeCell> {
        val birth = origin.asMonth()
        return (0 until MONTHS_IN_HORIZON).map { i ->
            val month = monthAt(origin, i)
            val cover = covering(events, month)
            val display = cover.firstOrNull()
            val next = month.plusMonths(1)
            val nextDisplay = index(origin, next)?.let { displayEvent(events, next) }
            val age = month.year - origin.year
            LifeCell(
                month = month,
                tone = tone(month, birth, today),
                display = display,
                covering = cover,
                birth = month == birth,
                ageLabel = if (month.month == 1 && age > 0 && age % 10 == 0) "${age}岁" else null,
                eventLabel = if (display != null && display.id != nextDisplay?.id) display.title else null,
            )
        }
    }

    fun progress(
        origin: LifeOrigin,
        today: LifeMonth,
        events: List<LifeEvent>,
    ): List<LifeProgressSeg> {
        val out = mutableListOf<LifeProgressSeg>()
        cells(origin, today, events).forEach { cell ->
            val last = out.lastOrNull()
            if (last != null && last.fillKey == cell.fillKey()) {
                out[out.lastIndex] = last.copy(length = last.length + 1)
            } else {
                out += LifeProgressSeg(cell.fillKey(), 1, cell.display?.color, cell.tone)
            }
        }
        return out
    }

    private fun tone(month: LifeMonth, birth: LifeMonth, today: LifeMonth): LifeTone = when {
        month == today -> LifeTone.CURRENT
        month < birth -> LifeTone.PREBIRTH
        month < today -> LifeTone.PAST
        else -> LifeTone.FUTURE
    }
}

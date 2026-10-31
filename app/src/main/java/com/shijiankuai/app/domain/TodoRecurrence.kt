package com.shijiankuai.app.domain

import java.time.LocalDate
import java.time.YearMonth

sealed class TodoRecurrence {
    data object None : TodoRecurrence()
    data class Weekly(val daysMask: Int) : TodoRecurrence()
    data class Monthly(val dayOfMonth: Int) : TodoRecurrence()

    fun matches(date: LocalDate): Boolean = when (this) {
        None -> false
        is Weekly -> daysMask and (1 shl (date.dayOfWeek.value - 1)) != 0
        is Monthly -> {
            val last = date.lengthOfMonth()
            val target = dayOfMonth.coerceIn(1, 31).coerceAtMost(last)
            date.dayOfMonth == target
        }
    }

    fun kind(): String = when (this) {
        None -> KIND_NONE
        is Weekly -> KIND_WEEKLY
        is Monthly -> KIND_MONTHLY
    }

    fun mask(): Int = when (this) {
        None -> 0
        is Weekly -> daysMask
        is Monthly -> dayOfMonth
    }

    fun hasDays(): Boolean = when (this) {
        None -> false
        is Weekly -> daysMask != 0
        is Monthly -> dayOfMonth in 1..31
    }

    companion object {
        const val KIND_NONE = "none"
        const val KIND_WEEKLY = "weekly"
        const val KIND_MONTHLY = "monthly"
        const val OCC_SEP = '@'

        fun weekly(isoDays: Set<Int>): Weekly {
            var mask = 0
            isoDays.forEach { day ->
                if (day in 1..7) mask = mask or (1 shl (day - 1))
            }
            return Weekly(mask)
        }

        fun weeklyDays(mask: Int): Set<Int> =
            (1..7).filter { mask and (1 shl (it - 1)) != 0 }.toSet()

        fun parse(kind: String?, mask: Int): TodoRecurrence = when (kind) {
            KIND_WEEKLY -> Weekly(mask)
            KIND_MONTHLY -> Monthly(mask.coerceIn(1, 31))
            else -> None
        }

        fun occId(seriesId: String, epochDay: Long): String = "$seriesId$OCC_SEP$epochDay"

        fun parseOcc(id: String): Pair<String, Long?> {
            val i = id.indexOf(OCC_SEP)
            if (i <= 0) return id to null
            return id.substring(0, i) to id.substring(i + 1).toLongOrNull()
        }

        fun encodeDays(days: Set<Long>): String =
            days.sorted().joinToString(",")

        fun decodeDays(raw: String?): Set<Long> =
            raw.orEmpty().split(',').mapNotNull { it.toLongOrNull() }.toSet()

        fun expand(items: List<TodoItem>, start: LocalDate, end: LocalDate): List<TodoItem> {
            if (start.isAfter(end)) return emptyList()
            val out = ArrayList<TodoItem>()
            items.forEach { item ->
                when (val rec = item.recurrence) {
                    None -> {
                        val day = LocalDate.ofEpochDay(item.epochDay)
                        if (!day.isBefore(start) && !day.isAfter(end)) out += item
                    }
                    else -> {
                        val from = LocalDate.ofEpochDay(item.epochDay).let { if (it.isAfter(start)) it else start }
                        datesMatching(rec, from, end).forEach { date ->
                            val epoch = date.toEpochDay()
                            if (epoch in item.skipDays) return@forEach
                            out += item.copy(
                                id = occId(item.seriesId, epoch),
                                epochDay = epoch,
                                done = epoch in item.doneDays,
                            )
                        }
                    }
                }
            }
            return out.sortedWith(compareBy<TodoItem> { it.epochDay }.thenBy { it.sortOrder }.thenBy { it.id })
        }

        fun completedEpochDays(items: List<TodoItem>): Set<Long> {
            val days = mutableSetOf<Long>()
            items.forEach { item ->
                if (item.recurrence is None) {
                    if (item.done) days += item.epochDay
                } else {
                    days += item.doneDays
                }
            }
            return days
        }

        private fun datesMatching(rec: TodoRecurrence, start: LocalDate, end: LocalDate): List<LocalDate> {
            return when (rec) {
                None -> emptyList()
                is Weekly -> {
                    val out = ArrayList<LocalDate>()
                    var d = start
                    while (!d.isAfter(end)) {
                        if (rec.matches(d)) out += d
                        d = d.plusDays(1)
                    }
                    out
                }
                is Monthly -> {
                    val out = ArrayList<LocalDate>()
                    var month = YearMonth.from(start)
                    val lastMonth = YearMonth.from(end)
                    while (!month.isAfter(lastMonth)) {
                        val date = month.atDay(rec.dayOfMonth.coerceIn(1, 31).coerceAtMost(month.lengthOfMonth()))
                        if (!date.isBefore(start) && !date.isAfter(end)) out += date
                        month = month.plusMonths(1)
                    }
                    out
                }
            }
        }
    }
}

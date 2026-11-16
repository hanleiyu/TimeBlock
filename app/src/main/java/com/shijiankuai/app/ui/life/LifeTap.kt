package com.shijiankuai.app.ui.life

import com.shijiankuai.app.domain.LifeCalendar
import com.shijiankuai.app.domain.LifeEvent
import com.shijiankuai.app.domain.LifeMonth

object LifeTap {
    sealed class Result
    data class OpenForm(val start: LifeMonth, val end: LifeMonth) : Result()
    data class OpenDetails(val month: LifeMonth, val covering: List<LifeEvent>) : Result()

    fun fromHit(month: LifeMonth?, events: List<LifeEvent>): Result? {
        val hit = month ?: return null
        val covering = LifeCalendar.covering(events, hit)
        return if (covering.isEmpty()) OpenForm(hit, hit) else OpenDetails(hit, covering)
    }

    fun addOverlap(month: LifeMonth): OpenForm = OpenForm(month, month)

    fun blankEvent(start: LifeMonth, end: LifeMonth) = LifeEvent(
        id = "",
        title = "",
        note = "",
        start = start,
        end = end,
        color = LifeCalendar.colors.first(),
        createdAt = 0L,
        updatedAt = 0L,
    )
}

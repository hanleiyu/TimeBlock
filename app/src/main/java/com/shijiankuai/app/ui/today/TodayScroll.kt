package com.shijiankuai.app.ui.today

import java.time.LocalDate
import kotlin.math.roundToInt

object TodayScroll {
    fun shouldSnapToNow(
        alreadySnappedOn: LocalDate?,
        viewing: LocalDate,
        isToday: Boolean,
    ): Boolean = isToday && alreadySnappedOn != viewing

    fun nowOffsetPx(nowIndex: Int, rowHeightPx: Float, topPadPx: Float): Int {
        val hour = nowIndex / 6
        return (rowHeightPx * hour - topPadPx).roundToInt().coerceAtLeast(0)
    }
}

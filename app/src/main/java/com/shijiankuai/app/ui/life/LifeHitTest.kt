package com.shijiankuai.app.ui.life

import com.shijiankuai.app.domain.LifeCalendar
import com.shijiankuai.app.domain.LifeMonth
import com.shijiankuai.app.domain.LifeOrigin

data class LifeHitLayout(
    val rowHeightPx: Float,
    val labelWidthPx: Float,
    val trailingWidthPx: Float,
    val gapPx: Float,
    val gridWidthPx: Float,
) {
    val cellWidthPx: Float
        get() {
            val inner = gridWidthPx - labelWidthPx - trailingWidthPx - gapPx * 11f
            return (inner / 12f).coerceAtLeast(0f)
        }
}

object LifeHitTest {
    fun monthAt(
        viewportX: Float,
        viewportY: Float,
        scrollY: Int,
        origin: LifeOrigin,
        layout: LifeHitLayout,
    ): LifeMonth? {
        if (layout.rowHeightPx <= 0f || layout.gridWidthPx <= 0f) return null
        val contentY = viewportY + scrollY
        if (contentY < 0f) return null
        val row = (contentY / layout.rowHeightPx).toInt()
        if (row !in 0 until LifeCalendar.HORIZON_YEARS) return null
        val inner = viewportX - layout.labelWidthPx
        val col = when {
            viewportX >= layout.gridWidthPx - layout.trailingWidthPx -> 11
            inner < 0f -> 0
            else -> {
                val step = layout.cellWidthPx + layout.gapPx
                if (step <= 0f) return null
                (inner / step).toInt().coerceIn(0, 11)
            }
        }
        return LifeCalendar.monthAt(origin, row * 12 + col)
    }
}

package com.shijiankuai.app.ui.today

import com.shijiankuai.app.domain.BLOCKS_PER_DAY

data class BlockHitLayout(
    val rowHeightPx: Float,
    val labelWidthPx: Float,
    val gapPx: Float,
    val gridWidthPx: Float,
) {
    val cellWidthPx: Float
        get() = ((gridWidthPx - labelWidthPx - gapPx * 5f) / 6f).coerceAtLeast(0f)
}

object BlockHitTest {
    /**
     * Map a viewport tap to a block index.
     * [viewportY] is in the visible grid; add [scrollY] every call so a sheet
     * dismiss or auto-scroll cannot reuse a stale content offset.
     */
    fun indexAt(
        viewportX: Float,
        viewportY: Float,
        scrollY: Int,
        layout: BlockHitLayout,
    ): Int? {
        if (layout.rowHeightPx <= 0f || layout.gridWidthPx <= 0f) return null
        val contentY = viewportY + scrollY
        if (contentY < 0f) return null
        val hour = (contentY / layout.rowHeightPx).toInt()
        if (hour !in 0..23) return null
        val inner = viewportX - layout.labelWidthPx
        val col = if (inner < 0f) {
            0
        } else {
            val step = layout.cellWidthPx + layout.gapPx
            if (step <= 0f) return null
            (inner / step).toInt().coerceIn(0, 5)
        }
        return (hour * 6 + col).coerceIn(0, BLOCKS_PER_DAY - 1)
    }
}

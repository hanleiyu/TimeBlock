package com.shijiankuai.app.ui.life

import com.shijiankuai.app.domain.LifeCalendar
import com.shijiankuai.app.domain.LifeMonth

object LifeFormSpec {
    const val singleSwitchLabel = "单个月"

    fun initialSingle(start: LifeMonth, end: LifeMonth): Boolean = start == end

    fun collapseToStart(start: LifeMonth, end: LifeMonth, rememberedEnd: LifeMonth?): Pair<LifeMonth, LifeMonth?> {
        val mem = if (end > start) end else rememberedEnd
        return start to mem
    }

    fun expandFromStart(start: LifeMonth, rememberedEnd: LifeMonth?): LifeMonth =
        if (rememberedEnd != null && rememberedEnd >= start) rememberedEnd else start

    fun endAfterStartChange(single: Boolean, start: LifeMonth, end: LifeMonth): LifeMonth =
        if (single || end < start) start else end

    fun saveBounds(single: Boolean, start: LifeMonth, end: LifeMonth): Pair<LifeMonth, LifeMonth> =
        if (single) start to start else LifeCalendar.normalizeRange(start, end)
}

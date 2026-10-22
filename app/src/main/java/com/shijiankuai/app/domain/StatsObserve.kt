package com.shijiankuai.app.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object StatsObserve {
    fun flow(
        kind: PeriodKind,
        anchor: LocalDate,
        now: () -> Instant,
        zone: ZoneId,
        blocks: Flow<Map<BlockId, String>>,
        types: Flow<List<BlockType>>,
        tick: Flow<Long>,
        showAllTime: Flow<Boolean> = flowOf(false),
    ): Flow<PeriodStats> = combine(blocks, types, tick, showAllTime) { map, typeList, _, all ->
        StatsCalc.aggregate(kind, anchor, now(), zone, map, typeList, fullSpan = all)
    }
}

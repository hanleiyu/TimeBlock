package com.shijiankuai.app.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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
    ): Flow<PeriodStats> = combine(blocks, types, tick) { map, typeList, _ ->
        StatsCalc.aggregate(kind, anchor, now(), zone, map, typeList)
    }
}

package com.shijiankuai.app.ui

import com.shijiankuai.app.domain.PeriodKind

enum class BottomTab(val label: String) {
    DAY("日"),
    WEEK("周"),
    MONTH("月"),
    YEAR("年"),
    SETTINGS("设置"),
}

enum class PeriodFeature(val label: String) {
    BLOCKS("时间块"),
    STATS("统计"),
    TODO("清单"),
    HEALTH("健康"),
}

object AppIa {
    fun features(tab: BottomTab): List<PeriodFeature> = when (tab) {
        BottomTab.DAY -> listOf(PeriodFeature.BLOCKS, PeriodFeature.STATS, PeriodFeature.TODO)
        BottomTab.WEEK -> listOf(PeriodFeature.STATS, PeriodFeature.HEALTH)
        BottomTab.MONTH -> listOf(PeriodFeature.STATS, PeriodFeature.TODO, PeriodFeature.HEALTH)
        BottomTab.YEAR -> listOf(PeriodFeature.STATS, PeriodFeature.TODO)
        BottomTab.SETTINGS -> emptyList()
    }

    fun defaultFeature(tab: BottomTab): PeriodFeature? = features(tab).firstOrNull()

    fun periodKind(tab: BottomTab): PeriodKind? = when (tab) {
        BottomTab.DAY -> PeriodKind.DAY
        BottomTab.WEEK -> PeriodKind.WEEK
        BottomTab.MONTH -> PeriodKind.MONTH
        BottomTab.YEAR -> PeriodKind.YEAR
        BottomTab.SETTINGS -> null
    }
}

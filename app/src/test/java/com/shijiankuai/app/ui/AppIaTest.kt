package com.shijiankuai.app.ui

import com.shijiankuai.app.domain.PeriodKind
import com.shijiankuai.app.domain.TodoMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppIaTest {
    @Test
    fun bottomTabsArePeriodFirstThenSettings() {
        assertEquals(
            listOf(
                BottomTab.DAY,
                BottomTab.WEEK,
                BottomTab.MONTH,
                BottomTab.YEAR,
                BottomTab.SETTINGS,
            ),
            BottomTab.entries.toList(),
        )
        assertEquals(
            listOf("日", "周", "月", "年", "设置"),
            BottomTab.entries.map { it.label },
        )
    }

    @Test
    fun innerFeaturesMatchWhatEachPeriodAlreadyHas() {
        assertEquals(
            listOf(PeriodFeature.BLOCKS, PeriodFeature.STATS, PeriodFeature.TODO),
            AppIa.features(BottomTab.DAY),
        )
        assertEquals(
            listOf(PeriodFeature.STATS, PeriodFeature.HEALTH),
            AppIa.features(BottomTab.WEEK),
        )
        assertEquals(
            listOf(PeriodFeature.STATS, PeriodFeature.TODO, PeriodFeature.HEALTH),
            AppIa.features(BottomTab.MONTH),
        )
        assertEquals(
            listOf(PeriodFeature.STATS, PeriodFeature.TODO, PeriodFeature.LIFE),
            AppIa.features(BottomTab.YEAR),
        )
        assertEquals(emptyList<PeriodFeature>(), AppIa.features(BottomTab.SETTINGS))
    }

    @Test
    fun innerLabelsStayTheExistingFeatureNames() {
        assertEquals("时间块", PeriodFeature.BLOCKS.label)
        assertEquals("统计", PeriodFeature.STATS.label)
        assertEquals("清单", PeriodFeature.TODO.label)
        assertEquals("健康", PeriodFeature.HEALTH.label)
        assertEquals("人生", PeriodFeature.LIFE.label)
    }

    @Test
    fun defaultFeatureIsTheFirstExistingOne() {
        assertEquals(PeriodFeature.BLOCKS, AppIa.defaultFeature(BottomTab.DAY))
        assertEquals(PeriodFeature.STATS, AppIa.defaultFeature(BottomTab.WEEK))
        assertEquals(PeriodFeature.STATS, AppIa.defaultFeature(BottomTab.MONTH))
        assertEquals(PeriodFeature.STATS, AppIa.defaultFeature(BottomTab.YEAR))
        assertNull(AppIa.defaultFeature(BottomTab.SETTINGS))
    }

    @Test
    fun missingViewsAreNotInvented() {
        val all = BottomTab.entries.flatMap { tab -> AppIa.features(tab).map { tab to it } }
        assertFalse(all.contains(BottomTab.WEEK to PeriodFeature.BLOCKS))
        assertFalse(all.contains(BottomTab.WEEK to PeriodFeature.TODO))
        assertFalse(all.contains(BottomTab.MONTH to PeriodFeature.BLOCKS))
        assertFalse(all.contains(BottomTab.YEAR to PeriodFeature.BLOCKS))
        assertFalse(all.contains(BottomTab.YEAR to PeriodFeature.HEALTH))
        assertFalse(all.contains(BottomTab.DAY to PeriodFeature.HEALTH))
        assertFalse(all.contains(BottomTab.DAY to PeriodFeature.LIFE))
        assertFalse(all.contains(BottomTab.WEEK to PeriodFeature.LIFE))
        assertFalse(all.contains(BottomTab.MONTH to PeriodFeature.LIFE))
        assertTrue(all.contains(BottomTab.YEAR to PeriodFeature.LIFE))
        assertTrue(PeriodFeature.entries.none { it.name == "METRICS" })
        assertTrue(BottomTab.entries.none { it.label == "指标" })
    }

    @Test
    fun periodKindMapsBottomTabAndSettingsHasNone() {
        assertEquals(PeriodKind.DAY, AppIa.periodKind(BottomTab.DAY))
        assertEquals(PeriodKind.WEEK, AppIa.periodKind(BottomTab.WEEK))
        assertEquals(PeriodKind.MONTH, AppIa.periodKind(BottomTab.MONTH))
        assertEquals(PeriodKind.YEAR, AppIa.periodKind(BottomTab.YEAR))
        assertNull(AppIa.periodKind(BottomTab.SETTINGS))
    }

    @Test
    fun checklistKindsStayDayMonthYear() {
        assertEquals(
            listOf(PeriodKind.DAY, PeriodKind.MONTH, PeriodKind.YEAR),
            TodoMonth.CHECKLIST_KINDS,
        )
        assertEquals(PeriodKind.MONTH, TodoMonth.coerceKind(PeriodKind.WEEK))
    }
}

package com.shijiankuai.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TypeSeedTest {
    private fun oldInstall(): List<BlockType> = listOf(
        BlockType("work", "工作", 0xFF8EBFD2, 0, archived = false, isPreset = true, isSleep = false),
        BlockType("eat", "吃饭", 0xFFE5AD8C, 1, archived = false, isPreset = true, isSleep = false),
        BlockType(SLEEP_TYPE_ID, "睡觉", 0xFF808BD0, 2, archived = false, isPreset = true, isSleep = true),
        BlockType("study", "学习", 0xFFA996CF, 3, archived = false, isPreset = true, isSleep = false),
        BlockType("read", "阅读", 0xFFDE8B9D, 4, archived = false, isPreset = true, isSleep = false),
        BlockType("play", "玩", 0xFFE3C783, 5, archived = false, isPreset = true, isSleep = false),
        BlockType("social", "人际", 0xFF9CCB97, 6, archived = false, isPreset = true, isSleep = false),
        BlockType("sport", "运动", 0xFF74B3AE, 7, archived = false, isPreset = true, isSleep = false),
        BlockType("custom-1", "写作", 0xFFC4B5E3, 8, archived = false, isPreset = false, isSleep = false),
    )

    @Test
    fun emptyDatabaseGetsCatalogWithCommuteAfterWork() {
        val plan = TypeSeed.plan(emptyList())
        assertEquals(Presets.types.map { it.id }, plan.insert.map { it.id })
        assertEquals(listOf("work", "commute"), plan.insert.take(2).map { it.id })
        assertEquals("通勤", plan.insert[1].name)
        assertEquals(0xFFA8A6A3, plan.insert[1].color)
        assertTrue(plan.insert[1].isPreset)
        assertEquals(emptyMap<String, Int>(), plan.sortUpdates)
    }

    @Test
    fun existingInstallInsertsCommuteAfterWorkAndShiftsLaterRows() {
        val existing = oldInstall()
        val plan = TypeSeed.plan(existing)
        assertEquals(listOf("commute"), plan.insert.map { it.id })
        assertEquals(1, plan.insert.single().sortOrder)
        assertEquals("写作", existing.single { it.id == "custom-1" }.name)
        assertEquals(0xFFC4B5E3, existing.single { it.id == "custom-1" }.color)
        assertEquals(9, plan.sortUpdates.getValue("custom-1"))
        assertEquals(2, plan.sortUpdates.getValue("eat"))
        assertTrue(plan.insert.none { !it.isPreset })
    }

    @Test
    fun skipsWhenCommuteNameAlreadyExists() {
        val existing = oldInstall() + BlockType(
            "mine", "通勤", 0xFF112233, 9, archived = false, isPreset = false, isSleep = false,
        )
        val plan = TypeSeed.plan(existing)
        assertTrue(plan.insert.isEmpty())
        assertTrue(plan.sortUpdates.isEmpty())
        assertEquals("mine", existing.single { it.name == "通勤" }.id)
        assertEquals(0xFF112233, existing.single { it.name == "通勤" }.color)
    }

    @Test
    fun skipsWhenCommuteIdAlreadyExists() {
        val existing = oldInstall() + BlockType(
            "commute", "通勤", 0xFFA8A6A3, 1, archived = false, isPreset = true, isSleep = false,
        )
        val plan = TypeSeed.plan(existing)
        assertTrue(plan.insert.none { it.id == "commute" })
    }
}

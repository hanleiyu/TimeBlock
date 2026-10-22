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

    private fun leftoverCommute(sort: Int = 1) = BlockType(
        "commute", "通勤", 0xFFA8A6A3, sort, archived = false, isPreset = true, isSleep = false,
    )

    @Test
    fun emptyDatabaseGetsOtherLastAndNeverCreatesCommute() {
        val plan = TypeSeed.plan(emptyList())
        assertEquals(Presets.types.map { it.id }, plan.insert.map { it.id })
        assertEquals("other", plan.insert.last().id)
        assertEquals("其他", plan.insert.last().name)
        assertEquals(0xFFA8A6A3L, plan.insert.last().color)
        assertEquals("sport", plan.insert[plan.insert.lastIndex - 1].id)
        assertTrue(plan.insert.none { it.id == "commute" || it.name == "通勤" })
        assertEquals(emptyMap<String, Int>(), plan.sortUpdates)
    }

    @Test
    fun existingInstallInsertsOtherAfterSport() {
        val existing = oldInstall()
        val plan = TypeSeed.plan(existing)
        assertEquals(listOf("other"), plan.insert.map { it.id })
        assertEquals(8, plan.insert.single().sortOrder)
        assertEquals("写作", existing.single { it.id == "custom-1" }.name)
        assertEquals(0xFFC4B5E3, existing.single { it.id == "custom-1" }.color)
        assertEquals(9, plan.sortUpdates.getValue("custom-1"))
        assertTrue("eat" !in plan.sortUpdates)
        assertTrue(plan.insert.none { it.name == "通勤" })
    }

    @Test
    fun skipsWhenOtherNameAlreadyExists() {
        val existing = oldInstall() + BlockType(
            "mine", "其他", 0xFF112233, 9, archived = false, isPreset = false, isSleep = false,
        )
        val plan = TypeSeed.plan(existing)
        assertTrue(plan.insert.isEmpty())
        assertTrue(plan.sortUpdates.isEmpty())
        assertEquals("mine", existing.single { it.name == "其他" }.id)
        assertEquals(0xFF112233, existing.single { it.name == "其他" }.color)
    }

    @Test
    fun unusedSeededCommuteIsRenamedToOther() {
        val existing = oldInstall() + leftoverCommute()
        val plan = TypeSeed.plan(existing, usedTypeIds = emptySet())
        assertEquals(listOf("commute"), plan.retireIds)
        assertEquals(listOf("other"), plan.insert.map { it.id })
        assertEquals("其他", plan.insert.single().name)
        assertTrue(plan.insert.none { it.name == "通勤" })
    }

    @Test
    fun usedCommuteIsKeptAndOtherIsStillAdded() {
        val existing = oldInstall() + leftoverCommute()
        val plan = TypeSeed.plan(existing, usedTypeIds = setOf("commute"))
        assertTrue(plan.retireIds.isEmpty())
        assertEquals(listOf("other"), plan.insert.map { it.id })
        assertEquals("通勤", existing.single { it.id == "commute" }.name)
    }

    @Test
    fun doesNotResetCustomizedPresetColor() {
        val existing = oldInstall().map {
            if (it.id == "work") it.copy(color = 0xFF336699L) else it
        }
        val plan = TypeSeed.plan(existing)
        assertTrue(plan.recolors.isEmpty())
        assertEquals(0xFF336699L, existing.single { it.id == "work" }.color)
    }

    @Test
    fun unusedCommuteIsRetiredWhenOtherNameExists() {
        val existing = oldInstall() + leftoverCommute() + BlockType(
            "mine", "其他", 0xFF112233, 9, archived = false, isPreset = false, isSleep = false,
        )
        val plan = TypeSeed.plan(existing, usedTypeIds = emptySet())
        assertEquals(listOf("commute"), plan.retireIds)
        assertTrue(plan.insert.isEmpty())
    }
}

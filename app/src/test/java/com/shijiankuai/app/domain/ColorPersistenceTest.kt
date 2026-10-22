package com.shijiankuai.app.domain

import com.shijiankuai.app.data.Backup
import com.shijiankuai.app.data.BlockRow
import com.shijiankuai.app.data.KvRow
import com.shijiankuai.app.data.SleepRow
import com.shijiankuai.app.data.TypeRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class ColorPersistenceTest {
    private val custom = 0xFF336699L

    @Test
    fun seedLeavesEditedPresetColorAloneOnEveryHydrate() {
        val existing = Presets.types.map {
            if (it.id == "work") it.copy(color = custom) else it
        }
        repeat(2) {
            val plan = TypeSeed.plan(existing)
            assertTrue(plan.recolors.isEmpty())
            assertTrue(plan.insert.none { it.id == "work" })
        }
        assertEquals(custom, existing.single { it.id == "work" }.color)
    }

    @Test
    fun backupJsonKeepsCanonicalCustomColor() {
        val types = listOf(
            TypeRow("work", "工作", custom, 0, archived = false, isPreset = true, isSleep = false),
            TypeRow("sleep", "睡觉", 0xFF808BD0L, 1, archived = false, isPreset = true, isSleep = true),
        )
        val snap = Backup.tryParse(
            Backup.exportJson(
                types,
                listOf(BlockRow(20_000L, 4, "work")),
                listOf(SleepRow(1, 1_000L, 2_000L)),
                listOf(KvRow("is_asleep", "0")),
            ),
        )!!
        assertEquals(custom, snap.types.single { it.id == "work" }.color)
        assertEquals(0xFF808BD0L, snap.types.single { it.id == "sleep" }.color)
    }

    @Test
    fun statsPieUsesStoredTypeColor() {
        val zone = ZoneId.of("Asia/Shanghai")
        val day = LocalDate.of(2026, 10, 21)
        val now = LocalDateTime.of(2026, 10, 21, 14, 20).atZone(zone).toInstant()
        val types = Presets.types.map {
            if (it.id == "work") it.copy(color = custom) else it
        }
        val blocks = (0 until 10).associate { BlockId(day.toEpochDay(), it) to "work" }
        val stats = StatsCalc.aggregate(PeriodKind.DAY, day, now, zone, blocks, types)
        assertEquals(custom, stats.byType.single { it.typeId == "work" }.color)
    }

    @Test
    fun roomSignExtendedColorSurvivesPickerNormalize() {
        val room = 0xFF8EBFD2.toLong()
        val saved = ColorMath.normalize(room)
        assertEquals(0xFF8EBFD2L, saved)
        val types = Presets.types.map {
            if (it.id == "work") it.copy(color = saved) else it
        }
        val plan = TypeSeed.plan(types)
        assertTrue(plan.recolors.isEmpty())
        assertEquals(saved, types.single { it.id == "work" }.color)
    }
}

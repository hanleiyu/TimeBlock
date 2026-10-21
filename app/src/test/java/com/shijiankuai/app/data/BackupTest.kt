package com.shijiankuai.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class BackupTest {
    private val types = listOf(
        TypeRow("sleep", "睡觉", 0xFF545D72, 0, archived = false, isPreset = true, isSleep = true),
        TypeRow("work", "工作", 0xFF7A8B99, 1, archived = false, isPreset = true, isSleep = false),
    )
    private val blocks = listOf(BlockRow(20_000L, 4, "sleep"), BlockRow(20_000L, 42, "work"))
    private val sleeps = listOf(SleepRow(1, 1_000L, 2_000L))
    private val kv = listOf(KvRow("is_asleep", "0"))

    @Test
    fun jsonRoundtripPreservesRows() {
        val json = Backup.exportJson(types, blocks, sleeps, kv)
        val snap = Backup.tryParse(json)!!
        assertEquals(types, snap.types)
        assertEquals(blocks, snap.blocks)
        assertEquals(1, snap.sleeps.size)
        assertEquals(1_000L, snap.sleeps[0].startedAtMillis)
        assertEquals(2_000L, snap.sleeps[0].endedAtMillis)
        assertEquals(kv, snap.kv)
    }

    @Test
    fun parseAcceptsMissingSleepsAndKv() {
        val json = Backup.exportJson(types, blocks, sleeps, kv)
        val slim = org.json.JSONObject(json).apply {
            remove("sleeps")
            remove("kv")
        }.toString()
        val snap = Backup.tryParse(slim)!!
        assertTrue(snap.sleeps.isEmpty())
        assertTrue(snap.kv.isEmpty())
        assertEquals(2, snap.types.size)
    }

    @Test
    fun tryParseRejectsGarbage() {
        assertNull(Backup.tryParse("{not-json"))
        assertNull(Backup.tryParse("{}"))
        assertNull(Backup.tryParse(""))
    }

    @Test
    fun csvHasHeaderAndEpochDayAsIsoDate() {
        val csv = Backup.csv(blocks, types)
        val lines = csv.trim().lines()
        val date = LocalDate.ofEpochDay(20_000)
        assertEquals("date,index,type_id,type_name", lines[0])
        assertEquals("$date,4,sleep,睡觉", lines[1])
        assertEquals("$date,42,work,工作", lines[2])
    }
}

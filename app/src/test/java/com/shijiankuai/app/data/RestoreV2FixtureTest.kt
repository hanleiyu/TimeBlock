package com.shijiankuai.app.data

import com.shijiankuai.app.domain.LifeMonth
import com.shijiankuai.app.exercise.data.toDomain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RestoreV2FixtureTest {
    private fun fixture(): String =
        javaClass.getResource("/restore_v2_life_null_intensity.json")!!.readText()

    @Test
    fun sanitizedPhoneExportParsesLifeOriginEventsAndNullIntensity() {
        val snap = Backup.parse(fixture())
        assertTrue(snap.applyTimeblock)
        assertTrue(snap.applyExercise)
        assertEquals("1998-06", snap.kv.single { it.key == "life_origin" }.value)
        assertEquals(12, snap.lifeEvents.size)
        snap.lifeEvents.forEach { row ->
            LifeMonth(row.startYear, row.startMonth)
            LifeMonth(row.endYear, row.endMonth)
            assertTrue(row.startMonth in 1..12)
            assertTrue(row.endMonth in 1..12)
        }
        val workouts = snap.exercise!!.workouts
        assertEquals(66, workouts.size)
        assertEquals(57, workouts.count { it.intensity == null })
        assertEquals(0, workouts.count { it.intensity == "null" })
        assertNull(snap.exercise.settings.pendingAdvanceMessage)
        workouts.forEach { it.toDomain() }
        assertNotNull(Backup.tryParse(fixture()))
    }

    @Test
    fun originalUneditedExportParsesTheSameFailingShape() {
        val path = listOf(
            "/home/ubuntu/.cursor/projects/agent-repos-TimeBlock/uploads/___-__-_______afbd.txt",
            "/workspace/时间块-备份-恢复用原文件.txt",
        ).map { java.io.File(it) }.firstOrNull { it.isFile } ?: return
        val snap = Backup.parse(path.readText())
        assertEquals("1998-06", snap.kv.single { it.key == "life_origin" }.value)
        assertEquals(12, snap.lifeEvents.size)
        val workouts = snap.exercise!!.workouts
        assertEquals(66, workouts.size)
        assertEquals(57, workouts.count { it.intensity == null })
        workouts.forEach { it.toDomain() }
        snap.lifeEvents.forEach { LifeMonth(it.startYear, it.startMonth) }
    }
}

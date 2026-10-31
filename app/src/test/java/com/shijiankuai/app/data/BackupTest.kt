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
    private val todos = listOf(
        TodoRow("t_1", 20_000L, "work", "回邮件", done = true, sortOrder = 0),
        TodoRow("t_2", 20_001L, "life", "倒垃圾", done = false, sortOrder = 1),
    )

    @Test
    fun jsonRoundtripKeepsCustomTypeColor() {
        val custom = types.map {
            if (it.id == "work") it.copy(color = 0xFF336699) else it
        }
        val snap = Backup.tryParse(Backup.exportJson(custom, blocks, sleeps, kv, todos))!!
        assertEquals(0xFF336699, snap.types.single { it.id == "work" }.color)
    }

    @Test
    fun jsonRoundtripPreservesRows() {
        val json = Backup.exportJson(types, blocks, sleeps, kv, todos)
        val snap = Backup.tryParse(json)!!
        assertEquals(types, snap.types)
        assertEquals(blocks, snap.blocks)
        assertEquals(1, snap.sleeps.size)
        assertEquals(1_000L, snap.sleeps[0].startedAtMillis)
        assertEquals(2_000L, snap.sleeps[0].endedAtMillis)
        assertEquals(kv, snap.kv)
        assertEquals(todos, snap.todos)
        assertEquals(2, org.json.JSONObject(json).getInt("version"))
        assertTrue(org.json.JSONObject(json).has("exercise"))
        assertTrue(snap.applyTimeblock)
        assertTrue(snap.applyExercise)
        assertTrue(snap.exercise != null)
    }

    @Test
    fun v2ExportNestsExerciseWithSettingsAndWorkouts() {
        val exercise = org.json.JSONObject()
            .put("exportedAt", "2026-10-31")
            .put(
                "settings",
                org.json.JSONObject()
                    .put("targetMode", "PLAN")
                    .put("fixedAerobic", 100)
                    .put("fixedStrength", 60)
                    .put("balanceGoal", 2)
                    .put("stage", "STAGE_1")
                    .put("consecutiveMetWeeks", 0)
                    .put("lastEvaluatedEpochDay", -1)
                    .put("weeksSpentJson", """{"STAGE_1":0,"STAGE_2":0,"STAGE_3":0,"MAINTAIN":0}""")
                    .put("pendingAdvanceMessage", org.json.JSONObject.NULL)
                    .put("stage1Aerobic", 60)
                    .put("stage1Strength", 40)
                    .put("stage2Aerobic", 80)
                    .put("stage2Strength", 50)
                    .put("stage3Aerobic", 100)
                    .put("stage3Strength", 60)
                    .put("weeksToAdvance", 2),
            )
            .put(
                "workouts",
                org.json.JSONArray().put(
                    org.json.JSONObject()
                        .put("category", "AEROBIC")
                        .put("activityName", "慢跑")
                        .put("durationMinutes", 30)
                        .put("intensity", "MID")
                        .put("date", "2026-10-30")
                        .put("note", org.json.JSONObject.NULL)
                        .put("isFree", false),
                ),
            )
            .put("activities", org.json.JSONArray())
            .put("metrics", org.json.JSONArray())
            .put("tests", org.json.JSONArray())
        val json = Backup.exportJson(types, blocks, sleeps, kv, todos, exercise)
        val snap = Backup.tryParse(json)!!
        assertTrue(snap.applyTimeblock)
        assertTrue(snap.applyExercise)
        assertEquals(1, snap.exercise!!.workouts.size)
        assertEquals("AEROBIC", snap.exercise.workouts[0].category)
        assertEquals("慢跑", snap.exercise.workouts[0].activityName)
        assertEquals("PLAN", snap.exercise.settings.targetMode)
    }

    @Test
    fun v1TimeblockDoesNotTouchExercise() {
        val legacy = org.json.JSONObject()
            .put("version", 1)
            .put("types", org.json.JSONArray())
            .put("blocks", org.json.JSONArray())
            .put("sleeps", org.json.JSONArray())
            .put("kv", org.json.JSONArray())
            .put("todos", org.json.JSONArray())
            .toString()
        val snap = Backup.tryParse(legacy)!!
        assertTrue(snap.applyTimeblock)
        assertTrue(!snap.applyExercise)
        assertEquals(null, snap.exercise)
    }

    @Test
    fun legacyExerciseBackupOnlyAppliesExercise() {
        val old = org.json.JSONObject()
            .put("version", 1)
            .put("exportedAt", "2026-10-01")
            .put(
                "settings",
                org.json.JSONObject()
                    .put("targetMode", "FIXED")
                    .put("fixedAerobic", 90)
                    .put("fixedStrength", 55)
                    .put("balanceGoal", 1)
                    .put("stage", "STAGE_2")
                    .put("consecutiveMetWeeks", 1),
            )
            .put(
                "workouts",
                org.json.JSONArray().put(
                    org.json.JSONObject()
                        .put("category", "STRENGTH")
                        .put("activityName", "深蹲")
                        .put("durationMinutes", 25)
                        .put("intensity", org.json.JSONObject.NULL)
                        .put("date", "2026-09-30")
                        .put("note", "")
                        .put("isFree", true),
                ),
            )
            .put("activities", org.json.JSONArray())
            .put("metrics", org.json.JSONArray())
            .put("tests", org.json.JSONArray())
            .toString()
        val snap = Backup.tryParse(old)!!
        assertTrue(!snap.applyTimeblock)
        assertTrue(snap.applyExercise)
        assertTrue(snap.types.isEmpty())
        assertEquals("FIXED", snap.exercise!!.settings.targetMode)
        assertEquals(1, snap.exercise.workouts.size)
        assertEquals("深蹲", snap.exercise.workouts[0].activityName)
        assertTrue(snap.exercise.workouts[0].isFree)
    }

    @Test
    fun parseAcceptsMissingSleepsKvAndTodos() {
        val json = Backup.exportJson(types, blocks, sleeps, kv, todos)
        val slim = org.json.JSONObject(json).apply {
            remove("sleeps")
            remove("kv")
            remove("todos")
        }.toString()
        val snap = Backup.tryParse(slim)!!
        assertTrue(snap.sleeps.isEmpty())
        assertTrue(snap.kv.isEmpty())
        assertTrue(snap.todos.isEmpty())
        assertEquals(2, snap.types.size)
    }

    @Test
    fun parseAcceptsLegacyBackupWithoutTodosKey() {
        val legacy = org.json.JSONObject()
            .put("version", 1)
            .put("types", org.json.JSONArray())
            .put("blocks", org.json.JSONArray())
            .put("sleeps", org.json.JSONArray())
            .put("kv", org.json.JSONArray())
            .toString()
        val snap = Backup.tryParse(legacy)!!
        assertTrue(snap.todos.isEmpty())
    }

    @Test
    fun jsonRoundtripPreservesTodoRecurrence() {
        val recurring = listOf(
            TodoRow(
                "t_w", 20_000L, "work", "站会", done = false, sortOrder = 0,
                recurKind = "weekly", recurMask = 31, doneDays = "20000,20007", skipDays = "20001",
            ),
            TodoRow(
                "t_m", 20_000L, "life", "交房租", done = false, sortOrder = 1,
                recurKind = "monthly", recurMask = 1, doneDays = "20000", skipDays = "",
            ),
        )
        val snap = Backup.tryParse(Backup.exportJson(types, blocks, sleeps, kv, recurring))!!
        assertEquals(recurring, snap.todos)
        val first = org.json.JSONObject(Backup.exportJson(types, blocks, sleeps, kv, recurring))
            .getJSONArray("todos").getJSONObject(0)
        assertEquals("weekly", first.getString("recurKind"))
        assertEquals(31, first.getInt("recurMask"))
        assertEquals("20000,20007", first.getString("doneDays"))
        assertEquals("20001", first.getString("skipDays"))
    }

    @Test
    fun parseTodoRecurrenceDefaultsWhenMissing() {
        val json = org.json.JSONObject()
            .put("version", 2)
            .put("types", org.json.JSONArray())
            .put("blocks", org.json.JSONArray())
            .put("sleeps", org.json.JSONArray())
            .put("kv", org.json.JSONArray())
            .put(
                "todos",
                org.json.JSONArray().put(
                    org.json.JSONObject()
                        .put("id", "t_old")
                        .put("epochDay", 20_000L)
                        .put("category", "work")
                        .put("title", "旧任务")
                        .put("done", false)
                        .put("sortOrder", 0),
                ),
            )
            .toString()
        val snap = Backup.tryParse(json)!!
        val row = snap.todos.single()
        assertEquals("none", row.recurKind)
        assertEquals(0, row.recurMask)
        assertEquals("", row.doneDays)
        assertEquals("", row.skipDays)
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

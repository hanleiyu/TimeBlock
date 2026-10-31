package com.shijiankuai.app.exercise.data

import com.shijiankuai.app.exercise.domain.AppSettings
import com.shijiankuai.app.exercise.domain.toIso
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

object ExerciseBackup {
    fun toObject(
        settings: AppSettings,
        workouts: List<com.shijiankuai.app.exercise.domain.Workout>,
        activities: List<com.shijiankuai.app.exercise.domain.ActivityItem>,
        metrics: List<com.shijiankuai.app.exercise.domain.Metric>,
        tests: List<com.shijiankuai.app.exercise.domain.MetricTest>,
        exportedAt: String = LocalDate.now().toIso(),
    ): JSONObject {
        val root = JSONObject()
        root.put("exportedAt", exportedAt)
        root.put("settings", JSONObject().apply {
            put("targetMode", settings.targetMode.name)
            put("fixedAerobic", settings.fixedAerobic)
            put("fixedStrength", settings.fixedStrength)
            put("balanceGoal", settings.balanceGoal)
            put("stage", settings.plan.stage.name)
            put("consecutiveMetWeeks", settings.plan.consecutiveMetWeeks)
            put("lastEvaluatedEpochDay", settings.plan.lastEvaluatedWeekStart?.toEpochDay() ?: -1L)
            put("weeksSpentJson", encodeWeeksSpent(settings.plan.weeksSpent))
            put("pendingAdvanceMessage", settings.plan.pendingAdvanceMessage ?: JSONObject.NULL)
            put("stage1Aerobic", settings.stageTargets.stage1Aerobic)
            put("stage1Strength", settings.stageTargets.stage1Strength)
            put("stage2Aerobic", settings.stageTargets.stage2Aerobic)
            put("stage2Strength", settings.stageTargets.stage2Strength)
            put("stage3Aerobic", settings.stageTargets.stage3Aerobic)
            put("stage3Strength", settings.stageTargets.stage3Strength)
            put("weeksToAdvance", settings.weeksToAdvance)
        })
        root.put("workouts", JSONArray().apply {
            workouts.forEach { w ->
                put(JSONObject().apply {
                    put("category", w.category.name)
                    put("activityName", w.activityName)
                    put("durationMinutes", w.durationMinutes)
                    put("intensity", w.intensity?.name ?: JSONObject.NULL)
                    put("date", w.date.toIso())
                    put("note", w.note ?: JSONObject.NULL)
                    put("isFree", w.isFree)
                })
            }
        })
        root.put("activities", JSONArray().apply {
            activities.forEach { a ->
                put(JSONObject().apply {
                    put("category", a.category.name)
                    put("name", a.name)
                    put("isPreset", a.isPreset)
                    put("hidden", a.hidden)
                })
            }
        })
        root.put("metrics", JSONArray().apply {
            metrics.forEach { m ->
                put(JSONObject().apply {
                    put("id", m.id)
                    put("name", m.name)
                    put("unit", m.unit)
                    put("higherIsBetter", m.higherIsBetter)
                })
            }
        })
        root.put("tests", JSONArray().apply {
            tests.forEach { t ->
                put(JSONObject().apply {
                    put("metricId", t.metricId)
                    put("date", t.date.toIso())
                    put("value", t.value)
                })
            }
        })
        return root
    }

    fun emptyObject(exportedAt: String = LocalDate.now().toIso()): JSONObject =
        toObject(
            settings = AppSettings(),
            workouts = emptyList(),
            activities = emptyList(),
            metrics = emptyList(),
            tests = emptyList(),
            exportedAt = exportedAt,
        )

    fun fromJson(root: JSONObject): ExercisePayload {
        val s = root.getJSONObject("settings")
        val settings = SettingsEntity(
            targetMode = s.getString("targetMode"),
            fixedAerobic = s.getInt("fixedAerobic"),
            fixedStrength = s.getInt("fixedStrength"),
            balanceGoal = s.getInt("balanceGoal"),
            stage = s.getString("stage"),
            consecutiveMetWeeks = s.getInt("consecutiveMetWeeks"),
            lastEvaluatedEpochDay = s.optLong("lastEvaluatedEpochDay", -1L),
            weeksSpentJson = s.optString(
                "weeksSpentJson",
                """{"STAGE_1":0,"STAGE_2":0,"STAGE_3":0,"MAINTAIN":0}""",
            ),
            pendingAdvanceMessage = s.optString("pendingAdvanceMessage").ifBlank { null },
            stage1Aerobic = s.optInt("stage1Aerobic", 60),
            stage1Strength = s.optInt("stage1Strength", 40),
            stage2Aerobic = s.optInt("stage2Aerobic", 80),
            stage2Strength = s.optInt("stage2Strength", 50),
            stage3Aerobic = s.optInt("stage3Aerobic", 100),
            stage3Strength = s.optInt("stage3Strength", 60),
            weeksToAdvance = s.optInt("weeksToAdvance", 2).coerceAtLeast(1),
        )
        val workouts = root.optJSONArray("workouts")?.mapObjects { o ->
            WorkoutEntity(
                category = o.getString("category"),
                activityName = o.getString("activityName"),
                durationMinutes = o.getInt("durationMinutes"),
                intensity = o.optString("intensity").ifBlank { null },
                dateEpochDay = LocalDate.parse(o.getString("date")).toEpochDay(),
                note = o.optString("note").ifBlank { null },
                createdAt = System.currentTimeMillis(),
                isFree = o.optBoolean("isFree", false),
            )
        } ?: emptyList()
        val activities = root.optJSONArray("activities")?.mapObjects { o ->
            ActivityEntity(
                category = o.getString("category"),
                name = o.getString("name"),
                isPreset = o.getBoolean("isPreset"),
                hidden = o.getBoolean("hidden"),
            )
        } ?: emptyList()
        val oldMetrics = root.optJSONArray("metrics")?.mapObjects { o ->
            o.optLong("id") to MetricEntity(
                name = o.getString("name"),
                unit = o.getString("unit"),
                higherIsBetter = o.getBoolean("higherIsBetter"),
            )
        } ?: emptyList()
        val testsByOldMetric = root.optJSONArray("tests")?.mapObjects { o ->
            o.getLong("metricId") to MetricTestEntity(
                metricId = 0,
                dateEpochDay = LocalDate.parse(o.getString("date")).toEpochDay(),
                value = o.getDouble("value"),
            )
        } ?: emptyList()
        return ExercisePayload(settings, workouts, activities, oldMetrics, testsByOldMetric)
    }

    fun looksLikeLegacyRoot(root: JSONObject): Boolean =
        !root.has("types") && root.has("workouts") && root.has("settings")

    private fun <T> JSONArray.mapObjects(block: (JSONObject) -> T): List<T> {
        return (0 until length()).map { block(getJSONObject(it)) }
    }
}

data class ExercisePayload(
    val settings: SettingsEntity,
    val workouts: List<WorkoutEntity>,
    val activities: List<ActivityEntity>,
    val metricsWithOldId: List<Pair<Long, MetricEntity>>,
    val testsByOldMetricId: List<Pair<Long, MetricTestEntity>>,
)

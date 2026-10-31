package com.shijiankuai.app.exercise.data

import android.content.Context
import androidx.room.withTransaction
import com.shijiankuai.app.exercise.domain.ActivityItem
import com.shijiankuai.app.exercise.domain.AppSettings
import com.shijiankuai.app.exercise.domain.Category
import com.shijiankuai.app.exercise.domain.Metric
import com.shijiankuai.app.exercise.domain.MetricTest
import com.shijiankuai.app.exercise.domain.PlanEngine
import com.shijiankuai.app.exercise.domain.Presets
import com.shijiankuai.app.exercise.domain.TargetMode
import com.shijiankuai.app.exercise.domain.Workout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class ExerciseRepository(private val db: ExerciseDatabase) {

    val workouts: Flow<List<Workout>> = db.workouts().observeAll().map { list -> list.map { it.toDomain() } }
    val activities: Flow<List<ActivityItem>> = db.activities().observeAll().map { list -> list.map { it.toDomain() } }
    val metrics: Flow<List<Metric>> = db.metrics().observeAll().map { list -> list.map { it.toDomain() } }
    val tests: Flow<List<MetricTest>> = db.tests().observeAll().map { list -> list.map { it.toDomain() } }
    val settings: Flow<AppSettings> = db.settings().observe().map { it?.toDomain() ?: AppSettings() }

    suspend fun seedIfNeeded() {
        if (db.settings().get() == null) {
            db.settings().upsert(AppSettings().toEntity())
        }
        if (db.metrics().count() == 0) {
            db.metrics().insertAll(Presets.metrics.map { it.toEntity() })
        }
    }

    suspend fun settingsOnce(): AppSettings = db.settings().get()?.toDomain() ?: AppSettings()

    suspend fun workoutsOnce(): List<Workout> = db.workouts().getAll().map { it.toDomain() }

    suspend fun activitiesOnce(): List<ActivityItem> = db.activities().getAll().map { it.toDomain() }

    suspend fun metricsOnce(): List<Metric> = db.metrics().getAll().map { it.toDomain() }

    suspend fun testsOnce(): List<MetricTest> = db.tests().getAll().map { it.toDomain() }

    suspend fun evaluatePlan(today: LocalDate = LocalDate.now()) {
        val settings = settingsOnce()
        val updated = PlanEngine.evaluate(
            state = settings.plan,
            today = today,
            mode = settings.targetMode,
            workouts = workoutsOnce(),
            targets = settings.stageTargets,
            weeksToAdvance = settings.weeksToAdvance,
        )
        if (updated != settings.plan) {
            db.settings().upsert(settings.copy(plan = updated).toEntity())
        }
    }

    suspend fun clearAdvanceMessage() {
        val settings = settingsOnce()
        if (settings.plan.pendingAdvanceMessage != null) {
            db.settings().upsert(
                settings.copy(plan = settings.plan.copy(pendingAdvanceMessage = null)).toEntity(),
            )
        }
    }

    suspend fun saveWorkout(workout: Workout) {
        if (workout.id == 0L) {
            db.workouts().insert(workout.toEntity())
        } else {
            val existing = db.workouts().get(workout.id)
            db.workouts().update(workout.toEntity(createdAt = existing?.createdAt ?: System.currentTimeMillis()))
        }
        evaluatePlan()
    }

    suspend fun deleteWorkout(id: Long) {
        db.workouts().delete(id)
        evaluatePlan()
    }

    suspend fun getWorkout(id: Long): Workout? = db.workouts().get(id)?.toDomain()

    suspend fun addCustomActivity(category: Category, name: String): Long {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty())
        val existing = db.activities().getAll().firstOrNull { it.category == category.name && it.name == trimmed }
        if (existing != null) {
            if (existing.hidden && !existing.isPreset) {
                db.activities().update(existing.copy(hidden = false))
            }
            return existing.id
        }
        return db.activities().insert(
            ActivityEntity(category = category.name, name = trimmed, isPreset = false, hidden = false),
        )
    }

    suspend fun addMetric(name: String, unit: String, higherIsBetter: Boolean): Long {
        return db.metrics().insert(
            MetricEntity(name = name.trim(), unit = unit.trim(), higherIsBetter = higherIsBetter),
        )
    }

    suspend fun updateMetric(metric: Metric) {
        db.metrics().update(metric.toEntity())
    }

    suspend fun deleteMetric(id: Long) {
        db.tests().deleteForMetric(id)
        db.metrics().delete(id)
    }

    suspend fun addTest(metricId: Long, date: LocalDate, value: Double): Long {
        return db.tests().insert(
            MetricTestEntity(metricId = metricId, dateEpochDay = date.toEpochDay(), value = value),
        )
    }

    suspend fun deleteTest(id: Long) {
        db.tests().delete(id)
    }

    suspend fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val current = settingsOnce()
        db.settings().upsert(transform(current).toEntity())
    }

    suspend fun setTargetMode(mode: TargetMode) {
        updateSettings { current ->
            val plan = if (mode == TargetMode.PLAN && current.targetMode == TargetMode.FIXED) {
                PlanEngine.freezeEvaluationAt(LocalDate.now(), current.plan)
            } else current.plan
            current.copy(targetMode = mode, plan = plan)
        }
        evaluatePlan()
    }

    suspend fun exportObject() = ExerciseBackup.toObject(
        settings = settingsOnce(),
        workouts = workoutsOnce(),
        activities = activitiesOnce(),
        metrics = metricsOnce(),
        tests = testsOnce(),
    )

    suspend fun restorePayload(payload: ExercisePayload) {
        db.withTransaction {
            db.workouts().deleteAll()
            db.activities().deleteAll()
            db.tests().deleteAll()
            db.metrics().deleteAll()
            db.settings().upsert(payload.settings)
            if (payload.workouts.isNotEmpty()) db.workouts().insertAll(payload.workouts)
            if (payload.activities.isNotEmpty()) db.activities().insertAll(payload.activities)
            val idMap = mutableMapOf<Long, Long>()
            payload.metricsWithOldId.forEach { (oldId, entity) ->
                val newId = db.metrics().insert(entity)
                idMap[oldId] = newId
            }
            val remapped = payload.testsByOldMetricId.mapNotNull { (oldMetricId, test) ->
                val newId = idMap[oldMetricId] ?: return@mapNotNull null
                test.copy(metricId = newId)
            }
            if (remapped.isNotEmpty()) db.tests().insertAll(remapped)
        }
        seedIfNeeded()
        evaluatePlan()
    }

    companion object {
        fun create(context: Context): ExerciseRepository =
            ExerciseRepository(ExerciseDatabase.create(context.applicationContext))
    }
}

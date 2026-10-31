package com.shijiankuai.app.data

import com.shijiankuai.app.exercise.data.ExerciseRepository

class BackupOps(
    private val repo: Repository,
    private val exercise: ExerciseRepository,
) {
    suspend fun export(): String = repo.exportBackup(exercise.exportObject())

    suspend fun restore(json: String): Boolean {
        val snap = Backup.tryParse(json) ?: return false
        if (snap.applyTimeblock) {
            repo.restoreTimeblock(snap)
        }
        if (snap.applyExercise) {
            snap.exercise?.let { exercise.restorePayload(it) }
        }
        return true
    }
}

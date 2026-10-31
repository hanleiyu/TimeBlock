package com.shijiankuai.app.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.shijiankuai.app.TimeBlockApp

class DailyBackupWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val container = (applicationContext as TimeBlockApp).container
        val repo = container.repo
        repo.hydrate()
        container.exercise.seedIfNeeded()
        if (!repo.autoBackup.value) {
            return Result.success()
        }
        return try {
            val json = container.backup.export()
            DownloadsBackupWriter(applicationContext).write(json)
            repo.markAutoBackupDone(repo.clock.now())
            Result.success()
        } catch (_: Exception) {
            if (runAttemptCount >= MAX_ATTEMPTS) Result.success() else Result.retry()
        }
    }

    companion object {
        private const val MAX_ATTEMPTS = 3
    }
}

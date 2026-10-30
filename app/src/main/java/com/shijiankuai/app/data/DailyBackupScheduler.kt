package com.shijiankuai.app.data

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.shijiankuai.app.domain.DailyBackup
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

object DailyBackupScheduler {
    fun ensure(context: Context, now: ZonedDateTime = ZonedDateTime.now()) {
        enqueue(context, now, ExistingPeriodicWorkPolicy.KEEP)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(DailyBackup.WORK_NAME)
    }

    fun setEnabled(context: Context, on: Boolean) {
        if (on) ensure(context) else cancel(context)
    }

    private fun enqueue(
        context: Context,
        now: ZonedDateTime,
        policy: ExistingPeriodicWorkPolicy,
    ) {
        val delay = DailyBackup.delayUntilNextMidnight(now).toMillis().coerceAtLeast(1L)
        val req = PeriodicWorkRequestBuilder<DailyBackupWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS,
            )
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(DailyBackup.WORK_NAME, policy, req)
    }
}

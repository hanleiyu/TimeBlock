package com.shijiankuai.app

import android.content.Context
import com.shijiankuai.app.data.AppDatabase
import com.shijiankuai.app.data.BackupOps
import com.shijiankuai.app.data.DailyBackupScheduler
import com.shijiankuai.app.data.Repository
import com.shijiankuai.app.domain.AppClock
import com.shijiankuai.app.exercise.data.ExerciseDatabase
import com.shijiankuai.app.exercise.data.ExerciseRepository
import java.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppContainer(context: Context) {
    val db: AppDatabase = AppDatabase.create(context)
    val exerciseDb: ExerciseDatabase = ExerciseDatabase.create(context)
    val clock = AppClock { Instant.now() }
    val repo: Repository = Repository(db, clock)
    val exercise: ExerciseRepository = ExerciseRepository(exerciseDb)
    val backup: BackupOps = BackupOps(repo, exercise)
}

class TimeBlockApp : android.app.Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            container.repo.hydrate()
            container.exercise.seedIfNeeded()
            container.exercise.evaluatePlan()
            DailyBackupScheduler.setEnabled(this@TimeBlockApp, container.repo.autoBackup.value)
        }
    }
}

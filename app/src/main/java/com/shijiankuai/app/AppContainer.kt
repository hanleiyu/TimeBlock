package com.shijiankuai.app

import android.content.Context
import com.shijiankuai.app.data.AppDatabase
import com.shijiankuai.app.data.Repository
import com.shijiankuai.app.domain.AppClock
import java.time.Instant

class AppContainer(context: Context) {
    val db: AppDatabase = AppDatabase.create(context)
    val clock = AppClock { Instant.now() }
    val repo: Repository = Repository(db, clock)
}

class TimeBlockApp : android.app.Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

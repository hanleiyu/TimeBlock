package com.shijiankuai.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.shijiankuai.app.domain.Presets

@Database(
    entities = [TypeRow::class, BlockRow::class, SleepRow::class, KvRow::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun types(): TypeDao
    abstract fun blocks(): BlockDao
    abstract fun sleeps(): SleepDao
    abstract fun kv(): KvDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "shijiankuai.db")
                .fallbackToDestructiveMigration()
                .build()

        suspend fun seedIfNeeded(db: AppDatabase) {
            db.types().insertIgnore(
                Presets.types.map {
                    TypeRow(
                        id = it.id,
                        name = it.name,
                        color = it.color,
                        sortOrder = it.sortOrder,
                        archived = it.archived,
                        isPreset = it.isPreset,
                        isSleep = it.isSleep,
                    )
                },
            )
            val next = Presets.types.associateBy { it.id }
            db.types().all().forEach { row ->
                val preset = next[row.id] ?: return@forEach
                if (row.isPreset && row.color != preset.color) {
                    db.types().update(row.copy(color = preset.color))
                }
            }
            if (db.kv().get("is_asleep") == null) {
                db.kv().put(KvRow("is_asleep", "1"))
            }
        }
    }
}

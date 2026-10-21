package com.shijiankuai.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.shijiankuai.app.domain.BlockType
import com.shijiankuai.app.domain.TypeSeed

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
            val existing = db.types().all()
            val plan = TypeSeed.plan(existing.map { it.toSeedType() })
            val byId = existing.associateBy { it.id }
            plan.sortUpdates.forEach { (id, sort) ->
                byId[id]?.let { db.types().update(it.copy(sortOrder = sort)) }
            }
            plan.recolors.forEach { type ->
                val row = byId[type.id] ?: return@forEach
                db.types().update(
                    row.copy(
                        color = type.color,
                        sortOrder = plan.sortUpdates[type.id] ?: row.sortOrder,
                    ),
                )
            }
            if (plan.insert.isNotEmpty()) {
                db.types().insertIgnore(plan.insert.map { it.toRow() })
            }
            if (db.kv().get("is_asleep") == null) {
                db.kv().put(KvRow("is_asleep", "1"))
            }
        }

        private fun TypeRow.toSeedType() = BlockType(
            id, name, color, sortOrder, archived, isPreset, isSleep,
        )

        private fun BlockType.toRow() = TypeRow(
            id, name, color, sortOrder, archived, isPreset, isSleep,
        )
    }
}

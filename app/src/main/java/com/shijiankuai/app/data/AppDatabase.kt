package com.shijiankuai.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.shijiankuai.app.domain.BlockType
import com.shijiankuai.app.domain.TypeSeed

@Database(
    entities = [TypeRow::class, BlockRow::class, SleepRow::class, KvRow::class, TodoRow::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun types(): TypeDao
    abstract fun blocks(): BlockDao
    abstract fun sleeps(): SleepDao
    abstract fun kv(): KvDao
    abstract fun todos(): TodoDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS todos (
                        id TEXT NOT NULL PRIMARY KEY,
                        epochDay INTEGER NOT NULL,
                        category TEXT NOT NULL,
                        title TEXT NOT NULL,
                        done INTEGER NOT NULL,
                        sortOrder INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_todos_epochDay ON todos(epochDay)")
            }
        }

        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "shijiankuai.db")
                .addMigrations(MIGRATION_1_2)
                .build()

        suspend fun seedIfNeeded(db: AppDatabase) {
            val existing = db.types().all()
            val used = db.blocks().usedTypeIds().toSet()
            val plan = TypeSeed.plan(existing.map { it.toSeedType() }, usedTypeIds = used)
            plan.retireIds.forEach { db.types().delete(it) }
            val byId = db.types().all().associateBy { it.id }
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

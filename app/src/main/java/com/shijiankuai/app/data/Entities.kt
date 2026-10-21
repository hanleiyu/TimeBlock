package com.shijiankuai.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "block_types")
data class TypeRow(
    @PrimaryKey val id: String,
    val name: String,
    val color: Long,
    val sortOrder: Int,
    val archived: Boolean,
    val isPreset: Boolean,
    val isSleep: Boolean,
)

@Entity(tableName = "blocks", primaryKeys = ["epochDay", "index"])
data class BlockRow(
    val epochDay: Long,
    val index: Int,
    val typeId: String,
)

@Entity(tableName = "sleep_sessions")
data class SleepRow(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAtMillis: Long,
    val endedAtMillis: Long?,
)

@Entity(tableName = "kv")
data class KvRow(
    @PrimaryKey val key: String,
    val value: String,
)

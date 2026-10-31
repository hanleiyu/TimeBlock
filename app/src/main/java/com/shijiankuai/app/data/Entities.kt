package com.shijiankuai.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shijiankuai.app.domain.TodoRecurrence

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

@Entity(tableName = "todos")
data class TodoRow(
    @PrimaryKey val id: String,
    val epochDay: Long,
    val category: String,
    val title: String,
    val done: Boolean,
    val sortOrder: Int,
    val recurKind: String = TodoRecurrence.KIND_NONE,
    val recurMask: Int = 0,
    val doneDays: String = "",
    val skipDays: String = "",
)

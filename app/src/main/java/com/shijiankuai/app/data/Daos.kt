package com.shijiankuai.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TypeDao {
    @Query("SELECT * FROM block_types ORDER BY sortOrder")
    fun observe(): Flow<List<TypeRow>>

    @Query("SELECT * FROM block_types ORDER BY sortOrder")
    suspend fun all(): List<TypeRow>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: TypeRow)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(rows: List<TypeRow>)

    @Update
    suspend fun update(row: TypeRow)

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM block_types")
    suspend fun maxSort(): Int

    @Query("DELETE FROM block_types WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM block_types")
    suspend fun clearAll()
}

@Dao
interface BlockDao {
    @Query("SELECT * FROM blocks WHERE epochDay = :day")
    fun observeDay(day: Long): Flow<List<BlockRow>>

    @Query("SELECT * FROM blocks WHERE epochDay BETWEEN :from AND :to")
    suspend fun range(from: Long, to: Long): List<BlockRow>

    @Query("SELECT * FROM blocks WHERE epochDay BETWEEN :from AND :to")
    fun observeRange(from: Long, to: Long): Flow<List<BlockRow>>

    @Query("SELECT * FROM blocks")
    suspend fun all(): List<BlockRow>

    @Query("SELECT DISTINCT typeId FROM blocks")
    suspend fun usedTypeIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: BlockRow)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(rows: List<BlockRow>)

    @Query("DELETE FROM blocks WHERE epochDay = :day AND `index` = :index")
    suspend fun clear(day: Long, index: Int)

    @Query("DELETE FROM blocks WHERE epochDay = :day AND `index` BETWEEN :from AND :to")
    suspend fun clearRange(day: Long, from: Int, to: Int)

    @Query("DELETE FROM blocks")
    suspend fun clearAll()
}

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep_sessions ORDER BY startedAtMillis DESC")
    fun observe(): Flow<List<SleepRow>>

    @Query("SELECT * FROM sleep_sessions ORDER BY startedAtMillis DESC")
    suspend fun all(): List<SleepRow>

    @Query("SELECT * FROM sleep_sessions WHERE endedAtMillis IS NULL ORDER BY startedAtMillis DESC LIMIT 1")
    suspend fun open(): SleepRow?

    @Query("SELECT * FROM sleep_sessions ORDER BY startedAtMillis DESC LIMIT 1")
    suspend fun latest(): SleepRow?

    @Insert
    suspend fun insert(row: SleepRow): Long

    @Update
    suspend fun update(row: SleepRow)

    @Query("DELETE FROM sleep_sessions")
    suspend fun clearAll()
}

@Dao
interface KvDao {
    @Query("SELECT value FROM kv WHERE `key` = :key")
    suspend fun get(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(row: KvRow)

    @Query("SELECT * FROM kv")
    suspend fun all(): List<KvRow>

    @Query("DELETE FROM kv")
    suspend fun clearAll()
}

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos WHERE epochDay = :day ORDER BY sortOrder")
    fun observeDay(day: Long): Flow<List<TodoRow>>

    @Query("SELECT * FROM todos WHERE epochDay = :day ORDER BY sortOrder")
    suspend fun day(day: Long): List<TodoRow>

    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun byId(id: String): TodoRow?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: TodoRow)

    @Update
    suspend fun update(row: TodoRow)

    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM todos WHERE epochDay = :day AND category = :category")
    suspend fun maxSort(day: Long, category: String): Int
}

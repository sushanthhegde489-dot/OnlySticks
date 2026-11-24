package com.sushanth.onlysticks.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.sushanth.onlysticks.data.local.entity.SettEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettDao {
    @Query("SELECT value FROM settings WHERE `key` = :key")
    suspend fun get(key: String): String?

    @Query("SELECT value FROM settings WHERE `key` = :key")
    fun observe(key: String): Flow<String?>

    @Upsert
    suspend fun put(entity: SettEntity)

    @Query("DELETE FROM settings WHERE `key` = :key")
    suspend fun remove(key: String)
}

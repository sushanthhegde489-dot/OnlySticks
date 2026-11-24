package com.sushanth.onlysticks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sushanth.onlysticks.data.local.entity.StoreBuyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreBuyDao {
    @Query("SELECT slot FROM store_purchases WHERE weekStart = :weekStart")
    fun observeBoughtSlots(weekStart: Long): Flow<List<Int>>

    @Query("SELECT COUNT(*) FROM store_purchases WHERE weekStart = :weekStart AND slot = :slot")
    suspend fun count(weekStart: Long, slot: Int): Int

    @Insert
    suspend fun insert(entity: StoreBuyEntity)

    @Query("SELECT * FROM store_purchases")
    suspend fun all(): List<StoreBuyEntity>

    @Insert
    suspend fun insertAll(rows: List<StoreBuyEntity>)

    @Query("DELETE FROM store_purchases")
    suspend fun deleteAll()
}

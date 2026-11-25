// Database access for weekly store purchases.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sushanth.onlysticks.data.local.entity.StoreBuyEntity
import kotlinx.coroutines.flow.Flow

/** Records which store slots were bought in which week, so each offer can only be bought once. */
@Dao
interface StoreBuyDao {
    /** Live list of slots already bought in the week starting at [weekStart]. */
    @Query("SELECT slot FROM store_purchases WHERE weekStart = :weekStart")
    fun observeBoughtSlots(weekStart: Long): Flow<List<Int>>

    /** 1 if that slot was already bought, otherwise 0. */
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

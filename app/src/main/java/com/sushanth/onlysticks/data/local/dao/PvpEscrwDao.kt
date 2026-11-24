package com.sushanth.onlysticks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sushanth.onlysticks.data.local.entity.PvpEscrwEntity

@Dao
interface PvpEscrwDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rows: List<PvpEscrwEntity>)

    @Query("SELECT cardId FROM pvp_escrow")
    suspend fun cardIds(): List<String>

    @Query("DELETE FROM pvp_escrow")
    suspend fun clear()
}

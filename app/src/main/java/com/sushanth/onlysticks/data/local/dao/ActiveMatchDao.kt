package com.sushanth.onlysticks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sushanth.onlysticks.data.local.entity.ActiveMatchEntity

@Dao
interface ActiveMatchDao {
    @Insert
    suspend fun insert(match: ActiveMatchEntity)

    @Query("SELECT * FROM active_match WHERE matchId = :matchId")
    suspend fun get(matchId: String): ActiveMatchEntity?

    @Query("SELECT * FROM active_match ORDER BY startedAt")
    suspend fun all(): List<ActiveMatchEntity>

    @Query("DELETE FROM active_match WHERE matchId = :matchId")
    suspend fun delete(matchId: String): Int
}

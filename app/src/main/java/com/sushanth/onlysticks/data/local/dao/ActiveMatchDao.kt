// Database access for in-progress bot matches.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sushanth.onlysticks.data.local.entity.ActiveMatchEntity

/** Tracks bot matches that have started but not finished (so a crash mid-match can be settled on restart). */
@Dao
interface ActiveMatchDao {
    /** Records that a match started. */
    @Insert
    suspend fun insert(match: ActiveMatchEntity)

    /** The open match with this id, if any. */
    @Query("SELECT * FROM active_match WHERE matchId = :matchId")
    suspend fun get(matchId: String): ActiveMatchEntity?

    /** Every unfinished match, oldest first. */
    @Query("SELECT * FROM active_match ORDER BY startedAt")
    suspend fun all(): List<ActiveMatchEntity>

    /** Removes a match once it has been settled. Returns the number of rows deleted (0 = already settled). */
    @Query("DELETE FROM active_match WHERE matchId = :matchId")
    suspend fun delete(matchId: String): Int
}

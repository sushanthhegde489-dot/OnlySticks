// Database access for the (single) player profile.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.sushanth.onlysticks.data.local.entity.PlrProfEntity
import kotlinx.coroutines.flow.Flow

/** The profile table only ever holds one row. */
@Dao
interface PlrProfDao {
    @Query("SELECT * FROM player_profile LIMIT 1")
    fun observe(): Flow<PlrProfEntity?>

    @Query("SELECT * FROM player_profile LIMIT 1")
    suspend fun get(): PlrProfEntity?

    @Upsert
    suspend fun upsert(entity: PlrProfEntity)
}

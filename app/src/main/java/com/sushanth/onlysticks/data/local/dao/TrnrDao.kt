// Database access for trainer cards.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.sushanth.onlysticks.data.local.entity.TrnrEntity
import kotlinx.coroutines.flow.Flow

/** Queries for the trainers table (trainers are stored separately from other cards). */
@Dao
interface TrnrDao {
    @Query("SELECT * FROM trainers ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<TrnrEntity>>

    @Query("SELECT * FROM trainers WHERE id = :id")
    fun observeById(id: String): Flow<TrnrEntity?>

    @Query("SELECT * FROM trainers WHERE id = :id")
    suspend fun getById(id: String): TrnrEntity?

    @Upsert
    suspend fun upsert(trainer: TrnrEntity)

    @Query("DELETE FROM trainers")
    suspend fun deleteAll()

    @Query("DELETE FROM trainers WHERE id = :id")
    suspend fun deleteById(id: String)
}

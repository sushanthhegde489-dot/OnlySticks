// Database access for owned cards (sticks and power-ups).
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.sushanth.onlysticks.data.local.entity.CardEntity
import kotlinx.coroutines.flow.Flow

/** Queries for the cards table. Quarantined cards (failed signature check) are hidden from normal lists. */
@Dao
interface CardDao {
    /** Live list of usable (non-quarantined) cards, oldest first. */
    @Query("SELECT * FROM cards WHERE quarantined = 0 ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<CardEntity>>

    /** Live view of one card. */
    @Query("SELECT * FROM cards WHERE id = :id")
    fun observeById(id: String): Flow<CardEntity?>

    /** Every card row, including quarantined ones. */
    @Query("SELECT * FROM cards ORDER BY createdAt ASC")
    suspend fun getAll(): List<CardEntity>

    @Upsert
    suspend fun upsert(card: CardEntity)

    @Upsert
    suspend fun upsertAll(list: List<CardEntity>)

    @Query("DELETE FROM cards WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM cards")
    suspend fun deleteAll()

    /** Marks or clears a card as quarantined. */
    @Query("UPDATE cards SET quarantined = :quarantined WHERE id = :id")
    suspend fun setQuar(id: String, quarantined: Boolean)

    /** Live list of quarantined cards (not used by any screen yet). */
    @Query("SELECT * FROM cards WHERE quarantined = 1 ORDER BY createdAt ASC")
    fun observeQuar(): Flow<List<CardEntity>>
}

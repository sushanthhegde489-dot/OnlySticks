// Database access for decks and their card slots.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.sushanth.onlysticks.data.local.entity.DeckEntity
import com.sushanth.onlysticks.data.local.entity.DeckSlotEntity
import com.sushanth.onlysticks.data.local.entity.DeckWithSlots
import kotlinx.coroutines.flow.Flow

/** Queries for decks. Decks and slots are loaded together as [DeckWithSlots]. */
@Dao
interface DeckDao {
    /** Live list of all decks with their slots. */
    @Transaction
    @Query("SELECT * FROM decks")
    fun observeDecks(): Flow<List<DeckWithSlots>>

    /** Live view of one deck. */
    @Transaction
    @Query("SELECT * FROM decks WHERE id = :id")
    fun observeDeck(id: String): Flow<DeckWithSlots?>

    @Upsert
    suspend fun upsertDeck(deck: DeckEntity)

    @Query("DELETE FROM deck_slots WHERE deckId = :deckId")
    suspend fun deleteSlotsByDeckId(deckId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlots(slots: List<DeckSlotEntity>)

    /** Atomically replaces every slot in a deck. */
    @Transaction
    suspend fun replaceSlots(deckId: String, slots: List<DeckSlotEntity>) {
        deleteSlotsByDeckId(deckId)
        insertSlots(slots.sortedBy { it.slotIndex })
    }

    @Query("DELETE FROM decks")
    suspend fun deleteAll()
}

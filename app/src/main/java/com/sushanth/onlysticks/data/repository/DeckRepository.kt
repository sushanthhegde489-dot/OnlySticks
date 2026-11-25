// Storage for saved decks.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.repository

import androidx.room.withTransaction
import com.sushanth.onlysticks.data.local.AppDatabase
import com.sushanth.onlysticks.data.local.entity.DeckEntity
import com.sushanth.onlysticks.data.local.entity.DeckSlotEntity
import com.sushanth.onlysticks.domain.model.Deck
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Reads and writes decks, resolving slot card ids against the verified card and trainer lists. */
class DeckRepository(private val db: AppDatabase, private val cardList: CardRepository, private val trnrs: TrainerRepository) {
    private val dao = db.deckDao()

    /** Live decks. Slots whose card is missing (deleted or quarantined) are skipped. */
    fun observeDecks(): Flow<List<Deck>> =
        combine(dao.observeDecks(), cardList.observeCards(), trnrs.observeTrnrs()) { decks, owned, trnrRecords ->
            val byId = owned.associateBy { it.id }
            val trnrById = trnrRecords.associate { it.trainer.id to it.trainer }
            decks.map { row ->
                Deck(
                    id = row.deck.id,
                    name = row.deck.name,
                    cardList = row.slots.sortedBy { it.slotIndex }.mapNotNull { byId[it.cardId] },
                    trainer = row.deck.trainerId?.let(trnrById::get),
                )
            }
        }

    /** Saves a deck and replaces all of its slots in one transaction. */
    suspend fun saveDeck(deck: Deck) {
        db.withTransaction {
            dao.upsertDeck(DeckEntity(id = deck.id, name = deck.name, trainerId = deck.trainer?.id))
            dao.replaceSlots(deck.id, deck.cardList.mapIndexed { i, card -> DeckSlotEntity(deck.id, i, card.id) })
        }
    }
}

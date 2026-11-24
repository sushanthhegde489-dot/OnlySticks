package com.sushanth.onlysticks.data.repository

import androidx.room.withTransaction
import com.sushanth.onlysticks.data.local.AppDatabase
import com.sushanth.onlysticks.data.local.entity.DeckEntity
import com.sushanth.onlysticks.data.local.entity.DeckSlotEntity
import com.sushanth.onlysticks.domain.model.Deck
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class DeckRepository(private val db: AppDatabase, private val cardList: CardRepository, private val trnrs: TrainerRepository) {
    private val dao = db.deckDao()

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

    suspend fun saveDeck(deck: Deck) {
        db.withTransaction {
            dao.upsertDeck(DeckEntity(id = deck.id, name = deck.name, trainerId = deck.trainer?.id))
            dao.replaceSlots(deck.id, deck.cardList.mapIndexed { i, card -> DeckSlotEntity(deck.id, i, card.id) })
        }
    }
}

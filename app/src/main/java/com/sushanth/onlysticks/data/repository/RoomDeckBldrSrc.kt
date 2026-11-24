package com.sushanth.onlysticks.data.repository

import com.sushanth.onlysticks.domain.model.Card
import com.sushanth.onlysticks.domain.model.Deck
import com.sushanth.onlysticks.domain.model.Trnr
import com.sushanth.onlysticks.ui.deck.DeckBldrSrc
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart

class RoomDeckBldrSrc(
    private val cardList: CardRepository,
    private val trnrs: TrainerRepository,
    private val decks: DeckRepository,
    private val seed: StartSeed,
) : DeckBldrSrc {
    override fun observeOwnedCards(): Flow<List<Card>> =
        combine(cardList.observeCards(), trnrs.observeTrnrs()) { owned, records -> owned + records.map { it.trainer } }
            .onStart { seed.seedIfEmpty() }

    override fun observeDecks(): Flow<List<Deck>> = decks.observeDecks().onStart { seed.seedIfEmpty() }

    override suspend fun saveDeck(deck: Deck) = decks.saveDeck(deck)

    override suspend fun renameCard(card: Card, nick: String) {
        if (card is Trnr) trnrs.rename(card.id, nick) else cardList.rename(card.id, nick)
    }
}

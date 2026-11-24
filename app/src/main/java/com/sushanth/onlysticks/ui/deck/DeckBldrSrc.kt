package com.sushanth.onlysticks.ui.deck

import com.sushanth.onlysticks.domain.model.Card
import com.sushanth.onlysticks.domain.model.Deck
import kotlinx.coroutines.flow.Flow

interface DeckBldrSrc {
    fun observeOwnedCards(): Flow<List<Card>>
    fun observeDecks(): Flow<List<Deck>>
    suspend fun saveDeck(deck: Deck)

    suspend fun renameCard(card: Card, nick: String) {}
}

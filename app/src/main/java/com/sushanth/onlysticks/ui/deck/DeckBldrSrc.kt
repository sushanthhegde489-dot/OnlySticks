// Data source interface for the deck builder (lets previews/tests use fake data).
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.deck

import com.sushanth.onlysticks.domain.model.Card
import com.sushanth.onlysticks.domain.model.Deck
import kotlinx.coroutines.flow.Flow

/** Where the deck builder gets cards and decks from, and saves them to. */
interface DeckBldrSrc {
    fun observeOwnedCards(): Flow<List<Card>>
    fun observeDecks(): Flow<List<Deck>>
    suspend fun saveDeck(deck: Deck)

    suspend fun renameCard(card: Card, nick: String) {}
}

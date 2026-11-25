// ViewModel for the deck builder.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.deck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.domain.config.Cfg
import com.sushanth.onlysticks.domain.model.Card
import com.sushanth.onlysticks.domain.model.Deck
import com.sushanth.onlysticks.domain.model.DeckErr
import com.sushanth.onlysticks.domain.model.DeckChecker
import com.sushanth.onlysticks.domain.model.PwrUp
import com.sushanth.onlysticks.domain.model.Stick
import com.sushanth.onlysticks.domain.model.Trnr
import com.sushanth.onlysticks.domain.trainer.TrainerProgression
import com.sushanth.onlysticks.ui.common.UiTxt
import com.sushanth.onlysticks.ui.common.uiTxt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Deck builder state: the owned collection, the deck being edited and any validation errors. */
data class DeckBldrUiState(
    val owned: List<Card> = emptyList(),
    val deckId: String = DeckBldrVm.NEW_DECK_ID,
    val deckName: String = "",
    val slots: List<Card> = emptyList(),
    val trainer: Trnr? = null,
    val slotLimit: Int = Cfg.DECK_SLOTS,
    val errors: List<DeckErr> = emptyList(),
    val saving: Boolean = false,
    val savedOnce: Boolean = false,
) {
    /** Ids of every card currently in the deck (including the trainer). */
    val inDeckIds: Set<String> get() = (slots.map { it.id } + listOfNotNull(trainer?.id)).toSet()
    /** The edited deck as a domain [Deck]. */
    val deck: Deck get() = Deck(deckId, deckName, slots, trainer)
}

/** Edits the player's deck. Every change is validated with [DeckChecker] before it is applied. */
class DeckBldrVm(private val source: DeckBldrSrc) : ViewModel() {
    private val _state = MutableStateFlow(DeckBldrUiState())
    val state: StateFlow<DeckBldrUiState> = _state.asStateFlow()

    /** Loads the owned cards, then the first saved deck (the game currently uses a single deck). */
    init {
        viewModelScope.launch { source.observeOwnedCards().collect { cardList -> _state.update { it.copy(owned = cardList) } } }
        viewModelScope.launch {
            source.observeDecks().first().firstOrNull()?.let { deck ->
                _state.update {
                    it.copy(
                        deckId = deck.id, deckName = deck.name, slots = deck.cardList, trainer = deck.trainer,
                        slotLimit = limitFor(deck.trainer),
                    )
                }
            }
        }
    }
    /** Adds a stick or power-up to the next slot. Returns false (and shows errors) if not allowed. */
    fun addToDeck(card: Card): Boolean {
        val s = _state.value
        if (card.id in s.inDeckIds) return false
        val candidate = s.slots + card
        val errors = DeckChecker.validate(candidate, listOfNotNull(s.trainer), extraSlots(s.trainer))
        if (errors.isNotEmpty()) {
            _state.update { it.copy(errors = errors) }
            return false
        }
        _state.update { it.copy(slots = candidate, errors = emptyList()) }
        return true
    }
    /** Removes the card in slot [index]. */
    fun removeFromDeck(index: Int) {
        _state.update { s ->
            if (index !in s.slots.indices) s else s.copy(slots = s.slots.filterIndexed { i, _ -> i != index }, errors = emptyList())
        }
    }
    /** Sets or clears the trainer. The slot limit changes because some trainers add deck slots. */
    fun setTrnr(trainer: Trnr?): Boolean {
        val s = _state.value
        val errors = DeckChecker.validate(s.slots, listOfNotNull(trainer), extraSlots(trainer))
        if (errors.isNotEmpty()) {
            _state.update { it.copy(errors = errors) }
            return false
        }
        _state.update { it.copy(trainer = trainer, slotLimit = limitFor(trainer), errors = emptyList()) }
        return true
    }
    /** Adds any card: trainers go to the trainer slot, everything else into the deck. */
    fun add(card: Card): Boolean = when (card) {
        is Trnr -> setTrnr(card)
        is Stick, is PwrUp -> addToDeck(card)
    }
    /** Saves a nickname for a card. */
    fun renameCard(card: Card, nick: String) {
        viewModelScope.launch { source.renameCard(card, nick) }
    }
    /** Validates and saves the deck in the background. */
    fun save(): Boolean {
        val s = _state.value
        val errors = DeckChecker.validate(s.deck, extraSlots(s.trainer))
        if (errors.isNotEmpty()) {
            _state.update { it.copy(errors = errors) }
            return false
        }
        _state.update { it.copy(saving = true, errors = emptyList()) }
        viewModelScope.launch {
            source.saveDeck(s.deck)
            _state.update { it.copy(saving = false, savedOnce = true) }
        }
        return true
    }
    /** Extra deck slots granted by [trainer]. */
    private fun extraSlots(trainer: Trnr?): Int = TrainerProgression.modifiersFor(trainer).extraDeckSlots
    /** Total deck slots with [trainer] equipped. */
    private fun limitFor(trainer: Trnr?): Int = Cfg.DECK_SLOTS + extraSlots(trainer)

    companion object {
        /** Id used for the player's deck. */
        const val NEW_DECK_ID = "deck-1"

        /** Localised message for a deck validation error. */
        fun errTxt(error: DeckErr): UiTxt = when (error) {
            is DeckErr.TooManyCards -> uiTxt(R.string.deck_err_too_many, error.count, error.limit)
            DeckErr.TrnrInCombatDeck -> uiTxt(R.string.deck_err_trnr_in_deck)
            DeckErr.TooManyTrnrs -> uiTxt(R.string.deck_err_too_many_trnrs)
        }
    }
}

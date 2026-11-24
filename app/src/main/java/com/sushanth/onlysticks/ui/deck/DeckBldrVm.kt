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
    val inDeckIds: Set<String> get() = (slots.map { it.id } + listOfNotNull(trainer?.id)).toSet()
    val deck: Deck get() = Deck(deckId, deckName, slots, trainer)
}

class DeckBldrVm(private val source: DeckBldrSrc) : ViewModel() {
    private val _state = MutableStateFlow(DeckBldrUiState())
    val state: StateFlow<DeckBldrUiState> = _state.asStateFlow()

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
    fun removeFromDeck(index: Int) {
        _state.update { s ->
            if (index !in s.slots.indices) s else s.copy(slots = s.slots.filterIndexed { i, _ -> i != index }, errors = emptyList())
        }
    }
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
    fun add(card: Card): Boolean = when (card) {
        is Trnr -> setTrnr(card)
        is Stick, is PwrUp -> addToDeck(card)
    }
    fun renameCard(card: Card, nick: String) {
        viewModelScope.launch { source.renameCard(card, nick) }
    }
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
    private fun extraSlots(trainer: Trnr?): Int = TrainerProgression.modifiersFor(trainer).extraDeckSlots
    private fun limitFor(trainer: Trnr?): Int = Cfg.DECK_SLOTS + extraSlots(trainer)

    companion object {
        const val NEW_DECK_ID = "deck-1"

        fun errTxt(error: DeckErr): UiTxt = when (error) {
            is DeckErr.TooManyCards -> uiTxt(R.string.deck_err_too_many, error.count, error.limit)
            DeckErr.TrnrInCombatDeck -> uiTxt(R.string.deck_err_trnr_in_deck)
            DeckErr.TooManyTrnrs -> uiTxt(R.string.deck_err_too_many_trnrs)
        }
    }
}

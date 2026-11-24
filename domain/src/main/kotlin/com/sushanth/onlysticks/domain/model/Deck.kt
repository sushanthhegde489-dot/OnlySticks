package com.sushanth.onlysticks.domain.model

import com.sushanth.onlysticks.domain.config.Cfg

data class Deck(
    val id: String,
    val name: String,
    val cardList: List<Card>,
    val trainer: Trnr?
) {
    val isFull: Boolean get() = cardList.size >= Cfg.DECK_SLOTS
}

sealed interface DeckErr {
    data class TooManyCards(val count: Int, val limit: Int) : DeckErr
    data object TrnrInCombatDeck : DeckErr
    data object TooManyTrnrs : DeckErr
}

object DeckChecker {
    fun validate(cardList: List<Card>, trnrs: List<Trnr>, extraSlots: Int = 0): List<DeckErr> {
        val errors = mutableListOf<DeckErr>()
        val limit = Cfg.DECK_SLOTS + extraSlots
        if (cardList.size > limit) errors += DeckErr.TooManyCards(cardList.size, limit)
        if (cardList.any { it is Trnr }) errors += DeckErr.TrnrInCombatDeck
        if (trnrs.size > Cfg.TRAINER_SLOTS) errors += DeckErr.TooManyTrnrs
        return errors
    }
    fun validate(deck: Deck, extraSlots: Int = 0): List<DeckErr> =
        validate(deck.cardList, listOfNotNull(deck.trainer), extraSlots)
}

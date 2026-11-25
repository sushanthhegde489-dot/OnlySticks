// What each player brings to a PvP match, and the checks run on the opponent's loadout.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.domain.pvp

import com.sushanth.onlysticks.domain.economy.BetRules
import com.sushanth.onlysticks.domain.model.Card
import com.sushanth.onlysticks.domain.model.DeckChecker
import com.sushanth.onlysticks.domain.model.Stick
import com.sushanth.onlysticks.domain.model.Trnr
import com.sushanth.onlysticks.domain.trainer.TrainerProgression

/** A player's deck, trainer and staked sticks for a PvP match. */
data class Loadout(val deck: List<Card>, val trainer: Trnr?, val bet: List<Stick>)

/** Reasons an opponent's loadout is refused. */
sealed interface LoadoutProblem {
    data object InvalidDeck : LoadoutProblem
    data object NoSticks : LoadoutProblem
    data object StatsOutOfRange : LoadoutProblem
    data object BadBetSize : LoadoutProblem
    data object BetsNotMatched : LoadoutProblem
}

/** Validates loadouts received over the network so a modified client can't cheat. */
object LoadoutRules {
    /** Returns the first problem with [loadout], or null if it's valid. */
    fun check(loadout: Loadout): LoadoutProblem? {
        val extra = TrainerProgression.modifiersFor(loadout.trainer).extraDeckSlots
        val sticks = loadout.deck.filterIsInstance<Stick>() + loadout.bet
        return when {
            DeckChecker.validate(loadout.deck, listOfNotNull(loadout.trainer), extra).isNotEmpty() -> LoadoutProblem.InvalidDeck
            loadout.deck.none { it is Stick } -> LoadoutProblem.NoSticks
            sticks.any { !statsInRange(it) } -> LoadoutProblem.StatsOutOfRange
            !BetRules.validBetCnt(loadout.bet.size) -> LoadoutProblem.BadBetSize
            else -> null
        }
    }

    /** Checks the opponent's loadout and that both bets are close enough in value. */
    fun checkPair(mine: Loadout, theirs: Loadout): LoadoutProblem? =
        check(theirs) ?: if (BetRules.canStart(mine.bet, theirs.bet)) null else LoadoutProblem.BetsNotMatched

    /** Stats must be inside the rarity's legal range (prevents forged super-sticks). */
    private fun statsInRange(stick: Stick): Boolean = with(stick) {
        base.durability in rarity.durability && base.speed in rarity.speed && base.attack in rarity.attack
    }
}

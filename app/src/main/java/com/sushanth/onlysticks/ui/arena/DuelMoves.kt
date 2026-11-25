// Builds the move buttons shown in the duel dock.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.arena

import com.sushanth.onlysticks.domain.combat.Combatant
import com.sushanth.onlysticks.domain.combat.DamageCalc
import com.sushanth.onlysticks.domain.model.Move

/** A move button: the move, its predicted damage (null for non-attacks), stamina change and whether it's allowed. */
data class DuelMove(val move: Move, val dmg: Int?, val stamDelta: Int, val legal: Boolean)

/** Calculates the move dock contents. */
object DuelMoves {
    /** Moves always shown in the dock, in display order. */
    private val DOCK = listOf(Move.JAB, Move.LUNGE, Move.PARRY, Move.SLASH, Move.REST)

    /** Move buttons for [self]. Exhaustion strike is added at the front when it is legal. */
    fun forCombatant(self: Combatant, legal: Set<Move>): List<DuelMove> {
        val moves = if (Move.EXHAUSTION_STRIKE in legal) listOf(Move.EXHAUSTION_STRIKE) + DOCK else DOCK
        return moves.map { move ->
            val dmg = when (move) {
                Move.PARRY, Move.REST -> null
                else -> DamageCalc.dmg(DamageCalc.inputFor(self, move))
            }
            DuelMove(move, dmg, stamDelta = move.stamSuccess, legal = move in legal)
        }
    }
}

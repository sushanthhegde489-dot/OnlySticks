package com.sushanth.onlysticks.ui.arena

import com.sushanth.onlysticks.domain.combat.Combatant
import com.sushanth.onlysticks.domain.combat.DamageCalc
import com.sushanth.onlysticks.domain.model.Move

data class DuelMove(val move: Move, val dmg: Int?, val stamDelta: Int, val legal: Boolean)

object DuelMoves {
    private val DOCK = listOf(Move.JAB, Move.LUNGE, Move.PARRY, Move.SLASH, Move.REST)

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

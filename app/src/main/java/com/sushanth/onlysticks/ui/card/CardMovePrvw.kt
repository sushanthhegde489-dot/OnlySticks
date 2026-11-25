// Move damage previews printed on stick cards.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.card

import com.sushanth.onlysticks.domain.combat.DamageCalc
import com.sushanth.onlysticks.domain.combat.DamageInput
import com.sushanth.onlysticks.domain.config.Cfg
import com.sushanth.onlysticks.domain.model.Move
import com.sushanth.onlysticks.domain.model.Stick
import com.sushanth.onlysticks.domain.stats.effectiveStats

/** One row of a card's move table. */
data class MovePrvw(val move: Move, val stamCost: Int, val dmg: Int?)

/** Calculates the move table for a stick card. */
object CardMovePrvw {
    /** Moves shown on cards (rest and exhaustion strike are left out). */
    val SHOWN_MOVES = listOf(Move.JAB, Move.LUNGE, Move.PARRY, Move.SLASH)

    /** Damage at full durability with base player speed, and the stamina each move costs. */
    fun forStick(stick: Stick): List<MovePrvw> {
        val stats = stick.effectiveStats
        return SHOWN_MOVES.map { move ->
            val dmg = if (move == Move.PARRY) null else DamageCalc.dmg(
                DamageInput(
                    move = move,
                    stickAtk = stats.attack,
                    stickSpd = stats.speed,
                    plrSpd = Cfg.BASE_PLAYER_SPEED,
                    curDur = stats.durability,
                    maxDurability = stats.durability,
                ),
            )
            MovePrvw(move, stamCost = -move.stamSuccess, dmg = dmg)
        }
    }
}

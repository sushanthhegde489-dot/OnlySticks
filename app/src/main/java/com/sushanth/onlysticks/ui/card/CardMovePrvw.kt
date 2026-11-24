package com.sushanth.onlysticks.ui.card

import com.sushanth.onlysticks.domain.combat.DamageCalc
import com.sushanth.onlysticks.domain.combat.DamageInput
import com.sushanth.onlysticks.domain.config.Cfg
import com.sushanth.onlysticks.domain.model.Move
import com.sushanth.onlysticks.domain.model.Stick
import com.sushanth.onlysticks.domain.stats.effectiveStats

data class MovePrvw(val move: Move, val stamCost: Int, val dmg: Int?)

object CardMovePrvw {
    val SHOWN_MOVES = listOf(Move.JAB, Move.LUNGE, Move.PARRY, Move.SLASH)

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

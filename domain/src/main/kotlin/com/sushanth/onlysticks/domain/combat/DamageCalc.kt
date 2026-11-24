package com.sushanth.onlysticks.domain.combat

import com.sushanth.onlysticks.domain.config.Cfg
import com.sushanth.onlysticks.domain.model.Move
import java.math.BigDecimal
import java.math.RoundingMode

data class DamageInput(
    val move: Move,
    val stickAtk: Int,
    val stickSpd: Int,
    val plrSpd: Int,
    val curDur: Int,
    val maxDurability: Int,
    val crit: Boolean = false,
    val burning: Boolean = false,
    val wet: Boolean = false,
    val unarmed: Boolean = false,
    val defenderDefensePct: Double = 0.0,
)

object DamageCalc {
    fun inputFor(attacker: Combatant, move: Move, crit: Boolean = false, defenderDefensePct: Double = 0.0): DamageInput {
        val stick = attacker.activeStick
        return DamageInput(
            move = move,
            stickAtk = stick?.attack ?: 0,
            stickSpd = stick?.roundSpd ?: 0,
            plrSpd = attacker.plrSpd,
            curDur = stick?.durability ?: 0,
            maxDurability = stick?.maxDurability ?: 0,
            crit = crit,
            burning = stick?.burning == true,
            wet = stick?.wet == true,
            unarmed = stick == null,
            defenderDefensePct = defenderDefensePct,
        )
    }

    fun dmg(input: DamageInput): Int = roundHalfUp(rawDmg(input))

    fun rawDmg(input: DamageInput): Double {
        if (!input.move.isAtk) return 0.0
        val exh = input.move == Move.EXHAUSTION_STRIKE
        val stickSpd = if (exh) input.stickSpd / 2.0 else input.stickSpd.toDouble()
        // first version, /100 like the original notes. UR vs UR took off half the hp bar in one hit
        // val core = input.move.baseDmg + input.stickAtk * stickSpd / 100.0 + input.plrSpd / 2.0
        val core = input.move.baseDmg +
            input.stickAtk * stickSpd / Cfg.DAMAGE_STAT_DIVISOR +
            input.plrSpd / Cfg.PLAYER_SPEED_DAMAGE_DIVISOR
        val durFactor = if (input.unarmed || input.maxDurability <= 0) 1.0
        else input.curDur.coerceAtLeast(0).toDouble() / input.maxDurability
        val crit = if (input.crit) Cfg.CRIT_MULTIPLIER else 1.0
        var status = 1.0
        if (input.burning) status *= Cfg.BURNING_DAMAGE_MULT
        if (input.wet) status *= Cfg.WET_DAMAGE_MULT
        var tot = core * durFactor * crit * status
        if (exh) tot *= Cfg.EXHAUSTION_DAMAGE_MULT
        if (input.unarmed) tot *= Cfg.UNARMED_DAMAGE_MULT
        tot *= (1.0 - input.defenderDefensePct.coerceIn(0.0, 1.0))
        return tot
    }

    fun counter(attackerRawDmg: Double, frac: Double, attackerDefensePct: Double = 0.0): Int =
        roundHalfUp(attackerRawDmg * frac * (1.0 - attackerDefensePct.coerceIn(0.0, 1.0)))

    fun roundHalfUp(value: Double): Int = BigDecimal(value).setScale(0, RoundingMode.HALF_UP).toInt()
}

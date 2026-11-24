package com.sushanth.onlysticks.domain.pve

import com.sushanth.onlysticks.domain.combat.Combatant
import com.sushanth.onlysticks.domain.combat.CombatEngine
import com.sushanth.onlysticks.domain.combat.DamageCalc
import com.sushanth.onlysticks.domain.combat.DamageInput
import com.sushanth.onlysticks.domain.combat.StickState
import com.sushanth.onlysticks.domain.config.Cfg
import com.sushanth.onlysticks.domain.model.Move
import com.sushanth.onlysticks.domain.model.PwrUpType
import com.sushanth.onlysticks.domain.model.TurnAction
import com.sushanth.onlysticks.domain.rng.DeterministicRng
import com.sushanth.onlysticks.domain.rng.Hash

object BotBrain {
    private val RNG_TAG = "bot".toByteArray()

    fun rngFor(shrdSeed: ByteArray): DeterministicRng = DeterministicRng.fromBytes(Hash.sha256(shrdSeed, RNG_TAG))

    private val MIX = listOf(Move.JAB, Move.SLASH, Move.LUNGE, Move.PARRY)
    fun chooseAction(self: Combatant, opp: Combatant, tier: Int, rng: DeterministicRng): TurnAction {
        val diff = BotDifficulty(tier)
        val rndRoll = rng.nextDouble()
        val pwrUp = if (diff.usesPwrUps) choosePwrUp(self, opp) else null
        val dousing = pwrUp != null && self.pwrUpAt(pwrUp) == PwrUpType.WATER_BOTTLE
        val swtchTo = chooseSwtch(self, dousing)
        val legal = CombatEngine.legalMoves(self).sortedBy { it.ordinal }
        val move = if (rndRoll < diff.rndMoveChance) legal[rng.nextInt(0, legal.size)]
        else heuristicMove(self, opp, stickAfterSwtch(self, swtchTo), rng)
        return TurnAction(move, swtchTo, pwrUp)
    }

    fun respDelayMs(rng: DeterministicRng): Long =
        rng.nextInt(Cfg.Bots.DELAY_MIN_MS.toInt()..Cfg.Bots.DELAY_MAX_MS.toInt()).toLong()
    private fun choosePwrUp(self: Combatant, opp: Combatant): Int? {
        fun slotOf(type: PwrUpType) = self.deck.indices.firstOrNull { self.pwrUpAt(it) == type }
        if (self.activeStick?.burning == true) slotOf(PwrUpType.WATER_BOTTLE)?.let { return it }
        val target = opp.activeStick ?: return null
        val healthy = !target.burning && target.durability > target.maxDurability * 0.5
        return if (healthy) slotOf(PwrUpType.LIGHTER) else null
    }
    private fun chooseSwtch(self: Combatant, dousing: Boolean): Int? {
        val active = self.activeStick ?: return null
        val bad = (active.burning && !dousing) || worn(active)
        if (!bad) return null
        return self.sticks.entries
            .filter { (slot, s) -> slot != self.activeSlot && !s.brkn && !s.burning && !worn(s) }
            .maxWithOrNull(compareBy<Map.Entry<Int, StickState>> { it.value.durability.toDouble() / it.value.maxDurability }
                .thenByDescending { it.key })
            ?.key
    }
    private fun worn(s: StickState) = s.durability <= s.maxDurability * 0.25

    private fun stickAfterSwtch(self: Combatant, swtchTo: Int?): StickState? =
        swtchTo?.let { self.sticks[it] } ?: self.activeStick ?: self.firstUsableSlot()?.let { self.sticks[it] }
    private fun heuristicMove(self: Combatant, opp: Combatant, stick: StickState?, rng: DeterministicRng): Move {
        fun dmg(move: Move) = estDmg(self, opp, stick, move)
        if (self.stam <= 0) {
            return if (dmg(Move.EXHAUSTION_STRIKE) >= opp.hp) Move.EXHAUSTION_STRIKE else Move.REST
        }
        val finisher = listOf(Move.JAB, Move.SLASH, Move.LUNGE).maxBy { dmg(it) }
        if (dmg(finisher) >= opp.hp) return finisher
        if (self.stam < Cfg.Bots.LOW_STAMINA) return Move.REST
        if (opp.stam <= 0) return Move.LUNGE
        return MIX[rng.weightedIdx(Cfg.Bots.MOVE_MIX_WEIGHTS)]
    }
    private fun estDmg(self: Combatant, opp: Combatant, stick: StickState?, move: Move): Int =
        DamageCalc.dmg(
            DamageInput(
                move = move,
                stickAtk = stick?.attack ?: 0,
                stickSpd = stick?.roundSpd ?: 0,
                plrSpd = self.plrSpd,
                curDur = stick?.durability ?: 0,
                maxDurability = stick?.maxDurability ?: 0,
                burning = stick?.burning == true,
                wet = stick?.wet == true,
                unarmed = stick == null,
                defenderDefensePct = opp.modifiers.defensePct,
            ),
        )
}

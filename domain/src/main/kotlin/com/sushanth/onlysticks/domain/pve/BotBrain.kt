// Decision-making for computer opponents in PvE duels.
// (Comments in this file were written by AI - Claude.)
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

/** Chooses the bot's action each turn using simple heuristics plus tier-based randomness. */
object BotBrain {
    /** Domain-separation tag so the bot's RNG stream differs from other streams derived from the same seed. */
    private val RNG_TAG = "bot".toByteArray()

    /** RNG for bot decisions, derived from the match seed. */
    fun rngFor(shrdSeed: ByteArray): DeterministicRng = DeterministicRng.fromBytes(Hash.sha256(shrdSeed, RNG_TAG))

    /** Attack mix the bot draws from when no special situation applies (weights in Cfg.Bots.MOVE_MIX_WEIGHTS). */
    private val MIX = listOf(Move.JAB, Move.SLASH, Move.LUNGE, Move.PARRY)
    /**
     * Picks the full turn: maybe a power-up, maybe a stick switch, then a move.
     * With probability rndMoveChance (higher for easy bots) the move is completely random.
     */
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

    /** How long the bot "thinks" before moving, so it feels less robotic. */
    fun respDelayMs(rng: DeterministicRng): Long =
        rng.nextInt(Cfg.Bots.DELAY_MIN_MS.toInt()..Cfg.Bots.DELAY_MAX_MS.toInt()).toLong()
    /** Douse own burning stick if possible; otherwise set fire to a healthy enemy stick. */
    private fun choosePwrUp(self: Combatant, opp: Combatant): Int? {
        fun slotOf(type: PwrUpType) = self.deck.indices.firstOrNull { self.pwrUpAt(it) == type }
        if (self.activeStick?.burning == true) slotOf(PwrUpType.WATER_BOTTLE)?.let { return it }
        val target = opp.activeStick ?: return null
        val healthy = !target.burning && target.durability > target.maxDurability * 0.5
        return if (healthy) slotOf(PwrUpType.LIGHTER) else null
    }
    /** Switches away from a burning (and not being doused) or badly worn stick to the healthiest other one. */
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
    /** A stick at 25% durability or less counts as worn out. */
    private fun worn(s: StickState) = s.durability <= s.maxDurability * 0.25

    /** The stick that will be in hand after any switch this turn. */
    private fun stickAfterSwtch(self: Combatant, swtchTo: Int?): StickState? =
        swtchTo?.let { self.sticks[it] } ?: self.activeStick ?: self.firstUsableSlot()?.let { self.sticks[it] }
    /**
     * Heuristic: finish the opponent if possible, rest when low on stamina, punish an exhausted opponent
     * with a lunge, otherwise pick from the weighted attack mix.
     */
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
    /** Estimated damage of [move] against [opp] (no crit). */
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

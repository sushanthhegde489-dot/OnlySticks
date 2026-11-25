// The turn-based combat rules engine. Pure and deterministic: given the same state,
// actions and RNG seed it always produces the same result (needed for PvP lockstep).
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.domain.combat

import com.sushanth.onlysticks.domain.config.Cfg
import com.sushanth.onlysticks.domain.model.Card
import com.sushanth.onlysticks.domain.model.Move
import com.sushanth.onlysticks.domain.model.PwrUpType
import com.sushanth.onlysticks.domain.model.PwrUpTarget
import com.sushanth.onlysticks.domain.model.TrnrModifiers
import com.sushanth.onlysticks.domain.model.TurnAction
import com.sushanth.onlysticks.domain.rng.DeterministicRng

/** Entry point for running a duel: creates the starting state and resolves rounds. */
object CombatEngine {
    /** Builds the initial [CombatState] for two decks, applying each side's trainer modifiers. */
    fun start(
        deck1: List<Card>, deck2: List<Card>,
        modifiers1: TrnrModifiers = TrnrModifiers.NONE,
        modifiers2: TrnrModifiers = TrnrModifiers.NONE,
    ): CombatState = CombatState(
        p1 = Combatant.start(PlrId.P1, deck1, modifiers1),
        p2 = Combatant.start(PlrId.P2, deck2, modifiers2),
    )
    /** Moves a combatant may pick. With no stamina left only REST or EXHAUSTION_STRIKE are allowed. */
    fun legalMoves(c: Combatant): Set<Move> =
        if (c.stam <= 0) setOf(Move.REST, Move.EXHAUSTION_STRIKE)
        else setOf(Move.JAB, Move.LUNGE, Move.PARRY, Move.SLASH, Move.REST)

    /** A normalised move choice; [autoSkip] is true when the player ran out of time or picked an illegal move. */
    data class Res(val move: Move, val autoSkip: Boolean)
    /**
     * Converts the player's raw choice into the move that will actually be performed.
     * No choice -> auto-skip rest; exhausted attackers become EXHAUSTION_STRIKE; exhausted parry becomes REST.
     */
    fun normalize(c: Combatant, chosen: Move?): Res = when {
        chosen == null -> Res(Move.REST, autoSkip = true)
        c.stam <= 0 && chosen.isAtk -> Res(Move.EXHAUSTION_STRIKE, false)
        c.stam <= 0 && chosen == Move.PARRY -> Res(Move.REST, false)
        chosen !in legalMoves(c) -> Res(Move.REST, autoSkip = true)
        else -> Res(chosen, false)
    }
    /** Total speed for initiative: player speed + active stick speed (halved for exhaustion strike) + move modifier. */
    fun totSpd(c: Combatant, move: Move): Int {
        val stickSpd = c.activeStick?.roundSpd ?: 0
        val effectiveStick = if (move == Move.EXHAUSTION_STRIKE) stickSpd / 2 else stickSpd
        return c.plrSpd + effectiveStick + move.spdModifier
    }
    /** Works out who acts first this round. Returns the player and whether the tie-breaker coin flip decided it. */
    fun firstToAct(s: CombatState, m1: Move, m2: Move, p1WinsTie: Boolean): Pair<PlrId, Boolean> {
        val t1 = totSpd(s.p1, m1)
        val t2 = totSpd(s.p2, m2)
        return when {
            t1 > t2 -> PlrId.P1 to false
            t2 > t1 -> PlrId.P2 to false
            else -> (if (p1WinsTie) PlrId.P1 else PlrId.P2) to true
        }
    }
    /** Resolves one full round from both players' actions and returns the new state plus the event log. */
    fun resRound(state: CombatState, a1: TurnAction, a2: TurnAction, rng: DeterministicRng): RoundRes {
        require(!state.isOver) { "match is over" }
        return RoundRun(state, rng).run(a1, a2)
    }

    /** Mutable helper that holds the evolving state and event list while a single round is resolved. */
    private class RoundRun(var s: CombatState, val rng: DeterministicRng) {
        val evts = mutableListOf<CombatEvt>()
        var itemTurns: Set<PlrId> = emptySet()
        /**
         * Round order: auto-switch broken sticks, voluntary switches, power-ups (in initiative order),
         * then the move interaction from [InteractionMatrix], then end-of-round upkeep.
         */
        fun run(a1: TurnAction, a2: TurnAction): RoundRes {
            // Coin flip used to break speed ties; drawn first so the RNG sequence is stable.
            val p1WinsTie = rng.nextBoolean(0.5)
            val actions = mapOf(PlrId.P1 to a1, PlrId.P2 to a2)
            PlrId.entries.forEach { autoSwtch(it) }
            PlrId.entries.forEach { id -> actions.getValue(id).swtchToSlot?.let { voluntarySwtch(id, it) } }
            // Using an item consumes the turn, so those players are treated as resting without the rest bonus.
            itemTurns = PlrId.entries.filter { actions.getValue(it).usesItem }.toSet()
            val r1 = if (PlrId.P1 in itemTurns) Res(Move.REST, autoSkip = false) else normalize(s.p1, a1.move)
            val r2 = if (PlrId.P2 in itemTurns) Res(Move.REST, autoSkip = false) else normalize(s.p2, a2.move)
            val moves = mapOf(PlrId.P1 to r1, PlrId.P2 to r2)
            // Power-ups resolve in initiative order (speed is recomputed afterwards because items can change it).
            val (itemFirst, _) = firstToAct(s, r1.move, r2.move, p1WinsTie)
            listOf(itemFirst, itemFirst.other).forEach { id ->
                val action = actions.getValue(id)
                action.pwrUpSlot?.let { usePwrUp(id, it, action.pwrUpTarget) }
            }
            val (first, tieBrkn) = firstToAct(s, r1.move, r2.move, p1WinsTie)
            evts += CombatEvt.Init(first, totSpd(s.p1, r1.move), totSpd(s.p2, r2.move), tieBrkn)
            PlrId.entries.filter { it !in itemTurns }.forEach { id -> moves.getValue(id).let { evts += CombatEvt.MoveRes(id, it.move, it.autoSkip) } }

            val cell = InteractionMatrix.cell(r1.move, r2.move)
            resCell(cell, moves, first)
            if (!s.isOver) endOfRound()
            return RoundRes(s, evts)
        }

        /** Applies one matrix cell: both hit (ordered), one side blocked by a parry, or the mixed rest/parry cases. */
        private fun resCell(cell: Cell, moves: Map<PlrId, Res>, initFirst: PlrId) {
            val out = mapOf(PlrId.P1 to cell.row, PlrId.P2 to cell.col)
            when {
                cell.row is SideOutcome.Hit && cell.col is SideOutcome.Hit -> {
                    val first = when (cell.order) {
                        Order.INITIATIVE -> initFirst
                        Order.ROW_FIRST -> PlrId.P1
                        Order.COL_FIRST -> PlrId.P2
                    }
                    atkStp(first, moves.getValue(first).move)
                    if (!s.isOver) atkStp(first.other, moves.getValue(first.other).move)
                }
                cell.row is SideOutcome.Blocked || cell.col is SideOutcome.Blocked -> {
                    val attacker = if (cell.row is SideOutcome.Blocked) PlrId.P1 else PlrId.P2
                    blockedStp(attacker, moves.getValue(attacker).move, out.getValue(attacker) as SideOutcome.Blocked)
                }
                else -> {
                    PlrId.entries.filter { out.getValue(it) is SideOutcome.Hit }.forEach { atkStp(it, moves.getValue(it).move) }
                    if (s.isOver) return
                    PlrId.entries.forEach { id ->
                        when (out.getValue(id)) {
                            SideOutcome.Rest -> rest(id, moves.getValue(id).autoSkip)
                            SideOutcome.ParryClink -> {
                                changeStam(id, -Cfg.PARRY_CLINK_STAMINA)
                                if (id == PlrId.P1) evts += CombatEvt.Clink(s.round)
                            }
                            SideOutcome.ParryWhiff -> {
                                changeStam(id, -Cfg.PARRY_WHIFF_STAMINA)
                                evts += CombatEvt.Whiff(id)
                            }
                            else -> Unit
                        }
                    }
                }
            }
        }
        /** Rolls crit and computes raw (unrounded) damage for an attack. */
        private fun rollAtk(attacker: PlrId, move: Move, defenderDefensePct: Double): Pair<Double, Boolean> {
            val crit = rng.nextBoolean(Cfg.CRIT_CHANCE)
            val input = DamageCalc.inputFor(s[attacker], move, crit, defenderDefensePct)
            return DamageCalc.rawDmg(input) to crit
        }
        /** A landed attack: damage the defender, reward the attacker's stamina, wear the stick, maybe burn recoil. */
        private fun atkStp(attacker: PlrId, move: Move) {
            val defender = attacker.other
            val wasBurn = s[attacker].activeStick?.burning == true
            val (raw, crit) = rollAtk(attacker, move, s[defender].modifiers.defensePct)
            val dmg = DamageCalc.roundHalfUp(raw)
            dmgHp(defender, dmg)
            evts += CombatEvt.Hit(attacker, move, dmg, crit)
            changeStam(attacker, move.stamSuccess)
            wearActive(attacker, move, Cfg.WEAR_PER_ATTACK)
            if (checkKo(defender)) return
            if (wasBurn) rcl(attacker)
        }
        /** An attack that was parried: the attacker takes counter damage and a stamina penalty, the parrier is rewarded. */
        private fun blockedStp(attacker: PlrId, move: Move, blocked: SideOutcome.Blocked) {
            val parrier = attacker.other
            val wasBurn = s[attacker].activeStick?.burning == true
            val (raw, _) = rollAtk(attacker, move, defenderDefensePct = 0.0)
            val counter = DamageCalc.counter(raw, blocked.counterFrac, s[attacker].modifiers.defensePct)
            dmgHp(attacker, counter)
            evts += CombatEvt.Countered(parrier, attacker, counter)
            changeStam(attacker, move.stamPenalty)
            wearActive(attacker, move, Cfg.WEAR_PER_ATTACK)
            changeStam(parrier, Move.PARRY.stamSuccess)
            wearActive(parrier, Move.PARRY, Cfg.WEAR_PARRY_SUCCESS)
            if (checkKo(attacker)) return
            if (wasBurn) rcl(attacker)
        }
        /** Recoil damage taken by a player who attacks while their stick is on fire. */
        private fun rcl(plr: PlrId) {
            dmgHp(plr, Cfg.FIRE_RECOIL)
            evts += CombatEvt.Rcl(plr, Cfg.FIRE_RECOIL)
            checkKo(plr)
        }
        /** Recovers stamina for a resting player (less if the rest came from an auto-skip). */
        private fun rest(id: PlrId, autoSkip: Boolean) {
            if (id in itemTurns) return
            val c = s[id]
            val amount = if (autoSkip) Cfg.AUTO_SKIP_RECOVERY else Cfg.REST_RECOVERY + c.modifiers.restBonus
            val before = c.stam
            changeStam(id, amount)
            evts += CombatEvt.Rested(id, s[id].stam - before, autoSkip)
        }
        /** If [loser] is out of HP, records the winner and a KnockedOut event. Returns true when the match ended. */
        private fun checkKo(loser: PlrId): Boolean {
            if (s[loser].alive) return false
            s = s.copy(winner = loser.other)
            evts += CombatEvt.KnockedOut(loser.other, loser)
            return true
        }
        private fun dmgHp(id: PlrId, amount: Int) {
            val c = s[id]
            s = s.with(c.copy(hp = (c.hp - amount).coerceAtLeast(0)))
        }

        private fun changeStam(id: PlrId, delta: Int) {
            val c = s[id]
            s = s.with(c.copy(stam = (c.stam + delta).coerceIn(0, Cfg.MAX_STAMINA)))
        }

        private fun wearActive(id: PlrId, move: Move, amount: Int) {
            if (move == Move.EXHAUSTION_STRIKE) return
            val c = s[id]
            val slot = c.activeSlot ?: return
            val stick = c.sticks[slot]?.takeUnless { it.brkn } ?: return
            updateStick(id, slot, stick.copy(durability = (stick.durability - amount).coerceAtLeast(0)))
        }
        /** Replaces a stick's state; clears fire/wet when it breaks and emits a StickBrkn event on the transition. */
        private fun updateStick(id: PlrId, slot: Int, updated: StickState) {
            val c = s[id]
            val becameBrkn = updated.brkn && c.sticks[slot]?.brkn == false
            val final = if (updated.brkn) updated.copy(burning = false, wetRounds = 0) else updated
            s = s.with(c.copy(sticks = c.sticks + (slot to final)))
            if (becameBrkn) evts += CombatEvt.StickBrkn(id, slot)
        }
        /** If the active stick is gone or broken, automatically equip the first usable stick. */
        private fun autoSwtch(id: PlrId) {
            val c = s[id]
            if (c.activeStick != null) return
            val next = c.firstUsableSlot() ?: return
            s = s.with(c.copy(activeSlot = next))
            evts += CombatEvt.Swtch(id, next, automatic = true)
        }
        private fun voluntarySwtch(id: PlrId, slot: Int) {
            val c = s[id]
            val target = c.sticks[slot] ?: return
            if (target.brkn || slot == c.activeSlot) return
            s = s.with(c.copy(activeSlot = slot))
            evts += CombatEvt.Swtch(id, slot, automatic = false)
        }
        /** Applies a Lighter (sets the target's active stick on fire) or Water Bottle (extinguishes and makes it wet). */
        private fun usePwrUp(id: PlrId, slot: Int, chosen: PwrUpTarget?) {
            val c = s[id]
            val type = c.pwrUpAt(slot) ?: return
            val side = chosen ?: PwrUpTarget.defFor(type)
            val targetId = if (side == PwrUpTarget.OPPONENT) id.other else id
            val target = s[targetId]
            val targetSlot = target.activeSlot ?: return
            val stick = target.activeStick ?: return
            val updated = when (type) {
                PwrUpType.LIGHTER -> stick.copy(burning = true)
                PwrUpType.WATER_BOTTLE -> stick.copy(burning = false, wetRounds = Cfg.WET_ROUNDS)
            }
            updateStick(targetId, targetSlot, updated)
            val user = s[id]
            s = s.with(user.copy(usedPwrUps = user.usedPwrUps + slot))
            evts += CombatEvt.PwrUpUsed(id, slot, c.deck[slot].id, type, targetId)
        }
        /** End-of-round upkeep: fire burns durability, wet timers tick down, passive stamina regen, round counter++. */
        private fun endOfRound() {
            PlrId.entries.forEach { id ->
                s[id].sticks.toSortedMap().forEach { (slot, stick) ->
                    var st = stick
                    if (st.burning && !st.brkn) {
                        val burn = DamageCalc.roundHalfUp(st.maxDurability * Cfg.FIRE_BURN_FRACTION)
                        st = st.copy(durability = (st.durability - burn).coerceAtLeast(0))
                        evts += CombatEvt.Burned(id, slot, burn)
                    }
                    if (st.wetRounds > 0) st = st.copy(wetRounds = st.wetRounds - 1)
                    if (st != stick) updateStick(id, slot, st)
                }
                changeStam(id, Cfg.PASSIVE_STAMINA_REGEN)
            }
            s = s.copy(round = s.round + 1)
        }
    }
}

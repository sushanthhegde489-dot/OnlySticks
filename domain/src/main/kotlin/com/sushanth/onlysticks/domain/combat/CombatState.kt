package com.sushanth.onlysticks.domain.combat

import com.sushanth.onlysticks.domain.config.Cfg
import com.sushanth.onlysticks.domain.model.Card
import com.sushanth.onlysticks.domain.model.Move
import com.sushanth.onlysticks.domain.model.PwrUpType
import com.sushanth.onlysticks.domain.model.Stick
import com.sushanth.onlysticks.domain.model.TrnrModifiers
import com.sushanth.onlysticks.domain.stats.effectiveStats

enum class PlrId { P1, P2;
    val other: PlrId get() = if (this == P1) P2 else P1
}

data class StickState(
    val stick: Stick,
    val maxDurability: Int,
    val durability: Int,
    val burning: Boolean = false,
    val wetRounds: Int = 0,
) {
    val brkn: Boolean get() = durability <= 0
    val wet: Boolean get() = wetRounds > 0
    val attack: Int get() = stick.effectiveStats.attack

    val roundSpd: Int get() = (stick.effectiveStats.speed - if (wet) Cfg.WET_SPEED_PENALTY else 0).coerceAtLeast(0)

    companion object {
        fun fresh(stick: Stick): StickState {
            val max = stick.effectiveStats.durability
            return StickState(stick, max, max)
        }
    }
}

data class Combatant(
    val id: PlrId,
    val deck: List<Card>,
    val modifiers: TrnrModifiers,
    val maxHp: Int,
    val hp: Int,
    val stam: Int,
    val sticks: Map<Int, StickState>,
    val activeSlot: Int?,
    val usedPwrUps: Set<Int> = emptySet(),
) {
    val activeStick: StickState? get() = activeSlot?.let { sticks[it] }?.takeUnless { it.brkn }
    val unarmed: Boolean get() = activeStick == null
    val alive: Boolean get() = hp > 0

    val plrSpd: Int get() {
        var speed = Cfg.BASE_PLAYER_SPEED - modifiers.spdPenalty
        if (stam < Cfg.FATIGUE_THRESHOLD) speed -= Cfg.FATIGUE_SPEED_PENALTY
        return speed.coerceAtLeast(0)
    }

    fun pwrUpAt(slot: Int): PwrUpType? =
        (deck.getOrNull(slot) as? com.sushanth.onlysticks.domain.model.PwrUp)?.type?.takeIf { slot !in usedPwrUps }

    fun firstUsableSlot(): Int? = sticks.entries.sortedBy { it.key }.firstOrNull { !it.value.brkn }?.key

    companion object {
        fun start(id: PlrId, deck: List<Card>, modifiers: TrnrModifiers = TrnrModifiers.NONE): Combatant {
            val sticks = deck.withIndex().filter { it.value is Stick }
                .associate { it.index to StickState.fresh(it.value as Stick) }
            val maxHp = Cfg.PLAYER_MAX_HP + modifiers.bonusMaxHp
            return Combatant(
                id = id, deck = deck, modifiers = modifiers, maxHp = maxHp, hp = maxHp,
                stam = Cfg.STARTING_STAMINA, sticks = sticks,
                activeSlot = sticks.keys.minOrNull(),
            )
        }
    }
}

data class CombatState(
    val p1: Combatant,
    val p2: Combatant,
    val round: Int = 1,
    val winner: PlrId? = null,
) {
    val isOver: Boolean get() = winner != null
    operator fun get(id: PlrId): Combatant = if (id == PlrId.P1) p1 else p2
    fun with(c: Combatant): CombatState = if (c.id == PlrId.P1) copy(p1 = c) else copy(p2 = c)
}

sealed interface CombatEvt {
    data class Swtch(val plr: PlrId, val toSlot: Int, val automatic: Boolean) : CombatEvt
    data class PwrUpUsed(val plr: PlrId, val slot: Int, val cardId: String, val type: PwrUpType, val target: PlrId = plr.other) : CombatEvt
    data class Init(val first: PlrId, val p1Spd: Int, val p2Spd: Int, val tieBrkn: Boolean) : CombatEvt
    data class MoveRes(val plr: PlrId, val move: Move, val autoSkip: Boolean) : CombatEvt
    data class Hit(val attacker: PlrId, val move: Move, val dmg: Int, val crit: Boolean) : CombatEvt
    data class Countered(val parrier: PlrId, val attacker: PlrId, val dmg: Int) : CombatEvt
    data class Rcl(val plr: PlrId, val dmg: Int) : CombatEvt
    data class Clink(val round: Int) : CombatEvt
    data class Whiff(val plr: PlrId) : CombatEvt
    data class Rested(val plr: PlrId, val recovered: Int, val autoSkip: Boolean) : CombatEvt
    data class Burned(val plr: PlrId, val slot: Int, val amount: Int) : CombatEvt
    data class StickBrkn(val plr: PlrId, val slot: Int) : CombatEvt
    data class KnockedOut(val winner: PlrId, val loser: PlrId) : CombatEvt
}

data class RoundRes(val state: CombatState, val evts: List<CombatEvt>)

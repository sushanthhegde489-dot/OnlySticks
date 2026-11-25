// Combat moves and the action a player submits each turn.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.domain.model

/**
 * The moves available in combat.
 * [spdModifier] affects initiative, [baseDmg] feeds the damage formula,
 * [stamSuccess]/[stamPenalty] are the stamina change on success / when blocked.
 */
enum class Move(
    val spdModifier: Int,
    val baseDmg: Int,
    val stamSuccess: Int,
    val stamPenalty: Int,
    val isAtk: Boolean,
) {
    JAB(40, 45, -10, -25, true),
    LUNGE(-20, 110, -25, -40, true),
    PARRY(15, 0, -15, -35, false),
    SLASH(0, 75, -18, -18, true),
    REST(0, 0, 30, 10, false),
    EXHAUSTION_STRIKE(0, 75, 0, 0, true),
}

/** Everything one player chose for a turn: a move, an optional stick switch and an optional power-up. */
data class TurnAction(
    val move: Move?,
    val swtchToSlot: Int? = null,
    val pwrUpSlot: Int? = null,
    val pwrUpTarget: PwrUpTarget? = null,
) {
    /** Using a power-up takes the whole turn. */
    val usesItem: Boolean get() = pwrUpSlot != null

    companion object {
        /** Action used when the turn timer runs out. */
        val AUTO_SKIP = TurnAction(null)
    }
}

/** Who a power-up is used on. */
enum class PwrUpTarget {
    SELF, OPPONENT;

    companion object {
        /** Default target: lighters go on the opponent, water bottles on yourself. */
        fun defFor(type: PwrUpType): PwrUpTarget = when (type) {
            PwrUpType.LIGHTER -> OPPONENT
            PwrUpType.WATER_BOTTLE -> SELF
        }
    }
}

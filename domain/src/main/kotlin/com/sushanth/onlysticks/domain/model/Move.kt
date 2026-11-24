package com.sushanth.onlysticks.domain.model

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

data class TurnAction(
    val move: Move?,
    val swtchToSlot: Int? = null,
    val pwrUpSlot: Int? = null,
    val pwrUpTarget: PwrUpTarget? = null,
) {
    val usesItem: Boolean get() = pwrUpSlot != null

    companion object {
        val AUTO_SKIP = TurnAction(null)
    }
}

enum class PwrUpTarget {
    SELF, OPPONENT;

    companion object {
        fun defFor(type: PwrUpType): PwrUpTarget = when (type) {
            PwrUpType.LIGHTER -> OPPONENT
            PwrUpType.WATER_BOTTLE -> SELF
        }
    }
}

// Bonuses a trainer applies to its owner during combat.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.domain.model

/** Combat bonuses from the equipped trainer. Use [NONE] when no trainer is equipped. */
data class TrnrModifiers(
    val bonusMaxHp: Int = 0,
    val restBonus: Int = 0,
    val defensePct: Double = 0.0,
    val extraDeckSlots: Int = 0,
    val spdPenalty: Int = 0,
) {
    companion object {
        val NONE = TrnrModifiers()
    }
}

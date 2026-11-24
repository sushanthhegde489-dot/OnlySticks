package com.sushanth.onlysticks.domain.model

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

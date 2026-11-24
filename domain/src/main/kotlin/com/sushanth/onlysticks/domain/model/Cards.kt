package com.sushanth.onlysticks.domain.model

enum class TuningFlag { WEIGHT, GRIP, QUALITY }

data class BaseStats(val durability: Int, val speed: Int, val attack: Int)

data class EffectiveStats(val durability: Int, val speed: Int, val attack: Int) {
    val tot: Int get() = durability + speed + attack
}

sealed interface Card {
    val id: String
    val customName: String?
}

data class Stick(
    override val id: String,
    val rarity: Rarity,
    val base: BaseStats,
    val tuning: Set<TuningFlag> = emptySet(),
    override val customName: String? = null,
) : Card {
    val displayName: String get() = customName ?: rarity.code
}

enum class PwrUpType { LIGHTER, WATER_BOTTLE }

data class PwrUp(
    override val id: String,
    val type: PwrUpType,
    override val customName: String? = null,
) : Card

enum class TrnrBuff { MAX_HP, STAMINA, DEFENSE, DECK_LIMIT }

data class Trnr(
    override val id: String,
    val buff: TrnrBuff,
    val affinity: Int = 0,
    override val customName: String? = null,
) : Card

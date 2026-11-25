// Card types. Every collectible is a [Card]: a Stick (weapon), a PwrUp (consumable) or a Trnr (trainer).
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.domain.model

/** Hidden "tuning" traits that nudge a stick's stats up or down (see StatGen). */
enum class TuningFlag { WEIGHT, GRIP, QUALITY }

/** Stats as rolled, before tuning is applied. */
data class BaseStats(val durability: Int, val speed: Int, val attack: Int)

/** Stats after tuning; these are what combat actually uses. */
data class EffectiveStats(val durability: Int, val speed: Int, val attack: Int) {
    /** Sum of all three stats (used for appraisal). */
    val tot: Int get() = durability + speed + attack
}

/** Common interface of every collectible card. [customName] is an optional player-chosen nickname. */
sealed interface Card {
    val id: String
    val customName: String?
}

/** A stick: the weapon card used in combat. */
data class Stick(
    override val id: String,
    val rarity: Rarity,
    val base: BaseStats,
    val tuning: Set<TuningFlag> = emptySet(),
    override val customName: String? = null,
) : Card {
    /** Nickname if set, otherwise the rarity code (e.g. "SR"). */
    val displayName: String get() = customName ?: rarity.code
}

/** Consumable item types. Lighter sets a stick on fire; water bottle puts it out and makes it wet. */
enum class PwrUpType { LIGHTER, WATER_BOTTLE }

/** A one-use power-up card placed in a deck slot. */
data class PwrUp(
    override val id: String,
    val type: PwrUpType,
    override val customName: String? = null,
) : Card

/** The bonus a trainer card focuses on. */
enum class TrnrBuff { MAX_HP, STAMINA, DEFENSE, DECK_LIMIT }

/** A trainer card. Sits in the deck's trainer slot and grows stronger as [affinity] increases. */
data class Trnr(
    override val id: String,
    val buff: TrnrBuff,
    val affinity: Int = 0,
    override val customName: String? = null,
) : Card

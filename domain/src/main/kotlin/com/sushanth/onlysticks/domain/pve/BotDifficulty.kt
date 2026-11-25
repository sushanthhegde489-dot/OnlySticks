// Difficulty settings for a bot tier.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.domain.pve

import com.sushanth.onlysticks.domain.config.Cfg

/** Bot difficulty tier 1 (easiest) to Cfg.Bots.TIERS (hardest). Looks up per-tier values from [Cfg.Bots]. */
data class BotDifficulty(val tier: Int) {
    init {
        require(tier in 1..Cfg.Bots.TIERS) { "bot tier must be 1..${Cfg.Bots.TIERS}, was $tier" }
    }

    /** Chance this bot picks a random move instead of thinking. */
    val rndMoveChance: Double get() = Cfg.Bots.RANDOM_MOVE_CHANCE[tier - 1]

    /** Rarity weights for this bot's deck. */
    val rarWeights: List<Int> get() = Cfg.Bots.RARITY_WEIGHTS[tier - 1]

    /** Only higher tiers use power-ups. */
    val usesPwrUps: Boolean get() = tier >= Cfg.Bots.POWERUPS_MIN_TIER
}

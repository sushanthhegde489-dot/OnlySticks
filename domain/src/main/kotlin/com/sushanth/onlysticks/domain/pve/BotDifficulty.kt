package com.sushanth.onlysticks.domain.pve

import com.sushanth.onlysticks.domain.config.Cfg

data class BotDifficulty(val tier: Int) {
    init {
        require(tier in 1..Cfg.Bots.TIERS) { "bot tier must be 1..${Cfg.Bots.TIERS}, was $tier" }
    }

    val rndMoveChance: Double get() = Cfg.Bots.RANDOM_MOVE_CHANCE[tier - 1]

    val rarWeights: List<Int> get() = Cfg.Bots.RARITY_WEIGHTS[tier - 1]

    val usesPwrUps: Boolean get() = tier >= Cfg.Bots.POWERUPS_MIN_TIER
}

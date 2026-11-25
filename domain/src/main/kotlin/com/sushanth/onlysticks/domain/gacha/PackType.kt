// The three booster packs sold in the shop.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.domain.gacha

import com.sushanth.onlysticks.domain.config.Cfg

/** A pack's store id, coin cost and rarity weights (common..ultra). */
enum class PackType(val id: String, val cost: Long, val rarWeights: List<Int>) {
    COMMON("N_pack", Cfg.Packs.COMMON_COST, Cfg.Packs.COMMON_WEIGHTS),
    RARE("R_pack", Cfg.Packs.RARE_COST, Cfg.Packs.RARE_WEIGHTS),
    ULTRA("UR_pack", Cfg.Packs.ULTRA_COST, Cfg.Packs.ULTRA_WEIGHTS);

    companion object {
        /** Looks up a pack by its store id, or null if unknown. */
        fun fromId(id: String): PackType? = entries.firstOrNull { it.id == id }
    }
}

package com.sushanth.onlysticks.domain.gacha

import com.sushanth.onlysticks.domain.config.Cfg

enum class PackType(val id: String, val cost: Long, val rarWeights: List<Int>) {
    COMMON("N_pack", Cfg.Packs.COMMON_COST, Cfg.Packs.COMMON_WEIGHTS),
    RARE("R_pack", Cfg.Packs.RARE_COST, Cfg.Packs.RARE_WEIGHTS),
    ULTRA("UR_pack", Cfg.Packs.ULTRA_COST, Cfg.Packs.ULTRA_WEIGHTS);

    companion object {
        fun fromId(id: String): PackType? = entries.firstOrNull { it.id == id }
    }
}

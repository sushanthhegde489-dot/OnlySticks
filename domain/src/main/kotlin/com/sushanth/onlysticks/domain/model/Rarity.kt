// Card rarity tiers and their stat ranges.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.domain.model

/** Rarity tier: short [code], the allowed range for each stat, and the UI colour as ARGB hex. */
enum class Rarity(val code: String, val durability: IntRange, val speed: IntRange, val attack: IntRange, val colHex: Long) {
    COMMON("C", 25 until 75, 1 until 75, 1 until 75, 0xFF78909C),
    UNCOMMON("U", 75 until 125, 75 until 125, 75 until 125, 0xFF2E7D32),
    RARE("R", 125 until 175, 125 until 175, 125 until 175, 0xFF1565C0),
    SUPER_RARE("SR", 175 until 225, 175 until 225, 175 until 225, 0xFF6A1B9A),
    ULTRA_RARE("UR", 225..250, 225..250, 225..250, 0xFFFF8F00);

    companion object {
        /** Parses a rarity code like "SR". Throws if the code is unknown. */
        fun fromCode(code: String): Rarity = entries.first { it.code == code }
    }
}

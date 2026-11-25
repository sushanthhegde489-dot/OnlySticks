// Stat rolling for new sticks and the tuning modifiers that adjust them.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.domain.stats

import com.sushanth.onlysticks.domain.config.Cfg
import com.sushanth.onlysticks.domain.model.BaseStats
import com.sushanth.onlysticks.domain.model.EffectiveStats
import com.sushanth.onlysticks.domain.model.Rarity
import com.sushanth.onlysticks.domain.model.Stick
import com.sushanth.onlysticks.domain.model.TuningFlag
import com.sushanth.onlysticks.domain.rng.DeterministicRng
import kotlin.math.roundToInt

/** Rolls base stats for new sticks. */
object StatGen {
    /** Picks each stat uniformly within the rarity's range. */
    fun roll(rarity: Rarity, rng: DeterministicRng): BaseStats = BaseStats(
        durability = rng.nextInt(rarity.durability),
        speed = rng.nextInt(rarity.speed),
        attack = rng.nextInt(rarity.attack),
    )
}

/** Percent adjustments applied by tuning flags. */
object TuningMults {
    /** Percentage change per stat (0.15 = +15%). */
    data class Pct(val durability: Double, val speed: Double, val attack: Double)

    /** WEIGHT: tougher but slower. GRIP: faster but more fragile. QUALITY: tougher and faster but weaker hits. */
    fun pctFor(flag: TuningFlag): Pct = when (flag) {
        TuningFlag.WEIGHT -> Pct(+Cfg.TUNING_MAJOR, -Cfg.TUNING_MAJOR, +Cfg.TUNING_MINOR)
        TuningFlag.GRIP -> Pct(-Cfg.TUNING_MAJOR, +Cfg.TUNING_MAJOR, +Cfg.TUNING_MINOR)
        TuningFlag.QUALITY -> Pct(+Cfg.TUNING_MAJOR, +Cfg.TUNING_MAJOR, -Cfg.TUNING_MINOR)
    }

    /** Adds up the adjustments of several flags. */
    fun combined(flags: Set<TuningFlag>): Pct = flags.map(::pctFor).fold(Pct(0.0, 0.0, 0.0)) { a, b ->
        Pct(a.durability + b.durability, a.speed + b.speed, a.attack + b.attack)
    }

    /** Highest allowed value after tuning: the rarity's max x STAT_CAP_FACTOR. */
    fun cap(range: IntRange): Int = (range.last * Cfg.STAT_CAP_FACTOR).roundToInt()

    /** Applies tuning to base stats, clamping each result between a floor and the cap. */
    fun apply(rarity: Rarity, base: BaseStats, flags: Set<TuningFlag>): EffectiveStats {
        val p = combined(flags)
        fun scale(value: Int, pct: Double, range: IntRange, floor: Int): Int =
            (value * (1.0 + pct)).roundToInt().coerceIn(floor, cap(range))
        return EffectiveStats(
            durability = scale(base.durability, p.durability, rarity.durability, Cfg.DURABILITY_FLOOR),
            speed = scale(base.speed, p.speed, rarity.speed, Cfg.MIN_STAT),
            attack = scale(base.attack, p.attack, rarity.attack, Cfg.MIN_STAT),
        )
    }
}

/** A stick's combat stats (base stats with tuning applied). */
val Stick.effectiveStats: EffectiveStats get() = TuningMults.apply(rarity, base, tuning)

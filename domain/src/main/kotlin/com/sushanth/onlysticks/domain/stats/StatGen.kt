package com.sushanth.onlysticks.domain.stats

import com.sushanth.onlysticks.domain.config.Cfg
import com.sushanth.onlysticks.domain.model.BaseStats
import com.sushanth.onlysticks.domain.model.EffectiveStats
import com.sushanth.onlysticks.domain.model.Rarity
import com.sushanth.onlysticks.domain.model.Stick
import com.sushanth.onlysticks.domain.model.TuningFlag
import com.sushanth.onlysticks.domain.rng.DeterministicRng
import kotlin.math.roundToInt

object StatGen {
    fun roll(rarity: Rarity, rng: DeterministicRng): BaseStats = BaseStats(
        durability = rng.nextInt(rarity.durability),
        speed = rng.nextInt(rarity.speed),
        attack = rng.nextInt(rarity.attack),
    )
}

object TuningMults {
    data class Pct(val durability: Double, val speed: Double, val attack: Double)

    fun pctFor(flag: TuningFlag): Pct = when (flag) {
        TuningFlag.WEIGHT -> Pct(+Cfg.TUNING_MAJOR, -Cfg.TUNING_MAJOR, +Cfg.TUNING_MINOR)
        TuningFlag.GRIP -> Pct(-Cfg.TUNING_MAJOR, +Cfg.TUNING_MAJOR, +Cfg.TUNING_MINOR)
        TuningFlag.QUALITY -> Pct(+Cfg.TUNING_MAJOR, +Cfg.TUNING_MAJOR, -Cfg.TUNING_MINOR)
    }

    fun combined(flags: Set<TuningFlag>): Pct = flags.map(::pctFor).fold(Pct(0.0, 0.0, 0.0)) { a, b ->
        Pct(a.durability + b.durability, a.speed + b.speed, a.attack + b.attack)
    }

    fun cap(range: IntRange): Int = (range.last * Cfg.STAT_CAP_FACTOR).roundToInt()

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

val Stick.effectiveStats: EffectiveStats get() = TuningMults.apply(rarity, base, tuning)

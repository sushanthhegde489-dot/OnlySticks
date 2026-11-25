// Generates random decks for bot opponents.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.domain.pve

import com.sushanth.onlysticks.domain.config.Cfg
import com.sushanth.onlysticks.domain.model.Card
import com.sushanth.onlysticks.domain.model.PwrUp
import com.sushanth.onlysticks.domain.model.PwrUpType
import com.sushanth.onlysticks.domain.model.Rarity
import com.sushanth.onlysticks.domain.model.Stick
import com.sushanth.onlysticks.domain.model.TuningFlag
import com.sushanth.onlysticks.domain.rng.DeterministicRng
import com.sushanth.onlysticks.domain.stats.StatGen

/** Builds bot decks whose rarity scales with the difficulty tier. */
object BotDeckGenerator {
    /** A full deck of sticks, plus 1-2 power-ups at tiers that use them. */
    fun gen(tier: Int, rng: DeterministicRng): List<Card> {
        val diff = BotDifficulty(tier)
        val pwrUps = if (diff.usesPwrUps) rng.nextInt(Cfg.Bots.POWERUP_COUNT) else 0
        val sticks = (0 until Cfg.DECK_SLOTS - pwrUps).map { n ->
            rndStick("bot-$tier-$n", diff, rng)
        }
        val items = (0 until pwrUps).map { i ->
            val type = PwrUpType.entries[rng.nextInt(0, PwrUpType.entries.size)]
            PwrUp("bot-$tier-${sticks.size + i}", type)
        }
        return sticks + items
    }
    /** Picks a rarity using this tier's weights. */
    internal fun rndRar(diff: BotDifficulty, rng: DeterministicRng): Rarity =
        Rarity.entries[rng.weightedIdx(diff.rarWeights)]

    /** Rolls a random stick with random tuning flags. */
    internal fun rndStick(id: String, diff: BotDifficulty, rng: DeterministicRng): Stick {
        val rarity = rndRar(diff, rng)
        val base = StatGen.roll(rarity, rng)
        val tuning = TuningFlag.entries.filter { rng.nextBoolean(Cfg.Bots.TUNING_FLAG_CHANCE) }.toSet()
        return Stick(id, rarity, base, tuning)
    }
}

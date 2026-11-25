// Trainer ranks and the combat bonuses they give.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.domain.trainer

import com.sushanth.onlysticks.domain.combat.DamageCalc
import com.sushanth.onlysticks.domain.config.Cfg
import com.sushanth.onlysticks.domain.model.Trnr
import com.sushanth.onlysticks.domain.model.TrnrBuff
import com.sushanth.onlysticks.domain.model.TrnrModifiers

/** A trainer plus how many matches it has won. */
data class TrnrRecord(val trainer: Trnr, val wins: Int = 0)

/** Converts trainer affinity into ranks and modifiers. */
object TrainerProgression {
    /** Rank 0..MAX_RANK reached for this [affinity]. */
    fun rankFor(affinity: Int): Int =
        Cfg.Trnr.RANK_THRESHOLDS.indexOfLast { affinity >= it }.coerceIn(0, Cfg.Trnr.MAX_RANK)

    /**
     * Combat modifiers for [trainer] at its current rank (NONE if no trainer).
     * DECK_LIMIT trainers add slots at certain ranks but slow the player down.
     */
    fun modifiersFor(trainer: Trnr?): TrnrModifiers {
        if (trainer == null) return TrnrModifiers.NONE
        val rank = rankFor(trainer.affinity)
        return when (trainer.buff) {
            TrnrBuff.MAX_HP -> TrnrModifiers(bonusMaxHp = Cfg.Trnr.MAX_HP_PER_RANK * rank)
            TrnrBuff.STAMINA -> TrnrModifiers(restBonus = Cfg.Trnr.REST_BONUS_PER_RANK * rank)
            TrnrBuff.DEFENSE -> TrnrModifiers(defensePct = Cfg.Trnr.DEFENSE_PCT_PER_RANK * rank)
            TrnrBuff.DECK_LIMIT -> {
                val extra = Cfg.Trnr.DECK_SLOT_RANKS.count { rank >= it }
                TrnrModifiers(extraDeckSlots = extra, spdPenalty = Cfg.Trnr.SPEED_PENALTY_PER_EXTRA_SLOT * extra)
            }
        }
    }

    /** Affinity earned for a win. Easy bots (below FULL_XP_MIN_BOT_TIER) give reduced XP; PvP (null tier) gives full. */
    fun winAffinity(botTier: Int?): Int = when {
        botTier == null || botTier >= Cfg.Trnr.FULL_XP_MIN_BOT_TIER -> Cfg.Trnr.WIN_AFFINITY
        else -> DamageCalc.roundHalfUp(Cfg.Trnr.WIN_AFFINITY * Cfg.Trnr.EASY_BOT_XP_FACTOR)
    }

    /** Adds non-negative affinity. */
    fun addAffinity(trainer: Trnr, amount: Int): Trnr =
        trainer.copy(affinity = trainer.affinity + amount.coerceAtLeast(0))

    /** Trainer after a win. */
    fun applyWin(trainer: Trnr, botTier: Int?): Trnr = addAffinity(trainer, winAffinity(botTier))

    /** Trainer record after a win (affinity added, win count + 1). */
    fun applyWin(record: TrnrRecord, botTier: Int?): TrnrRecord =
        TrnrRecord(applyWin(record.trainer, botTier), record.wins + 1)
}

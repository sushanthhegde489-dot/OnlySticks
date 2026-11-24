package com.sushanth.onlysticks.domain.trainer

import com.sushanth.onlysticks.domain.combat.DamageCalc
import com.sushanth.onlysticks.domain.config.Cfg
import com.sushanth.onlysticks.domain.model.Trnr
import com.sushanth.onlysticks.domain.model.TrnrBuff
import com.sushanth.onlysticks.domain.model.TrnrModifiers

data class TrnrRecord(val trainer: Trnr, val wins: Int = 0)

object TrainerProgression {
    fun rankFor(affinity: Int): Int =
        Cfg.Trnr.RANK_THRESHOLDS.indexOfLast { affinity >= it }.coerceIn(0, Cfg.Trnr.MAX_RANK)

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

    fun winAffinity(botTier: Int?): Int = when {
        botTier == null || botTier >= Cfg.Trnr.FULL_XP_MIN_BOT_TIER -> Cfg.Trnr.WIN_AFFINITY
        else -> DamageCalc.roundHalfUp(Cfg.Trnr.WIN_AFFINITY * Cfg.Trnr.EASY_BOT_XP_FACTOR)
    }

    fun addAffinity(trainer: Trnr, amount: Int): Trnr =
        trainer.copy(affinity = trainer.affinity + amount.coerceAtLeast(0))

    fun applyWin(trainer: Trnr, botTier: Int?): Trnr = addAffinity(trainer, winAffinity(botTier))

    fun applyWin(record: TrnrRecord, botTier: Int?): TrnrRecord =
        TrnrRecord(applyWin(record.trainer, botTier), record.wins + 1)
}

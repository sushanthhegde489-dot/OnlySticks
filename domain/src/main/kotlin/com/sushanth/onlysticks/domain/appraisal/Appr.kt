package com.sushanth.onlysticks.domain.appraisal

import com.sushanth.onlysticks.domain.combat.DamageCalc
import com.sushanth.onlysticks.domain.config.Cfg
import com.sushanth.onlysticks.domain.model.Card
import com.sushanth.onlysticks.domain.model.PwrUp
import com.sushanth.onlysticks.domain.model.PwrUpType
import com.sushanth.onlysticks.domain.model.Stick
import com.sushanth.onlysticks.domain.model.Trnr
import com.sushanth.onlysticks.domain.stats.effectiveStats
import com.sushanth.onlysticks.domain.trainer.TrainerProgression

object Appr {
    fun value(card: Card): Int = when (card) {
        is Stick -> DamageCalc.roundHalfUp(Cfg.Appr.tierMult(card.rarity) * card.effectiveStats.tot)
        is PwrUp -> when (card.type) {
            PwrUpType.LIGHTER -> Cfg.Appr.LIGHTER_VALUE
            PwrUpType.WATER_BOTTLE -> Cfg.Appr.WATER_BOTTLE_VALUE
        }
        is Trnr -> Cfg.Appr.TRAINER_BASE_VALUE +
            Cfg.Appr.TRAINER_VALUE_PER_RANK * TrainerProgression.rankFor(card.affinity)
    }

    fun tot(cardList: List<Card>): Int = cardList.sumOf { value(it) }
}

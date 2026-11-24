package com.sushanth.onlysticks.domain.economy

import com.sushanth.onlysticks.domain.appraisal.Appr
import com.sushanth.onlysticks.domain.config.Cfg
import com.sushanth.onlysticks.domain.model.Card
import java.math.BigDecimal
import kotlin.math.abs
import kotlin.math.max

object BetRules {
    fun withinTolerance(a: Int, b: Int): Boolean {
        val diff = BigDecimal.valueOf(abs(a.toLong() - b.toLong()))
        val allowed = BigDecimal.valueOf(Cfg.Economy.STAKE_TOLERANCE).multiply(BigDecimal.valueOf(max(a, b).toLong()))
        return diff <= allowed
    }

    fun validBetCnt(n: Int): Boolean = n in Cfg.MIN_WAGER_CARDS..Cfg.MAX_WAGER_CARDS

    fun canStart(plrBet: List<Card>, oppBet: List<Card>): Boolean =
        validBetCnt(plrBet.size) && validBetCnt(oppBet.size) &&
            withinTolerance(Appr.tot(plrBet), Appr.tot(oppBet))
}

// Rules for card wagers: how many cards can be bet and how close the two stakes must be.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.domain.economy

import com.sushanth.onlysticks.domain.appraisal.Appr
import com.sushanth.onlysticks.domain.config.Cfg
import com.sushanth.onlysticks.domain.model.Card
import java.math.BigDecimal
import kotlin.math.abs
import kotlin.math.max

/** Validates card bets placed before a wagered duel. */
object BetRules {
    /** True when the gap between [a] and [b] is within STAKE_TOLERANCE (15%) of the larger value. */
    fun withinTolerance(a: Int, b: Int): Boolean {
        val diff = BigDecimal.valueOf(abs(a.toLong() - b.toLong()))
        val allowed = BigDecimal.valueOf(Cfg.Economy.STAKE_TOLERANCE).multiply(BigDecimal.valueOf(max(a, b).toLong()))
        return diff <= allowed
    }

    /** A bet must contain between MIN_WAGER_CARDS and MAX_WAGER_CARDS cards. */
    fun validBetCnt(n: Int): Boolean = n in Cfg.MIN_WAGER_CARDS..Cfg.MAX_WAGER_CARDS

    /** Both bets have a legal size and their appraised totals are close enough to be fair. */
    fun canStart(plrBet: List<Card>, oppBet: List<Card>): Boolean =
        validBetCnt(plrBet.size) && validBetCnt(oppBet.size) &&
            withinTolerance(Appr.tot(plrBet), Appr.tot(oppBet))
}

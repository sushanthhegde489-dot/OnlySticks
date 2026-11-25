// Daily login streak: rewards grow over a 7-day cycle and reset if a day is missed.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.domain.economy

import com.sushanth.onlysticks.domain.config.Cfg
import java.time.LocalDate

/** Today's offer: which day of the streak (1-7) and how many coins it pays. */
data class DailyBonusOffer(val day: Int, val amount: Int)

/** Computes the daily bonus from the dates of previous claims. */
object DailyBonus {
    private val rwds get() = Cfg.Economy.DAILY_STREAK_REWARDS

    /** Offer for [today] given the dates of earlier claims. */
    fun offerFor(recentClaims: List<LocalDate>, today: LocalDate): DailyBonusOffer {
        val day = dayFor(recentClaims, today)
        return DailyBonusOffer(day, rwds[day - 1])
    }

    /** Counts consecutive claimed days ending yesterday, then wraps it into the 1..7 reward cycle. */
    fun dayFor(recentClaims: List<LocalDate>, today: LocalDate): Int {
        val dates = recentClaims.toSortedSet().reversed()
        var exp = today.minusDays(1)
        var run = 0
        for (date in dates) {
            if (date == today) continue
            if (date != exp) break
            run++
            exp = exp.minusDays(1)
        }
        return run % rwds.size + 1
    }
}

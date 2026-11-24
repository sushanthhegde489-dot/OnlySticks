package com.sushanth.onlysticks.domain.economy

import com.sushanth.onlysticks.domain.config.Cfg
import java.time.LocalDate

data class DailyBonusOffer(val day: Int, val amount: Int)

object DailyBonus {
    private val rwds get() = Cfg.Economy.DAILY_STREAK_REWARDS

    fun offerFor(recentClaims: List<LocalDate>, today: LocalDate): DailyBonusOffer {
        val day = dayFor(recentClaims, today)
        return DailyBonusOffer(day, rwds[day - 1])
    }

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

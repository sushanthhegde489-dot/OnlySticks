package com.sushanth.onlysticks.domain.economy

import com.sushanth.onlysticks.domain.config.Cfg
import java.time.LocalDate

sealed interface StpClaimRes {
    data class Accepted(val claimDate: LocalDate, val affinity: Int, val coins: Int) : StpClaimRes
    data class Rejected(val reason: ClaimRejection) : StpClaimRes
}

object StepRewards {
    fun claim(
        lastClaimDate: LocalDate?,
        lastClaimEpochMs: Long?,
        claimDate: LocalDate,
        nowEpochMs: Long,
        stps: Long,
    ): StpClaimRes {
        ClaimGuard.check(lastClaimDate, lastClaimEpochMs, claimDate, nowEpochMs)?.let { return StpClaimRes.Rejected(it) }
        return StpClaimRes.Accepted(claimDate, affinityFor(stps), coinsFor(stps))
    }
    fun affinityFor(stps: Long): Int =
        (stps.coerceIn(0L, Cfg.Trnr.DAILY_STEP_PROGRESSION_CAP.toLong()) / Cfg.Trnr.STEPS_PER_AFFINITY).toInt()

    fun coinsFor(stps: Long): Int =
        (stps.coerceIn(0L, Cfg.Economy.STEP_CURRENCY_DAILY_CAP.toLong()) / 100).toInt()
}

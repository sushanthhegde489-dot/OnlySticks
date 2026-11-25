// Claims step rewards for the previous day.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.health

import androidx.room.withTransaction
import com.sushanth.onlysticks.data.local.AppDatabase
import com.sushanth.onlysticks.data.repository.CoinsRepository
import com.sushanth.onlysticks.data.repository.TrainerRepository
import com.sushanth.onlysticks.domain.economy.ClaimRejection
import com.sushanth.onlysticks.domain.economy.CoinsRules
import com.sushanth.onlysticks.domain.economy.LdgrReason
import com.sushanth.onlysticks.domain.economy.StpClaimRes
import com.sushanth.onlysticks.domain.economy.StepRewards
import java.time.Clock
import java.time.LocalDate

/** What happened when step sync ran. */
sealed interface StpSyncOutcome {
    data object NoPerm : StpSyncOutcome
    data class Claimed(val date: LocalDate, val stps: Long, val affinity: Int, val coins: Int, val trainerId: String?) : StpSyncOutcome
    data class Rejected(val date: LocalDate, val reason: ClaimRejection) : StpSyncOutcome
}

/** Reads yesterday's steps and, if not already claimed, awards coins and trainer affinity. */
class StpSync(
    private val db: AppDatabase,
    private val stps: StpSrc,
    private val trnrs: TrainerRepository,
    private val coinsRepo: CoinsRepository,
    private val clock: Clock,
) {
    /** Claims yesterday's steps. Anti-cheat uses the latest claim from both trainers and the coin ledger. */
    suspend fun run(): StpSyncOutcome {
        if (!stps.hasPerm()) return StpSyncOutcome.NoPerm
        val zone = clock.zone
        val date = LocalDate.now(clock).minusDays(1)
        val tot = stps.stpsOn(date, zone)
        return db.withTransaction {
            val now = clock.millis()
            val last = trnrs.lastStpClaim().latest(coinsRepo.lastClaim(LdgrReason.STEPS))
            when (val result = StepRewards.claim(last.date, last.epochMs, date, now, tot)) {
                is StpClaimRes.Rejected -> StpSyncOutcome.Rejected(date, result.reason)
                is StpClaimRes.Accepted -> {
                    coinsRepo.append(CoinsRules.stps(result, now))
                    val trainerId = trnrs.equippedTrnrId()
                    trainerId?.let { trnrs.recordStpClaim(it, date, now, result.affinity) }
                    StpSyncOutcome.Claimed(date, tot, result.affinity, result.coins, trainerId)
                }
            }
        }
    }
}

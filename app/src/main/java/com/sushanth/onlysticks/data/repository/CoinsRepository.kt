// Coin balance and daily login bonus.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.repository

import androidx.room.withTransaction
import com.sushanth.onlysticks.data.local.AppDatabase
import com.sushanth.onlysticks.data.local.entity.CoinsLdgrEntity
import com.sushanth.onlysticks.domain.config.Cfg
import com.sushanth.onlysticks.domain.economy.ClaimGuard
import com.sushanth.onlysticks.domain.economy.CoinsRules
import com.sushanth.onlysticks.domain.economy.DailyBonus
import com.sushanth.onlysticks.domain.economy.DailyBonusOffer
import com.sushanth.onlysticks.domain.economy.LdgrEntry
import com.sushanth.onlysticks.domain.economy.LdgrReason
import kotlinx.coroutines.flow.Flow
import java.time.Clock
import java.time.LocalDate

/** Wraps the coin ledger table. */
class CoinsRepository(private val db: AppDatabase, private val clock: Clock) {
    private val dao = db.coinsLdgrDao()

    /** Live coin balance. */
    fun observeBal(): Flow<Long> = dao.observeBal()

    /** Writes a domain [LdgrEntry] to the database. */
    suspend fun append(entry: LdgrEntry) {
        dao.append(
            CoinsLdgrEntity(
                amount = entry.amount,
                reason = entry.reason.name,
                timestamp = entry.timestampMs,
                claimDate = entry.claimDate?.toString(),
            ),
        )
    }

    /** Date and time of the most recent claim for [reason]. */
    suspend fun lastClaim(reason: LdgrReason): LastClaim =
        dao.latestForReason(reason.name)?.let { LastClaim(it.claimDate?.let(LocalDate::parse), it.timestamp) } ?: LastClaim.NONE

    /** Today's daily bonus offer, or null if it was already claimed (or the clock looks tampered with). */
    suspend fun dlyBonusOffer(): DailyBonusOffer? {
        val last = lastClaim(LdgrReason.DAILY_LOGIN)
        val today = LocalDate.now(clock)
        if (ClaimGuard.check(last.date, last.epochMs, today, clock.millis()) != null) return null
        return DailyBonus.offerFor(recentClaims(), today)
    }

    /** Claims today's daily bonus inside a transaction. Returns the entry, or null if not allowed. */
    suspend fun claimDlyLogin(): LdgrEntry? = db.withTransaction {
        val last = lastClaim(LdgrReason.DAILY_LOGIN)
        val today = LocalDate.now(clock)
        val offer = DailyBonus.offerFor(recentClaims(), today)
        CoinsRules.dlyLogin(last.date, last.epochMs, today, clock.millis(), offer.amount)?.also { append(it) }
    }

    /** Recent daily-login claim dates, enough to work out the current streak. */
    private suspend fun recentClaims(): List<LocalDate> =
        dao.recentClaimDates(LdgrReason.DAILY_LOGIN.name, Cfg.Economy.DAILY_STREAK_REWARDS.size + 1).map(LocalDate::parse)
}

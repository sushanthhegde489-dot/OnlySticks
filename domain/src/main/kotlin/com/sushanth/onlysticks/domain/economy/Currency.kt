package com.sushanth.onlysticks.domain.economy

import com.sushanth.onlysticks.domain.config.Cfg
import java.time.LocalDate

enum class LdgrReason { DAILY_LOGIN, PVE_WIN, PVE_LOSS, STEPS, ANTE_STAKE, ANTE_PAYOUT, PACK_PURCHASE, CARD_PURCHASE, ADMIN_GRANT, STARTER_BONUS }

data class LdgrEntry(val amount: Long, val reason: LdgrReason, val timestampMs: Long, val claimDate: LocalDate? = null)

object CoinsRules {
    fun bal(entries: List<LdgrEntry>): Long = entries.sumOf { it.amount }

    fun startBonus(nowMs: Long, amount: Long = Cfg.Economy.STARTING_CURRENCY): LdgrEntry =
        LdgrEntry(amount, LdgrReason.STARTER_BONUS, nowMs)

    fun dlyLogin(
        lastClaimDate: LocalDate?,
        lastClaimEpochMs: Long?,
        today: LocalDate,
        nowMs: Long,
        amount: Int = Cfg.Economy.DAILY_LOGIN,
    ): LdgrEntry? {
        if (ClaimGuard.check(lastClaimDate, lastClaimEpochMs, today, nowMs) != null) return null
        return LdgrEntry(amount.toLong(), LdgrReason.DAILY_LOGIN, nowMs, today)
    }

    fun pveRes(won: Boolean, botTier: Int, nowMs: Long): LdgrEntry =
        if (won) LdgrEntry(Cfg.Economy.PVE_WIN_PER_TIER.toLong() * botTier, LdgrReason.PVE_WIN, nowMs)
        else LdgrEntry(5L, LdgrReason.PVE_LOSS, nowMs)

    fun pveAnte(botTier: Int): Long = 10L * botTier

    fun pvePot(botTier: Int): Long = 2 * pveAnte(botTier)

    fun anteBet(botTier: Int, nowMs: Long): LdgrEntry = LdgrEntry(-pveAnte(botTier), LdgrReason.ANTE_STAKE, nowMs)

    fun antePayout(botTier: Int, nowMs: Long): LdgrEntry = LdgrEntry(pvePot(botTier), LdgrReason.ANTE_PAYOUT, nowMs)

    fun buy(reason: LdgrReason, price: Long, nowMs: Long): LdgrEntry {
        require(price > 0) { "price must be positive" }
        return LdgrEntry(-price, reason, nowMs)
    }

    fun stps(accepted: StpClaimRes.Accepted, nowMs: Long): LdgrEntry =
        LdgrEntry(accepted.coins.toLong(), LdgrReason.STEPS, nowMs, accepted.claimDate)
}

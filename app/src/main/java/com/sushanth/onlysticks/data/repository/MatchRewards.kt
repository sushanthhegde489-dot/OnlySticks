// Coin ante, payouts and trainer XP for bot (PvE) matches.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.repository

import androidx.room.withTransaction
import com.sushanth.onlysticks.data.local.AppDatabase
import com.sushanth.onlysticks.data.local.entity.ActiveMatchEntity
import com.sushanth.onlysticks.domain.economy.CoinsRules
import java.time.Clock

/** Result of a finished bot match. */
data class MatchOutcome(val matchId: String, val won: Boolean)

/** Callbacks the duel screen fires as a bot match progresses. Default no-op implementations; [NONE] ignores everything. */
interface MatchLifecycleSink {
    fun onMatchStarted(matchId: String, botTier: Int, trainerId: String?) {}
    fun onPwrUpUsed(cardId: String) {}
    fun onMatchEnded(outcome: MatchOutcome) {}
    companion object {
        val NONE = object : MatchLifecycleSink {}
    }
}
/** Records bot match start/end in the database and pays out rewards. */
class MatchRewards(
    private val db: AppDatabase,
    private val cardList: CardRepository,
    private val trnrs: TrainerRepository,
    private val coinsRepo: CoinsRepository,
    private val clock: Clock,
) {
    private val active get() = db.activeMatchDao()
    /** Settles any leftover match, records this one as active and charges the ante. */
    suspend fun start(matchId: String, botTier: Int, trainerId: String?) {
        val now = clock.millis()
        db.withTransaction {
            settleUnfinishedInTransaction(now)
            active.insert(ActiveMatchEntity(matchId, botTier, trainerId, now))
            coinsRepo.append(CoinsRules.anteBet(botTier, now))
        }
    }
    /** Deletes a power-up the player used during the match. */
    suspend fun pwrUpUsed(cardId: String) = cardList.consumePwrUp(cardId)
    /** Pays out for a finished match. Does nothing if the match was already settled (e.g. as a forfeit). */
    suspend fun end(outcome: MatchOutcome) {
        val now = clock.millis()
        db.withTransaction {
            val match = active.get(outcome.matchId) ?: return@withTransaction
            active.delete(match.matchId)
            coinsRepo.append(CoinsRules.pveRes(outcome.won, match.botTier, now))
            if (outcome.won) {
                coinsRepo.append(CoinsRules.antePayout(match.botTier, now))
                match.trainerId?.let { trnrs.recordWin(it, match.botTier) }
            }
        }
    }
    /** Settles matches left open by a crash or force-quit (counted as losses). */
    suspend fun settleUnfinished() {
        val now = clock.millis()
        db.withTransaction { settleUnfinishedInTransaction(now) }
    }
    /** Same as [settleUnfinished] but must be called inside an existing transaction. */
    private suspend fun settleUnfinishedInTransaction(now: Long) {
        active.all().forEach { match ->
            active.delete(match.matchId)
            coinsRepo.append(CoinsRules.pveRes(won = false, botTier = match.botTier, nowMs = now))
        }
    }
}

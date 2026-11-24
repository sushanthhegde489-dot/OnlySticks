package com.sushanth.onlysticks.data.repository

import androidx.room.withTransaction
import com.sushanth.onlysticks.data.local.AppDatabase
import com.sushanth.onlysticks.data.local.entity.ActiveMatchEntity
import com.sushanth.onlysticks.domain.economy.CoinsRules
import java.time.Clock

data class MatchOutcome(val matchId: String, val won: Boolean)

interface MatchLifecycleSink {
    fun onMatchStarted(matchId: String, botTier: Int, trainerId: String?) {}
    fun onPwrUpUsed(cardId: String) {}
    fun onMatchEnded(outcome: MatchOutcome) {}
    companion object {
        val NONE = object : MatchLifecycleSink {}
    }
}
class MatchRewards(
    private val db: AppDatabase,
    private val cardList: CardRepository,
    private val trnrs: TrainerRepository,
    private val coinsRepo: CoinsRepository,
    private val clock: Clock,
) {
    private val active get() = db.activeMatchDao()
    suspend fun start(matchId: String, botTier: Int, trainerId: String?) {
        val now = clock.millis()
        db.withTransaction {
            settleUnfinishedInTransaction(now)
            active.insert(ActiveMatchEntity(matchId, botTier, trainerId, now))
            coinsRepo.append(CoinsRules.anteBet(botTier, now))
        }
    }
    suspend fun pwrUpUsed(cardId: String) = cardList.consumePwrUp(cardId)
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
    suspend fun settleUnfinished() {
        val now = clock.millis()
        db.withTransaction { settleUnfinishedInTransaction(now) }
    }
    private suspend fun settleUnfinishedInTransaction(now: Long) {
        active.all().forEach { match ->
            active.delete(match.matchId)
            coinsRepo.append(CoinsRules.pveRes(won = false, botTier = match.botTier, nowMs = now))
        }
    }
}

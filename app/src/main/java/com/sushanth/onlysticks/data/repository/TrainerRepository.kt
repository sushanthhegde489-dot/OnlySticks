package com.sushanth.onlysticks.data.repository

import com.sushanth.onlysticks.data.local.AppDatabase
import com.sushanth.onlysticks.data.local.entity.TrnrEntity
import com.sushanth.onlysticks.domain.model.Trnr
import com.sushanth.onlysticks.domain.trainer.TrainerProgression
import com.sushanth.onlysticks.domain.trainer.TrnrRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate

data class LastClaim(val date: LocalDate?, val epochMs: Long?) {
    fun latest(other: LastClaim): LastClaim = LastClaim(
        date = listOfNotNull(date, other.date).maxOrNull(),
        epochMs = listOfNotNull(epochMs, other.epochMs).maxOrNull(),
    )

    companion object {
        val NONE = LastClaim(null, null)
    }
}

class TrainerRepository(db: AppDatabase, private val clock: Clock) {
    private val dao = db.trnrDao()
    private val deckDao = db.deckDao()

    fun observeTrnrs(): Flow<List<TrnrRecord>> = dao.observeAll().map { rows -> rows.map(TrnrMapper::toRecord) }

    suspend fun all(): List<TrnrEntity> = dao.observeAll().first()

    suspend fun save(trainer: Trnr, createdAt: Long? = null) {
        val existing = dao.getById(trainer.id)
        if (existing == null) {
            dao.upsert(TrnrMapper.toEntity(trainer, createdAt ?: clock.millis()))
        } else {
            dao.upsert(TrnrMapper.update(existing, TrnrRecord(trainer, existing.wins)))
        }
    }

    suspend fun overwriteProg(trainerId: String, affinity: Int, wins: Int): Boolean {
        val row = dao.getById(trainerId) ?: return false
        dao.upsert(row.copy(affinity = affinity, rank = TrainerProgression.rankFor(affinity), wins = wins))
        return true
    }

    suspend fun delete(trainerId: String) = dao.deleteById(trainerId)

    suspend fun rename(trainerId: String, nick: String?): Boolean {
        val row = dao.getById(trainerId) ?: return false
        dao.upsert(row.copy(customName = nick?.trim()?.ifEmpty { null }))
        return true
    }

    suspend fun equippedTrnrId(): String? =
        deckDao.observeDecks().first().firstNotNullOfOrNull { it.deck.trainerId } ?: all().firstOrNull()?.id

    suspend fun recordWin(trainerId: String, botTier: Int): TrnrRecord? {
        val row = dao.getById(trainerId) ?: return null
        val record = TrainerProgression.applyWin(TrnrMapper.toRecord(row), botTier)
        dao.upsert(TrnrMapper.update(row, record))
        return record
    }

    suspend fun lastStpClaim(): LastClaim = all().fold(LastClaim.NONE) { acc, row ->
        acc.latest(LastClaim(row.lastStepClaimDate?.let(LocalDate::parse), row.lastStepClaimEpochMs))
    }

    suspend fun recordStpClaim(trainerId: String, date: LocalDate, epochMs: Long, affinity: Int) {
        val row = dao.getById(trainerId) ?: return
        val trainer = TrainerProgression.addAffinity(Trnr(id = row.id, buff = row.buff, affinity = row.affinity, customName = row.customName), affinity)
        dao.upsert(
            TrnrMapper.update(row, TrnrRecord(trainer, row.wins))
                .copy(lastStepClaimDate = date.toString(), lastStepClaimEpochMs = epochMs),
        )
    }
}

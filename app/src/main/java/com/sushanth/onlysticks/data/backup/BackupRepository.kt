// Converts the local database to and from a backup snapshot.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.backup

import androidx.room.withTransaction
import com.sushanth.onlysticks.data.local.AppDatabase
import com.sushanth.onlysticks.data.local.entity.DeckEntity
import com.sushanth.onlysticks.data.local.entity.DeckSlotEntity
import com.sushanth.onlysticks.data.repository.CardRepository
import com.sushanth.onlysticks.data.repository.ProfileRepository
import com.sushanth.onlysticks.data.repository.StatsRepository
import kotlinx.coroutines.flow.first
import java.time.Clock

/** Builds backup snapshots from the database and restores them. */
class BackupRepository(
    private val db: AppDatabase,
    private val cardList: CardRepository,
    private val prof: ProfileRepository,
    private val stats: StatsRepository,
    private val clock: Clock,
) {
    /** Reads all cards, trainers, decks, coin ledger, store purchases and stats in one transaction. */
    suspend fun export(): BkpSnap = db.withTransaction {
        val owned = cardList.observeCards().first()
        val createdAt = db.cardDao().getAll().associate { it.id to it.createdAt }
        BkpSnap(
            version = BkpSnap.VERSION,
            exportedAt = clock.millis(),
            displayName = prof.displayName()?.ifEmpty { null },
            cardList = owned.map { TimedCard(it, createdAt[it.id] ?: 0L) },
            trnrs = db.trnrDao().observeAll().first(),
            decks = db.deckDao().observeDecks().first().map { d ->
                DeckRecord(d.deck.id, d.deck.name, d.deck.trainerId, d.slots.sortedBy { it.slotIndex }.map { it.cardId })
            },
            ldgr = db.coinsLdgrDao().all(),
            storeBuys = db.storeBuyDao().all(),
            stats = stats.all().mapKeys { it.key.key },
        )
    }

    /**
     * Replaces the local collection with [snap] in one transaction.
     * Deck slots/trainers that point at missing cards are dropped; ledger ids are regenerated.
     * The display name is only restored if none is set locally.
     */
    suspend fun restore(snap: BkpSnap) = db.withTransaction {
        db.clearPlrColl()
        snap.cardList.forEach { cardList.save(it.card, it.createdAt) }
        snap.trnrs.forEach { db.trnrDao().upsert(it) }
        val cardIds = snap.cardList.map { it.card.id }.toSet()
        val trnrIds = snap.trnrs.map { it.id }.toSet()
        snap.decks.forEach { d ->
            db.deckDao().upsertDeck(DeckEntity(d.id, d.name, d.trainerId?.takeIf { it in trnrIds }))
            db.deckDao().replaceSlots(d.id, d.cardIds.filter { it in cardIds }.mapIndexed { i, id -> DeckSlotEntity(d.id, i, id) })
        }
        db.coinsLdgrDao().insertAll(snap.ldgr.map { it.copy(id = 0) })
        db.storeBuyDao().insertAll(snap.storeBuys)
        stats.restore(snap.stats)
        if (prof.displayName().isNullOrEmpty()) snap.displayName?.let { prof.setDispName(it) }
    }
}

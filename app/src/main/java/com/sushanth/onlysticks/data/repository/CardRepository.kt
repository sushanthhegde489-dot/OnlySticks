// Storage for owned cards with tamper detection.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.repository

import com.sushanth.onlysticks.data.local.AppDatabase
import com.sushanth.onlysticks.data.local.entity.CardEntity
import com.sushanth.onlysticks.data.local.entity.CardKind
import com.sushanth.onlysticks.data.security.CardFieldEncoding
import com.sushanth.onlysticks.data.security.CardSigner
import com.sushanth.onlysticks.data.security.SecurityLog
import com.sushanth.onlysticks.domain.model.Card
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Clock
import java.util.concurrent.ConcurrentHashMap

/**
 * Reads and writes cards. Every row is signed on write and verified on read; rows that fail
 * verification are quarantined and hidden.
 */
class CardRepository(
    db: AppDatabase,
    private val signr: CardSigner,
    private val log: SecurityLog,
    private val clock: Clock,
) {
    private val dao = db.cardDao()
    /** Cache of signatures that already verified (keyed by signature, checked against the exact bytes) to avoid redoing HMACs. */
    private val passedSigs = ConcurrentHashMap<String, ByteArray>()
    /** Live list of verified cards. */
    fun observeCards(): Flow<List<Card>> =
        dao.observeAll().map { rows -> rows.mapNotNull { verified(it) } }.flowOn(Dispatchers.Default)
    suspend fun isEmpty(): Boolean = dao.getAll().isEmpty()
    /** Signs and saves a card. Keeps the original createdAt if the card already exists. */
    suspend fun save(card: Card, createdAt: Long? = null) {
        val created = createdAt ?: dao.observeById(card.id).first()?.createdAt ?: clock.millis()
        dao.upsert(withContext(Dispatchers.Default) { sign(CardMapper.toEntity(card, created)) })
    }
    /** Removes a power-up after it's used. Returns false if it doesn't exist or isn't a power-up. */
    suspend fun consumePwrUp(cardId: String): Boolean {
        val row = dao.observeById(cardId).first() ?: return false
        if (row.kind != CardKind.POWER_UP) return false
        dao.deleteById(cardId)
        return true
    }
    suspend fun delete(cardId: String) = dao.deleteById(cardId)
    /** Sets or clears a card's nickname (re-signs the row). Refuses quarantined/tampered cards. */
    suspend fun rename(cardId: String, nick: String?): Boolean {
        val row = dao.observeById(cardId).first() ?: return false
        if (row.quarantined || verified(row) == null) return false
        dao.upsert(withContext(Dispatchers.Default) { sign(row.copy(customName = nick?.trim()?.ifEmpty { null })) })
        return true
    }
    /** Returns [entity] with a fresh signature. */
    private fun sign(entity: CardEntity): CardEntity =
        entity.copy(signature = signr.sign(CardFieldEncoding.encode(entity)), quarantined = false)

    /** Converts a row to a card if its signature is valid; otherwise quarantines it and returns null. */
    private suspend fun verified(row: CardEntity): Card? {
        val card = if (sigOk(row)) CardMapper.toDomain(row) else null
        if (card == null) {
            log.warn("Card ${row.id} failed its integrity check and was quarantined")
            dao.setQuar(row.id, true)
        }
        return card
    }

    /** Checks the row's signature, using the cache when possible. */
    private suspend fun sigOk(row: CardEntity): Boolean = withContext(Dispatchers.Default) {
        val fields = CardFieldEncoding.encode(row)
        if (passedSigs[row.signature]?.contentEquals(fields) == true) return@withContext true
        val ok = signr.verify(fields, row.signature)
        if (ok) passedSigs[row.signature] = fields
        ok
    }
}

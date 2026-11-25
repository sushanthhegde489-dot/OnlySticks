// Card escrow and settlement for PvP wagers.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.repository

import androidx.room.withTransaction
import com.sushanth.onlysticks.data.local.AppDatabase
import com.sushanth.onlysticks.data.local.entity.PvpEscrwEntity
import com.sushanth.onlysticks.domain.model.Stick
import java.util.UUID

/** Locks staked cards while a PvP match runs and transfers them when it ends. */
class PvpRewards(private val db: AppDatabase, private val cardList: CardRepository) {
    private val escrw = db.pvpEscrwDao()

    /** Puts [bet] in escrow (forfeiting anything left over from an abandoned match first). */
    suspend fun hold(bet: List<Stick>) = db.withTransaction {
        forfeitHeld()
        escrw.insertAll(bet.map { PvpEscrwEntity(it.id) })
    }

    /** Releases escrow without changes (match voided, e.g. disconnected before it began). */
    suspend fun release() = escrw.clear()

    suspend fun pwrUpUsed(cardId: String) = cardList.consumePwrUp(cardId)

    /** Winner gets copies of the loser's staked sticks (with fresh ids); loser's staked sticks are deleted. */
    suspend fun settle(won: Boolean, myBet: List<Stick>, theirBet: List<Stick>) = db.withTransaction {
        if (won) theirBet.forEach { cardList.save(it.copy(id = "won-${UUID.randomUUID()}", customName = null)) }
        else myBet.forEach { cardList.delete(it.id) }
        escrw.clear()
    }

    /** Called on start-up: cards still in escrow belong to a match that was abandoned, so they are lost. */
    suspend fun forfeitAbandoned() = db.withTransaction { forfeitHeld() }

    private suspend fun forfeitHeld() {
        escrw.cardIds().forEach { cardList.delete(it) }
        escrw.clear()
    }
}

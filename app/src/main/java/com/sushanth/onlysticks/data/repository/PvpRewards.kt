package com.sushanth.onlysticks.data.repository

import androidx.room.withTransaction
import com.sushanth.onlysticks.data.local.AppDatabase
import com.sushanth.onlysticks.data.local.entity.PvpEscrwEntity
import com.sushanth.onlysticks.domain.model.Stick
import java.util.UUID

class PvpRewards(private val db: AppDatabase, private val cardList: CardRepository) {
    private val escrw = db.pvpEscrwDao()

    suspend fun hold(bet: List<Stick>) = db.withTransaction {
        forfeitHeld()
        escrw.insertAll(bet.map { PvpEscrwEntity(it.id) })
    }

    suspend fun release() = escrw.clear()

    suspend fun pwrUpUsed(cardId: String) = cardList.consumePwrUp(cardId)

    suspend fun settle(won: Boolean, myBet: List<Stick>, theirBet: List<Stick>) = db.withTransaction {
        if (won) theirBet.forEach { cardList.save(it.copy(id = "won-${UUID.randomUUID()}", customName = null)) }
        else myBet.forEach { cardList.delete(it.id) }
        escrw.clear()
    }

    suspend fun forfeitAbandoned() = db.withTransaction { forfeitHeld() }

    private suspend fun forfeitHeld() {
        escrw.cardIds().forEach { cardList.delete(it) }
        escrw.clear()
    }
}

package com.sushanth.onlysticks.data.repository

import androidx.room.withTransaction
import com.sushanth.onlysticks.data.local.AppDatabase
import com.sushanth.onlysticks.domain.economy.LdgrEntry
import com.sushanth.onlysticks.domain.economy.LdgrReason
import com.sushanth.onlysticks.domain.gacha.PackOpnr
import com.sushanth.onlysticks.domain.gacha.PackRes
import com.sushanth.onlysticks.domain.gacha.PackType
import com.sushanth.onlysticks.domain.model.BaseStats
import com.sushanth.onlysticks.domain.model.Card
import com.sushanth.onlysticks.domain.model.PwrUp
import com.sushanth.onlysticks.domain.model.PwrUpType
import com.sushanth.onlysticks.domain.model.Rarity
import com.sushanth.onlysticks.domain.model.Stick
import com.sushanth.onlysticks.domain.model.Trnr
import com.sushanth.onlysticks.domain.model.TrnrBuff
import com.sushanth.onlysticks.domain.model.TuningFlag
import com.sushanth.onlysticks.domain.rng.DeterministicRng
import com.sushanth.onlysticks.domain.stats.StatGen
import java.security.SecureRandom
import java.time.Clock
import java.util.UUID

class NotAdminException : IllegalStateException("admin only")

class AdminRepository(
    private val db: AppDatabase,
    private val prof: ProfileRepository,
    private val cardList: CardRepository,
    private val trnrs: TrainerRepository,
    private val coinsRepo: CoinsRepository,
    private val opnr: PackOpnr,
    private val sett: SettingsRepository,
    private val clock: Clock,
    private val rng: DeterministicRng = DeterministicRng(SecureRandom().nextLong()),
) {
    suspend fun adjCoins(amount: Long): Long = admin {
        db.withTransaction {
            val bal = db.coinsLdgrDao().bal()
            val applied = amount.coerceAtLeast(-bal)
            if (applied != 0L) coinsRepo.append(LdgrEntry(applied, LdgrReason.ADMIN_GRANT, clock.millis()))
            bal + applied
        }
    }

    suspend fun createStick(rarity: Rarity, stats: BaseStats? = null, tuning: Set<TuningFlag> = emptySet()): Stick = admin {
        Stick(newId(), rarity, stats ?: StatGen.roll(rarity, rng), tuning).also { cardList.save(it) }
    }

    suspend fun createPwrUp(type: PwrUpType): PwrUp = admin { PwrUp(newId(), type).also { cardList.save(it) } }

    suspend fun createTrnr(buff: TrnrBuff, affinity: Int = 0): Trnr = admin {
        Trnr(newId(), buff, affinity.coerceAtLeast(0)).also { trnrs.save(it) }
    }

    suspend fun updateCard(card: Card) = admin {
        when (card) {
            is Trnr -> trnrs.save(card)
            else -> cardList.save(card)
        }
    }

    suspend fun setTrnrProg(trainerId: String, affinity: Int, wins: Int) = admin {
        trnrs.overwriteProg(trainerId, affinity.coerceAtLeast(0), wins.coerceAtLeast(0))
    }

    suspend fun deleteCard(card: Card) = admin {
        when (card) {
            is Trnr -> trnrs.delete(card.id)
            else -> cardList.delete(card.id)
        }
    }

    suspend fun openFreePack(type: PackType): PackRes = admin {
        db.withTransaction {
            val result = opnr.open(type)
            val now = clock.millis()
            result.cardList.forEachIndexed { i, card ->
                if (card is Trnr) trnrs.save(card, now + i) else cardList.save(card, now + i)
            }
            result
        }
    }

    suspend fun forceStoreRefr() = admin { sett.setLong(SettingsRepository.STORE_FORCED_OPEN_AT, clock.millis()) }

    private suspend fun <T> admin(block: suspend () -> T): T {
        if (!prof.isAdmin()) throw NotAdminException()
        return block()
    }

    private fun newId() = "admin-${UUID.randomUUID()}"
}

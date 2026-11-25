// Admin-only actions used by the in-app admin screen (debug/testing tools).
// (Comments in this file were written by AI - Claude.)
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

/** Thrown when a non-admin tries to use an admin action. */
class NotAdminException : IllegalStateException("admin only")

/** Cheat/debug actions. Every method checks [ProfileRepository.isAdmin] first. */
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
    /** Adds or removes coins (never below zero). Returns the new balance. */
    suspend fun adjCoins(amount: Long): Long = admin {
        db.withTransaction {
            val bal = db.coinsLdgrDao().bal()
            val applied = amount.coerceAtLeast(-bal)
            if (applied != 0L) coinsRepo.append(LdgrEntry(applied, LdgrReason.ADMIN_GRANT, clock.millis()))
            bal + applied
        }
    }

    /** Creates a stick of [rarity], with random stats unless [stats] is given. */
    suspend fun createStick(rarity: Rarity, stats: BaseStats? = null, tuning: Set<TuningFlag> = emptySet()): Stick = admin {
        Stick(newId(), rarity, stats ?: StatGen.roll(rarity, rng), tuning).also { cardList.save(it) }
    }

    /** Creates a power-up card. */
    suspend fun createPwrUp(type: PwrUpType): PwrUp = admin { PwrUp(newId(), type).also { cardList.save(it) } }

    /** Creates a trainer card. */
    suspend fun createTrnr(buff: TrnrBuff, affinity: Int = 0): Trnr = admin {
        Trnr(newId(), buff, affinity.coerceAtLeast(0)).also { trnrs.save(it) }
    }

    /** Saves edits to any card. */
    suspend fun updateCard(card: Card) = admin {
        when (card) {
            is Trnr -> trnrs.save(card)
            else -> cardList.save(card)
        }
    }

    /** Overwrites a trainer's affinity and win count. */
    suspend fun setTrnrProg(trainerId: String, affinity: Int, wins: Int) = admin {
        trnrs.overwriteProg(trainerId, affinity.coerceAtLeast(0), wins.coerceAtLeast(0))
    }

    /** Deletes a card or trainer. */
    suspend fun deleteCard(card: Card) = admin {
        when (card) {
            is Trnr -> trnrs.delete(card.id)
            else -> cardList.delete(card.id)
        }
    }

    /** Opens a pack without charging coins. */
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

    /** Forces the weekly store to open for the next 24 hours. */
    suspend fun forceStoreRefr() = admin { sett.setLong(SettingsRepository.STORE_FORCED_OPEN_AT, clock.millis()) }

    /** Runs [block] only if the current user is an admin. */
    private suspend fun <T> admin(block: suspend () -> T): T {
        if (!prof.isAdmin()) throw NotAdminException()
        return block()
    }

    private fun newId() = "admin-${UUID.randomUUID()}"
}

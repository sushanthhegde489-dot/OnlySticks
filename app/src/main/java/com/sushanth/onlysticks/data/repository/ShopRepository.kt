package com.sushanth.onlysticks.data.repository

import androidx.room.withTransaction
import com.sushanth.onlysticks.data.local.AppDatabase
import com.sushanth.onlysticks.data.local.entity.StoreBuyEntity
import com.sushanth.onlysticks.domain.economy.CoinsRules
import com.sushanth.onlysticks.domain.economy.LdgrReason
import com.sushanth.onlysticks.domain.gacha.PackOpnr
import com.sushanth.onlysticks.domain.gacha.PackRes
import com.sushanth.onlysticks.domain.gacha.PackType
import com.sushanth.onlysticks.domain.model.Card
import com.sushanth.onlysticks.domain.model.Trnr
import com.sushanth.onlysticks.domain.store.BuyRes
import com.sushanth.onlysticks.domain.store.StoreOffer
import com.sushanth.onlysticks.domain.store.StoreWin
import com.sushanth.onlysticks.domain.store.WklyCardStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock

sealed interface PackBuy {
    data class Opened(val result: PackRes) : PackBuy
    data object NotEnoughCoins : PackBuy
}

data class CardStoreState(val win: StoreWin, val offers: List<StoreOffer>, val boughtSlots: Set<Int>)

class ShopRepository(
    private val db: AppDatabase,
    private val cardList: CardRepository,
    private val trnrs: TrainerRepository,
    private val coinsRepo: CoinsRepository,
    private val opnr: PackOpnr,
    private val store: WklyCardStore,
    private val sett: SettingsRepository,
    private val clock: Clock,
    private val stats: StatsRepository,
) {
    private val ldgr = db.coinsLdgrDao()
    private val buys = db.storeBuyDao()

    suspend fun buyPack(type: PackType): PackBuy = db.withTransaction {
        if (ldgr.bal() < type.cost) return@withTransaction PackBuy.NotEnoughCoins
        val now = clock.millis()
        val result = opnr.open(type)
        coinsRepo.append(CoinsRules.buy(LdgrReason.PACK_PURCHASE, type.cost, now))
        result.cardList.forEachIndexed { i, card -> keep(card, now + i) }
        stats.add(Stat.PACKS_OPENED)
        PackBuy.Opened(result)
    }

    suspend fun curWin(): StoreWin {
        val now = clock.millis()
        val win = store.win(now, sett.getLong(SettingsRepository.STORE_FORCED_OPEN_AT))
        val lastLdgr = ldgr.latestTimestamp() ?: now
        val clockWentBack = now + CLOCK_SLACK_MS < lastLdgr
        return if (clockWentBack) win.copy(closesAtEpochMs = win.refreshedAtEpochMs) else win
    }

    fun observeCardStore(win: StoreWin): Flow<CardStoreState> =
        buys.observeBoughtSlots(win.refreshedAtEpochMs).map { CardStoreState(win, store.offers(win), it.toSet()) }

    suspend fun buyCard(slot: Int): BuyRes = db.withTransaction {
        val win = curWin()
        val offer = store.offers(win).firstOrNull { it.slot == slot } ?: return@withTransaction BuyRes.SoldOut
        val bought = buys.count(win.refreshedAtEpochMs, slot) > 0
        val result = store.buy(win, offer, ldgr.bal(), bought)
        if (result is BuyRes.Success) {
            val now = clock.millis()
            buys.insert(StoreBuyEntity(win.refreshedAtEpochMs, slot, now))
            coinsRepo.append(CoinsRules.buy(LdgrReason.CARD_PURCHASE, result.price, now))
            keep(result.card, now)
        }
        result
    }

    private suspend fun keep(card: Card, createdAt: Long) = when (card) {
        is Trnr -> trnrs.save(card, createdAt)
        else -> cardList.save(card, createdAt)
    }

    private companion object {
        const val CLOCK_SLACK_MS = 5 * 60_000L
    }
}

package com.sushanth.onlysticks.domain.store

import com.sushanth.onlysticks.domain.appraisal.Appr
import com.sushanth.onlysticks.domain.config.Cfg
import com.sushanth.onlysticks.domain.model.Card
import com.sushanth.onlysticks.domain.model.Rarity
import com.sushanth.onlysticks.domain.model.Stick
import com.sushanth.onlysticks.domain.model.TuningFlag
import com.sushanth.onlysticks.domain.rng.DeterministicRng
import com.sushanth.onlysticks.domain.rng.Hash
import com.sushanth.onlysticks.domain.stats.StatGen
import kotlin.math.roundToLong
import kotlin.time.Duration

data class StoreWin(val refreshedAtEpochMs: Long, val closesAtEpochMs: Long, val nextRefrEpochMs: Long, val nowEpochMs: Long) {
    val isOpen: Boolean get() = nowEpochMs in refreshedAtEpochMs until closesAtEpochMs
}

data class StoreOffer(val slot: Int, val card: Card, val price: Long)

sealed interface BuyRes {
    data class Success(val card: Card, val price: Long) : BuyRes
    data object StoreClosed : BuyRes
    data object NotEnoughCoins : BuyRes
    data object SoldOut : BuyRes
}

class WklyCardStore {
    val refrIntvl: Duration = Cfg.Store.REFRESH_INTERVAL
    val availWin: Duration = Cfg.Store.AVAILABILITY_WINDOW

    fun win(nowEpochMs: Long, forcedOpenAt: Long? = null): StoreWin {
        val intvl = refrIntvl.inWholeMilliseconds
        val open = availWin.inWholeMilliseconds
        if (forcedOpenAt != null && nowEpochMs >= forcedOpenAt && nowEpochMs < forcedOpenAt + open) {
            val regularNext = nowEpochMs - Math.floorMod(nowEpochMs, intvl) + intvl
            return StoreWin(forcedOpenAt, forcedOpenAt + open, regularNext, nowEpochMs)
        }
        val refreshedAt = nowEpochMs - Math.floorMod(nowEpochMs, intvl)
        return StoreWin(
            refreshedAtEpochMs = refreshedAt,
            closesAtEpochMs = refreshedAt + availWin.inWholeMilliseconds,
            nextRefrEpochMs = refreshedAt + intvl,
            nowEpochMs = nowEpochMs,
        )
    }

    fun offers(win: StoreWin): List<StoreOffer> {
        val rng = DeterministicRng.fromBytes(Hash.sha256(SEED_TAG, win.refreshedAtEpochMs.toString().encodeToByteArray()))
        return List(Cfg.Store.SLOTS) { slot ->
            val rarity = Rarity.entries[rng.weightedIdx(Cfg.Store.RARITY_WEIGHTS)]
            val tuning = TuningFlag.entries.filter { rng.nextBoolean(Cfg.Store.TUNING_FLAG_CHANCE) }.toSet()
            val stick = Stick("store-${win.refreshedAtEpochMs}-$slot", rarity, StatGen.roll(rarity, rng), tuning)
            StoreOffer(slot, stick, priceOf(Appr.value(stick)))
        }
    }

    fun buy(win: StoreWin, offer: StoreOffer, bal: Long, alreadyBought: Boolean): BuyRes = when {
        !win.isOpen -> BuyRes.StoreClosed
        alreadyBought -> BuyRes.SoldOut
        bal < offer.price -> BuyRes.NotEnoughCoins
        else -> BuyRes.Success(offer.card, offer.price)
    }

    private fun priceOf(appr: Int): Long {
        val stp = Cfg.Store.PRICE_ROUNDING
        return ((appr * Cfg.Store.PRICE_FACTOR) / stp).roundToLong().coerceAtLeast(1) * stp
    }

    private companion object {
        val SEED_TAG = "onlysticks/card-store/v1".encodeToByteArray()
    }
}

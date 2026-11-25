// The weekly card store: a rotating set of sticks for sale during a short window each week.
// (Comments in this file were written by AI - Claude.)
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

/** The current store window: when it opened, closes, and next refreshes. */
data class StoreWin(val refreshedAtEpochMs: Long, val closesAtEpochMs: Long, val nextRefrEpochMs: Long, val nowEpochMs: Long) {
    /** True while the store is open for buying. */
    val isOpen: Boolean get() = nowEpochMs in refreshedAtEpochMs until closesAtEpochMs
}

/** One card for sale in a store slot. */
data class StoreOffer(val slot: Int, val card: Card, val price: Long)

/** Result of attempting to buy a store card. */
sealed interface BuyRes {
    data class Success(val card: Card, val price: Long) : BuyRes
    data object StoreClosed : BuyRes
    data object NotEnoughCoins : BuyRes
    data object SoldOut : BuyRes
}

/** Weekly store logic. Offers are derived from the window start time, so every device sees the same stock. */
class WklyCardStore {
    val refrIntvl: Duration = Cfg.Store.REFRESH_INTERVAL
    val availWin: Duration = Cfg.Store.AVAILABILITY_WINDOW

    /** The store window containing [nowEpochMs]. [forcedOpenAt] lets admins open the store early for 24h. */
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

    /** The cards on sale in [win], generated deterministically from the window start time. */
    fun offers(win: StoreWin): List<StoreOffer> {
        val rng = DeterministicRng.fromBytes(Hash.sha256(SEED_TAG, win.refreshedAtEpochMs.toString().encodeToByteArray()))
        return List(Cfg.Store.SLOTS) { slot ->
            val rarity = Rarity.entries[rng.weightedIdx(Cfg.Store.RARITY_WEIGHTS)]
            val tuning = TuningFlag.entries.filter { rng.nextBoolean(Cfg.Store.TUNING_FLAG_CHANCE) }.toSet()
            val stick = Stick("store-${win.refreshedAtEpochMs}-$slot", rarity, StatGen.roll(rarity, rng), tuning)
            StoreOffer(slot, stick, priceOf(Appr.value(stick)))
        }
    }

    /** Decides whether a purchase is allowed; does not change any state itself. */
    fun buy(win: StoreWin, offer: StoreOffer, bal: Long, alreadyBought: Boolean): BuyRes = when {
        !win.isOpen -> BuyRes.StoreClosed
        alreadyBought -> BuyRes.SoldOut
        bal < offer.price -> BuyRes.NotEnoughCoins
        else -> BuyRes.Success(offer.card, offer.price)
    }

    /** Price = appraisal x PRICE_FACTOR, rounded to the nearest PRICE_ROUNDING coins (minimum one step). */
    private fun priceOf(appr: Int): Long {
        val stp = Cfg.Store.PRICE_ROUNDING
        return ((appr * Cfg.Store.PRICE_FACTOR) / stp).roundToLong().coerceAtLeast(1) * stp
    }

    private companion object {
        val SEED_TAG = "onlysticks/card-store/v1".encodeToByteArray()
    }
}

// Gives new players their starter cards, deck and coins.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.repository

import androidx.room.withTransaction
import com.sushanth.onlysticks.data.local.AppDatabase
import com.sushanth.onlysticks.domain.economy.CoinsRules
import com.sushanth.onlysticks.domain.model.Deck
import com.sushanth.onlysticks.domain.model.Rarity
import com.sushanth.onlysticks.domain.model.Stick
import com.sushanth.onlysticks.domain.model.TuningFlag
import com.sushanth.onlysticks.domain.rng.DeterministicRng
import com.sushanth.onlysticks.domain.stats.StatGen
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Clock

/** Seeds a starter collection the first time the app runs (or when an admin resets it). */
class StartSeed(
    private val db: AppDatabase,
    private val cardList: CardRepository,
    private val decks: DeckRepository,
    private val coinsRepo: CoinsRepository,
    private val clock: Clock,
) {
    private val mutex = Mutex()
    private var done = false

    /** Seeds once per process if the collection is empty. */
    suspend fun seedIfEmpty() {
        if (!done) seedStart(forceReset = false)
    }

    /** Seeds the starter data if the collection is empty or [forceReset] is true. Thread-safe. */
    suspend fun seedStart(forceReset: Boolean = false) = mutex.withLock {
        db.withTransaction {
            if (forceReset || cardList.isEmpty()) seed(forceReset)
        }
        done = true
    }

    /** Writes three starter sticks, a starter deck and the starting coin bonus. */
    private suspend fun seed(forceReset: Boolean) {
        if (forceReset) db.clearPlrColl()
        val coll = startColl()
        val base = clock.millis()
        coll.forEachIndexed { i, stick -> cardList.save(stick, createdAt = base + i) }
        decks.saveDeck(startDeck(coll))
        coinsRepo.append(CoinsRules.startBonus(base))
    }

    companion object {
        /** Three common sticks, generated with a fixed seed so every player gets the same starters. */
        fun startColl(): List<Stick> {
            val rng = DeterministicRng(42)
            return List(3) { i ->
                val tuning = TuningFlag.entries.filter { rng.nextBoolean(0.25) }.toSet()
                Stick(id = "starter-stick-${i + 1}", rarity = Rarity.COMMON, base = StatGen.roll(Rarity.COMMON, rng), tuning = tuning)
            }
        }

        /** The default deck holding the starter sticks. */
        fun startDeck(sticks: List<Stick>): Deck = Deck(id = "starter-deck", name = "Starter", cardList = sticks, trainer = null)
    }
}

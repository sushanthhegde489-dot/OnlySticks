// Gameplay counters (matches played/won, packs opened).
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Tracked statistics and their storage keys. */
enum class Stat(val key: String) {
    PVE_PLAYED("pve_played"),
    PVE_WON("pve_won"),
    PVE_LOST("pve_lost"),
    PVP_PLAYED("pvp_played"),
    PVP_WON("pvp_won"),
    PACKS_OPENED("packs_opened"),
}

/** Stores stat totals and how much of each has been sent to Play Games, so increments are sent exactly once. */
class StatsRepository(private val sett: SettingsRepository) {
    private val lock = Mutex()

    /** Increments each given stat by one. */
    suspend fun add(vararg stats: Stat) = lock.withLock {
        stats.forEach { sett.setLong(total(it), (sett.getLong(total(it)) ?: 0L) + 1) }
    }

    /** Current totals. */
    suspend fun all(): Map<Stat, Long> = Stat.entries.associateWith { sett.getLong(total(it)) ?: 0L }

    /** Live totals. */
    fun observe(): Flow<Map<Stat, Long>> =
        combine(Stat.entries.map { s -> sett.observeLong(total(s)) }) { vals -> Stat.entries.zip(vals.map { it ?: 0L }).toMap() }

    /** Restores totals from a backup and marks them all as already sent. */
    suspend fun restore(saved: Map<String, Long>) = lock.withLock {
        Stat.entries.forEach { s ->
            val v = saved[s.key] ?: return@forEach
            sett.setLong(total(s), v)
            sett.setLong(sent(s), v)
        }
    }

    /** Increments not yet sent to Play Games. */
    suspend fun unsent(): Map<Stat, Long> = lock.withLock {
        Stat.entries.associateWith { (sett.getLong(total(it)) ?: 0L) - (sett.getLong(sent(it)) ?: 0L) }.filterValues { it > 0 }
    }

    /** Records that [n] increments of [stat] were sent. */
    suspend fun markSent(stat: Stat, n: Long) = lock.withLock {
        sett.setLong(sent(stat), (sett.getLong(sent(stat)) ?: 0L) + n)
    }

    private fun total(s: Stat) = "stats.${s.key}"
    private fun sent(s: Stat) = "stats.sent.${s.key}"
}

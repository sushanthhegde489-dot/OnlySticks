package com.sushanth.onlysticks.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

enum class Stat(val key: String) {
    PVE_PLAYED("pve_played"),
    PVE_WON("pve_won"),
    PVE_LOST("pve_lost"),
    PVP_PLAYED("pvp_played"),
    PVP_WON("pvp_won"),
    PACKS_OPENED("packs_opened"),
}

class StatsRepository(private val sett: SettingsRepository) {
    private val lock = Mutex()

    suspend fun add(vararg stats: Stat) = lock.withLock {
        stats.forEach { sett.setLong(total(it), (sett.getLong(total(it)) ?: 0L) + 1) }
    }

    suspend fun all(): Map<Stat, Long> = Stat.entries.associateWith { sett.getLong(total(it)) ?: 0L }

    fun observe(): Flow<Map<Stat, Long>> =
        combine(Stat.entries.map { s -> sett.observeLong(total(s)) }) { vals -> Stat.entries.zip(vals.map { it ?: 0L }).toMap() }

    suspend fun restore(saved: Map<String, Long>) = lock.withLock {
        Stat.entries.forEach { s ->
            val v = saved[s.key] ?: return@forEach
            sett.setLong(total(s), v)
            sett.setLong(sent(s), v)
        }
    }

    suspend fun unsent(): Map<Stat, Long> = lock.withLock {
        Stat.entries.associateWith { (sett.getLong(total(it)) ?: 0L) - (sett.getLong(sent(it)) ?: 0L) }.filterValues { it > 0 }
    }

    suspend fun markSent(stat: Stat, n: Long) = lock.withLock {
        sett.setLong(sent(stat), (sett.getLong(sent(stat)) ?: 0L) + n)
    }

    private fun total(s: Stat) = "stats.${s.key}"
    private fun sent(s: Stat) = "stats.sent.${s.key}"
}

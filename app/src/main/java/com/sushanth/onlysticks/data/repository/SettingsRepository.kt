// Typed helpers over the key/value settings table.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.repository

import com.sushanth.onlysticks.data.local.AppDatabase
import com.sushanth.onlysticks.data.local.entity.SettEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Stores longs and boolean flags as strings. Flags are "1" when on and absent when off. */
class SettingsRepository(db: AppDatabase) {
    private val dao = db.settDao()

    suspend fun getLong(key: String): Long? = dao.get(key)?.toLongOrNull()

    suspend fun setLong(key: String, value: Long) = dao.put(SettEntity(key, value.toString()))

    fun observeLong(key: String): Flow<Long?> = dao.observe(key).map { it?.toLongOrNull() }

    fun observeFlag(key: String): Flow<Boolean> = dao.observe(key).map { it == "1" }

    suspend fun flag(key: String): Boolean = dao.get(key) == "1"

    suspend fun setFlag(key: String, on: Boolean) = if (on) dao.put(SettEntity(key, "1")) else dao.remove(key)

    companion object {
        /** When an admin last force-opened the weekly store. */
        const val STORE_FORCED_OPEN_AT = "store.forcedOpenAt"
        /** Whether the user chose offline debug sign-in. */
        const val OFFLINE_DEBUG = "account.offlineDebug"
    }
}

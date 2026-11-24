package com.sushanth.onlysticks.data.repository

import com.sushanth.onlysticks.data.local.AppDatabase
import com.sushanth.onlysticks.data.local.entity.SettEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(db: AppDatabase) {
    private val dao = db.settDao()

    suspend fun getLong(key: String): Long? = dao.get(key)?.toLongOrNull()

    suspend fun setLong(key: String, value: Long) = dao.put(SettEntity(key, value.toString()))

    fun observeLong(key: String): Flow<Long?> = dao.observe(key).map { it?.toLongOrNull() }

    fun observeFlag(key: String): Flow<Boolean> = dao.observe(key).map { it == "1" }

    suspend fun flag(key: String): Boolean = dao.get(key) == "1"

    suspend fun setFlag(key: String, on: Boolean) = if (on) dao.put(SettEntity(key, "1")) else dao.remove(key)

    companion object {
        const val STORE_FORCED_OPEN_AT = "store.forcedOpenAt"
        const val OFFLINE_DEBUG = "account.offlineDebug"
    }
}

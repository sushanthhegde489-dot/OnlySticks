package com.sushanth.onlysticks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sushanth.onlysticks.data.local.entity.CoinsLdgrEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CoinsLdgrDao {
    @Insert
    suspend fun append(entry: CoinsLdgrEntity): Long

    @Query("SELECT COALESCE(SUM(amount), 0) FROM currency_ledger")
    fun observeBal(): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM currency_ledger")
    suspend fun bal(): Long

    @Query("SELECT claimDate FROM currency_ledger WHERE reason = :reason AND claimDate IS NOT NULL ORDER BY claimDate DESC LIMIT :limit")
    suspend fun recentClaimDates(reason: String, limit: Int): List<String>

    @Query("SELECT MAX(timestamp) FROM currency_ledger")
    suspend fun latestTimestamp(): Long?

    @Query("SELECT * FROM currency_ledger ORDER BY id ASC")
    suspend fun all(): List<CoinsLdgrEntity>

    @Insert
    suspend fun insertAll(rows: List<CoinsLdgrEntity>)

    @Query("DELETE FROM currency_ledger")
    suspend fun deleteAll()

    @Query("SELECT * FROM currency_ledger ORDER BY timestamp DESC, id DESC")
    fun observeEntries(): Flow<List<CoinsLdgrEntity>>

    @Query("SELECT * FROM currency_ledger WHERE reason = :reason ORDER BY timestamp DESC, id DESC LIMIT 1")
    suspend fun latestForReason(reason: String): CoinsLdgrEntity?

    @Query("SELECT COUNT(*) FROM currency_ledger WHERE reason = :reason AND claimDate = :claimDate")
    suspend fun cntForClaimDate(reason: String, claimDate: String): Int
}

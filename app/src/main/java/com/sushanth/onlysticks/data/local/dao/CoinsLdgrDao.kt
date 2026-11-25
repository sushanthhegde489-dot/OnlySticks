// Database access for the coin ledger.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sushanth.onlysticks.data.local.entity.CoinsLdgrEntity
import kotlinx.coroutines.flow.Flow

/** The coin ledger is append-only; the balance is the SUM of all amounts. */
@Dao
interface CoinsLdgrDao {
    /** Adds a transaction and returns its row id. */
    @Insert
    suspend fun append(entry: CoinsLdgrEntity): Long

    /** Live coin balance. */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM currency_ledger")
    fun observeBal(): Flow<Long>

    /** Current coin balance. */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM currency_ledger")
    suspend fun bal(): Long

    /** Most recent claim dates for a reward type (used for daily streaks). */
    @Query("SELECT claimDate FROM currency_ledger WHERE reason = :reason AND claimDate IS NOT NULL ORDER BY claimDate DESC LIMIT :limit")
    suspend fun recentClaimDates(reason: String, limit: Int): List<String>

    /** Newest transaction time, used to detect a clock that was moved backwards. */
    @Query("SELECT MAX(timestamp) FROM currency_ledger")
    suspend fun latestTimestamp(): Long?

    @Query("SELECT * FROM currency_ledger ORDER BY id ASC")
    suspend fun all(): List<CoinsLdgrEntity>

    @Insert
    suspend fun insertAll(rows: List<CoinsLdgrEntity>)

    @Query("DELETE FROM currency_ledger")
    suspend fun deleteAll()

    /** Live list of transactions, newest first. */
    @Query("SELECT * FROM currency_ledger ORDER BY timestamp DESC, id DESC")
    fun observeEntries(): Flow<List<CoinsLdgrEntity>>

    /** Most recent transaction for a given reason. */
    @Query("SELECT * FROM currency_ledger WHERE reason = :reason ORDER BY timestamp DESC, id DESC LIMIT 1")
    suspend fun latestForReason(reason: String): CoinsLdgrEntity?

    /** How many times a reward was claimed for a given date (should be 0 or 1). */
    @Query("SELECT COUNT(*) FROM currency_ledger WHERE reason = :reason AND claimDate = :claimDate")
    suspend fun cntForClaimDate(reason: String, claimDate: String): Int
}

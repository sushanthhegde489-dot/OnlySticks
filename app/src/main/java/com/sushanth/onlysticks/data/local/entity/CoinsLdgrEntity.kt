// Database row for one coin transaction.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** One ledger transaction. [reason] is a LdgrReason name; [claimDate] (ISO date) is set for daily rewards. */
@Entity(
    tableName = "currency_ledger",
    indices = [Index(value = ["reason", "claimDate"])],
)
data class CoinsLdgrEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Long,
    val reason: String,
    val timestamp: Long,
    val claimDate: String? = null,
)

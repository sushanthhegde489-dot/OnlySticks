package com.sushanth.onlysticks.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

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

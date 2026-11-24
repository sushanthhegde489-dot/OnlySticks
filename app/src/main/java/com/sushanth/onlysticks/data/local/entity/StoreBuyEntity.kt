package com.sushanth.onlysticks.data.local.entity

import androidx.room.Entity

@Entity(tableName = "store_purchases", primaryKeys = ["weekStart", "slot"])
data class StoreBuyEntity(
    val weekStart: Long,
    val slot: Int,
    val boughtAt: Long,
)

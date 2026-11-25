// Database row for a weekly store purchase.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.local.entity

import androidx.room.Entity

/** Marks store slot [slot] of the week starting [weekStart] as bought. */
@Entity(tableName = "store_purchases", primaryKeys = ["weekStart", "slot"])
data class StoreBuyEntity(
    val weekStart: Long,
    val slot: Int,
    val boughtAt: Long,
)

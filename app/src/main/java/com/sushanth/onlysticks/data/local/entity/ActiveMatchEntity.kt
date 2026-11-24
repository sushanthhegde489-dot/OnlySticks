package com.sushanth.onlysticks.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_match")
data class ActiveMatchEntity(
    @PrimaryKey val matchId: String,
    val botTier: Int,
    val trainerId: String?,
    val startedAt: Long,
)

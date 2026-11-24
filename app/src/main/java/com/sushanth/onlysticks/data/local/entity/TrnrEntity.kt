package com.sushanth.onlysticks.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sushanth.onlysticks.domain.model.TrnrBuff

@Entity(tableName = "trainers")
data class TrnrEntity(
    @PrimaryKey val id: String,
    val buff: TrnrBuff,
    val affinity: Int,
    val rank: Int,
    val wins: Int = 0,
    val customName: String? = null,
    val lastStepClaimDate: String? = null,
    val lastStepClaimEpochMs: Long? = null,
    val createdAt: Long,
)

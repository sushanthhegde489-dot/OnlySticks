package com.sushanth.onlysticks.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pvp_escrow")
data class PvpEscrwEntity(@PrimaryKey val cardId: String)

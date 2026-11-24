package com.sushanth.onlysticks.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettEntity(@PrimaryKey val key: String, val value: String)

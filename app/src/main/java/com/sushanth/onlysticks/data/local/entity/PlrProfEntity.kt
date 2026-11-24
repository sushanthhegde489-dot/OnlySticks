package com.sushanth.onlysticks.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlrProfEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val googleEmail: String? = null,
    val lastBackupAt: Long? = null,
    @ColumnInfo(defaultValue = "0") val isAdmin: Boolean = false,
    @ColumnInfo(defaultValue = "0") val tutorialDone: Boolean = false,
)

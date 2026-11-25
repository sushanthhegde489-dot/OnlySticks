// Database row for the player profile.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Player profile: display name, linked Google email, last backup time and flags. */
@Entity(tableName = "player_profile")
data class PlrProfEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val googleEmail: String? = null,
    val lastBackupAt: Long? = null,
    @ColumnInfo(defaultValue = "0") val isAdmin: Boolean = false,
    @ColumnInfo(defaultValue = "0") val tutorialDone: Boolean = false,
)

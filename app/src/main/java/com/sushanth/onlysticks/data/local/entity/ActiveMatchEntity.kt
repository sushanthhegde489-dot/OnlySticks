// Row for a bot match that has started but not yet been settled.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** An in-progress PvE match. If the app dies, it is counted as a loss on next start. */
@Entity(tableName = "active_match")
data class ActiveMatchEntity(
    @PrimaryKey val matchId: String,
    val botTier: Int,
    val trainerId: String?,
    val startedAt: Long,
)

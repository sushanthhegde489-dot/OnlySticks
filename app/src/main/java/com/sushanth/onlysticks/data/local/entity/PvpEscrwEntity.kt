// Database row for a card staked in an ongoing PvP match.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A staked card id. If the app dies mid-match these cards are forfeited on next start. */
@Entity(tableName = "pvp_escrow")
data class PvpEscrwEntity(@PrimaryKey val cardId: String)

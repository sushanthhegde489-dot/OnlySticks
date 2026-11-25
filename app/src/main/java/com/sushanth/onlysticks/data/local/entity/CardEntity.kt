// Database row for a stick or power-up card.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sushanth.onlysticks.domain.model.PwrUpType
import com.sushanth.onlysticks.domain.model.Rarity

/**
 * One owned card. Stick-only columns are left at defaults for power-ups (and vice versa).
 * [signature] is an HMAC over the card's fields; rows whose signature doesn't verify get [quarantined].
 */
@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey val id: String,
    val kind: CardKind,
    val customName: String? = null,
    val rarity: Rarity? = null,
    val durability: Int = 0,
    val speed: Int = 0,
    val attack: Int = 0,
    /** Tuning flags stored as three booleans. */
    val weightFlag: Boolean = false,
    val gripFlag: Boolean = false,
    val qualityFlag: Boolean = false,
    val maxDurability: Int = 0,
    val powerUpType: PwrUpType? = null,
    val createdAt: Long,
    /** Keystore-backed signature used to detect edits made directly to the database. */
    val signature: String,
    val quarantined: Boolean = false,
)

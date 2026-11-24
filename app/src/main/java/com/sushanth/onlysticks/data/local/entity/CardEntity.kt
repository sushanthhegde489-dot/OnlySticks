package com.sushanth.onlysticks.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sushanth.onlysticks.domain.model.PwrUpType
import com.sushanth.onlysticks.domain.model.Rarity

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey val id: String,
    val kind: CardKind,
    val customName: String? = null,
    val rarity: Rarity? = null,
    val durability: Int = 0,
    val speed: Int = 0,
    val attack: Int = 0,
    val weightFlag: Boolean = false,
    val gripFlag: Boolean = false,
    val qualityFlag: Boolean = false,
    val maxDurability: Int = 0,
    val powerUpType: PwrUpType? = null,
    val createdAt: Long,
    val signature: String,
    val quarantined: Boolean = false,
)

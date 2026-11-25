// Database row linking a card to a deck slot.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** One card in one deck slot. Deleting the deck or the card removes the slot row. */
@Entity(
    tableName = "deck_slots",
    primaryKeys = ["deckId", "slotIndex"],
    foreignKeys = [
        ForeignKey(
            entity = DeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CardEntity::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["cardId"])],
)
data class DeckSlotEntity(
    val deckId: String,
    val slotIndex: Int,
    val cardId: String,
)

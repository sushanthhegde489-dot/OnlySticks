// Room relation that loads a deck together with its slots.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/** A deck plus all of its slot rows (loaded in one query by Room). */
data class DeckWithSlots(
    @Embedded val deck: DeckEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "deckId",
    )
    val slots: List<DeckSlotEntity>,
)

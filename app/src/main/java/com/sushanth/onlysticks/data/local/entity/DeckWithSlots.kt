package com.sushanth.onlysticks.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class DeckWithSlots(
    @Embedded val deck: DeckEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "deckId",
    )
    val slots: List<DeckSlotEntity>,
)

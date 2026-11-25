// Database row for a saved deck.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** A deck's name and optional trainer. If the trainer is deleted, trainerId is set to null. */
@Entity(
    tableName = "decks",
    foreignKeys = [
        ForeignKey(
            entity = TrnrEntity::class,
            parentColumns = ["id"],
            childColumns = ["trainerId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index(value = ["trainerId"])],
)
data class DeckEntity(
    @PrimaryKey val id: String,
    val name: String,
    val trainerId: String? = null,
)

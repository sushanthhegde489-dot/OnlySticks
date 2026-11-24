package com.sushanth.onlysticks.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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

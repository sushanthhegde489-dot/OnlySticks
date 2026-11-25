// Database row for one key/value setting.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A single setting stored as text. */
@Entity(tableName = "settings")
data class SettEntity(@PrimaryKey val key: String, val value: String)

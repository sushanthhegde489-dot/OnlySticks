// Canonical byte encoding of a card row, used as the input to its signature.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.security

import com.sushanth.onlysticks.data.local.entity.CardEntity
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

/**
 * Serialises every signed column of a [CardEntity] in a fixed order. Changing this breaks all existing signatures,
 * so bump VERSION if the layout ever changes.
 */
object CardFieldEncoding {
    private const val VERSION = 1
    /** Encodes the card's fields (everything except signature and quarantine flag). */
    fun encode(card: CardEntity): ByteArray {
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use { out ->
            out.writeInt(VERSION)
            out.string(card.id)
            out.string(card.kind.name)
            out.string(card.customName)
            out.string(card.rarity?.name)
            out.writeInt(card.durability)
            out.writeInt(card.speed)
            out.writeInt(card.attack)
            out.writeBoolean(card.weightFlag)
            out.writeBoolean(card.gripFlag)
            out.writeBoolean(card.qualityFlag)
            out.writeInt(card.maxDurability)
            out.string(card.powerUpType?.name)
            out.writeLong(card.createdAt)
        }
        return bytes.toByteArray()
    }
    /** Writes a length-prefixed UTF-8 string, or -1 for null. */
    private fun DataOutputStream.string(value: String?) {
        if (value == null) {
            writeInt(-1)
            return
        }
        val utf8 = value.encodeToByteArray()
        writeInt(utf8.size)
        write(utf8)
    }
}

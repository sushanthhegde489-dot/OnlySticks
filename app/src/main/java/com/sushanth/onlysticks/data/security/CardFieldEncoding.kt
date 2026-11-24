package com.sushanth.onlysticks.data.security

import com.sushanth.onlysticks.data.local.entity.CardEntity
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

object CardFieldEncoding {
    private const val VERSION = 1
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

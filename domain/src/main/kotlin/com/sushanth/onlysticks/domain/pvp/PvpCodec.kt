// Text encoding for PvP messages: cards, actions and raw bytes.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.domain.pvp

import com.sushanth.onlysticks.domain.model.BaseStats
import com.sushanth.onlysticks.domain.model.Card
import com.sushanth.onlysticks.domain.model.Move
import com.sushanth.onlysticks.domain.model.PwrUp
import com.sushanth.onlysticks.domain.model.PwrUpType
import com.sushanth.onlysticks.domain.model.PwrUpTarget
import com.sushanth.onlysticks.domain.model.Rarity
import com.sushanth.onlysticks.domain.model.Stick
import com.sushanth.onlysticks.domain.model.Trnr
import com.sushanth.onlysticks.domain.model.TrnrBuff
import com.sushanth.onlysticks.domain.model.TuningFlag
import com.sushanth.onlysticks.domain.model.TurnAction
import com.sushanth.onlysticks.domain.rng.Hash.hexToBytesOrNull
import java.util.Base64

/** Thrown when the peer sends something malformed. */
class ProtocolException(message: String) : Exception(message)

/** Serialises game objects to compact comma/semicolon separated strings for the wire. */
object PvpCodec {
    /** Placeholder for "no value". */
    private const val NONE = "-"

    /** Base64url-encodes text so it can't contain spaces or newlines. */
    fun encodeTxt(text: String): String = Base64.getUrlEncoder().withoutPadding().encodeToString(text.encodeToByteArray())

    fun decodeTxt(enc: String): String = runCatching { Base64.getUrlDecoder().decode(enc).decodeToString() }
        .getOrElse { throw ProtocolException("bad text") }

    fun encodeBytes(bytes: ByteArray): String = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)

    fun decodeBytes(enc: String): ByteArray = runCatching { Base64.getUrlDecoder().decode(enc) }
        .getOrElse { throw ProtocolException("bad bytes") }

    fun hexToBytes(hex: String): ByteArray = hex.hexToBytesOrNull() ?: throw ProtocolException("bad hex")

    /** Cards are joined with ';'. */
    fun encodeCards(cardList: List<Card>): String = if (cardList.isEmpty()) NONE else cardList.joinToString(";", transform = ::encodeCard)

    fun decodeCards(text: String): List<Card> = if (text == NONE) emptyList() else text.split(";").map(::decodeCard)

    /** Card format: S,id,rarity,dur,spd,atk,tuning | P,id,type | T,id,buff,affinity */
    fun encodeCard(card: Card): String = when (card) {
        is Stick -> listOf("S", card.id, card.rarity.code, card.base.durability, card.base.speed, card.base.attack,
            card.tuning.joinToString("") { it.name.take(1) }.ifEmpty { NONE }).joinToString(",")
        is PwrUp -> listOf("P", card.id, card.type.name).joinToString(",")
        is Trnr -> listOf("T", card.id, card.buff.name, card.affinity).joinToString(",")
    }

    fun decodeCard(text: String): Card {
        val f = text.split(",")
        return runCatching {
            when (f[0]) {
                "S" -> Stick(f[1], Rarity.fromCode(f[2]), BaseStats(f[3].toInt(), f[4].toInt(), f[5].toInt()), decodeTuning(f[6]))
                "P" -> PwrUp(f[1], PwrUpType.valueOf(f[2]))
                "T" -> Trnr(f[1], TrnrBuff.valueOf(f[2]), f[3].toInt())
                else -> null
            }
        }.getOrNull() ?: throw ProtocolException("bad card")
    }

    /** Action format: move,switchSlot,powerUpSlot,target (each '-' when absent). */
    fun encodeAction(action: TurnAction): String =
        listOf(action.move?.name ?: NONE, action.swtchToSlot?.toString() ?: NONE, action.pwrUpSlot?.toString() ?: NONE, action.pwrUpTarget?.name ?: NONE)
            .joinToString(",")

    fun decodeAction(text: String): TurnAction {
        val f = text.split(",")
        if (f.size != 4) throw ProtocolException("bad action")
        return runCatching {
            TurnAction(
                move = f[0].takeUnless { it == NONE }?.let(Move::valueOf),
                swtchToSlot = f[1].takeUnless { it == NONE }?.toInt(),
                pwrUpSlot = f[2].takeUnless { it == NONE }?.toInt(),
                pwrUpTarget = f[3].takeUnless { it == NONE }?.let(PwrUpTarget::valueOf),
            )
        }.getOrElse { throw ProtocolException("bad action") }
    }

    /** Tuning flags are stored as their first letters, e.g. "WG". */
    private fun decodeTuning(text: String): Set<TuningFlag> =
        if (text == NONE) emptySet() else text.map { c -> TuningFlag.entries.first { it.name.first() == c } }.toSet()
}

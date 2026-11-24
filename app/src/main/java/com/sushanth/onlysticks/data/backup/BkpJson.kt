package com.sushanth.onlysticks.data.backup

import com.sushanth.onlysticks.data.local.entity.CoinsLdgrEntity
import com.sushanth.onlysticks.data.local.entity.StoreBuyEntity
import com.sushanth.onlysticks.data.local.entity.TrnrEntity
import com.sushanth.onlysticks.domain.model.BaseStats
import com.sushanth.onlysticks.domain.model.Card
import com.sushanth.onlysticks.domain.model.PwrUp
import com.sushanth.onlysticks.domain.model.PwrUpType
import com.sushanth.onlysticks.domain.model.Rarity
import com.sushanth.onlysticks.domain.model.Stick
import com.sushanth.onlysticks.domain.model.Trnr
import com.sushanth.onlysticks.domain.model.TrnrBuff
import com.sushanth.onlysticks.domain.model.TuningFlag
import org.json.JSONArray
import org.json.JSONObject

class BkpFormatException(message: String, cause: Throwable? = null) : Exception(message, cause)

object BkpJson {
    fun encode(s: BkpSnap): String = JSONObject()
        .put("version", s.version)
        .put("exportedAt", s.exportedAt)
        .put("displayName", s.displayName ?: JSONObject.NULL)
        .put("cards", s.cardList.toJson(::card))
        .put("trainers", s.trnrs.toJson(::trainer))
        .put("decks", s.decks.toJson(::deck))
        .put("ledger", s.ldgr.toJson(::ldgrRow))
        .put("storePurchases", s.storeBuys.toJson { JSONObject().put("weekStart", it.weekStart).put("slot", it.slot).put("boughtAt", it.boughtAt) })
        .put("stats", JSONObject(s.stats))
        .toString()

    fun decode(text: String): BkpSnap = try {
        val o = JSONObject(text)
        val version = o.getInt("version")
        if (version > BkpSnap.VERSION) throw BkpFormatException("backup is from a newer app version")
        BkpSnap(
            version = version,
            exportedAt = o.getLong("exportedAt"),
            displayName = o.optStrOrNull("displayName"),
            cardList = o.getJSONArray("cards").map(::card),
            trnrs = o.getJSONArray("trainers").map(::trainer),
            decks = o.getJSONArray("decks").map(::deck),
            ldgr = o.getJSONArray("ledger").map(::ldgrRow),
            storeBuys = o.getJSONArray("storePurchases").map { StoreBuyEntity(it.getLong("weekStart"), it.getInt("slot"), it.getLong("boughtAt")) },
            stats = o.optJSONObject("stats")?.let { st -> st.keys().asSequence().associateWith { st.getLong(it) } } ?: emptyMap(),
        )
    } catch (e: BkpFormatException) {
        throw e
    } catch (e: Exception) {
        throw BkpFormatException("backup file is damaged", e)
    }

    private fun card(c: TimedCard): JSONObject {
        val o = JSONObject().put("id", c.card.id).put("createdAt", c.createdAt).put("name", c.card.customName ?: JSONObject.NULL)
        return when (val card = c.card) {
            is Stick -> o.put("kind", "stick").put("rarity", card.rarity.code)
                .put("durability", card.base.durability).put("speed", card.base.speed).put("attack", card.base.attack)
                .put("tuning", JSONArray(card.tuning.map { it.name }))
            is PwrUp -> o.put("kind", "powerUp").put("type", card.type.name)
            is Trnr -> o.put("kind", "trainer").put("buff", card.buff.name).put("affinity", card.affinity)
        }
    }

    private fun card(o: JSONObject): TimedCard {
        val id = o.getString("id")
        val name = o.optStrOrNull("name")
        val card: Card = when (o.getString("kind")) {
            "stick" -> Stick(
                id, Rarity.fromCode(o.getString("rarity")),
                BaseStats(o.getInt("durability"), o.getInt("speed"), o.getInt("attack")),
                o.getJSONArray("tuning").let { a -> (0 until a.length()).map { TuningFlag.valueOf(a.getString(it)) }.toSet() },
                name,
            )
            "powerUp" -> PwrUp(id, PwrUpType.valueOf(o.getString("type")), name)
            else -> throw BkpFormatException("unknown card kind")
        }
        return TimedCard(card, o.getLong("createdAt"))
    }

    private fun trainer(t: TrnrEntity) = JSONObject()
        .put("id", t.id).put("buff", t.buff.name).put("affinity", t.affinity).put("rank", t.rank).put("wins", t.wins)
        .put("name", t.customName ?: JSONObject.NULL)
        .put("lastStepClaimDate", t.lastStepClaimDate ?: JSONObject.NULL)
        .put("lastStepClaimEpochMs", t.lastStepClaimEpochMs ?: JSONObject.NULL)
        .put("createdAt", t.createdAt)

    private fun trainer(o: JSONObject) = TrnrEntity(
        id = o.getString("id"),
        buff = TrnrBuff.valueOf(o.getString("buff")),
        affinity = o.getInt("affinity"),
        rank = o.getInt("rank"),
        wins = o.getInt("wins"),
        customName = o.optStrOrNull("name"),
        lastStepClaimDate = o.optStrOrNull("lastStepClaimDate"),
        lastStepClaimEpochMs = if (o.isNull("lastStepClaimEpochMs")) null else o.getLong("lastStepClaimEpochMs"),
        createdAt = o.getLong("createdAt"),
    )

    private fun deck(d: DeckRecord) = JSONObject()
        .put("id", d.id).put("name", d.name).put("trainerId", d.trainerId ?: JSONObject.NULL).put("cardIds", JSONArray(d.cardIds))

    private fun deck(o: JSONObject) = DeckRecord(
        o.getString("id"), o.getString("name"), o.optStrOrNull("trainerId"),
        o.getJSONArray("cardIds").let { a -> (0 until a.length()).map(a::getString) },
    )

    private fun ldgrRow(e: CoinsLdgrEntity) = JSONObject()
        .put("amount", e.amount).put("reason", e.reason).put("timestamp", e.timestamp).put("claimDate", e.claimDate ?: JSONObject.NULL)

    private fun ldgrRow(o: JSONObject) =
        CoinsLdgrEntity(amount = o.getLong("amount"), reason = o.getString("reason"), timestamp = o.getLong("timestamp"), claimDate = o.optStrOrNull("claimDate"))

    private fun <T> List<T>.toJson(f: (T) -> JSONObject) = JSONArray().also { a -> forEach { a.put(f(it)) } }

    private fun <T> JSONArray.map(f: (JSONObject) -> T): List<T> = (0 until length()).map { f(getJSONObject(it)) }

    private fun JSONObject.optStrOrNull(key: String): String? = if (isNull(key)) null else optString(key)
}

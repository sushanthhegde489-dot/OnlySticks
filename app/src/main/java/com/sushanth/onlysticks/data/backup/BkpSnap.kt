// In-memory representation of a full save-data backup.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.backup

import com.sushanth.onlysticks.data.local.entity.CoinsLdgrEntity
import com.sushanth.onlysticks.data.local.entity.StoreBuyEntity
import com.sushanth.onlysticks.data.local.entity.TrnrEntity
import com.sushanth.onlysticks.domain.model.Card

/** Everything needed to rebuild a player's save: profile name, cards, trainers, decks, coins and stats. */
data class BkpSnap(
    val version: Int,
    val exportedAt: Long,
    val displayName: String?,
    val cardList: List<TimedCard>,
    val trnrs: List<TrnrEntity>,
    val decks: List<DeckRecord>,
    val ldgr: List<CoinsLdgrEntity>,
    val storeBuys: List<StoreBuyEntity>,
    val stats: Map<String, Long> = emptyMap(),
) {
    companion object {
        /** Current backup format version. Older apps refuse backups with a higher version. */
        const val VERSION = 1
    }
}

/** A card plus when it was obtained (keeps collection ordering after restore). */
data class TimedCard(val card: Card, val createdAt: Long)

/** A deck as a list of card ids in slot order. */
data class DeckRecord(val id: String, val name: String, val trainerId: String?, val cardIds: List<String>)

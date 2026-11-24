package com.sushanth.onlysticks.data.backup

import com.sushanth.onlysticks.data.local.entity.CoinsLdgrEntity
import com.sushanth.onlysticks.data.local.entity.StoreBuyEntity
import com.sushanth.onlysticks.data.local.entity.TrnrEntity
import com.sushanth.onlysticks.domain.model.Card

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
        const val VERSION = 1
    }
}

data class TimedCard(val card: Card, val createdAt: Long)

data class DeckRecord(val id: String, val name: String, val trainerId: String?, val cardIds: List<String>)

// Shop tab: booster packs and the weekly card store.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.shop

import androidx.annotation.StringRes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.data.repository.CardStoreState
import com.sushanth.onlysticks.domain.appraisal.Appr
import com.sushanth.onlysticks.domain.gacha.PackType
import com.sushanth.onlysticks.domain.model.Stick
import com.sushanth.onlysticks.domain.stats.effectiveStats
import com.sushanth.onlysticks.ui.card.GameCard
import com.sushanth.onlysticks.ui.common.asStr
import com.sushanth.onlysticks.ui.components.ChunkyBtn
import com.sushanth.onlysticks.ui.components.GameBadge
import com.sushanth.onlysticks.ui.components.GameIcos
import com.sushanth.onlysticks.ui.components.OutlinedTxt
import com.sushanth.onlysticks.ui.components.ParchmentPanel
import com.sushanth.onlysticks.ui.components.SegmentedTabs
import com.sushanth.onlysticks.ui.components.WoodPanel
import com.sushanth.onlysticks.ui.theme.GameTheme
import java.time.Duration

/** Shop screen wired to [ShopVm]. */
@Composable
fun ShopRoute(vm: ShopVm, bal: Long, modifier: Modifier = Modifier) {
    val st by vm.state.collectAsStateWithLifecycle()
    ShopScr(st, bal, vm::selectTab, vm::buyPack, vm::buyCard, vm::dismissShow, modifier)
}

/** Stateless shop layout with Packs / Cards tabs, messages and the pack reveal overlay. */
@Composable
fun ShopScr(
    state: ShopUiState,
    bal: Long,
    onTab: (ShopTab) -> Unit,
    onBuyPack: (PackType) -> Unit,
    onBuyCard: (Int) -> Unit,
    onDismissShow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SegmentedTabs(
                opts = listOf(stringResource(R.string.shop_tab_packs), stringResource(R.string.shop_tab_cards)),
                selected = state.tab.ordinal,
                onSelect = { onTab(ShopTab.entries[it]) },
            )
            state.message?.let { msg ->
                ParchmentPanel(Modifier.fillMaxWidth()) {
                    Text(msg.asStr(), style = MaterialTheme.typography.bodyLarge, color = GameTheme.colors.ink)
                }
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when (state.tab) {
                    ShopTab.PACKS -> PackType.entries.forEach { PackTile(it, (bal >= it.cost) && !state.busy) { onBuyPack(it) } }
                    ShopTab.CARDS -> CardStore(state.cardStore, bal, state.busy, onBuyCard)
                }
            }
        }
        state.shown?.let { PackShow(it, onDismissShow) }
    }
}

/** Name string for each pack. */
@StringRes
fun packNameRes(type: PackType): Int = when (type) {
    PackType.COMMON -> R.string.pack_common
    PackType.RARE -> R.string.pack_rare
    PackType.ULTRA -> R.string.pack_ultra
}

/** A pack for sale; the buy button is disabled when the player can't afford it. */
@Composable
private fun PackTile(type: PackType, canAfford: Boolean, onBuy: () -> Unit) {
    val c = GameTheme.colors
    val (name, blurb, tint) = when (type) {
        PackType.COMMON -> Triple(R.string.pack_common, R.string.pack_common_blurb, c.leaf)
        PackType.RARE -> Triple(R.string.pack_rare, R.string.pack_rare_blurb, Color(0xFF1565C0))
        PackType.ULTRA -> Triple(R.string.pack_ultra, R.string.pack_ultra_blurb, Color(0xFFFF8F00))
    }
    WoodPanel(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            PackArt(tint)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTxt(stringResource(name), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(blurb), style = MaterialTheme.typography.bodyMedium, color = c.onWood)
                ChunkyBtn(
                    text = stringResource(R.string.shop_price, type.cost),
                    onClick = onBuy,
                    enabled = canAfford,
                    icon = GameIcos.Coin,
                    textStyle = MaterialTheme.typography.titleMedium,
                    minHeight = 44.dp,
                )
            }
        }
    }
}

/** Simple drawing of a booster pack in the given colour. */
@Composable
private fun PackArt(tint: Color) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        Modifier.width(72.dp).aspectRatio(0.7f).clip(shape).background(tint),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.fillMaxWidth().height(10.dp).align(Alignment.TopCenter).background(Color.White.copy(alpha = 0.3f)))
        Icon(GameIcos.Cards, contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(40.dp))
    }
}

/** Weekly store: open/closed countdown and the offered cards with their prices. */
@Composable
private fun CardStore(store: CardStoreState?, bal: Long, busy: Boolean, onBuy: (Int) -> Unit) {
    val c = GameTheme.colors
    store ?: return
    val win = store.win
    WoodPanel(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTxt(stringResource(R.string.shop_wkly_title), style = MaterialTheme.typography.titleLarge)
            val cntdwn = if (win.isOpen) {
                stringResource(R.string.shop_closes_in, formatSpan(win.closesAtEpochMs - win.nowEpochMs))
            } else {
                stringResource(R.string.shop_opens_in, formatSpan(win.nextRefrEpochMs - win.nowEpochMs))
            }
            GameBadge(cntdwn, color = if (win.isOpen) c.leaf else c.woodDark)
            if (!win.isOpen) {
                Text(stringResource(R.string.shop_wkly_closed_body), style = MaterialTheme.typography.bodyMedium, color = c.onWood)
            }
        }
    }
    if (!win.isOpen) return
    FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), maxItemsInEachRow = 2) {
        store.offers.forEach { offer ->
            val sold = offer.slot in store.boughtSlots
            ParchmentPanel(Modifier.weight(1f)) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    GameCard(offer.card, width = 116.dp, dimmed = sold)
                    (offer.card as? Stick)?.effectiveStats?.let { st ->
                        Text(stringResource(R.string.shop_stats, st.durability, st.speed, st.attack), style = MaterialTheme.typography.titleSmall, color = c.ink, maxLines = 1)
                    }
                    Text(stringResource(R.string.shop_val, Appr.value(offer.card)), style = MaterialTheme.typography.labelMedium, color = c.inkSoft)
                    ChunkyBtn(
                        text = if (sold) stringResource(R.string.shop_sold) else stringResource(R.string.shop_price, offer.price),
                        onClick = { onBuy(offer.slot) },
                        enabled = !sold && !busy && bal >= offer.price,
                        icon = if (sold) null else GameIcos.Coin,
                        textStyle = MaterialTheme.typography.titleSmall,
                        minHeight = 40.dp,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

/** Formats a duration like "2d 4h" or "35m" for store countdowns. */
private fun formatSpan(ms: Long): String {
    val d = Duration.ofMillis(ms.coerceAtLeast(0))
    return when {
        d.toDays() > 0 -> "${d.toDays()}d ${d.toHours() % 24}h"
        d.toHours() > 0 -> "${d.toHours()}h ${d.toMinutes() % 60}m"
        else -> "${d.toMinutes().coerceAtLeast(1)}m"
    }
}

/** Android Studio preview. */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun ShopScrPrvw() {
    com.sushanth.onlysticks.ui.theme.OnlySticksTheme {
        ShopScr(
            state = ShopUiState(),
            bal = 2500L,
            onTab = {},
            onBuyPack = {},
            onBuyCard = {},
            onDismissShow = {},
        )
    }
}


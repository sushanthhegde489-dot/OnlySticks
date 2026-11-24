package com.sushanth.onlysticks.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.domain.gacha.PackType
import com.sushanth.onlysticks.domain.model.BaseStats
import com.sushanth.onlysticks.domain.model.Card
import com.sushanth.onlysticks.domain.model.PwrUpType
import com.sushanth.onlysticks.domain.model.Rarity
import com.sushanth.onlysticks.domain.model.TrnrBuff
import com.sushanth.onlysticks.domain.model.TuningFlag
import com.sushanth.onlysticks.domain.trainer.TrnrRecord
import com.sushanth.onlysticks.ui.common.asStr
import com.sushanth.onlysticks.ui.components.BtnStyle
import com.sushanth.onlysticks.ui.components.ChunkyBtn
import com.sushanth.onlysticks.ui.components.GameIcos
import com.sushanth.onlysticks.ui.components.OutlinedTxt
import com.sushanth.onlysticks.ui.components.ParchmentPanel
import com.sushanth.onlysticks.ui.components.SegmentedTabs
import com.sushanth.onlysticks.ui.components.WoodPanel
import com.sushanth.onlysticks.ui.shop.PackShow
import com.sushanth.onlysticks.ui.theme.GameTheme

@Composable
fun AdminScr(
    vm: AdminVm,
    bal: Long,
    cardList: List<Card>,
    trnrs: List<TrnrRecord>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val st by vm.state.collectAsStateWithLifecycle()
    AdminScrContent(
        state = st,
        bal = bal,
        cardList = cardList,
        trnrs = trnrs,
        onBack = onBack,
        onSelectTab = { vm.selectTab(AdminTab.entries[it]) },
        onAdjCoins = vm::adjCoins,
        onOpenFreePack = vm::openFreePack,
        onForceStoreRefr = vm::forceStoreRefr,
        onCreateStick = vm::createStick,
        onCreatePwrUp = vm::createPwrUp,
        onCreateTrnr = vm::createTrnr,
        onDeleteCard = vm::delete,
        onSaveCard = vm::save,
        onSaveTrnr = vm::saveTrnr,
        onDismissPack = vm::dismissPack,
        modifier = modifier,
    )
}

@Composable
fun AdminScrContent(
    state: AdminUiState,
    bal: Long,
    cardList: List<Card>,
    trnrs: List<TrnrRecord>,
    onBack: () -> Unit,
    onSelectTab: (Int) -> Unit,
    onAdjCoins: (Long) -> Unit,
    onOpenFreePack: (PackType) -> Unit,
    onForceStoreRefr: () -> Unit,
    onCreateStick: (Rarity, BaseStats?, Set<TuningFlag>) -> Unit,
    onCreatePwrUp: (PwrUpType) -> Unit,
    onCreateTrnr: (TrnrBuff, Int) -> Unit,
    onDeleteCard: (Card) -> Unit,
    onSaveCard: (Card) -> Unit,
    onSaveTrnr: (com.sushanth.onlysticks.domain.model.Trnr, Int) -> Unit,
    onDismissPack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().imePadding().padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ChunkyBtn(stringResource(R.string.pvp_back), onBack, style = BtnStyle.Wood, minHeight = 40.dp, textStyle = MaterialTheme.typography.titleSmall)
                OutlinedTxt(stringResource(R.string.admin_title), style = MaterialTheme.typography.headlineSmall)
            }
            SegmentedTabs(
                opts = listOf(stringResource(R.string.admin_tab_economy), stringResource(R.string.admin_tab_create), stringResource(R.string.admin_tab_coll)),
                selected = state.tab.ordinal,
                onSelect = onSelectTab,
            )
            state.message?.let { msg ->
                ParchmentPanel(Modifier.fillMaxWidth()) { Text(msg.asStr(), color = GameTheme.colors.ink, style = MaterialTheme.typography.bodyLarge) }
            }
            when (state.tab) {
                AdminTab.ECONOMY -> Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    EconomyPanel(bal, state.busy, onAdjCoins, onOpenFreePack, onForceStoreRefr)
                }
                AdminTab.CREATE -> Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CreatePanel(state.busy, onCreateStick, onCreatePwrUp, onCreateTrnr)
                }
                AdminTab.COLLECTION -> AdminCollContent(cardList, trnrs, onDeleteCard, onSaveCard, onSaveTrnr, Modifier.weight(1f))
            }
        }
        state.opened?.let { PackShow(it.cardList, onDismissPack) }
    }
}

@Composable
private fun EconomyPanel(
    bal: Long,
    busy: Boolean,
    onAdjCoins: (Long) -> Unit,
    onOpenFreePack: (PackType) -> Unit,
    onForceStoreRefr: () -> Unit,
) {
    var amount by rememberSaveable { mutableStateOf("1000") }
    WoodPanel(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTxt(stringResource(R.string.admin_coins, bal), style = MaterialTheme.typography.titleLarge)
            AdminTxtField(stringResource(R.string.admin_amt), amount, { amount = it }, Modifier.fillMaxWidth(), numeric = true)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val value = amount.toLongOrNull() ?: 0L
                ChunkyBtn(stringResource(R.string.admin_add), { onAdjCoins(value) }, Modifier.weight(1f), enabled = !busy, icon = GameIcos.Coin, textStyle = MaterialTheme.typography.titleMedium)
                ChunkyBtn(stringResource(R.string.admin_remove), { onAdjCoins(-value) }, Modifier.weight(1f), style = BtnStyle.Wood, enabled = !busy, textStyle = MaterialTheme.typography.titleMedium)
            }
        }
    }
    WoodPanel(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTxt(stringResource(R.string.admin_free_packs), style = MaterialTheme.typography.titleLarge)
            PackType.entries.forEach { type ->
                val packName = when (type) {
                    PackType.COMMON -> R.string.pack_common
                    PackType.RARE -> R.string.pack_rare
                    PackType.ULTRA -> R.string.pack_ultra
                }
                ChunkyBtn(stringResource(packName), { onOpenFreePack(type) }, Modifier.fillMaxWidth(), style = BtnStyle.Secondary, enabled = !busy, minHeight = 44.dp, textStyle = MaterialTheme.typography.titleMedium)
            }
        }
    }
    WoodPanel(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTxt(stringResource(R.string.admin_store), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.admin_store_body), style = MaterialTheme.typography.bodyMedium, color = GameTheme.colors.onWood)
            ChunkyBtn(stringResource(R.string.admin_store_refr), onForceStoreRefr, Modifier.fillMaxWidth(), enabled = !busy, icon = GameIcos.Shop)
        }
    }
}

private enum class NewKind { STICK, LIGHTER, WATER, TRAINER }

@Composable
private fun CreatePanel(
    busy: Boolean,
    onCreateStick: (Rarity, BaseStats?, Set<TuningFlag>) -> Unit,
    onCreatePwrUp: (PwrUpType) -> Unit,
    onCreateTrnr: (TrnrBuff, Int) -> Unit,
) {
    var kind by rememberSaveable { mutableStateOf(NewKind.STICK) }
    var rarity by rememberSaveable { mutableStateOf(Rarity.COMMON) }
    var d by rememberSaveable { mutableStateOf("") }
    var s by rememberSaveable { mutableStateOf("") }
    var a by rememberSaveable { mutableStateOf("") }
    var tuning by rememberSaveable { mutableStateOf(setOf<TuningFlag>()) }
    var buff by rememberSaveable { mutableStateOf(TrnrBuff.MAX_HP) }
    var affinity by rememberSaveable { mutableStateOf("0") }
    WoodPanel(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTxt(stringResource(R.string.admin_create_title), style = MaterialTheme.typography.titleLarge)
            SegmentedTabs(
                opts = listOf(stringResource(R.string.admin_kind_stick), stringResource(R.string.powerup_lighter), stringResource(R.string.powerup_water_bottle), stringResource(R.string.card_code_trnr)),
                selected = kind.ordinal,
                onSelect = { kind = NewKind.entries[it] },
            )
            when (kind) {
                NewKind.STICK -> {
                    SegmentedTabs(Rarity.entries.map { it.code }, rarity.ordinal, { rarity = Rarity.entries[it] })
                    Text(stringResource(R.string.admin_stats_hint), style = MaterialTheme.typography.bodySmall, color = GameTheme.colors.onWood)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AdminTxtField("D", d, { d = it }, Modifier.weight(1f), numeric = true)
                        AdminTxtField("S", s, { s = it }, Modifier.weight(1f), numeric = true)
                        AdminTxtField("A", a, { a = it }, Modifier.weight(1f), numeric = true)
                    }
                    ChipRow(TuningFlag.entries, { it in tuning }, { it.name.lowercase().replaceFirstChar(Char::uppercase) }) {
                        tuning = if (it in tuning) tuning - it else tuning + it
                    }
                }
                NewKind.TRAINER -> {
                    ChipRow(TrnrBuff.entries, { it == buff }, { it.name.replace('_', ' ').lowercase() }) { buff = it }
                    AdminTxtField(stringResource(R.string.admin_affinity), affinity, { affinity = it }, numeric = true)
                }
                else -> Unit
            }
            ChunkyBtn(
                text = stringResource(R.string.admin_create),
                onClick = {
                    if (kind == NewKind.STICK) {
                        val stats = listOf(d, s, a).map { it.toIntOrNull() }
                        val base = if (stats.all { it != null }) BaseStats(stats[0]!!.coerceIn(1, 999), stats[1]!!.coerceIn(1, 999), stats[2]!!.coerceIn(1, 999)) else null
                        onCreateStick(rarity, base, tuning)
                    } else {
                        if (kind == NewKind.TRAINER) {
                            onCreateTrnr(buff, affinity.toIntOrNull() ?: 0)
                        } else {
                            if (kind == NewKind.LIGHTER) onCreatePwrUp(PwrUpType.LIGHTER) else onCreatePwrUp(PwrUpType.WATER_BOTTLE)
                        }
                    }
                },
                enabled = !busy,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun AdminScrPrvw() {
    com.sushanth.onlysticks.ui.theme.OnlySticksTheme {
        AdminScrContent(
            state = AdminUiState(),
            bal = 5000L,
            cardList = emptyList(),
            trnrs = emptyList(),
            onBack = {},
            onSelectTab = {},
            onAdjCoins = {},
            onOpenFreePack = {},
            onForceStoreRefr = {},
            onCreateStick = { _, _, _ -> },
            onCreatePwrUp = {},
            onCreateTrnr = { _, _ -> },
            onDeleteCard = {},
            onSaveCard = {},
            onSaveTrnr = { _, _ -> },
            onDismissPack = {},
        )
    }
}


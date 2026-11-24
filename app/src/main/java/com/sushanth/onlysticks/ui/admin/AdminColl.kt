package com.sushanth.onlysticks.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.domain.model.BaseStats
import com.sushanth.onlysticks.domain.model.Card
import com.sushanth.onlysticks.domain.model.PwrUp
import com.sushanth.onlysticks.domain.model.PwrUpType
import com.sushanth.onlysticks.domain.model.Rarity
import com.sushanth.onlysticks.domain.model.Stick
import com.sushanth.onlysticks.domain.model.Trnr
import com.sushanth.onlysticks.domain.model.TuningFlag
import com.sushanth.onlysticks.domain.trainer.TrnrRecord
import com.sushanth.onlysticks.ui.card.GameCard
import com.sushanth.onlysticks.ui.card.title
import com.sushanth.onlysticks.ui.components.BtnStyle
import com.sushanth.onlysticks.ui.components.ChunkyBtn
import com.sushanth.onlysticks.ui.components.ParchmentPanel
import com.sushanth.onlysticks.ui.components.SegmentedTabs
import com.sushanth.onlysticks.ui.theme.GameTheme

@Composable
internal fun AdminColl(cardList: List<Card>, trnrs: List<TrnrRecord>, vm: AdminVm, modifier: Modifier = Modifier) {
    AdminCollContent(
        cardList = cardList,
        trnrs = trnrs,
        onDeleteCard = vm::delete,
        onSaveCard = vm::save,
        onSaveTrnr = vm::saveTrnr,
        modifier = modifier
    )
}

@Composable
internal fun AdminCollContent(
    cardList: List<Card>,
    trnrs: List<TrnrRecord>,
    onDeleteCard: (Card) -> Unit,
    onSaveCard: (Card) -> Unit,
    onSaveTrnr: (Trnr, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var editing by remember { mutableStateOf<Card?>(null) }
    val wins = trnrs.associate { it.trainer.id to it.wins }
    val all: List<Card> = trnrs.map { it.trainer } + cardList
    LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(all, key = { it.id }) { card ->
            ParchmentPanel(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GameCard(card, width = 56.dp)
                    Column(Modifier.weight(1f)) {
                        Text(card.title(), style = MaterialTheme.typography.titleSmall, color = Color(0xFF3A2312))
                        Text(describe(card, wins[card.id]), style = MaterialTheme.typography.labelMedium, color = Color(0xFF6E4F35))
                    }
                    ChunkyBtn(stringResource(R.string.admin_edit), { editing = card }, minHeight = 36.dp, textStyle = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
    editing?.let { card ->
        CardEditor(
            card = card,
            wins = wins[card.id] ?: 0,
            onDelete = onDeleteCard,
            onSaveCard = onSaveCard,
            onSaveTrnr = onSaveTrnr,
            onClose = { editing = null }
        )
    }
}

private fun describe(card: Card, wins: Int?): String = when (card) {
    is Stick -> "D ${card.base.durability} · S ${card.base.speed} · A ${card.base.attack}" + card.tuning.joinToString("") { " · " + it.name.lowercase() }
    is PwrUp -> card.type.name.lowercase().replace('_', ' ')
    is Trnr -> "affinity ${card.affinity} · wins ${wins ?: 0}"
}

@Composable
private fun CardEditor(
    card: Card,
    wins: Int,
    onDelete: (Card) -> Unit,
    onSaveCard: (Card) -> Unit,
    onSaveTrnr: (Trnr, Int) -> Unit,
    onClose: () -> Unit
) {
    var name by remember { mutableStateOf(card.customName.orEmpty()) }
    var rarity by remember { mutableStateOf((card as? Stick)?.rarity ?: Rarity.COMMON) }
    var d by remember { mutableStateOf((card as? Stick)?.base?.durability?.toString().orEmpty()) }
    var s by remember { mutableStateOf((card as? Stick)?.base?.speed?.toString().orEmpty()) }
    var a by remember { mutableStateOf((card as? Stick)?.base?.attack?.toString().orEmpty()) }
    var tuning by remember { mutableStateOf((card as? Stick)?.tuning ?: emptySet()) }
    var type by remember { mutableStateOf((card as? PwrUp)?.type ?: PwrUpType.LIGHTER) }
    var affinity by remember { mutableStateOf((card as? Trnr)?.affinity?.toString().orEmpty()) }
    var trnrWins by remember { mutableStateOf(wins.toString()) }
    var confirmDelete by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(card.title()) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AdminTxtField(stringResource(R.string.admin_name), name, { name = it }, Modifier.fillMaxWidth())
                when (card) {
                    is Stick -> {
                        SegmentedTabs(Rarity.entries.map { it.code }, rarity.ordinal, { rarity = Rarity.entries[it] })
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            AdminTxtField("D", d, { d = it }, Modifier.weight(1f), numeric = true)
                            AdminTxtField("S", s, { s = it }, Modifier.weight(1f), numeric = true)
                            AdminTxtField("A", a, { a = it }, Modifier.weight(1f), numeric = true)
                        }
                        ChipRow(TuningFlag.entries, { it in tuning }, { it.name.lowercase() }) { tuning = if (it in tuning) tuning - it else tuning + it }
                    }
                    is PwrUp -> ChipRow(PwrUpType.entries, { it == type }, { it.name.lowercase().replace('_', ' ') }) { type = it }
                    is Trnr -> Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        AdminTxtField(stringResource(R.string.admin_affinity), affinity, { affinity = it }, Modifier.weight(1f), numeric = true)
                        AdminTxtField(stringResource(R.string.admin_wins), trnrWins, { trnrWins = it }, Modifier.weight(1f), numeric = true)
                    }
                }
                ChunkyBtn(
                    stringResource(if (confirmDelete) R.string.admin_delete_confirm else R.string.admin_delete),
                    { if (confirmDelete) { onDelete(card); onClose() } else confirmDelete = true },
                    Modifier.fillMaxWidth(), style = BtnStyle.Wood, minHeight = 40.dp, textStyle = MaterialTheme.typography.titleSmall
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val nick = name.trim().ifEmpty { null }
                when (card) {
                    is Stick -> {
                        fun clampStat(v: String, old: Int) = (v.toIntOrNull() ?: old).coerceIn(1, 999)
                        onSaveCard(card.copy(rarity = rarity, base = BaseStats(clampStat(d, card.base.durability), clampStat(s, card.base.speed), clampStat(a, card.base.attack)), tuning = tuning, customName = nick))
                    }
                    is PwrUp -> onSaveCard(card.copy(type = type, customName = nick))
                    is Trnr -> onSaveTrnr(card.copy(customName = nick, affinity = (affinity.toIntOrNull() ?: card.affinity).coerceAtLeast(0)), (trnrWins.toIntOrNull() ?: wins).coerceAtLeast(0))
                }
                onClose()
            }) { Text(stringResource(R.string.admin_save)) }
        },
        dismissButton = { TextButton(onClick = onClose) { Text(stringResource(R.string.pvp_cancel)) } }
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun AdminCollPrvw() {
    val sampleStick = Stick("s1", Rarity.RARE, BaseStats(100, 50, 40))
    val sampleTrnr = Trnr("t1", com.sushanth.onlysticks.domain.model.TrnrBuff.MAX_HP, 100)
    com.sushanth.onlysticks.ui.theme.OnlySticksTheme {
        AdminCollContent(
            cardList = listOf(sampleStick),
            trnrs = listOf(TrnrRecord(sampleTrnr, 5)),
            onDeleteCard = {},
            onSaveCard = {},
            onSaveTrnr = { _, _ -> }
        )
    }
}



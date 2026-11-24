package com.sushanth.onlysticks.ui.deck

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.domain.appraisal.Appr
import com.sushanth.onlysticks.domain.model.Card
import com.sushanth.onlysticks.domain.model.PwrUp
import com.sushanth.onlysticks.domain.model.Rarity
import com.sushanth.onlysticks.domain.model.Stick
import com.sushanth.onlysticks.domain.stats.effectiveStats
import com.sushanth.onlysticks.domain.model.Trnr
import com.sushanth.onlysticks.ui.card.GameCard
import com.sushanth.onlysticks.ui.card.PokemonCardV
import com.sushanth.onlysticks.ui.card.title
import com.sushanth.onlysticks.ui.components.BtnStyle
import com.sushanth.onlysticks.ui.components.ChunkyBtn
import com.sushanth.onlysticks.ui.components.OutlinedTxt
import com.sushanth.onlysticks.ui.components.ParchmentPanel
import com.sushanth.onlysticks.ui.theme.GameTheme

internal enum class DeckCardFilt {
    ALL,
    COMMON,
    UNCOMMON,
    RARE,
    SUPER_RARE,
    ULTRA_RARE,
    POWER_UP,
    TRAINER,
    ;

    fun matches(card: Card): Boolean = when (this) {
        ALL -> true
        COMMON -> card is Stick && card.rarity == Rarity.COMMON
        UNCOMMON -> card is Stick && card.rarity == Rarity.UNCOMMON
        RARE -> card is Stick && card.rarity == Rarity.RARE
        SUPER_RARE -> card is Stick && card.rarity == Rarity.SUPER_RARE
        ULTRA_RARE -> card is Stick && card.rarity == Rarity.ULTRA_RARE
        POWER_UP -> card is PwrUp
        TRAINER -> card is Trnr
    }
}

@Composable
internal fun DeckCardFilt.label(): String = when (this) {
    DeckCardFilt.ALL -> stringResource(R.string.deck_filt_all)
    DeckCardFilt.COMMON -> Rarity.COMMON.code
    DeckCardFilt.UNCOMMON -> Rarity.UNCOMMON.code
    DeckCardFilt.RARE -> Rarity.RARE.code
    DeckCardFilt.SUPER_RARE -> Rarity.SUPER_RARE.code
    DeckCardFilt.ULTRA_RARE -> Rarity.ULTRA_RARE.code
    DeckCardFilt.POWER_UP -> stringResource(R.string.card_code_powerup)
    DeckCardFilt.TRAINER -> stringResource(R.string.card_code_trnr)
}

@Composable
fun DeckBldrRoute(
    source: DeckBldrSrc,
    modifier: Modifier = Modifier,
    vm: DeckBldrVm = viewModel { DeckBldrVm(source) },
) {
    val st by vm.state.collectAsStateWithLifecycle()
    DeckBldrScr(
        state = st,
        onAdd = { vm.add(it) },
        onRemove = vm::removeFromDeck,
        onClearTrnr = { vm.setTrnr(null) },
        onSave = { vm.save() },
        onRenameCard = vm::renameCard,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckBldrScr(
    state: DeckBldrUiState,
    onAdd: (Card) -> Unit,
    onRemove: (Int) -> Unit,
    onClearTrnr: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    onRenameCard: (Card, String) -> Unit = { _, _ -> },
) {
    val c = GameTheme.colors
    var selFilt by rememberSaveable { mutableStateOf(DeckCardFilt.ALL) }
    var inspectedCardId by rememberSaveable { mutableStateOf<String?>(null) }

    val avail = remember(state.owned, state.inDeckIds, selFilt) {
        state.owned.filter { it.id !in state.inDeckIds && selFilt.matches(it) }
    }
    val inspectedCard = remember(state.owned, inspectedCardId) {
        state.owned.firstOrNull { it.id == inspectedCardId }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PinnedDeckShelf(
            state = state,
            onRemove = onRemove,
            onClearTrnr = onClearTrnr,
            onSave = onSave,
        )

        FiltChipsRow(
            selected = selFilt,
            onSelect = { selFilt = it },
        )

        if (avail.isEmpty()) {
            ParchmentPanel(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.deck_owned_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = c.ink,
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(104.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(avail, key = { it.id }) { card ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(role = Role.Button, onClick = { inspectedCardId = card.id })
                            .padding(2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        GameCard(card = card, width = 96.dp, onClick = { inspectedCardId = card.id }, modifier = Modifier.fillMaxWidth())
                        OutlinedTxt(
                            text = (card as? Stick)?.effectiveStats?.let { stringResource(R.string.deck_tile_stats, it.attack, it.durability) } ?: card.title(),
                            style = MaterialTheme.typography.labelLarge,
                            color = c.onWood,
                            outlW = 2.dp,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }

    if (inspectedCard != null) {
        ModalBottomSheet(
            onDismissRequest = { inspectedCardId = null },
            containerColor = c.parchment,
            contentColor = c.ink,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp).padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                PokemonCardV(
                    card = inspectedCard,
                    apprVal = Appr.value(inspectedCard),
                    trnrSynergy = false,
                    onNickChange = { onRenameCard(inspectedCard, it) },
                    modifier = Modifier.width(260.dp),
                )
                }
                ChunkyBtn(
                    text = stringResource(
                        if (inspectedCard is Trnr) R.string.deck_equip_trnr else R.string.deck_add,
                    ),
                    onClick = {
                        onAdd(inspectedCard)
                        inspectedCardId = null
                    },
                    style = BtnStyle.Primary,
                    minHeight = 52.dp,
                    modifier = Modifier.width(260.dp),
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun DeckBldrScrPrvw() {
    val sampleStick = Stick("s1", Rarity.COMMON, com.sushanth.onlysticks.domain.model.BaseStats(100, 50, 40))
    val sampleTrnr = Trnr("t1", com.sushanth.onlysticks.domain.model.TrnrBuff.MAX_HP, 100)
    com.sushanth.onlysticks.ui.theme.OnlySticksTheme {
        DeckBldrScr(
            state = DeckBldrUiState(
                owned = listOf(sampleStick, sampleTrnr),
                slots = listOf(sampleStick),
                trainer = sampleTrnr,
            ),
            onAdd = {},
            onRemove = {},
            onClearTrnr = {},
            onSave = {},
        )
    }
}



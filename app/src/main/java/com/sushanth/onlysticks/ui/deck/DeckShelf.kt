package com.sushanth.onlysticks.ui.deck

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.ui.card.GameCard
import com.sushanth.onlysticks.ui.common.asStr
import com.sushanth.onlysticks.ui.components.BtnStyle
import com.sushanth.onlysticks.ui.components.ChunkyBtn
import com.sushanth.onlysticks.ui.components.GameBadge
import com.sushanth.onlysticks.ui.components.OutlinedTxt
import com.sushanth.onlysticks.ui.components.WoodPanel
import com.sushanth.onlysticks.ui.theme.GameTheme

private const val SHELF_COLUMNS = 4

@Composable
internal fun PinnedDeckShelf(
    state: DeckBldrUiState,
    onRemove: (Int) -> Unit,
    onClearTrnr: () -> Unit,
    onSave: () -> Unit,
) {
    val c = GameTheme.colors
    val titleTxt = state.deckName.ifBlank {
        stringResource(R.string.deck_slots_title, state.slots.size, state.slotLimit)
    }

    WoodPanel(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                OutlinedTxt(
                    text = titleTxt,
                    style = MaterialTheme.typography.titleMedium,
                    color = c.onWood,
                    outlW = 2.dp,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
                GameBadge(text = "${state.slots.size}/${state.slotLimit}")
            }

            val slotCnt = state.slotLimit + 1
            (0 until slotCnt).chunked(SHELF_COLUMNS).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    row.forEach { pos ->
                        BoxWithConstraints(Modifier.weight(1f)) {
                            if (pos == 0) {
                                GameCard(
                                    card = state.trainer,
                                    width = maxWidth,
                                    onClick = if (state.trainer != null) onClearTrnr else null,
                                    modifier = Modifier
                                        .border(2.dp, c.gold, RoundedCornerShape(maxWidth * 0.08f))
                                        ,
                                )
                            } else {
                                val index = pos - 1
                                val slotCard = state.slots.getOrNull(index)
                                GameCard(
                                    card = slotCard,
                                    width = maxWidth,
                                    onClick = if (slotCard != null) ({ onRemove(index) }) else null,
                                )
                            }
                        }
                    }
                    repeat(SHELF_COLUMNS - row.size) { Box(Modifier.weight(1f)) }
                }
            }
            Text(
                stringResource(R.string.deck_shelf_hint),
                style = MaterialTheme.typography.labelMedium,
                color = c.onWood.copy(alpha = 0.85f),
            )

            state.errors.forEach { error ->
                val errBg = lerp(c.parchment, c.hpFoe, 0.22f)
                val errShape = RoundedCornerShape(10.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(errShape)
                        .background(errBg)
                        .border(1.5.dp, c.hpFoe, errShape)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = DeckBldrVm.errTxt(error).asStr(),
                        style = MaterialTheme.typography.bodySmall,
                        color = c.ink,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ChunkyBtn(
                    text = stringResource(R.string.deck_save),
                    onClick = onSave,
                    style = BtnStyle.Primary,
                    enabled = !state.saving,
                    minHeight = 48.dp,
                )
                if (state.savedOnce && !state.saving) {
                    OutlinedTxt(
                        text = stringResource(R.string.deck_saved),
                        style = MaterialTheme.typography.titleMedium,
                        color = c.leafLight,
                        outlW = 2.dp,
                    )
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun PinnedDeckShelfPrvw() {
    val sampleStick = com.sushanth.onlysticks.domain.model.Stick(
        "s1", com.sushanth.onlysticks.domain.model.Rarity.COMMON, com.sushanth.onlysticks.domain.model.BaseStats(100, 50, 40)
    )
    com.sushanth.onlysticks.ui.theme.OnlySticksTheme {
        PinnedDeckShelf(
            state = DeckBldrUiState(
                slots = listOf(sampleStick),
                deckName = "Starter Deck",
            ),
            onRemove = {},
            onClearTrnr = {},
            onSave = {},
        )
    }
}


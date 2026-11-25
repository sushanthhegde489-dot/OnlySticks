// Filter chips for the deck builder collection.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.deck

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.domain.model.Card
import com.sushanth.onlysticks.domain.model.Stick
import com.sushanth.onlysticks.ui.components.OutlinedTxt
import com.sushanth.onlysticks.ui.theme.GameTheme

/** Horizontally scrolling row of filter chips. */
@Composable
internal fun FiltChipsRow(
    selected: DeckCardFilt,
    onSelect: (DeckCardFilt) -> Unit,
) {
    val c = GameTheme.colors
    val filts = DeckCardFilt.entries
    val pillShape = RoundedCornerShape(50)

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(filts, key = { it.name }) { filter ->
            val isSel = filter == selected
            val bgBrush = if (isSel) {
                Brush.verticalGradient(listOf(c.woodLight, c.wood))
            } else {
                Brush.verticalGradient(listOf(c.wood, c.woodDark))
            }
            Box(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clip(pillShape)
                    .background(bgBrush)
                    .border(
                        width = if (isSel) 2.dp else 1.5.dp,
                        color = if (isSel) c.gold else c.outline,
                        shape = pillShape,
                    )
                    .clickable(role = Role.Tab) { onSelect(filter) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                OutlinedTxt(
                    text = filter.label(),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSel) c.gold else c.onWood,
                    outlW = 2.dp,
                    maxLines = 1,
                )
            }
        }
    }
}

/** Android Studio preview. */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun FiltChipsRowPrvw() {
    com.sushanth.onlysticks.ui.theme.OnlySticksTheme {
        FiltChipsRow(selected = DeckCardFilt.ALL, onSelect = {})
    }
}


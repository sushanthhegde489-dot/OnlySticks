package com.sushanth.onlysticks.ui.arena

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.unit.dp
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.domain.model.PwrUpTarget
import com.sushanth.onlysticks.domain.model.PwrUpType
import com.sushanth.onlysticks.ui.card.GameCard
import com.sushanth.onlysticks.ui.card.pwrUpDesc
import com.sushanth.onlysticks.ui.components.OutlinedTxt
import com.sushanth.onlysticks.ui.components.ParchmentPanel
import com.sushanth.onlysticks.ui.components.SegmentedTabs
import com.sushanth.onlysticks.ui.components.woodBoard
import com.sushanth.onlysticks.ui.theme.DispFont
import com.sushanth.onlysticks.ui.theme.GameTheme

@Composable
internal fun ExtrasRow(match: MatchUi, onSelectSwtch: (Int?) -> Unit, onSelectPwrUp: (Int?) -> Unit) {
    if (match.switchableSlots.isEmpty() && match.pwrUpSlots.isEmpty()) return
    val you = match.combat.p1
    Row(
        Modifier.fillMaxWidth().woodBoard(crnr = 12.dp, trim = false).padding(start = 10.dp, end = 10.dp, top = 6.dp, bottom = 10.dp)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (match.switchableSlots.isNotEmpty()) {
            OutlinedTxt(stringResource(R.string.arena_sticks), style = MaterialTheme.typography.labelLarge.copy(fontFamily = DispFont), outlW = 2.dp)
            match.switchableSlots.forEach { slot ->
                val s = you.sticks[slot] ?: return@forEach
                val picked = slot == match.selSwtchSlot
                GameCard(
                    s.stick, width = 64.dp, selected = picked,
                    durability = s.durability.toFloat() / s.maxDurability,
                    onClick = { onSelectSwtch(if (picked) null else slot) },
                )
            }
        }
        if (match.pwrUpSlots.isNotEmpty()) {
            OutlinedTxt(stringResource(R.string.arena_items), style = MaterialTheme.typography.labelLarge.copy(fontFamily = DispFont), outlW = 2.dp)
            match.pwrUpSlots.forEach { slot ->
                val card = you.deck.getOrNull(slot) ?: return@forEach
                val picked = slot == match.selPwrUpSlot
                GameCard(
                    card, width = 64.dp, selected = picked,
                    onClick = { onSelectPwrUp(if (picked) null else slot) },
                )
            }
        }
    }
}

@Composable
internal fun PwrUpPanel(type: PwrUpType, target: PwrUpTarget?, onTarget: (PwrUpTarget) -> Unit) {
    val c = GameTheme.colors
    ParchmentPanel(Modifier.fillMaxWidth(), contentPadding = PaddingValues(10.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(pwrUpDesc(type), style = MaterialTheme.typography.bodySmall, color = c.ink)
            Text(stringResource(R.string.powerup_uses_turn), style = MaterialTheme.typography.labelSmall, color = c.inkSoft)
            SegmentedTabs(
                opts = listOf(stringResource(R.string.powerup_target_self), stringResource(R.string.powerup_target_opp)),
                selected = (target ?: PwrUpTarget.defFor(type)).ordinal,
                onSelect = { onTarget(PwrUpTarget.entries[it]) },
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun PwrUpPanelPrvw() {
    com.sushanth.onlysticks.ui.theme.OnlySticksTheme {
        PwrUpPanel(type = PwrUpType.LIGHTER, target = PwrUpTarget.OPPONENT, onTarget = {})
    }
}


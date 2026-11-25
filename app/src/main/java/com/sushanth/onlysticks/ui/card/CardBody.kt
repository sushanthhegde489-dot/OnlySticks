// Middle section of a full card: stat chips, move previews and item descriptions.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.domain.config.Cfg
import com.sushanth.onlysticks.domain.model.PwrUpType
import com.sushanth.onlysticks.domain.model.Stick
import com.sushanth.onlysticks.domain.stats.effectiveStats
import com.sushanth.onlysticks.ui.components.GameIcos
import com.sushanth.onlysticks.ui.theme.GameTheme

/** Durability / speed / attack chips for a stick. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun StatChips(stick: Stick) {
    val stats = stick.effectiveStats
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        StatChip(GameIcos.Shield, stringResource(R.string.stat_lbl_dur), stats.durability)
        StatChip(GameIcos.Spd, stringResource(R.string.stat_lbl_spd), stats.speed)
        StatChip(GameIcos.Atk, stringResource(R.string.stat_lbl_atk), stats.attack)
    }
}

/** One stat chip with an icon, label and value. */
@Composable
internal fun StatChip(icon: ImageVector, label: String, value: Int) {
    val c = GameTheme.colors
    val pillShape = RoundedCornerShape(50)
    Row(
        modifier = Modifier
            .clip(pillShape)
            .background(c.parchmentDeep)
            .border(1.5.dp, c.woodLight.copy(alpha = 0.8f), pillShape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = stringResource(R.string.stat_chip, label, value),
            style = MaterialTheme.typography.labelLarge,
            color = c.ink,
        )
    }
}

/** Table of the stick's moves with predicted damage and stamina cost. */
@Composable
internal fun MovesBox(stick: Stick) {
    val c = GameTheme.colors
    val moves = remember(stick) { CardMovePrvw.forStick(stick) }
    val boxShape = RoundedCornerShape(8.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(boxShape)
            .background(c.parchment.copy(alpha = 0.85f))
            .border(1.5.dp, c.woodLight.copy(alpha = 0.65f), boxShape)
            .padding(8.dp)
            ,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        moves.forEachIndexed { index, preview ->
            if (index > 0) HorizontalDivider(color = c.woodLight.copy(alpha = 0.35f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = preview.move.icon(),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = stringResource(preview.move.lblRes()),
                    style = MaterialTheme.typography.titleSmall,
                    color = c.ink,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(R.string.card_move_stam, preview.stamCost),
                    style = MaterialTheme.typography.bodySmall,
                    color = c.inkSoft,
                )
                Text(
                    text = preview.dmg?.let { stringResource(R.string.card_move_dmg, it) }
                        ?: stringResource(R.string.card_move_counter),
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.ink,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

/** Title and description box for power-ups and trainers. */
@Composable
internal fun ItemDesc(title: String, body: String) {
    val c = GameTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = c.ink)
        Text(body, style = MaterialTheme.typography.bodyMedium, color = c.inkSoft)
    }
}

/** Explanation text for a power-up type. */
@Composable
fun pwrUpDesc(type: PwrUpType): String = when (type) {
    PwrUpType.LIGHTER -> stringResource(R.string.powerup_lighter_desc, 40)
    PwrUpType.WATER_BOTTLE -> stringResource(
        R.string.powerup_water_bottle_desc, Cfg.WET_SPEED_PENALTY, Cfg.WET_ROUNDS,
    )
}

/** Android Studio preview. */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun StatChipsPrvw() {
    val stick = com.sushanth.onlysticks.domain.model.Stick(
        "preview", com.sushanth.onlysticks.domain.model.Rarity.RARE, com.sushanth.onlysticks.domain.model.BaseStats(100, 50, 40)
    )
    com.sushanth.onlysticks.ui.theme.OnlySticksTheme {
        StatChips(stick = stick)
    }
}


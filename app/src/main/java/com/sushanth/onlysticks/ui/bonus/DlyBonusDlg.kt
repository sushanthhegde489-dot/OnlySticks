package com.sushanth.onlysticks.ui.bonus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.domain.config.Cfg
import com.sushanth.onlysticks.domain.economy.DailyBonusOffer
import com.sushanth.onlysticks.ui.components.ChunkyBtn
import com.sushanth.onlysticks.ui.components.GameIcos
import com.sushanth.onlysticks.ui.components.OutlinedTxt
import com.sushanth.onlysticks.ui.components.WoodPanel
import com.sushanth.onlysticks.ui.theme.GameTheme

@Composable
fun DlyBonusDlg(offer: DailyBonusOffer, onClaim: () -> Unit) {
    val c = GameTheme.colors
    Dialog(onDismissRequest = onClaim) {
        WoodPanel(Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTxt(stringResource(R.string.bonus_title), style = MaterialTheme.typography.headlineSmall)
                Text(stringResource(R.string.bonus_body), style = MaterialTheme.typography.bodyMedium, color = c.onWood)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Cfg.Economy.DAILY_STREAK_REWARDS.forEachIndexed { i, amount ->
                        val day = i + 1
                        val today = day == offer.day
                        val shape = RoundedCornerShape(8.dp)
                        Column(
                            Modifier
                                .weight(1f)
                                .clip(shape)
                                .background(if (today) c.leaf else if (day < offer.day) c.woodDark else c.parchment.copy(alpha = 0.25f))
                                .border(2.dp, if (today) c.gold else Color.Transparent, shape)
                                .padding(vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(stringResource(R.string.bonus_day, day), style = MaterialTheme.typography.labelSmall, color = c.onWood)
                            Text(amount.toString(), style = MaterialTheme.typography.labelLarge, color = if (today) c.gold else c.onWood)
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(GameIcos.Coin, contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(36.dp))
                    OutlinedTxt("+${offer.amount}", style = MaterialTheme.typography.displaySmall, color = c.gold)
                }
                Box(Modifier.fillMaxWidth()) {
                    ChunkyBtn(stringResource(R.string.bonus_claim), onClaim, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun DlyBonusDlgPrvw() {
    com.sushanth.onlysticks.ui.theme.OnlySticksTheme {
        DlyBonusDlg(
            offer = DailyBonusOffer(day = 3, amount = 250),
            onClaim = {},
        )
    }
}


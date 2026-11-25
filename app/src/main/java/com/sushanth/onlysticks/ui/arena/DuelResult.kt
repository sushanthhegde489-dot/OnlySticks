// Victory / defeat overlay shown at the end of a duel.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.arena

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.ui.components.BtnStyle
import com.sushanth.onlysticks.ui.components.ChunkyBtn
import com.sushanth.onlysticks.ui.components.GameIcos
import com.sushanth.onlysticks.ui.components.OutlinedTxt
import com.sushanth.onlysticks.ui.components.ParchmentPanel
import com.sushanth.onlysticks.ui.components.WoodPanel
import com.sushanth.onlysticks.ui.components.woodBoard
import com.sushanth.onlysticks.ui.theme.GameTheme

/** End-of-match overlay with the result, round count and buttons to rematch (if allowed) or leave. */
@Composable
internal fun ResOverlay(won: Boolean, botName: String, rounds: Int, onPlayAgain: (() -> Unit)?, onLeave: () -> Unit) {
    val c = GameTheme.colors
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(remember { MutableInteractionSource() }, indication = null) { },
        contentAlignment = Alignment.Center,
    ) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                Box(Modifier.size(width = 280.dp, height = 84.dp).woodBoard(crnr = 22.dp))
                OutlinedTxt(
                    stringResource(if (won) R.string.res_victory else R.string.res_defeat),
                    style = MaterialTheme.typography.displaySmall,
                    color = if (won) c.gold else Color(0xFFFFF3D6),
                    outlW = 4.dp,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
            WoodPanel(Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    if (!won) {
                        Icon(GameIcos.Brkn, null, tint = Color.Unspecified, modifier = Modifier.size(64.dp))
                    } else Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        repeat(3) { i ->
                            Icon(
                                GameIcos.Star, null,
                                tint = if (won) Color.Unspecified else c.woodDark,
                                modifier = Modifier.size(if (i == 1) 52.dp else 40.dp),
                            )
                        }
                    }
                    ParchmentPanel(Modifier.fillMaxWidth()) {
                        Text(
                            stringResource(if (won) R.string.res_victory_body else R.string.res_defeat_body, botName, rounds),
                            style = MaterialTheme.typography.bodyLarge,
                            color = c.ink,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    if (onPlayAgain != null) {
                        ChunkyBtn(
                            text = stringResource(R.string.arena_play_again),
                            onClick = onPlayAgain,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    ChunkyBtn(
                        text = stringResource(R.string.res_lobby),
                        onClick = onLeave,
                        style = BtnStyle.Wood,
                        minHeight = 48.dp,
                        textStyle = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

/** Android Studio preview. */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun ResOverlayPrvw() {
    com.sushanth.onlysticks.ui.theme.OnlySticksTheme {
        ResOverlay(
            won = true,
            botName = "StickBot",
            rounds = 4,
            onPlayAgain = {},
            onLeave = {},
        )
    }
}


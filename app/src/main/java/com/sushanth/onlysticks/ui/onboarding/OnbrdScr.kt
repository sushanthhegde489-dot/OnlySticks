package com.sushanth.onlysticks.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.domain.player.DispNameRes
import com.sushanth.onlysticks.ui.components.GameIcos
import com.sushanth.onlysticks.ui.components.Medallion
import com.sushanth.onlysticks.ui.components.OutlinedTxt
import com.sushanth.onlysticks.ui.components.WoodPanel
import com.sushanth.onlysticks.ui.theme.GameTheme

@Composable
fun OnbrdScr(onSave: suspend (String) -> DispNameRes, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Medallion(size = 96.dp, fill = GameTheme.colors.leaf) {
                Icon(GameIcos.Battle, contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(56.dp))
            }
            OutlinedTxt(stringResource(R.string.app_name), style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center)
            WoodPanel(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTxt(stringResource(R.string.onbrd_title), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.onbrd_body), style = MaterialTheme.typography.bodyLarge, color = GameTheme.colors.onWood)
                    NameForm(initial = "", btnTxt = R.string.onbrd_start, onSave = onSave)
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun OnbrdScrPrvw() {
    com.sushanth.onlysticks.ui.theme.OnlySticksTheme {
        OnbrdScr(onSave = { DispNameRes.Valid(it) })
    }
}


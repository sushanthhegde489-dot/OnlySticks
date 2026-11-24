package com.sushanth.onlysticks.ui.onboarding

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.ui.account.AcctFx
import com.sushanth.onlysticks.ui.account.AcctVm
import com.sushanth.onlysticks.ui.common.asStr
import com.sushanth.onlysticks.ui.components.BtnStyle
import com.sushanth.onlysticks.ui.components.ChunkyBtn
import com.sushanth.onlysticks.ui.components.GameIcos
import com.sushanth.onlysticks.ui.components.Medallion
import com.sushanth.onlysticks.ui.components.OutlinedTxt
import com.sushanth.onlysticks.ui.components.ParchmentPanel
import com.sushanth.onlysticks.ui.components.WoodPanel
import com.sushanth.onlysticks.ui.theme.GameTheme

@Composable
fun WelcScr(viewModel: AcctVm, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val act = LocalActivity.current
    AcctFx(viewModel, state)
    WelcScrContent(
        state = state,
        onSignIn = { act?.let(viewModel::signIn) },
        onContinueOffline = viewModel::continueOffline,
        canSignIn = act != null,
        modifier = modifier,
    )
}

@Composable
fun WelcScrContent(
    state: com.sushanth.onlysticks.ui.account.AcctUiState,
    onSignIn: () -> Unit,
    onContinueOffline: () -> Unit,
    canSignIn: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val c = GameTheme.colors
    Box(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Medallion(size = 112.dp, fill = c.leaf) {
                Icon(GameIcos.Battle, contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(64.dp))
            }
            OutlinedTxt(stringResource(R.string.app_name), style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center)
            Text(stringResource(R.string.welc_tagline), style = MaterialTheme.typography.titleMedium, color = c.ink, textAlign = TextAlign.Center)
            WoodPanel(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTxt(stringResource(R.string.welc_sign_in_title), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.welc_sign_in_body), style = MaterialTheme.typography.bodyLarge, color = c.onWood)
                    if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth(), color = c.gold)
                    ChunkyBtn(
                        text = stringResource(R.string.acct_sign_in),
                        onClick = onSignIn,
                        enabled = state.googConfigured && !state.busy && canSignIn,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (!state.googConfigured) {
                        Text(stringResource(R.string.acct_goog_unconfigured), style = MaterialTheme.typography.bodySmall, color = c.onWood)
                    }
                    if (state.offlineAllowed) {
                        ChunkyBtn(
                            text = stringResource(R.string.welc_offline_debug),
                            onClick = onContinueOffline,
                            style = BtnStyle.Wood,
                            minHeight = 44.dp,
                            textStyle = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
            state.message?.let { msg ->
                ParchmentPanel(Modifier.fillMaxWidth()) { Text(msg.asStr(), color = c.ink, style = MaterialTheme.typography.bodyLarge) }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun WelcScrPrvw() {
    com.sushanth.onlysticks.ui.theme.OnlySticksTheme {
        WelcScrContent(
            state = com.sushanth.onlysticks.ui.account.AcctUiState(googConfigured = true, offlineAllowed = true),
            onSignIn = {},
            onContinueOffline = {},
        )
    }
}


// Bottom sheet showing the player's account: name, Google sign-in, Drive backup, stats and admin entry.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.account

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.data.repository.AcctState
import com.sushanth.onlysticks.data.repository.Stat
import com.sushanth.onlysticks.ui.common.asStr
import com.sushanth.onlysticks.ui.components.BtnStyle
import com.sushanth.onlysticks.ui.components.ChunkyBtn
import com.sushanth.onlysticks.ui.components.OutlinedTxt
import com.sushanth.onlysticks.ui.components.ParchmentPanel
import com.sushanth.onlysticks.ui.components.WoodPanel
import com.sushanth.onlysticks.ui.onboarding.NameForm
import com.sushanth.onlysticks.ui.theme.GameTheme
import com.sushanth.onlysticks.ui.theme.OnlySticksTheme
import java.text.DateFormat
import java.util.Date

/** Account bottom sheet wired to [AcctVm]. [onTut] replays the tutorial; [onOpenAdmin] opens the admin tools. */
@Composable
fun AcctSheet(viewModel: AcctVm, onDismiss: () -> Unit, onTut: () -> Unit, onOpenAdmin: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AcctFx(viewModel, state)
    AcctSheetContent(
        state = state,
        onDismiss = onDismiss,
        onTut = onTut,
        onOpenAdmin = onOpenAdmin,
        onSaveName = viewModel::saveName,
        onBkp = viewModel::bkp,
        onAskRest = viewModel::askRest,
        onSignOut = viewModel::signOut,
    )
}

/** Stateless content of the account sheet (also used by previews). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcctSheetContent(
    state: AcctUiState,
    onDismiss: () -> Unit,
    onTut: () -> Unit,
    onOpenAdmin: () -> Unit,
    onSaveName: suspend (String) -> com.sushanth.onlysticks.domain.player.DispNameRes,
    onBkp: () -> Unit,
    onAskRest: () -> Unit,
    onSignOut: () -> Unit,
) {
    val c = GameTheme.colors
    val account = state.account

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = c.woodDark) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 16.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            OutlinedTxt(stringResource(R.string.acct_title), style = MaterialTheme.typography.headlineSmall)
            WoodPanel(Modifier.fillMaxWidth()) {
                NameForm(
                    initial = account?.displayName.orEmpty(),
                    btnTxt = R.string.acct_save_name,
                    onSave = onSaveName,
                )
            }
            WoodPanel(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTxt(stringResource(R.string.acct_goog), style = MaterialTheme.typography.titleLarge)
                    val email = account?.googleEmail
                    Text(
                        when {
                            !state.googConfigured -> stringResource(R.string.acct_goog_unconfigured)
                            email == null -> stringResource(R.string.acct_goog_body)
                            else -> stringResource(R.string.acct_goog_as, email)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.onWood,
                    )
                    account?.lastBackupAt?.let {
                        Text(stringResource(R.string.acct_last_bkp, DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(it))), style = MaterialTheme.typography.labelMedium, color = c.onWood)
                    }
                    if (state.stats.values.any { it > 0 }) {
                        val st = state.stats
                        Text(
                            stringResource(R.string.acct_stats, st[Stat.PVE_PLAYED] ?: 0L, st[Stat.PVE_WON] ?: 0L, st[Stat.PVP_WON] ?: 0L, st[Stat.PACKS_OPENED] ?: 0L),
                            style = MaterialTheme.typography.labelMedium,
                            color = c.onWood,
                        )
                    }
                    if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth(), color = c.gold)
                    if (email != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ChunkyBtn(stringResource(R.string.acct_bkp), onBkp, Modifier.weight(1f), enabled = !state.busy, textStyle = MaterialTheme.typography.titleMedium)
                            ChunkyBtn(stringResource(R.string.acct_rest), onAskRest, Modifier.weight(1f), style = BtnStyle.Secondary, enabled = !state.busy, textStyle = MaterialTheme.typography.titleMedium)
                        }
                    }
                    run {
                        ChunkyBtn(
                            text = stringResource(R.string.acct_sign_out),
                            onClick = onSignOut,
                            style = BtnStyle.Wood,
                            enabled = !state.busy,
                            minHeight = 44.dp,
                            textStyle = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ChunkyBtn(
                    stringResource(R.string.acct_how_to_play), onTut, Modifier.weight(1f),
                    style = BtnStyle.Wood, minHeight = 44.dp, textStyle = MaterialTheme.typography.titleSmall,
                )
                if (account?.isAdmin == true) {
                    ChunkyBtn(
                        stringResource(R.string.admin_open), onOpenAdmin, Modifier.weight(1f),
                        style = BtnStyle.Secondary, minHeight = 44.dp, textStyle = MaterialTheme.typography.titleSmall,
                    )
                }
            }
            state.message?.let { msg ->
                ParchmentPanel(Modifier.fillMaxWidth()) { Text(msg.asStr(), color = c.ink, style = MaterialTheme.typography.bodyLarge) }
            }
        }
    }
}

/** Side effects for the account flow: launches the Drive consent screen and shows the restore / sign-out dialogs. */
@Composable
fun AcctFx(viewModel: AcctVm, state: AcctUiState) {
    val consent = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
        viewModel.onConsentRes(it.data)
    }
    LaunchedEffect(state.consent) {
        state.consent?.let { consent.launch(IntentSenderRequest.Builder(it.intentSender).build()) }
    }
    if (state.confirmRest) {
        val info = state.bkpInfo
        AlertDialog(
            onDismissRequest = viewModel::dismissRest,
            title = { Text(stringResource(R.string.acct_rest_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (info != null) {
                        val saved = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(info.savedAt))
                        Text(stringResource(R.string.acct_rest_saved, saved), style = MaterialTheme.typography.titleSmall)
                        if (info.cardCnt != null && info.coins != null) {
                            Text(stringResource(R.string.acct_rest_counts, info.cardCnt, info.coins))
                        }
                    }
                    Text(stringResource(R.string.acct_rest_body))
                }
            },
            confirmButton = { TextButton(onClick = viewModel::restore) { Text(stringResource(R.string.acct_rest)) } },
            dismissButton = { TextButton(onClick = viewModel::dismissRest) { Text(stringResource(R.string.acct_not_now)) } },
        )
    }
    if (state.confirmSignOut) {
        AlertDialog(
            onDismissRequest = viewModel::staySignedIn,
            title = { Text(stringResource(R.string.acct_sign_out_title)) },
            text = { Text(stringResource(R.string.acct_sign_out_body)) },
            confirmButton = { TextButton(onClick = viewModel::signOutAnyway) { Text(stringResource(R.string.acct_sign_out_anyway)) } },
            dismissButton = { TextButton(onClick = viewModel::staySignedIn) { Text(stringResource(R.string.acct_stay)) } },
        )
    }
}

/** Android Studio preview. */
@Preview(showBackground = true)
@Composable
private fun AcctSheetPrvw() {
    OnlySticksTheme {
        AcctSheetContent(
            state = AcctUiState(
                account = AcctState(
                    displayName = "StickMaster",
                    googleEmail = "player@example.com",
                    lastBackupAt = System.currentTimeMillis(),
                    isAdmin = true,
                ),
                googConfigured = true,
            ),
            onDismiss = {},
            onTut = {},
            onOpenAdmin = {},
            onSaveName = { com.sushanth.onlysticks.domain.player.DispNameRes.Valid(it) },
            onBkp = {},
            onAskRest = {},
            onSignOut = {},
        )
    }
}


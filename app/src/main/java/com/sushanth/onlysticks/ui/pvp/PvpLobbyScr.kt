// Play-a-friend lobby: choose stake cards, then host a game, join a nearby one or enter an address.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.pvp

import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.domain.appraisal.Appr
import com.sushanth.onlysticks.domain.config.Cfg
import com.sushanth.onlysticks.domain.economy.BetRules
import com.sushanth.onlysticks.domain.model.Deck
import com.sushanth.onlysticks.domain.model.Stick
import com.sushanth.onlysticks.ui.card.GameCard
import com.sushanth.onlysticks.ui.components.BtnStyle
import com.sushanth.onlysticks.ui.components.ChunkyBtn
import com.sushanth.onlysticks.ui.components.GameBadge
import com.sushanth.onlysticks.ui.components.GameIcos
import com.sushanth.onlysticks.ui.components.OutlinedTxt
import com.sushanth.onlysticks.ui.components.ParchmentPanel
import com.sushanth.onlysticks.ui.components.WoodPanel
import com.sushanth.onlysticks.ui.theme.DispFont
import com.sushanth.onlysticks.ui.theme.GameTheme

/** Android 17+ requires this runtime permission for local network access. */
private const val LOCAL_NETWORK = "android.permission.ACCESS_LOCAL_NETWORK"
private const val LOCAL_NETWORK_SDK = 37

/**
 * PvP lobby. Asks for the local-network permission when needed, browses for games while open,
 * and lets the player pick 1-3 stake sticks before hosting or joining.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PvpLobbyScr(
    phase: PvpPhase,
    deck: Deck?,
    sticks: List<Stick>,
    onHost: (List<Stick>) -> Unit,
    onJoin: (String, Int, List<Stick>) -> Unit,
    nearby: List<LanHost>,
    onBrowse: (Boolean) -> Unit,
    onCancel: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = GameTheme.colors
    val context = LocalContext.current
    var myBetIds by rememberSaveable { mutableStateOf(listOf<String>()) }
    var address by rememberSaveable { mutableStateOf("") }
    var pend by remember { mutableStateOf<(() -> Unit)?>(null) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) pend?.invoke()
        pend = null
    }
    val withNetwork: (() -> Unit) -> Unit = { action ->
        val needed = Build.VERSION.SDK_INT >= LOCAL_NETWORK_SDK &&
            ContextCompat.checkSelfPermission(context, LOCAL_NETWORK) != PackageManager.PERMISSION_GRANTED
        if (needed) { pend = action; permission.launch(LOCAL_NETWORK) } else action()
    }
    var manual by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) { withNetwork { onBrowse(true) } }
    DisposableEffect(Unit) { onDispose { onBrowse(false) } }
    val bet = sticks.filter { it.id in myBetIds }
    val busy = phase is PvpPhase.Hosting || phase is PvpPhase.Joining || phase is PvpPhase.Handshaking
    val ready = deck != null && bet.size in Cfg.MIN_WAGER_CARDS..Cfg.MAX_WAGER_CARDS && !busy

    Column(
        modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row {
            ChunkyBtn(stringResource(R.string.pvp_back), onBack, style = BtnStyle.Wood, minHeight = 40.dp, textStyle = MaterialTheme.typography.titleSmall, enabled = !busy)
        }
        WoodPanel(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTxt(stringResource(R.string.pvp_title), style = MaterialTheme.typography.headlineSmall)
                Text(stringResource(R.string.pvp_same_wifi), style = MaterialTheme.typography.bodyMedium, color = c.onWood)
            }
        }
        NearbyGames(nearby, myBetVal = Appr.tot(bet), joinOn = ready, onJoin = { host -> withNetwork { onJoin(host.address, host.port, bet) } })
        WoodPanel(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row {
                    OutlinedTxt(stringResource(R.string.pvp_bet_title), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(c.leaf)
                            .border(2.dp, c.outline, RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        OutlinedTxt(stringResource(R.string.pvp_bet_val, Appr.tot(bet)), style = MaterialTheme.typography.labelLarge.copy(fontFamily = DispFont), outlW = 2.dp)
                    }
                }
                Text(stringResource(R.string.pvp_bet_body, Cfg.MIN_WAGER_CARDS, Cfg.MAX_WAGER_CARDS), style = MaterialTheme.typography.bodyMedium, color = c.onWood)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sticks.forEach { stick ->
                        val picked = stick.id in myBetIds
                        GameCard(
                            stick,
                            width = 84.dp,
                            selected = picked,
                            onClick = {
                                if (busy) return@GameCard
                                if (picked) {
                                    myBetIds = myBetIds - stick.id
                                } else {
                                    if (myBetIds.size < 3) {
                                        myBetIds = myBetIds + stick.id
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
        PhasePanel(phase, onCancel)
        ChunkyBtn(
            text = stringResource(R.string.pvp_host),
            onClick = { withNetwork { onHost(bet) } },
            enabled = ready,
            icon = GameIcos.Battle,
            modifier = Modifier.fillMaxWidth()
        )
        if (!manual) {
            ChunkyBtn(
                stringResource(R.string.pvp_manual),
                { manual = true },
                style = BtnStyle.Wood,
                minHeight = 40.dp,
                textStyle = MaterialTheme.typography.titleSmall
            )
        }
        if (manual) WoodPanel(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it.filter { ch -> ch.isDigit() || ch == '.' }.take(15) },
                    label = { Text(stringResource(R.string.pvp_addr)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = c.ink, unfocusedTextColor = c.ink,
                        focusedContainerColor = c.parchment, unfocusedContainerColor = c.parchment,
                        focusedLabelColor = c.ink, unfocusedLabelColor = c.inkSoft
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                ChunkyBtn(
                    text = stringResource(R.string.pvp_join),
                    onClick = { withNetwork { onJoin(address, Cfg.Pvp.PORT, bet) } },
                    enabled = ready && address.count { it == '.' } == 3,
                    style = BtnStyle.Secondary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        if (deck == null) Text(stringResource(R.string.arena_need_deck), color = c.onWood)
    }
}

/** List of nearby games with their stake value; join is enabled only when the stakes are close enough. */
@Composable
private fun NearbyGames(hosts: List<LanHost>, myBetVal: Int, joinOn: Boolean, onJoin: (LanHost) -> Unit) {
    val c = GameTheme.colors
    WoodPanel(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTxt(stringResource(R.string.pvp_nearby_title), style = MaterialTheme.typography.titleLarge)
            if (hosts.isEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CircularProgressIndicator(Modifier.size(18.dp), color = c.gold, strokeWidth = 2.dp)
                    Text(stringResource(R.string.pvp_nearby_searching), style = MaterialTheme.typography.bodyMedium, color = c.onWood)
                }
            }
            hosts.forEach { host ->
                val matched = host.betVal?.let { myBetVal > 0 && BetRules.withinTolerance(myBetVal, it) }
                ParchmentPanel(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(stringResource(R.string.pvp_nearby_game, host.name), style = MaterialTheme.typography.titleMedium, color = c.ink)
                            Text(
                                host.betVal?.let { stringResource(R.string.pvp_their_bet, it) } ?: stringResource(R.string.pvp_their_bet_unknown),
                                style = MaterialTheme.typography.titleSmall,
                                color = c.ink
                            )
                            if (matched != null && joinOn) Text(
                                stringResource(if (matched) R.string.pvp_bet_match else R.string.pvp_bet_mismatch, myBetVal),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (matched) c.leafDark else c.ink
                            )
                            Text(host.address, style = MaterialTheme.typography.labelSmall, color = c.inkSoft)
                        }
                        ChunkyBtn(
                            stringResource(R.string.pvp_join),
                            { onJoin(host) },
                            enabled = joinOn && matched != false,
                            minHeight = 40.dp,
                            textStyle = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }
            if (hosts.isNotEmpty() && !joinOn) {
                Text(stringResource(R.string.pvp_pick_bet_first), style = MaterialTheme.typography.labelMedium, color = c.onWood)
            }
        }
    }
}

/** Status panel for hosting/joining/handshaking/failure, with a cancel button. */
@Composable
private fun PhasePanel(phase: PvpPhase, onCancel: () -> Unit) {
    val c = GameTheme.colors
    val text = when (phase) {
        PvpPhase.Idle, PvpPhase.InMatch -> return
        is PvpPhase.Hosting -> phase.address?.let { stringResource(R.string.pvp_hosting, it) } ?: stringResource(R.string.pvp_hosting_no_ip)
        PvpPhase.Joining -> stringResource(R.string.pvp_joining)
        PvpPhase.Handshaking -> stringResource(R.string.pvp_handshaking)
        is PvpPhase.Failed -> stringResource(phase.reason.msgRes())
    }
    ParchmentPanel(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text, style = MaterialTheme.typography.bodyLarge, color = c.ink)
            if (phase !is PvpPhase.Failed) {
                LinearProgressIndicator(Modifier.fillMaxWidth(), color = c.leaf)
                ChunkyBtn(stringResource(R.string.pvp_cancel), onCancel, style = BtnStyle.Wood, minHeight = 40.dp, textStyle = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

/** Message for each failure reason. */
private fun PvpFailure.msgRes(): Int = when (this) {
    PvpFailure.CANNOT_HOST -> R.string.pvp_fail_host
    PvpFailure.CANNOT_CONNECT -> R.string.pvp_fail_connect
    PvpFailure.DISCONNECTED -> R.string.pvp_fail_disconnected
    PvpFailure.PROTOCOL -> R.string.pvp_fail_protocol
    PvpFailure.STAKES_NOT_MATCHED -> R.string.pvp_fail_bets
    PvpFailure.OPPONENT_DECK_INVALID -> R.string.pvp_fail_their_deck
    PvpFailure.MY_LOADOUT_INVALID -> R.string.pvp_fail_my_deck
}

/** Android Studio preview. */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun PvpLobbyScrPrvw() {
    val sampleStick = Stick("s1", com.sushanth.onlysticks.domain.model.Rarity.COMMON, com.sushanth.onlysticks.domain.model.BaseStats(100, 50, 40))
    val sampleDeck = Deck("d1", "My Deck", listOf(sampleStick), null)
    com.sushanth.onlysticks.ui.theme.OnlySticksTheme {
        PvpLobbyScr(
            phase = PvpPhase.Idle,
            deck = sampleDeck,
            sticks = listOf(sampleStick),
            onHost = {},
            onJoin = { _, _, _ -> },
            nearby = listOf(LanHost("Player 2", "192.168.1.100", 8080, betVal = 420)),
            onBrowse = {},
            onCancel = {},
            onBack = {}
        )
    }
}


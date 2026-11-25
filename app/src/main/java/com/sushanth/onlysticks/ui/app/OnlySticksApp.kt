// Root composable and top-level navigation (welcome, onboarding, tutorial, then the main game shell).
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sushanth.onlysticks.AppCont
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.data.repository.AcctState
import com.sushanth.onlysticks.data.repository.SettingsRepository
import com.sushanth.onlysticks.domain.economy.DailyBonusOffer
import com.sushanth.onlysticks.domain.model.Stick
import com.sushanth.onlysticks.net.LocalAddr
import com.sushanth.onlysticks.ui.account.AcctSheet
import com.sushanth.onlysticks.ui.account.AcctVm
import com.sushanth.onlysticks.ui.admin.AdminScr
import com.sushanth.onlysticks.ui.admin.AdminVm
import com.sushanth.onlysticks.ui.arena.CombatArenaVm
import com.sushanth.onlysticks.ui.arena.DuelRoute
import com.sushanth.onlysticks.ui.bonus.DlyBonusDlg
import com.sushanth.onlysticks.ui.components.CoinsPill
import com.sushanth.onlysticks.ui.components.GameBg
import com.sushanth.onlysticks.ui.components.GameIcos
import com.sushanth.onlysticks.ui.components.GameMotion
import com.sushanth.onlysticks.ui.components.Medallion
import com.sushanth.onlysticks.ui.components.OutlinedTxt
import com.sushanth.onlysticks.ui.components.woodBoard
import com.sushanth.onlysticks.ui.deck.DeckBldrRoute
import com.sushanth.onlysticks.ui.home.LobbyScr
import com.sushanth.onlysticks.ui.onboarding.OnbrdScr
import com.sushanth.onlysticks.ui.onboarding.TutScr
import com.sushanth.onlysticks.ui.onboarding.WelcScr
import com.sushanth.onlysticks.ui.pvp.PvpLobbyScr
import com.sushanth.onlysticks.ui.pvp.PvpMatchVm
import com.sushanth.onlysticks.ui.shop.ShopRoute
import com.sushanth.onlysticks.ui.shop.ShopVm
import com.sushanth.onlysticks.ui.theme.GameTheme
import com.sushanth.onlysticks.ui.theme.OnlySticksTheme
import java.text.NumberFormat
import kotlinx.coroutines.launch

/** Tabs in the bottom tray. */
enum class AppTab { DECK, BATTLE, SHOP }

/**
 * Root of the UI. Chooses which flow to show from the account state:
 * not signed in -> welcome, no name -> onboarding, tutorial not done -> tutorial, otherwise the game.
 */
@Composable
fun OnlySticksApp(cont: AppCont) {
    if (LocalInspectionMode.current) {
        GameBg {
            Column(Modifier.fillMaxSize()) {
                TopBar(name = "StickMaster", bal = 1250L, onProf = {}, modifier = Modifier.statusBarsPadding())
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    LobbyScr(
                        deck = null,
                        selTier = 1,
                        onSelectTier = {},
                        onBattle = {},
                        onEditDeck = {},
                        onPlayFriend = {},
                    )
                }
                BottomTray(selected = AppTab.BATTLE, onSelect = {})
            }
        }
        return
    }
    val account by remember { cont.profileRepo.observeAcct() }.collectAsStateWithLifecycle(null)
    val offline by remember { cont.settRepo.observeFlag(SettingsRepository.OFFLINE_DEBUG) }.collectAsStateWithLifecycle<Boolean?>(null)
    val acctVm = viewModel {
        AcctVm(
            cont.profileRepo,
            cont.googAcct,
            cont.cloudBkp,
            cont.settRepo,
            cont.offlineSignInAllowed,
            cont.startSeed,
            cont.statsRepo,
        )
    }
    var replayTut by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val a = account
    val signedIn = a?.googleEmail != null || (offline == true && cont.offlineSignInAllowed)
    GameBg {
        when {
            a == null || offline == null -> Unit
            !signedIn -> WelcScr(acctVm, Modifier.safeDrawingPadding())
            a.displayName.isNullOrEmpty() -> OnbrdScr(
                onSave = { name -> cont.profileRepo.setDispName(name) },
                modifier = Modifier.safeDrawingPadding(),
            )
            !a.tutorialDone || replayTut -> TutScr(
                onDone = {
                    replayTut = false
                    scope.launch { cont.profileRepo.setTutDone(true) }
                },
                modifier = Modifier.safeDrawingPadding(),
            )
            else -> GameShell(cont, a, acctVm, onTut = { replayTut = true })
        }
    }
}

/**
 * Main game: top bar, bottom tabs (deck / battle / shop), duel screens, admin tools, account sheet
 * and the daily bonus dialog. Creates the screen ViewModels.
 */
@Composable
private fun GameShell(cont: AppCont, account: AcctState, acctVm: AcctVm, onTut: () -> Unit) {
    val plrName = account.displayName.orEmpty()
    val context = LocalContext.current.applicationContext
    val deckSrc = cont.deckSrc
    val arena = viewModel { CombatArenaVm(matchSink = cont.matchSink) }
    val pvp = viewModel {
        PvpMatchVm(network = cont.pvpNetwork, localAddr = { LocalAddr.ipv4(context) }, sink = cont.pvpSink)
    }
    val shop = viewModel { ShopVm(cont.shopRepo) }
    val admin = viewModel { AdminVm(cont.adminRepo) }
    val scope = rememberCoroutineScope()
    var bonus by remember { mutableStateOf<DailyBonusOffer?>(null) }
    LaunchedEffect(Unit) { bonus = cont.coinsRepo.dlyBonusOffer() }
    var showAdmin by rememberSaveable { mutableStateOf(false) }
    val trnrs by remember { cont.trainerRepo.observeTrnrs() }.collectAsStateWithLifecycle(emptyList())
    val arenaState by arena.state.collectAsStateWithLifecycle()
    val pvpState by pvp.state.collectAsStateWithLifecycle()
    val pvpPhase by pvp.phase.collectAsStateWithLifecycle()
    val nearby by pvp.nearbyHosts.collectAsStateWithLifecycle()
    val decks by deckSrc.observeDecks().collectAsStateWithLifecycle(emptyList())
    val cardList by remember { cont.cardRepo.observeCards() }.collectAsStateWithLifecycle(emptyList())
    val bal by remember { cont.coinsRepo.observeBal() }.collectAsStateWithLifecycle(0L)
    var tab by rememberSaveable { mutableStateOf(AppTab.BATTLE) }
    var friendMode by rememberSaveable { mutableStateOf(false) }
    var showAcct by rememberSaveable { mutableStateOf(false) }
    val deck = decks.firstOrNull()
    val mode = when {
        arenaState.match != null -> Mode.BOT_DUEL
        pvpState.match != null -> Mode.PVP_DUEL
        else -> Mode.MENU
    }

    AnimatedContent(
        targetState = mode,
        transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.96f)) togetherWith fadeOut() },
        label = "mode",
    ) { current ->
        when (current) {
            Mode.BOT_DUEL -> DuelRoute(
                vm = arena,
                onPlayAgain = { deck?.let(arena::startMatch) },
                modifier = Modifier.fillMaxSize().safeDrawingPadding(),
            )
            Mode.PVP_DUEL -> DuelRoute(vm = pvp, onPlayAgain = null, modifier = Modifier.fillMaxSize().safeDrawingPadding())
            Mode.MENU -> if (showAdmin && account.isAdmin) {
                AdminScr(admin, bal, cardList, trnrs, onBack = { showAdmin = false }, modifier = Modifier.safeDrawingPadding())
            } else Column(Modifier.fillMaxSize()) {
                TopBar(plrName, bal, onProf = { showAcct = true }, modifier = Modifier.statusBarsPadding())
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    when (tab) {
                        AppTab.BATTLE -> if (friendMode) {
                            PvpLobbyScr(
                                phase = pvpPhase,
                                deck = deck,
                                sticks = cardList.filterIsInstance<Stick>(),
                                onHost = { bet -> deck?.let { pvp.host(plrName, it, bet) } },
                                onJoin = { address, port, bet -> deck?.let { pvp.join(address, plrName, it, bet, port) } },
                                nearby = nearby,
                                onBrowse = { on -> if (on) pvp.startBrowsing() else pvp.stopBrowsing() },
                                onCancel = pvp::cancel,
                                onBack = { pvp.cancel(); friendMode = false },
                            )
                        } else {
                            LobbyScr(
                                deck = deck,
                                selTier = arenaState.botTier,
                                onSelectTier = arena::selectTier,
                                onBattle = { deck?.let(arena::startMatch) },
                                onEditDeck = { tab = AppTab.DECK },
                                onPlayFriend = { friendMode = true },
                            )
                        }
                        AppTab.DECK -> DeckBldrRoute(source = deckSrc)
                        AppTab.SHOP -> ShopRoute(shop, bal)
                    }
                }
                BottomTray(tab, onSelect = { tab = it })
            }
        }
    }
    if (showAcct) {
        AcctSheet(
            acctVm,
            onDismiss = { showAcct = false },
            onTut = { showAcct = false; onTut() },
            onOpenAdmin = { showAcct = false; showAdmin = true },
        )
    }
    bonus?.let { offer ->
        DlyBonusDlg(offer, onClaim = {
            bonus = null
            scope.launch { cont.coinsRepo.claimDlyLogin() }
        })
    }
}

/** What fills the screen: the menus, a bot duel or a PvP duel. */
private enum class Mode { MENU, BOT_DUEL, PVP_DUEL }

/** Top bar showing the player's name (tap for the account sheet) and coin balance. */
@Composable
private fun TopBar(name: String, bal: Long, onProf: () -> Unit, modifier: Modifier = Modifier) {
    val coins = remember(bal) { NumberFormat.getIntegerInstance().format(bal) }
    val profLbl = stringResource(R.string.top_prof, name)
    Row(
        modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier
                .weight(1f)
                .clickable(role = Role.Button, onClickLabel = profLbl, onClick = onProf)
                ,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Medallion(size = 44.dp, fill = GameTheme.colors.leaf) {
                OutlinedTxt(name.take(1).uppercase(), style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.width(10.dp))
            OutlinedTxt(name, style = MaterialTheme.typography.titleLarge, maxLines = 1)
        }
        CoinsPill(
            amount = coins,
            icon = GameIcos.Coin,
            contentDescription = stringResource(R.string.top_coins, coins),
            modifier = Modifier.semantics(mergeDescendants = true) {},
        )
    }
}

/** Bottom navigation tray. */
@Composable
private fun BottomTray(selected: AppTab, onSelect: (AppTab) -> Unit) {
    Box(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 18.dp)
                .woodBoard(crnr = 0.dp, lip = 0.dp)
                .navigationBarsPadding()
                .height(88.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            TrayItem(AppTab.DECK, GameIcos.Cards, R.string.nav_deck, selected, onSelect, Modifier.weight(1f))
            TrayItem(AppTab.BATTLE, GameIcos.Battle, R.string.nav_battle, selected, onSelect, Modifier.weight(1.3f), center = true)
            TrayItem(AppTab.SHOP, GameIcos.Shop, R.string.nav_shop, selected, onSelect, Modifier.weight(1f))
        }
    }
}

/** One tray button; the selected one lifts up and grows with a bouncy animation. */
@Composable
private fun TrayItem(
    tab: AppTab,
    icon: ImageVector,
    label: Int,
    selected: AppTab,
    onSelect: (AppTab) -> Unit,
    modifier: Modifier = Modifier,
    center: Boolean = false,
) {
    val isSel = tab == selected
    val lift by animateDpAsState(if (isSel) (-14).dp else 0.dp, GameMotion.bouncy(), label = "lift")
    val scale by animateFloatAsState(if (isSel) 1.12f else 1f, GameMotion.bouncy(), label = "scale")
    val c = GameTheme.colors
    Column(
        modifier
            .semantics { this.selected = isSel }
            .clickable(remember { MutableInteractionSource() }, indication = null, role = Role.Tab) { onSelect(tab) }
            
            .padding(bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        Box(
            Modifier.offset(y = lift).graphicsLayer { scaleX = scale; scaleY = scale },
            contentAlignment = Alignment.Center,
        ) {
            Medallion(
                size = if (center) 64.dp else 48.dp,
                fill = when {
                    isSel && center -> c.leaf
                    isSel -> c.amber
                    else -> c.woodDark
                },
                rim = if (isSel) c.gold else c.woodLight,
            ) {
                Icon(icon, contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(if (center) 36.dp else 28.dp))
            }
        }
        OutlinedTxt(
            text = stringResource(label),
            style = MaterialTheme.typography.labelLarge.copy(fontFamily = com.sushanth.onlysticks.ui.theme.DispFont),
            color = if (isSel) c.gold else c.onWood,
            outlW = 2.dp,
        )
    }
}

/** Android Studio preview. */
@Preview(showBackground = true)
@Composable
private fun OnlySticksAppPrvw() {
    OnlySticksTheme {
        OnlySticksApp(cont = AppCont(LocalContext.current))
    }
}

/** Android Studio preview (onboarding). */
@Preview(showBackground = true)
@Composable
private fun OnlySticksAppOnbrdPrvw() {
    OnlySticksTheme {
        GameBg {
            OnbrdScr(onSave = { com.sushanth.onlysticks.domain.player.DispNameRes.Valid(it) })
        }
    }
}

/** Android Studio preview (tutorial). */
@Preview(showBackground = true)
@Composable
private fun OnlySticksAppTutPrvw() {
    OnlySticksTheme {
        GameBg {
            TutScr(onDone = {})
        }
    }
}

/** Android Studio preview. */
@Preview(showBackground = true)
@Composable
private fun TopBarPrvw() {
    OnlySticksTheme {
        TopBar(name = "StickMaster", bal = 2500L, onProf = {})
    }
}

/** Android Studio preview. */
@Preview(showBackground = true)
@Composable
private fun BottomTrayPrvw() {
    OnlySticksTheme {
        BottomTray(selected = AppTab.BATTLE, onSelect = {})
    }
}



// The main duel screen: fighters, animated stage, timer, move dock and result overlay.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.arena

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.domain.combat.Combatant
import com.sushanth.onlysticks.domain.combat.PlrId
import com.sushanth.onlysticks.domain.combat.TurnTmr
import com.sushanth.onlysticks.domain.model.Move
import com.sushanth.onlysticks.domain.model.PwrUpTarget
import com.sushanth.onlysticks.ui.arena.stage.ArenaStage
import com.sushanth.onlysticks.ui.card.GameCard
import com.sushanth.onlysticks.ui.card.icon
import com.sushanth.onlysticks.ui.card.lblRes
import com.sushanth.onlysticks.ui.card.title
import com.sushanth.onlysticks.ui.common.asStr
import com.sushanth.onlysticks.ui.components.ChunkyBtn
import com.sushanth.onlysticks.ui.components.GameIcos
import com.sushanth.onlysticks.ui.components.GameMotion
import com.sushanth.onlysticks.ui.components.Medallion
import com.sushanth.onlysticks.ui.components.OutlinedTxt
import com.sushanth.onlysticks.ui.components.StatBar
import com.sushanth.onlysticks.ui.components.WoodPanel
import com.sushanth.onlysticks.ui.theme.DispFont
import com.sushanth.onlysticks.ui.theme.GameTheme
import com.sushanth.onlysticks.ui.timer.CntdwnTmrHud
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Connects a [DuelCtrl] (bot or PvP) to [DuelScr]. */
@Composable
fun DuelRoute(vm: DuelCtrl, onPlayAgain: (() -> Unit)?, modifier: Modifier = Modifier) {
    val st by vm.state.collectAsStateWithLifecycle()
    DuelScr(
        state = st,
        tmr = vm.tmr,
        onSelectMove = { vm.selectMove(it) },
        onSelectSwtch = { vm.selectSwtch(it) },
        onSelectPwrUp = { vm.selectPwrUp(it) },
        onSelectPwrUpTarget = { vm.selectPwrUpTarget(it) },
        onSbmt = { vm.sbmt() },
        onTmrTick = vm::onTmrTick,
        onPauseTmr = vm::pauseTmr,
        onResumeTmr = vm::resumeTmr,
        onPlayAgain = onPlayAgain,
        onLeave = vm::leaveMatch,
        modifier = modifier,
    )
}

/** Stateless duel screen. The timer pauses while the stage replays the last round and resumes afterwards. */
@Composable
fun DuelScr(
    state: ArenaUiState,
    tmr: StateFlow<TurnTmr>,
    onSelectMove: (Move) -> Unit,
    onSelectSwtch: (Int?) -> Unit,
    onSelectPwrUp: (Int?) -> Unit,
    onSelectPwrUpTarget: (PwrUpTarget) -> Unit,
    onSbmt: () -> Unit,
    onTmrTick: (Long) -> Unit,
    onPauseTmr: () -> Unit,
    onResumeTmr: () -> Unit,
    onPlayAgain: (() -> Unit)?,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val match = state.match ?: return
    val you = match.selSwtchSlot?.let { match.combat.p1.copy(activeSlot = it) } ?: match.combat.p1
    val foe = match.combat.p2
    var playing by remember { mutableStateOf(false) }
    var showLog by rememberSaveable { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    val botName = when (val o = state.opp) {
        is OppLbl.Bot -> stringResource(
            when (o.tier) {
                1 -> R.string.bot_name_1
                2 -> R.string.bot_name_2
                3 -> R.string.bot_name_3
                4 -> R.string.bot_name_4
                else -> R.string.bot_name_5
            }
        )
        is OppLbl.Plr -> o.name
    }
    val badge = (state.opp as? OppLbl.Bot)?.tier?.toString()

    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FgtrPlate(
                name = botName,
                badge = badge,
                c = foe,
                barCol = Color(0xFFE5533D),
                ready = match.oppReady && !match.isOver,
                mirrored = true,
            )
            Box(Modifier.weight(1f).fillMaxWidth()) {
                ArenaStage(
                    you = you,
                    opp = foe,
                    round = match.combat.round,
                    pb = state.lastRound,
                    onPbStart = { playing = true; onPauseTmr() },
                    onPbEnd = { playing = false; onResumeTmr() },
                    modifier = Modifier.fillMaxSize(),
                )
                if (!match.isOver) {
                    DuelTmr(
                        tmr = tmr,
                        onTick = onTmrTick,
                        onPause = onPauseTmr,
                        onResume = { if (!playing) onResumeTmr() },
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                    )
                }
                state.log.lastOrNull()?.let { last ->
                    LogTicker(last.asStr(), onClick = { showLog = true }, modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp))
                }
                androidx.compose.animation.AnimatedVisibility(
                    visible = state.notice != null,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp, start = 12.dp, end = 84.dp),
                ) {
                    state.notice?.let { NoticeChip(it.asStr()) }
                }
            }
            FgtrPlate(
                name = stringResource(R.string.arena_you),
                badge = null,
                c = you,
                barCol = Color(0xFF5CC93B),
                ready = false,
                mirrored = false,
            )
            if (!match.isOver) {
                MoveDock(
                    moves = DuelMoves.forCombatant(you, match.legalMoves),
                    selected = match.selMove,
                    onSelect = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSelectMove(it)
                    },
                )
                ExtrasRow(match, onSelectSwtch, onSelectPwrUp)
                val itemSlot = match.selPwrUpSlot
                val itemType = itemSlot?.let { match.combat.p1.pwrUpAt(it) }
                if (itemType != null) PwrUpPanel(itemType, match.selPwrUpTarget, onSelectPwrUpTarget)
                val btnTxt = if (match.waitForOpp) {
                    stringResource(R.string.arena_wait_for, botName)
                } else {
                    if (match.selPwrUpSlot != null) {
                        stringResource(R.string.arena_use_item, match.combat.p1.deck.getOrNull(match.selPwrUpSlot)?.title().orEmpty())
                    } else if (match.selMove == null) {
                        stringResource(R.string.arena_pick_move)
                    } else {
                        stringResource(R.string.arena_sbmt)
                    }
                }
                ChunkyBtn(
                    text = btnTxt,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSbmt()
                    },
                    enabled = match.canSbmt,
                    icon = GameIcos.Battle,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        AnimatedVisibility(
            visible = match.isOver && !playing,
            enter = fadeIn() + scaleIn(initialScale = 0.8f, animationSpec = GameMotion.bouncy()),
            exit = fadeOut(),
        ) {
            ResOverlay(
                won = match.winner == PlrId.P1,
                botName = botName,
                rounds = match.combat.round,
                onPlayAgain = onPlayAgain.takeIf { state.canRematch },
                onLeave = onLeave,
            )
        }
    }

    if (showLog) BattleLogSheet(state, onDismiss = { showLog = false })
}

/** Turn timer ring for the duel screen; wraps [CntdwnTmrHud] around the controller's timer flow. */
@Composable
private fun DuelTmr(
    tmr: StateFlow<TurnTmr>,
    onTick: (Long) -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cur by tmr.collectAsStateWithLifecycle()
    CntdwnTmrHud(
        tmr = cur,
        onTick = onTick,
        onPause = onPause,
        onResume = onResume,
        size = 68.dp,
        modifier = modifier,
    )
}

/** Android Studio preview. */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun DuelScrPrvw() {
    val stick1 = com.sushanth.onlysticks.domain.model.Stick(
        "s1", com.sushanth.onlysticks.domain.model.Rarity.COMMON, com.sushanth.onlysticks.domain.model.BaseStats(100, 50, 40)
    )
    val rareStick = com.sushanth.onlysticks.domain.model.Stick(
        "s2", com.sushanth.onlysticks.domain.model.Rarity.RARE, com.sushanth.onlysticks.domain.model.BaseStats(120, 60, 50)
    )
    val p1 = com.sushanth.onlysticks.domain.combat.Combatant.start(
        PlrId.P1, listOf(stick1)
    )
    val p2 = com.sushanth.onlysticks.domain.combat.Combatant.start(
        PlrId.P2, listOf(rareStick)
    )
    val combatState = com.sushanth.onlysticks.domain.combat.CombatState(round = 1, p1 = p1, p2 = p2)
    val matchUi = MatchUi(
        combat = combatState,
        legalMoves = setOf(Move.JAB, Move.LUNGE, Move.PARRY),
        selMove = Move.JAB,
    )
    com.sushanth.onlysticks.ui.theme.OnlySticksTheme {
        DuelScr(
            state = ArenaUiState(
                opp = OppLbl.Bot(1),
                match = matchUi,
            ),
            tmr = MutableStateFlow(TurnTmr()),
            onSelectMove = {},
            onSelectSwtch = {},
            onSelectPwrUp = {},
            onSelectPwrUpTarget = {},
            onSbmt = {},
            onTmrTick = {},
            onPauseTmr = {},
            onResumeTmr = {},
            onPlayAgain = {},
            onLeave = {},
        )
    }
}

/** HP/stamina plate for one fighter, with active stick info and a "ready" indicator for the opponent. */
@Composable
internal fun FgtrPlate(name: String, badge: String?, c: Combatant, barCol: Color, ready: Boolean, mirrored: Boolean) {
    val colors = GameTheme.colors
    val stick = c.activeStick
    WoodPanel(Modifier.fillMaxWidth(), crnr = 16.dp, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Medallion(size = 46.dp, fill = if (mirrored) colors.hpFoe else colors.leaf) {
                if (badge != null) OutlinedTxt(badge, style = MaterialTheme.typography.titleLarge)
                else Icon(GameIcos.Trnr, null, tint = Color.Unspecified, modifier = Modifier.size(26.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTxt(name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f), maxLines = 1, outlW = 2.dp)
                    if (ready) Icon(GameIcos.Star, null, tint = Color.Unspecified, modifier = Modifier.size(18.dp))
                    OutlinedTxt(
                        stringResource(R.string.arena_hp_val, c.hp, c.maxHp),
                        style = MaterialTheme.typography.titleSmall,
                        outlW = 2.dp,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(GameIcos.Heart, null, tint = Color.Unspecified, modifier = Modifier.size(16.dp))
                    StatBar(c.hp.toFloat() / c.maxHp, barCol, height = 16.dp, modifier = Modifier.weight(1f))
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(GameIcos.Bolt, null, tint = Color.Unspecified, modifier = Modifier.size(16.dp))
                    StatBar(c.stam / 100f, colors.stam, height = 10.dp, modifier = Modifier.weight(1f), ghost = colors.stam.copy(alpha = 0.4f))
                    Text(
                        stringResource(R.string.arena_stam_val, c.stam),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onWood,
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                GameCard(
                    card = stick?.stick,
                    width = 60.dp,
                    durability = stick?.let { it.durability.toFloat() / it.maxDurability },
                )
                Row {
                    if (stick?.burning == true) Icon(GameIcos.Flame, stringResource(R.string.arena_burn), tint = Color.Unspecified, modifier = Modifier.size(16.dp))
                    if (stick?.wet == true) Icon(GameIcos.Drop, stringResource(R.string.arena_wet), tint = Color.Unspecified, modifier = Modifier.size(16.dp))
                    if (stick == null) Text(stringResource(R.string.arena_unarmed), style = MaterialTheme.typography.labelSmall, color = colors.onWood)
                }
            }
        }
    }
}

/** Row of move cards. */
@Composable
internal fun MoveDock(moves: List<DuelMove>, selected: Move?, onSelect: (Move) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        moves.forEach { m ->
            MoveCard(m, selected = m.move == selected, onClick = { onSelect(m.move) }, modifier = Modifier.weight(1f))
        }
    }
}

/** Theme colour for each move. */
@Composable
internal fun Move.tint(): Color {
    val c = GameTheme.colors
    return when (this) {
        Move.JAB -> c.amber
        Move.LUNGE -> c.hpFoe
        Move.PARRY -> c.leaf
        Move.SLASH -> Color(0xFF8E6BD8)
        Move.REST -> c.stam
        Move.EXHAUSTION_STRIKE -> c.woodLight
    }
}

/** One tappable move card showing its icon, damage and stamina cost. Lifts when selected. */
@Composable
internal fun MoveCard(m: DuelMove, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = GameTheme.colors
    val lift by animateDpAsState(if (selected) (-10).dp else 0.dp, GameMotion.bouncy(), label = "lift")
    val scale by animateFloatAsState(if (selected) 1.06f else 1f, GameMotion.bouncy(), label = "scale")
    val shape = RoundedCornerShape(14.dp)
    val tint = m.move.tint()
    val face = if (m.legal) tint else Color(0xFF9C8B78)
    Column(
        modifier
            .offset(y = lift)
            .graphicsLayer { scaleX = scale; scaleY = scale; alpha = if (m.legal) 1f else 0.55f }
            .semantics { this.selected = selected }
            .clip(shape)
            .background(Brush.verticalGradient(listOf(lerp(face, Color.White, 0.3f), face)))
            .border(if (selected) 3.dp else 2.dp, if (selected) c.gold else c.outline, shape)
            .clickable(enabled = m.legal, role = Role.Button, onClick = onClick)
            
            .heightIn(min = 48.dp)
            .padding(vertical = 6.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(Modifier.size(34.dp).clip(CircleShape).background(c.parchment.copy(alpha = 0.9f)), contentAlignment = Alignment.Center) {
            Icon(m.move.icon(), null, tint = Color.Unspecified, modifier = Modifier.size(26.dp))
        }
        OutlinedTxt(
            stringResource(m.move.lblRes()),
            style = MaterialTheme.typography.labelLarge.copy(fontFamily = DispFont),
            outlW = 2.dp,
            maxLines = 1,
        )
        val numStyle = MaterialTheme.typography.labelMedium.copy(fontFamily = DispFont)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(GameIcos.Atk, null, tint = Color.Unspecified, modifier = Modifier.size(12.dp))
            OutlinedTxt(
                m.dmg?.let { stringResource(R.string.arena_move_dmg, it) } ?: "–",
                style = numStyle, outlW = 1.5.dp, maxLines = 1,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(GameIcos.Bolt, null, tint = Color.Unspecified, modifier = Modifier.size(12.dp))
            OutlinedTxt(
                (if (m.stamDelta > 0) "+" else "") + stringResource(R.string.arena_move_stam, m.stamDelta),
                style = numStyle, outlW = 1.5.dp, maxLines = 1,
            )
        }
    }
}

/** Android Studio preview. */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun FgtrPlatePrvw() {
    val stick = com.sushanth.onlysticks.domain.model.Stick(
        "s1", com.sushanth.onlysticks.domain.model.Rarity.COMMON, com.sushanth.onlysticks.domain.model.BaseStats(100, 50, 40)
    )
    val combatant = com.sushanth.onlysticks.domain.combat.Combatant.start(
        com.sushanth.onlysticks.domain.combat.PlrId.P1, listOf(stick)
    )
    com.sushanth.onlysticks.ui.theme.OnlySticksTheme {
        FgtrPlate(
            name = "StickMaster",
            badge = "T1",
            c = combatant,
            barCol = GameTheme.colors.hpYou,
            ready = true,
            mirrored = false,
        )
    }
}

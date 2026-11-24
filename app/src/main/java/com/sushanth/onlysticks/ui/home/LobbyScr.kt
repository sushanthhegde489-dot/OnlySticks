package com.sushanth.onlysticks.ui.home

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.domain.config.Cfg
import com.sushanth.onlysticks.domain.model.BaseStats
import com.sushanth.onlysticks.domain.model.Deck
import com.sushanth.onlysticks.domain.model.Rarity
import com.sushanth.onlysticks.domain.model.Stick
import com.sushanth.onlysticks.domain.model.Trnr
import com.sushanth.onlysticks.domain.model.TrnrBuff
import com.sushanth.onlysticks.ui.card.GameCard
import com.sushanth.onlysticks.ui.card.title
import com.sushanth.onlysticks.ui.components.BtnStyle
import com.sushanth.onlysticks.ui.components.ChunkyBtn
import com.sushanth.onlysticks.ui.components.GameBadge
import com.sushanth.onlysticks.ui.components.GameIcos
import com.sushanth.onlysticks.ui.components.GameMotion
import com.sushanth.onlysticks.ui.components.Medallion
import com.sushanth.onlysticks.ui.components.OutlinedTxt
import com.sushanth.onlysticks.ui.components.ParchmentPanel
import com.sushanth.onlysticks.ui.components.WoodPanel
import com.sushanth.onlysticks.ui.theme.DispFont
import com.sushanth.onlysticks.ui.theme.GameTheme
import com.sushanth.onlysticks.ui.theme.OnlySticksTheme

@StringRes
fun botNameRes(tier: Int): Int = when (tier) {
    1 -> R.string.bot_name_1
    2 -> R.string.bot_name_2
    3 -> R.string.bot_name_3
    4 -> R.string.bot_name_4
    else -> R.string.bot_name_5
}

@StringRes
private fun botBlurbRes(tier: Int): Int = when (tier) {
    1 -> R.string.bot_blurb_1
    2 -> R.string.bot_blurb_2
    3 -> R.string.bot_blurb_3
    4 -> R.string.bot_blurb_4
    else -> R.string.bot_blurb_5
}

@Composable
fun LobbyScr(
    deck: Deck?,
    selTier: Int,
    onSelectTier: (Int) -> Unit,
    onBattle: () -> Unit,
    onEditDeck: () -> Unit,
    onPlayFriend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = GameTheme.colors
    Column(modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            WoodPanel(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTxt(stringResource(R.string.lobby_your_deck), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(c.leaf)
                                .border(2.dp, c.outline, RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            OutlinedTxt(stringResource(R.string.lobby_cards_cnt, deck?.cardList?.size ?: 0, Cfg.DECK_SLOTS), style = MaterialTheme.typography.labelLarge.copy(fontFamily = DispFont), outlW = 2.dp)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Medallion(size = 60.dp, fill = if (deck?.trainer != null) c.leaf else c.woodDark) {
                                Icon(GameIcos.Trnr, null, tint = Color.Unspecified, modifier = Modifier.size(32.dp))
                            }
                            Text(
                                deck?.trainer?.title() ?: stringResource(R.string.lobby_no_trnr),
                                style = MaterialTheme.typography.labelMedium,
                                color = c.onWood,
                                maxLines = 2,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.width(72.dp),
                            )
                        }
                        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy((-14).dp)) {
                            val cardList = deck?.cardList.orEmpty().take(7)
                            (0 until 7).forEach { i ->
                                val tilt = (i - 3f) * 3f
                                GameCard(cardList.getOrNull(i), width = 46.dp, inspectable = false, modifier = Modifier.zIndex(-i.toFloat()).graphicsLayer { rotationZ = tilt })
                            }
                        }
                    }
                    ChunkyBtn(
                        text = stringResource(R.string.lobby_edit_deck),
                        onClick = onEditDeck,
                        style = BtnStyle.Wood,
                        icon = GameIcos.Cards,
                        textStyle = MaterialTheme.typography.titleMedium,
                        minHeight = 48.dp,
                        modifier = Modifier.align(Alignment.End),
                    )
                }
            }
            OppLadder(selTier, onSelectTier)
            WoodPanel(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Medallion(size = 52.dp, fill = Color(0xFFF29E38)) {
                        Icon(GameIcos.Trnr, null, tint = Color.Unspecified, modifier = Modifier.size(28.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        OutlinedTxt(stringResource(R.string.lobby_friend_title), style = MaterialTheme.typography.titleLarge)
                        Text(stringResource(R.string.lobby_friend_body), style = MaterialTheme.typography.bodyMedium, color = c.onWood)
                    }
                    ChunkyBtn(
                        text = stringResource(R.string.lobby_friend_go),
                        onClick = onPlayFriend,
                        enabled = deck != null,
                        style = BtnStyle.Secondary,
                        textStyle = MaterialTheme.typography.titleMedium,
                        minHeight = 48.dp,
                    )
                }
            }
        }
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (deck == null) {
                OutlinedTxt(stringResource(R.string.arena_need_deck), style = MaterialTheme.typography.titleSmall, outlW = 2.dp)
                Spacer(Modifier.height(6.dp))
            }
            ChunkyBtn(
                text = stringResource(R.string.lobby_battle),
                onClick = onBattle,
                enabled = deck != null,
                icon = GameIcos.Battle,
                textStyle = MaterialTheme.typography.headlineMedium,
                minHeight = 72.dp,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun OppLadder(selTier: Int, onSelectTier: (Int) -> Unit) {
    val c = GameTheme.colors
    WoodPanel(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTxt(stringResource(R.string.lobby_choose_opp), style = MaterialTheme.typography.titleLarge)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                (1..Cfg.Bots.TIERS).forEach { tier ->
                    val selected = tier == selTier
                    val scale by animateFloatAsState(if (selected) 1.15f else 1f, GameMotion.bouncy(), label = "tier")
                    val name = stringResource(botNameRes(tier))
                    val desc = stringResource(R.string.lobby_tier_desc, name, tier, Cfg.Bots.TIERS)
                    Column(
                        Modifier
                            .semantics { this.selected = selected; contentDescription = desc }
                            .clickable(remember { MutableInteractionSource() }, indication = null, role = Role.RadioButton) { onSelectTier(tier) }
                            
                            .graphicsLayer { scaleX = scale; scaleY = scale },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Medallion(size = 52.dp, fill = if (selected) c.leaf else c.woodDark, rim = if (selected) c.gold else c.woodLight) {
                            OutlinedTxt(tier.toString(), style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }
            }
            AnimatedContent(
                targetState = selTier,
                transitionSpec = { (fadeIn() + slideInVertically { it / 3 }) togetherWith fadeOut() },
                label = "bot",
            ) { tier ->
                ParchmentPanel(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(stringResource(botNameRes(tier)), style = MaterialTheme.typography.titleLarge, color = c.ink)
                            Text(stringResource(botBlurbRes(tier)), style = MaterialTheme.typography.bodyMedium, color = c.inkSoft)
                        }
                        Row {
                            repeat(Cfg.Bots.TIERS) { i ->
                                Icon(
                                    GameIcos.Star, null,
                                    tint = if (i < tier) Color.Unspecified else c.inkSoft.copy(alpha = 0.25f),
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LobbyScrPrvw() {
    val sampleStick1 = Stick("1", Rarity.COMMON, BaseStats(100, 50, 40))
    val rareStick = Stick("2", Rarity.RARE, BaseStats(150, 70, 60))
    val sampleTrnr = Trnr("t1", TrnrBuff.MAX_HP, 100)
    val sampleDeck = Deck("d1", "My Deck", listOf(sampleStick1, rareStick), sampleTrnr)
    OnlySticksTheme {
        LobbyScr(
            deck = sampleDeck,
            selTier = 2,
            onSelectTier = {},
            onBattle = {},
            onEditDeck = {},
            onPlayFriend = {},
        )
    }
}


package com.sushanth.onlysticks.ui.card

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.domain.appraisal.Appr
import com.sushanth.onlysticks.domain.model.Card
import com.sushanth.onlysticks.domain.model.PwrUp
import com.sushanth.onlysticks.domain.model.PwrUpType
import com.sushanth.onlysticks.domain.model.Stick
import com.sushanth.onlysticks.domain.model.Trnr
import com.sushanth.onlysticks.domain.stats.effectiveStats
import com.sushanth.onlysticks.ui.components.GameIcos
import com.sushanth.onlysticks.ui.components.GameMotion
import com.sushanth.onlysticks.ui.components.OutlinedTxt
import com.sushanth.onlysticks.ui.components.StatBar
import com.sushanth.onlysticks.ui.theme.DispFont
import com.sushanth.onlysticks.ui.theme.FrameCols
import com.sushanth.onlysticks.ui.theme.GameTheme
import com.sushanth.onlysticks.ui.theme.ItemFrameCols
import com.sushanth.onlysticks.ui.theme.rarFrameCols
import kotlin.math.roundToInt

fun Card.frameCol(): Color = frameCols().frame

fun Card.frameCols(): FrameCols = when (this) {
    is Stick -> rarFrameCols(rarity)
    else -> ItemFrameCols
}

fun Card.icon(): ImageVector = when (this) {
    is Stick -> GameIcos.Atk
    is PwrUp -> if (type == PwrUpType.LIGHTER) GameIcos.Flame else GameIcos.Drop
    is Trnr -> GameIcos.Trnr
}

@Composable
fun Card.title(): String = when (this) {
    is Stick -> displayName
    is PwrUp -> customName ?: stringResource(type.lblRes())
    is Trnr -> customName ?: stringResource(buff.lblRes())
}

@Composable
fun Card.code(): String = when (this) {
    is Stick -> rarity.code
    is PwrUp -> stringResource(R.string.card_code_powerup)
    is Trnr -> stringResource(R.string.card_code_trnr)
}

object GameCardDefaults {
    const val ASPECT = 0.72f
    val FullCardW = 260.dp
    val FullDetailMin = 200.dp
    val StatsMin = 76.dp
    val NameMin = 104.dp
    val InspectW = 300.dp
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameCard(
    card: Card?,
    modifier: Modifier = Modifier,
    width: Dp = 64.dp,
    selected: Boolean = false,
    dimmed: Boolean = false,
    durability: Float? = null,
    inspectable: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val c = GameTheme.colors
    val lift by animateDpAsState(if (selected) (-6).dp else 0.dp, GameMotion.bouncy(), label = "lift")
    var inspecting by remember { mutableStateOf(false) }
    val desc = card?.let { "${it.title()}, ${stringResource(R.string.card_score_lbl, Appr.value(it))}" }
        ?: stringResource(R.string.deck_slot_empty)
    val canInspect = inspectable && card != null
    val inspectLbl = stringResource(R.string.card_inspect)
    val interaction = when {
        canInspect -> Modifier.combinedClickable(
            role = Role.Button,
            onLongClickLabel = inspectLbl,
            onLongClick = { inspecting = true },
            onClick = onClick ?: { inspecting = true },
        )
        onClick != null -> Modifier.clickable(role = Role.Button, onClick = onClick)
        else -> Modifier
    }
    Column(
        modifier
            .width(width)
            .offset(y = lift)
            .semantics(mergeDescendants = true) { contentDescription = desc }
            .then(interaction),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val w = maxWidth
            val shape = RoundedCornerShape(w * 0.08f)
            when {
                card == null -> EmptySlot(shape)
                w >= GameCardDefaults.FullDetailMin -> ScaledFullCard(card)
                else -> CompactCard(card, w, shape)
            }
            if (card != null && dimmed) {
                Box(Modifier.matchParentSize().clip(shape).background(Color.Black.copy(alpha = 0.45f)))
                Icon(GameIcos.Brkn, null, tint = Color.Unspecified, modifier = Modifier.size(w * 0.4f).align(Alignment.Center))
            }
            if (selected) Box(Modifier.matchParentSize().border(3.dp, c.gold, shape))
        }
        if (durability != null) StatBar(durability, c.leafLight, height = 7.dp)
    }
    if (inspecting && card != null) {
        Dialog(onDismissRequest = { inspecting = false }) {
            Box(
                Modifier.verticalScroll(rememberScrollState()).clickable { inspecting = false },
                contentAlignment = Alignment.Center,
            ) {
                PokemonCardV(
                    card = card,
                    apprVal = Appr.value(card),
                    trnrSynergy = false,
                    onNickChange = {},
                    editable = false,
                    modifier = Modifier.width(GameCardDefaults.InspectW),
                )
            }
        }
    }
}

@Composable
private fun EmptySlot(shape: RoundedCornerShape) {
    val c = GameTheme.colors
    Box(
        Modifier.fillMaxWidth().aspectRatio(GameCardDefaults.ASPECT).clip(shape)
            .background(c.woodDark.copy(alpha = 0.35f)).border(2.dp, c.parchment.copy(alpha = 0.5f), shape),
        contentAlignment = Alignment.Center,
    ) {
        OutlinedTxt("+", style = MaterialTheme.typography.headlineSmall, color = c.parchment.copy(alpha = 0.8f), outlW = 2.dp)
    }
}

@Composable
private fun CompactCard(card: Card, w: Dp, shape: RoundedCornerShape) {
    val c = GameTheme.colors
    val colors = card.frameCols()
    val density = LocalDensity.current
    fun size(frac: Float): TextUnit = with(density) { (w * frac).toSp() }
    val pad = w * 0.06f
    val score = Appr.value(card)
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(GameCardDefaults.ASPECT)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(lerp(colors.frame, Color.White, 0.28f), colors.frame)))
            .border(if (w < 60.dp) 1.5.dp else 2.dp, c.outline, shape)
            .padding(pad),
    ) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(pad * 0.7f)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    card.code(),
                    style = TextStyle(fontFamily = DispFont, fontSize = size(0.17f), lineHeight = size(0.19f)),
                    color = colors.onFrame,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    score.toString(),
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = size(0.14f), lineHeight = size(0.16f)),
                    color = c.gold,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(c.woodDark.copy(alpha = 0.9f))
                        .padding(horizontal = w * 0.04f, vertical = w * 0.01f)
                        ,
                )
            }
            if (w >= GameCardDefaults.NameMin) {
                Text(
                    card.title(),
                    style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = size(0.1f), lineHeight = size(0.12f)),
                    color = colors.onFrame,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(
                Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(w * 0.06f))
                    .background(Brush.radialGradient(listOf(c.leafLight.copy(alpha = 0.45f), c.parchment, c.parchmentDeep))),
                contentAlignment = Alignment.Center,
            ) {
                if (card is Stick) StickSilhouette(gripCol = colors.frame, inset = w * 0.08f)
                else Icon(card.icon(), null, tint = Color.Unspecified, modifier = Modifier.size(w * 0.45f))
            }
            if (card is Stick && w >= GameCardDefaults.StatsMin) {
                val st = card.effectiveStats
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(w * 0.05f)).background(c.woodDark.copy(alpha = 0.85f))
                        .padding(vertical = w * 0.015f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    listOf(
                        stringResource(R.string.stat_lbl_dur) to st.durability,
                        stringResource(R.string.stat_lbl_spd) to st.speed,
                        stringResource(R.string.stat_lbl_atk) to st.attack,
                    ).forEach { (label, value) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(label, style = TextStyle(fontSize = size(0.075f), lineHeight = size(0.085f)), color = c.onWood.copy(alpha = 0.8f))
                            Text(
                                value.toString(),
                                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = size(0.11f), lineHeight = size(0.125f)),
                                color = c.onWood,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScaledFullCard(card: Card) {
    Layout(
        content = {
            PokemonCardV(
                card = card,
                apprVal = Appr.value(card),
                trnrSynergy = false,
                onNickChange = {},
                editable = false,
            )
        },
        modifier = Modifier.fillMaxWidth().clipToBounds().clearAndSetSemantics {},
    ) { measurables, constraints ->
        val natural = GameCardDefaults.FullCardW.roundToPx()
        val target = if (constraints.hasBoundedWidth) constraints.maxWidth else natural
        val scale = target.toFloat() / natural
        val placeable = measurables.single().measure(Constraints.fixedWidth(natural))
        layout(target, (placeable.height * scale).roundToInt()) {
            placeable.placeWithLayer(0, 0) {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0f, 0f)
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun GameCardPrvw() {
    val stick = Stick("preview", com.sushanth.onlysticks.domain.model.Rarity.RARE, com.sushanth.onlysticks.domain.model.BaseStats(100, 50, 40))
    com.sushanth.onlysticks.ui.theme.OnlySticksTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GameCard(stick, width = 56.dp)
            GameCard(stick, width = 84.dp)
            GameCard(stick, width = 120.dp)
        }
    }
}

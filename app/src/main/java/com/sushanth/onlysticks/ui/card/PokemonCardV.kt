// Full-size card layout: header with name and value, artwork, stats/moves and footer.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.domain.model.Card
import com.sushanth.onlysticks.domain.model.PwrUp
import com.sushanth.onlysticks.domain.model.Stick
import com.sushanth.onlysticks.domain.model.Trnr
import com.sushanth.onlysticks.domain.trainer.TrainerProgression
import com.sushanth.onlysticks.ui.components.GameIcos
import com.sushanth.onlysticks.ui.components.ParchmentPanel
import com.sushanth.onlysticks.ui.theme.FrameCols
import com.sushanth.onlysticks.ui.theme.GameTheme
import com.sushanth.onlysticks.ui.theme.ItemFrameCols
import com.sushanth.onlysticks.ui.theme.RarCodeStyle
import com.sushanth.onlysticks.ui.theme.rarFrameCols

/**
 * Full card view. [apprVal] is the coin value shown in the header; [trnrSynergy] highlights the footer.
 * When [editable], tapping the name lets the player set a nickname.
 */
@Composable
fun PokemonCardV(
    card: Card,
    apprVal: Int,
    trnrSynergy: Boolean,
    onNickChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    art: (@Composable () -> Unit)? = null,
    editable: Boolean = true,
) {
    val c = GameTheme.colors
    val colors = when (card) {
        is Stick -> rarFrameCols(card.rarity)
        else -> ItemFrameCols
    }
    val code = when (card) {
        is Stick -> card.rarity.code
        is PwrUp -> stringResource(R.string.card_code_powerup)
        is Trnr -> stringResource(R.string.card_code_trnr)
    }
    val defName = when (card) {
        is Stick -> card.rarity.code
        is PwrUp -> stringResource(card.type.lblRes())
        is Trnr -> stringResource(card.buff.lblRes())
    }
    val cardShape = RoundedCornerShape(18.dp)
    val frameLight = lerp(colors.frame, Color.White, 0.28f)
    Box(
        modifier = modifier
            
            .clip(cardShape)
            .background(Brush.verticalGradient(listOf(frameLight, colors.frame)))
            .border(3.dp, c.outline, cardShape)
            .drawBehind {
                val inset = 4.dp.toPx()
                drawRoundRect(
                    color = c.gold,
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - inset * 2, size.height - inset * 2),
                    cornerRadius = CornerRadius((18.dp.toPx() - inset).coerceAtLeast(2f)),
                    style = Stroke(width = 2.dp.toPx()),
                )
            }
            .padding(8.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            CardHdr(card, code, defName, colors, apprVal, onNickChange, editable)
            ParchmentPanel(
                crnr = 12.dp,
                contentPadding = PaddingValues(8.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ArtWin(card, colors, art)
                    when (card) {
                        is Stick -> {
                            StatChips(card)
                            MovesBox(card)
                        }
                        is PwrUp -> ItemDesc(
                            title = stringResource(card.type.lblRes()),
                            body = pwrUpDesc(card.type),
                        )
                        is Trnr -> ItemDesc(
                            title = stringResource(card.buff.lblRes()),
                            body = stringResource(card.buff.descRes()) + "\n" + stringResource(
                                R.string.trnr_rank, TrainerProgression.rankFor(card.affinity),
                            ),
                        )
                    }
                }
            }
            CardFtr(trnrSynergy, colors)
        }
    }
}

/** Card header: rarity code, (editable) name and appraisal value. */
@Composable
internal fun CardHdr(
    card: Card,
    code: String,
    defName: String,
    colors: FrameCols,
    apprVal: Int,
    onNickChange: (String) -> Unit,
    editable: Boolean = true,
) {
    val c = GameTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .defaultMinSize(minWidth = 32.dp, minHeight = 32.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(lerp(colors.frame, Color.White, 0.28f), colors.frame)))
                .border(2.dp, c.gold, CircleShape)
                .padding(horizontal = 5.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = code,
                style = RarCodeStyle.copy(
                    fontSize = RarCodeStyle.fontSize * 0.72f,
                    lineHeight = RarCodeStyle.lineHeight * 0.72f,
                ),
                color = colors.onFrame,
                maxLines = 1,
                softWrap = false,
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.verticalGradient(listOf(c.wood, c.woodDark)))
                .border(1.5.dp, c.goldDark, RoundedCornerShape(8.dp))
                .padding(horizontal = 5.dp, vertical = 4.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            NickField(
                initial = card.customName ?: defName,
                cardKey = card.id,
                defName = defName,
                onNickChange = onNickChange,
                editable = editable,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        val scoreLbl = stringResource(R.string.card_score_lbl, apprVal)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(c.woodDark.copy(alpha = 0.9f))
                .border(1.5.dp, c.gold, RoundedCornerShape(50))
                .padding(horizontal = 5.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Icon(
                imageVector = GameIcos.Coin,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(13.dp),
            )
            Text(
                text = apprVal.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = c.gold,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.End,
                modifier = Modifier.semantics { contentDescription = scoreLbl },
            )
        }
    }
}

/** Inline nickname editor. Empty input resets to [defName]. */
@Composable
internal fun NickField(
    initial: String,
    cardKey: String,
    defName: String,
    onNickChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    editable: Boolean = true,
) {
    val c = GameTheme.colors
    var shown by rememberSaveable(cardKey, initial) { mutableStateOf(initial) }
    var editing by rememberSaveable(cardKey) { mutableStateOf(false) }
    val textStyle = MaterialTheme.typography.titleSmall.copy(color = c.onWood)
    if (editing) {
        var draft by rememberSaveable(cardKey) { mutableStateOf(shown) }
        val focus = remember { FocusRequester() }
        val editLbl = stringResource(R.string.card_nick_edit_lbl)
        fun commit() {
            val value = draft.trim().ifEmpty { defName }
            shown = value
            editing = false
            onNickChange(value)
        }
        BasicTextField(
            value = draft,
            onValueChange = { draft = it },
            singleLine = true,
            textStyle = textStyle,
            cursorBrush = SolidColor(c.onWood),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { commit() }),
            modifier = modifier
                .focusRequester(focus)
                .semantics { contentDescription = editLbl }
                ,
        )
        LaunchedEffect(Unit) { focus.requestFocus() }
    } else {
        Text(
            text = shown,
            style = textStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = modifier
                .let { if (editable) it.clickable(onClickLabel = stringResource(R.string.card_nick_edit_lbl)) { editing = true } else it }
                ,
        )
    }
}

/** Android Studio preview. */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun CardHdrPrvw() {
    val stick = com.sushanth.onlysticks.domain.model.Stick(
        "preview", com.sushanth.onlysticks.domain.model.Rarity.RARE, com.sushanth.onlysticks.domain.model.BaseStats(100, 50, 40)
    )
    com.sushanth.onlysticks.ui.theme.OnlySticksTheme {
        CardHdr(
            card = stick,
            code = "RARE",
            defName = "Oak Stick",
            colors = com.sushanth.onlysticks.ui.theme.rarFrameCols(com.sushanth.onlysticks.domain.model.Rarity.RARE),
            apprVal = 450,
            onNickChange = {},
        )
    }
}

/** Card footer, highlighted when a trainer boosts this card. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CardFtr(trnrSynergy: Boolean, colors: FrameCols) {
    val c = GameTheme.colors
    val pillShape = RoundedCornerShape(50)
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier
                .clip(pillShape)
                .background(if (trnrSynergy) c.woodDark.copy(alpha = 0.92f) else c.woodDark.copy(alpha = 0.65f))
                .border(1.dp, if (trnrSynergy) c.gold else colors.onFrame.copy(alpha = 0.4f), pillShape)
                .padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = GameIcos.Star,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(15.dp).alpha(if (trnrSynergy) 1f else 0.45f),
            )
            Text(
                text = stringResource(if (trnrSynergy) R.string.card_synergy_on else R.string.card_synergy_off),
                style = MaterialTheme.typography.labelLarge,
                color = if (trnrSynergy) c.gold else c.onWood.copy(alpha = 0.8f),
            )
        }
    }
}

/** Android Studio preview. */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun CardFtrPrvw() {
    com.sushanth.onlysticks.ui.theme.OnlySticksTheme {
        CardFtr(
            trnrSynergy = true,
            colors = com.sushanth.onlysticks.ui.theme.rarFrameCols(com.sushanth.onlysticks.domain.model.Rarity.RARE),
        )
    }
}

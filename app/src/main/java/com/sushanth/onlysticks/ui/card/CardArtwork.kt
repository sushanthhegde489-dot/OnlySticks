package com.sushanth.onlysticks.ui.card

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.domain.model.Card
import com.sushanth.onlysticks.domain.model.Rarity
import com.sushanth.onlysticks.domain.model.Stick
import com.sushanth.onlysticks.ui.components.readAnimrScl
import com.sushanth.onlysticks.ui.theme.FrameCols
import com.sushanth.onlysticks.ui.theme.GameTheme

@Composable
internal fun ArtWin(
    card: Card,
    colors: FrameCols,
    art: (@Composable () -> Unit)?,
) {
    val c = GameTheme.colors
    val context = LocalContext.current
    val desc = stringResource(R.string.card_art_desc)
    val artShape = RoundedCornerShape(10.dp)
    val rarity = (card as? Stick)?.rarity
    val isRarePlus = rarity == Rarity.RARE || rarity == Rarity.SUPER_RARE || rarity == Rarity.ULTRA_RARE
    val isUltraRare = rarity == Rarity.ULTRA_RARE
    val animOn = remember(context) { readAnimrScl(context) > 0f }
    val shine: State<Float>? = if (isRarePlus && animOn) {
        val transition = rememberInfiniteTransition(label = "card_shine")
        transition.animateFloat(
            initialValue = -0.35f,
            targetValue = 1.35f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "shine_sweep",
        )
    } else {
        null
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .clip(artShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        c.leafLight.copy(alpha = 0.45f),
                        c.parchment,
                        c.parchmentDeep,
                    ),
                ),
            )
            .border(2.dp, c.wood.copy(alpha = 0.75f), artShape)
            .drawWithContent {
                drawContent()
                if (isUltraRare) {
                    drawUltraRareSparkles(c.gold, c.outline)
                }
                val shineProg = shine?.value ?: -1f
                if (shineProg in -0.3f..1.3f) {
                    val bandCenter = (size.width + size.height) * shineProg
                    val bandHalf = size.width * 0.22f
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.32f),
                                Color.Transparent,
                            ),
                            start = Offset(bandCenter - bandHalf, 0f),
                            end = Offset(bandCenter + bandHalf, size.height),
                        ),
                    )
                }
            }
            .semantics { contentDescription = desc }
            ,
        contentAlignment = Alignment.Center,
    ) {
        when {
            art != null -> art()
            card is Stick -> StickSilhouette(gripCol = colors.frame)
            else -> Icon(
                imageVector = card.icon(),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(68.dp),
            )
        }
    }
}

internal fun androidx.compose.ui.graphics.drawscope.DrawScope.drawUltraRareSparkles(
    gold: Color,
    outline: Color,
) {
    val points = listOf(
        Offset(0.16f, 0.22f),
        Offset(0.82f, 0.20f),
        Offset(0.24f, 0.78f),
        Offset(0.84f, 0.74f),
        Offset(0.50f, 0.16f),
    )
    val baseRad = 3.5.dp.toPx()
    points.forEachIndexed { idx, frac ->
        val center = Offset(size.width * frac.x, size.height * frac.y)
        val r = if (idx % 2 == 0) baseRad else baseRad * 0.75f
        drawCircle(outline.copy(alpha = 0.7f), radius = r + 1.dp.toPx(), center = center)
        drawCircle(gold, radius = r, center = center)
        drawCircle(Color.White.copy(alpha = 0.85f), radius = r * 0.4f, center = center)
    }
}

@Composable
internal fun StickSilhouette(gripCol: Color, inset: androidx.compose.ui.unit.Dp = 14.dp) {
    val c = GameTheme.colors
    Canvas(Modifier.fillMaxSize().padding(inset)) {
        val w = size.width
        val h = size.height
        val stroke = minOf(w, h) * 0.12f
        val outlStrk = stroke + 4.dp.toPx()
        val start = Offset(w * 0.16f, h * 0.86f)
        val end = Offset(w * 0.84f, h * 0.14f)
        val fork = Offset(w * 0.56f, h * 0.44f)
        val twigEnd = Offset(w * 0.74f, h * 0.57f)
        drawLine(c.outline, start, end, strokeWidth = outlStrk, cap = StrokeCap.Round)
        drawLine(c.outline, fork, twigEnd, strokeWidth = stroke * 0.58f + 4.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(c.outline, radius = stroke * 0.72f + 2.dp.toPx(), center = Offset(w * 0.42f, h * 0.58f))
        drawLine(c.woodLight, start, end, strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(c.woodLight, fork, twigEnd, strokeWidth = stroke * 0.58f, cap = StrokeCap.Round)
        drawCircle(c.wood, radius = stroke * 0.72f, center = Offset(w * 0.42f, h * 0.58f))
        drawCircle(c.woodGrain, radius = stroke * 0.35f, center = Offset(w * 0.42f, h * 0.58f))
        val tickFractions = listOf(0.48f, 0.64f, 0.76f)
        tickFractions.forEach { t ->
            val px = start.x + (end.x - start.x) * t
            val py = start.y + (end.y - start.y) * t
            drawLine(
                color = c.woodGrain,
                start = Offset(px - stroke * 0.22f, py - stroke * 0.12f),
                end = Offset(px + stroke * 0.18f, py + stroke * 0.20f),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
        val gripStart = Offset(
            start.x + (end.x - start.x) * 0.10f,
            start.y + (end.y - start.y) * 0.10f,
        )
        val gripEnd = Offset(
            start.x + (end.x - start.x) * 0.28f,
            start.y + (end.y - start.y) * 0.28f,
        )
        drawLine(c.outline, gripStart, gripEnd, strokeWidth = stroke * 1.08f + 3.dp.toPx(), cap = StrokeCap.Butt)
        drawLine(gripCol, gripStart, gripEnd, strokeWidth = stroke * 1.05f, cap = StrokeCap.Butt)
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun ArtWinPrvw() {
    val card = com.sushanth.onlysticks.domain.model.Stick(
        "preview", com.sushanth.onlysticks.domain.model.Rarity.RARE, com.sushanth.onlysticks.domain.model.BaseStats(100, 50, 40)
    )
    com.sushanth.onlysticks.ui.theme.OnlySticksTheme {
        Box(modifier = Modifier.size(200.dp, 160.dp)) {
            ArtWin(
                card = card,
                colors = com.sushanth.onlysticks.ui.theme.rarFrameCols(com.sushanth.onlysticks.domain.model.Rarity.RARE),
                art = null,
            )
        }
    }
}





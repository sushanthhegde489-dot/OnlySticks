package com.sushanth.onlysticks.ui.arena.stage

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.domain.combat.Combatant
import com.sushanth.onlysticks.ui.arena.RoundPb
import com.sushanth.onlysticks.ui.components.OutlinedTxt
import com.sushanth.onlysticks.ui.components.readAnimrScl
import com.sushanth.onlysticks.ui.theme.DispFont
import com.sushanth.onlysticks.ui.theme.GameTheme
import com.sushanth.onlysticks.ui.theme.OnlySticksTheme
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun ArenaStage(
    you: Combatant?,
    opp: Combatant?,
    round: Int,
    pb: RoundPb?,
    onPbStart: () -> Unit,
    onPbEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = GameTheme.colors
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val curOnStart by rememberUpdatedState(onPbStart)
    val curOnEnd by rememberUpdatedState(onPbEnd)

    val fx = remember { StageFx() }

    LaunchedEffect(round, you?.alive, opp?.alive) {
        if (you?.alive != false && fx.p1Anim.fallDeg.value > 0f && pb == null) {
            fx.p1Anim.fallDeg.snapTo(0f)
        }
        if (opp?.alive != false && fx.p2Anim.fallDeg.value > 0f && pb == null) {
            fx.p2Anim.fallDeg.snapTo(0f)
        }
    }

    val seq = pb?.seq
    LaunchedEffect(seq) {
        val curPb = pb ?: return@LaunchedEffect
        curOnStart()
        try {
            fx.play(curPb, context, colors, haptic)
        } finally {
            fx.flts.clear()
            curOnEnd()
        }
    }

    val animrScl = remember(context, seq) { readAnimrScl(context) }
    val idlePhase: State<Float> = if (animrScl > 0f) {
        val infinite = rememberInfiniteTransition(label = "arena_idle")
        infinite.animateFloat(
            initialValue = 0f,
            targetValue = (2f * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1600, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "idle_phase",
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    val plankPath = remember { Path() }
    val bandanaPath = remember { Path() }
    val flamePath = remember { Path() }

    val roundDesc = stringResource(R.string.arena_round, round)
    val density = LocalDensity.current
    var stageSize by remember { mutableStateOf(IntSize.Zero) }
    val wPx = if (stageSize.width > 0) stageSize.width.toFloat() else with(density) { 360.dp.toPx() }
    val hPx = if (stageSize.height > 0) stageSize.height.toFloat() else with(density) { 260.dp.toPx() }


    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 280.dp, minHeight = 200.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(2.dp, colors.outline, RoundedCornerShape(20.dp))
            .onSizeChanged { stageSize = it }
            .semantics { contentDescription = roundDesc }
            ,
    ) {
        val shake = Modifier
            .fillMaxSize()
            .offset {
                val p = fx.stageShake.value
                IntOffset(if (p > 0f) (sin(p * 28f) * 8.dp.toPx() * p).roundToInt() else 0, 0)
            }
        Canvas(
            modifier = shake,
        ) {
            drawBackdrop(colors, plankPath)
            drawGround(colors)
            drawRoundSignBoard(colors)
        }
        Canvas(modifier = shake) {
            drawFgtr(
                combatant = you,
                baseXFrac = 0.28f,
                facing = 1f,
                bandanaCol = colors.leaf,
                anim = fx.p1Anim,
                idlePhase = idlePhase.value,
                colors = colors,
                bandanaPath = bandanaPath,
                flamePath = flamePath,
            )
            drawFgtr(
                combatant = opp,
                baseXFrac = 0.72f,
                facing = -1f,
                bandanaCol = colors.hpFoe,
                anim = fx.p2Anim,
                idlePhase = idlePhase.value + 1.2f,
                colors = colors,
                bandanaPath = bandanaPath,
                flamePath = flamePath,
            )

            if (fx.sparkProg.value > 0f) {
                drawSparkBurst(
                    center = Offset(size.width * fx.sparkXFrac, size.height * 0.63f),
                    progress = fx.sparkProg.value,
                    goldOnly = fx.sparkGoldOnly,
                    colors = colors,
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
                .clearAndSetSemantics {},
        ) {
            OutlinedTxt(
                text = round.toString(),
                style = TextStyle(
                    fontFamily = DispFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                ),
                color = colors.onWood,
                outline = colors.outline,
                outlW = 2.5.dp,
            )
        }

        fx.flts.forEach { flt ->
            FltOverlay(
                flt = flt,
                stageWPx = wPx,
                stageHPx = hPx,
                colors = colors,
            )
        }
    }
}

@Preview(name = "ArenaStageIdlePreview", widthDp = 360, heightDp = 260)
@Composable
internal fun ArenaStageIdlePrvw() {
    OnlySticksTheme {
        ArenaStage(
            you = null,
            opp = null,
            round = 3,
            pb = null,
            onPbStart = {},
            onPbEnd = {},
            modifier = Modifier.size(360.dp, 260.dp),
        )
    }
}

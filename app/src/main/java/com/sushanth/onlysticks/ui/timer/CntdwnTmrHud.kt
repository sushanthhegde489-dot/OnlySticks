// Circular countdown display for the turn timer.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.timer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.sp
import com.sushanth.onlysticks.ui.components.OutlinedTxt
import com.sushanth.onlysticks.ui.theme.GameTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.domain.combat.TurnTmr
import com.sushanth.onlysticks.domain.config.Cfg
import com.sushanth.onlysticks.ui.theme.OnlySticksTheme

/** Whole seconds left, rounded up (so 0.1s shows as 1). */
fun TurnTmr.secsLeft(): Int = ((leftMs + 999) / 1000).toInt()

/**
 * Timer HUD driven by an external [TurnTmr]. Calls [onTick] every frame while running, and pauses/resumes
 * the timer when the app goes to the background and comes back.
 */
@Composable
fun CntdwnTmrHud(
    tmr: TurnTmr,
    onTick: (elapsedMs: Long) -> Unit,
    modifier: Modifier = Modifier,
    onPause: () -> Unit = {},
    onResume: () -> Unit = {},
    size: Dp = 88.dp,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var started by remember(lifecycleOwner) {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
    }
    val curOnTick by rememberUpdatedState(onTick)
    val curOnPause by rememberUpdatedState(onPause)
    val curOnResume by rememberUpdatedState(onResume)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, evt ->
            when (evt) {
                Lifecycle.Event.ON_STOP -> {
                    started = false
                    curOnPause()
                }
                Lifecycle.Event.ON_START -> {
                    started = true
                    curOnResume()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val ticking = started && tmr.isActive
    LaunchedEffect(ticking) {
        if (!ticking) return@LaunchedEffect
        var last = withFrameMillis { it }
        while (true) {
            val now = withFrameMillis { it }
            curOnTick(now - last)
            last = now
        }
    }
    TmrDial(tmr, modifier, size)
}
/** Self-contained version that owns its own timer and calls [onAutoSkip] when it expires. */
@Composable
fun CntdwnTmrHud(
    onAutoSkip: () -> Unit,
    modifier: Modifier = Modifier,
    durMs: Long = Cfg.TURN_TIMER_MS,
    resetKey: Any? = null,
    size: Dp = 88.dp,
) {
    var tmr by remember(durMs, resetKey) { mutableStateOf(TurnTmr(durMs)) }
    val curOnAutoSkip by rememberUpdatedState(onAutoSkip)
    CntdwnTmrHud(
        tmr = tmr,
        onTick = { elapsed ->
            val (next, evt) = tmr.tick(elapsed)
            tmr = next
            if (evt == TurnTmr.Event.AutoSkip) curOnAutoSkip()
        },
        onPause = { tmr = tmr.pause() },
        onResume = { tmr = tmr.resume() },
        modifier = modifier,
        size = size,
    )
}
/** The dial itself: a shrinking ring and the seconds left. Pulses red in the last 3 seconds. */
@Composable
private fun TmrDial(tmr: TurnTmr, modifier: Modifier, size: Dp) {
    val seconds = tmr.secsLeft()
    val desc = pluralStringResource(R.plurals.tmr_secs_left, seconds, seconds)
    val c = GameTheme.colors
    val urgent = tmr.leftMs <= 3_000 && tmr.leftMs > 0
    val ring = if (urgent) c.hpFoe else c.leafLight
    val pulse: State<Float>? = if (urgent) {
        val t = rememberInfiniteTransition(label = "pulse")
        t.animateFloat(1f, 1.12f, infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "pulse")
    } else null
    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                val p = pulse?.value ?: 1f
                scaleX = p
                scaleY = p
            }
            .semantics { contentDescription = desc }
            ,
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(size)) {
            val r = this.size.minDimension / 2
            drawCircle(c.outline, r)
            drawCircle(Brush.radialGradient(listOf(c.woodLight, c.woodDark), radius = r), r - 3.dp.toPx())
            val stroke = r * 0.2f
            val inset = 3.dp.toPx() + stroke / 2 + 2.dp.toPx()
            val arcSize = Size(this.size.width - inset * 2, this.size.height - inset * 2)
            drawArc(Color(0xFF2B1A0D), 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
            drawArc(ring, -90f, 360f * tmr.fracLeft, false, Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            drawCircle(c.gold, r - 1.5.dp.toPx(), style = Stroke(2.dp.toPx()))
        }
        Box(Modifier.semantics(mergeDescendants = true) {}) {
            OutlinedTxt(
                text = stringResource(R.string.tmr_secs, seconds),
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = (size.value * 0.36f).sp),
                color = if (urgent) c.hpGhost else Color.White,
            )
        }
    }
}
/** Android Studio preview. */
@Preview(name = "Timer")
@Composable
private fun CntdwnTmrHudPrvw() {
    OnlySticksTheme { CntdwnTmrHud(tmr = TurnTmr(leftMs = 4_200), onTick = {}) }
}
/** Android Studio preview (last seconds). */
@Preview(name = "Timer urgent", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CntdwnTmrHudUrgentPrvw() {
    OnlySticksTheme { CntdwnTmrHud(tmr = TurnTmr(leftMs = 1_500), onTick = {}) }
}

// Floating damage numbers and speech bubbles that rise over the stage.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.arena.stage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sushanth.onlysticks.ui.theme.DispFont
import com.sushanth.onlysticks.ui.theme.GameCols
import kotlin.math.roundToInt

/** Draws one floating text that rises and fades according to its own progress animation. */
@Composable
internal fun FltOverlay(
    flt: StageFlt,
    stageWPx: Float,
    stageHPx: Float,
    colors: GameCols,
) {
    val density = LocalDensity.current
    val targetX = (stageWPx * flt.xFrac).roundToInt()
    val baseY = stageHPx * flt.yFrac
    val offsetNow: Density.() -> IntOffset = {
        val risePx = (flt.riseDp * flt.progress.value).dp.toPx()
        IntOffset(targetX, (baseY - risePx).roundToInt())
    }
    val fade: () -> Float = {
        val progress = flt.progress.value
        if (progress < 0.6f) 1f else ((1f - progress) / 0.4f).coerceIn(0f, 1f)
    }

    val textStyle = TextStyle(
        fontFamily = DispFont,
        fontSize = flt.fontSizeSp.sp,
        fontWeight = FontWeight.Normal,
    )

    if (flt.isBubble) {
        Box(
            modifier = Modifier
                .offset(offsetNow)
                .graphicsLayer {
                    translationX = -size.width / 2f
                    translationY = -size.height / 2f
                    scaleX = flt.scale.value
                    scaleY = flt.scale.value
                    alpha = fade()
                }
                .clip(RoundedCornerShape(10.dp))
                .background(colors.woodDark)
                .border(2.dp, colors.gold, RoundedCornerShape(10.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = flt.text,
                style = textStyle,
                color = flt.color,
            )
        }
    } else {
        val strkPx = with(density) { 3.dp.toPx() }
        Box(
            modifier = Modifier
                .offset(offsetNow)
                .graphicsLayer {
                    translationX = -size.width / 2f
                    translationY = -size.height / 2f
                    scaleX = flt.scale.value
                    scaleY = flt.scale.value
                    alpha = fade()
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = flt.text,
                style = textStyle.copy(drawStyle = Stroke(width = strkPx, join = StrokeJoin.Round)),
                color = colors.outline,
                modifier = Modifier.clearAndSetSemantics {},
            )
            Text(
                text = flt.text,
                style = textStyle,
                color = flt.color,
            )
        }
    }
}

/** Android Studio preview. */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun FltOverlayPrvw() {
    com.sushanth.onlysticks.ui.theme.OnlySticksTheme {
        Box(modifier = Modifier.size(200.dp, 100.dp)) {
            FltOverlay(
                flt = StageFlt(
                    id = 1,
                    text = "-15 HP",
                    xFrac = 0.5f,
                    yFrac = 0.5f,
                    color = com.sushanth.onlysticks.ui.theme.GameTheme.colors.hpFoe,
                    isBubble = false,
                ),
                stageWPx = 300f,
                stageHPx = 150f,
                colors = com.sushanth.onlysticks.ui.theme.GameTheme.colors,
            )
        }
    }
}



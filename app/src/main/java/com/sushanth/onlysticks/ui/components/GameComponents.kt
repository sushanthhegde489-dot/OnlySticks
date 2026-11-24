package com.sushanth.onlysticks.ui.components

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.platform.LocalContext
import com.sushanth.onlysticks.audio.SoundFx
import com.sushanth.onlysticks.audio.SoundMgr
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.StrokeJoin
import com.sushanth.onlysticks.ui.theme.DispFont
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sushanth.onlysticks.ui.theme.GameTheme
import kotlin.math.sin

object GameMotion {
    fun <T> snappy() = spring<T>(dampingRatio = 0.85f, stiffness = 322f)
    fun <T> bouncy() = spring<T>(dampingRatio = 0.65f, stiffness = 158f)
}

@Composable
private fun animatedPressScl(prssd: Boolean): Float =
    animateFloatAsState(if (prssd) 0.92f else 1f, if (prssd) tween(80) else GameMotion.bouncy(), label = "press").value

@Composable
fun GameBg(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val c = GameTheme.colors
    Box(
        modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(Brush.verticalGradient(listOf(c.meadowTop, c.meadowBottom)))
                val hill = c.meadowBottom.copy(alpha = 0.55f)
                drawOval(hill, Offset(-size.width * 0.3f, size.height * 0.62f), Size(size.width * 1.1f, size.height * 0.6f))
                drawOval(
                    c.leafDark.copy(alpha = 0.25f),
                    Offset(size.width * 0.35f, size.height * 0.7f),
                    Size(size.width * 1.0f, size.height * 0.5f),
                )
                val dot = c.leafLight.copy(alpha = 0.18f)
                for (i in 0 until 28) {
                    val x = ((i * 137) % 100) / 100f * size.width
                    val y = ((i * 71) % 100) / 100f * size.height
                    drawCircle(dot, radius = 4.dp.toPx() + (i % 3) * 2.dp.toPx(), center = Offset(x, y))
                }
                val tuft = c.leafDark.copy(alpha = 0.5f)
                val stp = 18.dp.toPx()
                var x = 0f
                var i = 0
                while (x < size.width) {
                    val h = 10.dp.toPx() + (sin(i * 1.7f) + 1f) * 5.dp.toPx()
                    drawLine(tuft, Offset(x, size.height), Offset(x - 3.dp.toPx(), size.height - h), 3.dp.toPx(), StrokeCap.Round)
                    drawLine(tuft, Offset(x + 5.dp.toPx(), size.height), Offset(x + 8.dp.toPx(), size.height - h * 0.8f), 3.dp.toPx(), StrokeCap.Round)
                    x += stp
                    i++
                }
            },
        content = content,
    )
}

fun Modifier.woodBoard(crnr: Dp = 18.dp, lip: Dp = 4.dp, trim: Boolean = true): Modifier = this.drawBehind {
    val c = DayWood
    val r = CornerRadius(crnr.toPx())
    val lipPx = lip.toPx()
    drawRoundRect(c.lip, topLeft = Offset(0f, lipPx), size = Size(size.width, size.height - lipPx), cornerRadius = r)
    val face = Size(size.width, size.height - lipPx)
    drawRoundRect(Brush.verticalGradient(listOf(c.light, c.mid, c.dark), endY = face.height), size = face, cornerRadius = r)
    val grain = c.grain.copy(alpha = 0.35f)
    val rows = (face.height / 9.dp.toPx()).toInt().coerceAtLeast(1)
    for (row in 1 until rows) {
        val y = row * face.height / rows
        val wobble = sin(row * 2.3f) * 3.dp.toPx()
        drawLine(grain, Offset(crnr.toPx() * 0.6f, y + wobble), Offset(size.width - crnr.toPx() * 0.6f, y - wobble), 1.dp.toPx())
    }
    drawRoundRect(
        Color.White.copy(alpha = 0.18f), topLeft = Offset(3.dp.toPx(), 2.dp.toPx()),
        size = Size(size.width - 6.dp.toPx(), face.height * 0.35f), cornerRadius = r,
    )
    if (trim) {
        val inset = 4.dp.toPx()
        drawRoundRect(
            c.gold, topLeft = Offset(inset, inset), size = Size(face.width - inset * 2, face.height - inset * 2),
            cornerRadius = CornerRadius((crnr.toPx() - inset).coerceAtLeast(2f)), style = Stroke(2.dp.toPx()),
        )
    }
}

private object DayWood {
    val light = Color(0xFFA86B38)
    val mid = Color(0xFF8A5528)
    val dark = Color(0xFF6B3F1D)
    val grain = Color(0xFF4E2F17)
    val lip = Color(0xFF3B220F)
    val gold = Color(0xFFE8B84A)
}

@Composable
fun WoodPanel(
    modifier: Modifier = Modifier,
    crnr: Dp = 18.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier.woodBoard(crnr).padding(bottom = 4.dp).padding(contentPadding), content = content)
}

@Composable
fun ParchmentPanel(
    modifier: Modifier = Modifier,
    crnr: Dp = 14.dp,
    contentPadding: PaddingValues = PaddingValues(12.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    val c = GameTheme.colors
    val shape = RoundedCornerShape(crnr)
    Box(
        modifier
            .clip(shape)
            .background(Brush.verticalGradient(listOf(c.parchment, c.parchmentDeep)))
            .border(2.dp, c.woodLight.copy(alpha = 0.6f), shape)
            .padding(contentPadding),
        content = content,
    )
}

enum class BtnStyle { Primary, Secondary, Wood }

@Composable
fun ChunkyBtn(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: BtnStyle = BtnStyle.Primary,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    textStyle: TextStyle = MaterialTheme.typography.titleLarge,
    minHeight: Dp = 56.dp,
) {
    val c = GameTheme.colors
    val context = LocalContext.current
    val soundMgr = remember { SoundMgr.get(context) }
    val (face, lip) = when {
        !enabled -> Color(0xFF9C8B78) to Color(0xFF5E5245)
        style == BtnStyle.Primary -> c.leaf to c.leafDark
        style == BtnStyle.Secondary -> c.amber to c.amberDark
        else -> c.woodLight to c.woodDark
    }
    val interaction = remember { MutableInteractionSource() }
    val prssd by interaction.collectIsPressedAsState()
    val scale = animatedPressScl(prssd && enabled)
    val lipPx = 5.dp
    Box(
        modifier
            .heightIn(min = minHeight)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(
                interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = {
                    soundMgr.play(SoundFx.TAP, volume = 0.7f)
                    onClick()
                },
            )
            .drawBehind {
                val r = CornerRadius(16.dp.toPx())
                val lp = lipPx.toPx()
                val sink = if (prssd && enabled) lp * 0.6f else 0f
                drawRoundRect(c.outline, size = size, cornerRadius = r)
                val inner = 2.dp.toPx()
                drawRoundRect(lip, topLeft = Offset(inner, inner + lp), size = Size(size.width - inner * 2, size.height - inner * 2 - lp), cornerRadius = r)
                val faceSize = Size(size.width - inner * 2, size.height - inner * 2 - lp)
                drawRoundRect(
                    Brush.verticalGradient(listOf(lerp(face, Color.White, 0.25f), face), startY = inner + sink, endY = inner + sink + faceSize.height),
                    topLeft = Offset(inner, inner + sink), size = faceSize, cornerRadius = r,
                )
                drawRoundRect(
                    Color.White.copy(alpha = 0.35f), topLeft = Offset(inner + 6.dp.toPx(), inner + sink + 3.dp.toPx()),
                    size = Size(faceSize.width - 12.dp.toPx(), faceSize.height * 0.32f), cornerRadius = CornerRadius(10.dp.toPx()),
                )
                if (enabled) {
                    drawRoundRect(
                        c.gold.copy(alpha = 0.9f), topLeft = Offset(inner, inner + sink), size = faceSize,
                        cornerRadius = r, style = Stroke(1.5.dp.toPx()),
                    )
                }
            }
            .padding(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 8.dp + lipPx),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(28.dp))
                Box(Modifier.size(8.dp))
            }
            OutlinedTxt(text, style = textStyle, color = Color.White, outlW = 3.dp)
        }
    }
}
@Composable
fun OutlinedTxt(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleLarge,
    color: Color = Color.White,
    outline: Color = GameTheme.colors.outline,
    outlW: Dp = 3.dp,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
) {
    Box(modifier) {
        Text(
            text = text,
            style = style.copy(drawStyle = Stroke(width = with(LocalDensity.current) { outlW.toPx() }, join = StrokeJoin.Round)),
            color = outline,
            textAlign = textAlign,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clearSemantics(),
        )
        Text(text = text, style = style, color = color, textAlign = textAlign, maxLines = maxLines, overflow = TextOverflow.Ellipsis)
    }
}
private fun Modifier.clearSemantics(): Modifier = this.clearAndSetSemantics { }
@Composable
fun StatBar(
    frac: Float,
    color: Color,
    modifier: Modifier = Modifier,
    height: Dp = 14.dp,
    ghost: Color = GameTheme.colors.hpGhost,
) {
    val target = frac.coerceIn(0f, 1f)
    val fill by animateFloatAsState(target, GameMotion.snappy(), label = "bar")
    val trail = remember { Animatable(target) }
    LaunchedEffect(target) {
        if (target < trail.value) {
            kotlinx.coroutines.delay(400)
            trail.animateTo(target, tween(450))
        } else {
            trail.snapTo(target)
        }
    }
    val c = GameTheme.colors
    Canvas(modifier.fillMaxWidth().height(height)) {
        val r = CornerRadius(size.height / 2)
        drawRoundRect(c.outline, cornerRadius = r)
        val inset = 2.dp.toPx()
        val inner = Size(size.width - inset * 2, size.height - inset * 2)
        val ir = CornerRadius(inner.height / 2)
        drawRoundRect(Color(0xFF2B1A0D), topLeft = Offset(inset, inset), size = inner, cornerRadius = ir)
        if (trail.value > fill) {
            drawRoundRect(ghost, topLeft = Offset(inset, inset), size = Size(inner.width * trail.value, inner.height), cornerRadius = ir)
        }
        if (fill > 0f) {
            val w = (inner.width * fill).coerceAtLeast(inner.height)
            drawRoundRect(color, topLeft = Offset(inset, inset), size = Size(w, inner.height), cornerRadius = ir)
            drawRoundRect(
                Color.White.copy(alpha = 0.35f), topLeft = Offset(inset + 3.dp.toPx(), inset + 1.5.dp.toPx()),
                size = Size((w - 6.dp.toPx()).coerceAtLeast(0f), inner.height * 0.3f), cornerRadius = ir,
            )
        }
    }
}
@Composable
fun CoinsPill(amount: String, icon: ImageVector, modifier: Modifier = Modifier, contentDescription: String? = null) {
    val c = GameTheme.colors
    Row(
        modifier
            .clip(CircleShape)
            .background(c.woodDark.copy(alpha = 0.85f))
            .border(2.dp, c.gold, CircleShape)
            .padding(start = 4.dp, end = 14.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = Color.Unspecified, modifier = Modifier.size(26.dp))
        Box(Modifier.size(6.dp))
        OutlinedTxt(amount, style = MaterialTheme.typography.titleMedium, outlW = 2.dp)
    }
}
@Composable
fun Medallion(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    fill: Color = GameTheme.colors.wood,
    rim: Color = GameTheme.colors.gold,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(lerp(fill, Color.White, 0.25f), fill)))
            .border(3.dp, rim, CircleShape)
            .border(5.dp, GameTheme.colors.outline.copy(alpha = 0.6f), CircleShape),
        contentAlignment = Alignment.Center,
        content = content,
    )
}
@Composable
fun GameBadge(text: String, modifier: Modifier = Modifier, color: Color = GameTheme.colors.leaf) {
    Box(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color)
            .border(2.dp, GameTheme.colors.outline, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        OutlinedTxt(text, style = MaterialTheme.typography.labelLarge.copy(fontFamily = DispFont), outlW = 2.dp)
    }
}
@Composable
fun SegmentedTabs(opts: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val c = GameTheme.colors
    Row(
        modifier.fillMaxWidth().clip(CircleShape).background(c.woodDark.copy(alpha = 0.85f)).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        opts.forEachIndexed { i, label ->
            val isSel = i == selected
            Box(
                Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(if (isSel) c.leaf else Color.Transparent)
                    .clickable(role = androidx.compose.ui.semantics.Role.Tab) { onSelect(i) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                OutlinedTxt(label, style = MaterialTheme.typography.titleMedium, color = if (isSel) c.gold else c.onWood, outlW = 2.dp)
            }
        }
    }
}

fun readAnimrScl(context: Context): Float = runCatching {
    Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
}.getOrDefault(1f)

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun GameCompsPrvw() {
    com.sushanth.onlysticks.ui.theme.OnlySticksTheme {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ChunkyBtn(text = "Play Battle", onClick = {})
            CoinsPill(amount = "1,500", icon = GameIcos.Coin)
            GameBadge(text = "LVL 5")
            SegmentedTabs(opts = listOf("Tab 1", "Tab 2"), selected = 0, onSelect = {})
        }
    }
}



// Colour palettes: card frame colours by rarity and the day/night game palettes.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.sushanth.onlysticks.domain.model.Rarity
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/** Colours for a card frame: the frame, text on it, and an accent. */
data class FrameCols(val frame: Color, val onFrame: Color, val accent: Color)
private val DarkInk = Color(0xFF000000)
private val White = Color(0xFFFFFFFF)
private val PaleYellow = Color(0xFFFFF59D)
/** Frame colours for a rarity. Text colour is chosen so it stays readable on the frame. */
fun rarFrameCols(rarity: Rarity): FrameCols {
    val frame = Color(rarity.colHex)
    return when (rarity) {
        Rarity.COMMON -> FrameCols(frame, onFrame = DarkInk, accent = Color(0xFF1A1A1A))
        Rarity.ULTRA_RARE -> FrameCols(frame, onFrame = DarkInk, accent = Color(0xFF3E2723))
        Rarity.UNCOMMON, Rarity.RARE, Rarity.SUPER_RARE -> FrameCols(frame, onFrame = White, accent = PaleYellow)
    }
}
/** Faded version for disabled cards. */
fun FrameCols.dimmed(): FrameCols = copy(frame = frame.copy(alpha = 0.55f), accent = accent.copy(alpha = 0.55f))

/** Frame colours for power-ups and trainers. */
val ItemFrameCols = FrameCols(frame = Color(0xFF5D3A1E), onFrame = White, accent = Color(0xFFFFD866))
/** WCAG relative luminance of a colour. */
fun relativeLuminance(color: Color): Double {
    fun chan(c: Float): Double {
        val v = c.toDouble()
        return if (v <= 0.04045) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
    }
    return 0.2126 * chan(color.red) + 0.7152 * chan(color.green) + 0.0722 * chan(color.blue)
}
/** WCAG contrast ratio between two colours (used to check text readability). */
fun contrastRatio(a: Color, b: Color): Double {
    val la = relativeLuminance(a)
    val lb = relativeLuminance(b)
    return (max(la, lb) + 0.05) / (min(la, lb) + 0.05)
}
/** The game's full colour palette, available via [GameTheme.colors]. */
@Immutable
data class GameCols(
    val meadowTop: Color,
    val meadowBottom: Color,
    val woodDark: Color = Color(0xFF4E2F17),
    val wood: Color = Color(0xFF7A4A24),
    val woodLight: Color = Color(0xFFA86B38),
    val woodGrain: Color = Color(0xFF5F3A1C),
    val parchment: Color,
    val parchmentDeep: Color,
    val ink: Color,
    val inkSoft: Color,
    val onWood: Color = Color(0xFFFFF3D6),
    val leaf: Color = Color(0xFF4FA83D),
    val leafDark: Color = Color(0xFF2E7A26),
    val leafLight: Color = Color(0xFF8BD46A),
    val amber: Color = Color(0xFFF29E38),
    val amberDark: Color = Color(0xFFB86A14),
    val gold: Color = Color(0xFFF5C542),
    val goldDark: Color = Color(0xFFA8790F),
    val hpYou: Color = Color(0xFF5CC93B),
    val hpFoe: Color = Color(0xFFE5533D),
    val hpGhost: Color = Color(0xFFFFE08A),
    val stam: Color = Color(0xFF3AB6C9),
    val crit: Color = Color(0xFFFFD23F),
    val fire: Color = Color(0xFFFF7A2F),
    val water: Color = Color(0xFF4FB3E8),
    val outline: Color = Color(0xFF2A170A),
)
/** Light (daytime) palette. */
val DayGameCols = GameCols(
    meadowTop = Color(0xFFC9E7A0),
    meadowBottom = Color(0xFF6FAE4E),
    parchment = Color(0xFFFFF3D6),
    parchmentDeep = Color(0xFFEBD3A0),
    ink = Color(0xFF3A2312),
    inkSoft = Color(0xFF6E4F35),
)
/** Dark (night) palette. */
val NightGameCols = GameCols(
    meadowTop = Color(0xFF6E9A4A),
    meadowBottom = Color(0xFF3F6B2E),
    parchment = Color(0xFFF3E3C0),
    parchmentDeep = Color(0xFFDDC393),
    ink = Color(0xFF3A2312),
    inkSoft = Color(0xFF6E4F35),
)

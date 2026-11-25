// Typography: Lilita One for headings and Nunito for body text.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sushanth.onlysticks.R

/** Chunky display font for titles and numbers. */
val DispFont = FontFamily(Font(R.font.lilita_one))

/** Rounded body font. */
@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
val BodyFont = FontFamily(
    listOf(400, 600, 700, 800, 900).map { w ->
        Font(R.font.nunito, FontWeight(w), variationSettings = FontVariation.Settings(FontVariation.weight(w)))
    },
)

private val Base = Typography()

private fun TextStyle.disp() = copy(fontFamily = DispFont, fontWeight = FontWeight.Normal, letterSpacing = 0.5.sp)
private fun TextStyle.body(weight: Int) = copy(fontFamily = BodyFont, fontWeight = FontWeight(weight))

/** Material typography using the game's fonts. */
val OnlySticksTypography = Typography(
    displayLarge = Base.displayLarge.disp(),
    displayMedium = Base.displayMedium.disp(),
    displaySmall = Base.displaySmall.disp(),
    headlineLarge = Base.headlineLarge.disp(),
    headlineMedium = Base.headlineMedium.disp(),
    headlineSmall = Base.headlineSmall.disp(),
    titleLarge = Base.titleLarge.disp(),
    titleMedium = Base.titleMedium.disp(),
    titleSmall = Base.titleSmall.disp(),
    bodyLarge = Base.bodyLarge.body(600),
    bodyMedium = Base.bodyMedium.body(600),
    bodySmall = Base.bodySmall.body(600),
    labelLarge = Base.labelLarge.body(800),
    labelMedium = Base.labelMedium.body(800),
    labelSmall = Base.labelSmall.body(800),
)

/** Big text style for rarity codes on cards. */
val RarCodeStyle = TextStyle(fontFamily = DispFont, fontSize = 30.sp, lineHeight = 34.sp)

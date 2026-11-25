// Helpers for drawing the chunky outlined icon style in code.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Outline stroke width in viewport units. */
internal const val OutlW = 1.5f

/** Dark brown outline colour shared by all icons. */
internal val DarkOutlCol = Color(0xFF2A170A)

/** A 24x24 dp ImageVector builder. */
internal fun icoBldr(name: String): ImageVector.Builder =

    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    )

/** Adds a path with optional fill and the standard dark rounded outline. */
internal inline fun ImageVector.Builder.chunkyPath(
    fillCol: Color? = null,
    strkCol: Color? = DarkOutlCol,
    strokeWidth: Float = OutlW,
    block: PathBuilder.() -> Unit
): ImageVector.Builder = path(
    fill = fillCol?.let { SolidColor(it) },
    stroke = strkCol?.let { SolidColor(it) },
    strokeLineWidth = strokeWidth,
    strokeLineCap = StrokeCap.Round,
    strokeLineJoin = StrokeJoin.Round,
    pathBuilder = block
)

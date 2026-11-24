package com.sushanth.onlysticks.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal const val OutlW = 1.5f

internal val DarkOutlCol = Color(0xFF2A170A)

internal fun icoBldr(name: String): ImageVector.Builder =

    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    )

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

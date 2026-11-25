// Canvas drawing for the stage background.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.arena.stage

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.sushanth.onlysticks.ui.theme.GameCols

/** Sky gradient, sun, clouds, bushes and a wooden fence. */
internal fun DrawScope.drawBackdrop(colors: GameCols, plankPath: Path) {
    val w = size.width
    val h = size.height

    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(SkyTop, SkyBottom),
            startY = 0f,
            endY = h * 0.55f,
        ),
        size = Size(w, h * 0.65f),
    )

    val sunCenter = Offset(w * 0.85f, h * 0.18f)
    val sunRad = minOf(w, h) * 0.14f
    drawCircle(
        color = SunCol.copy(alpha = 0.20f),
        radius = sunRad * 1.35f,
        center = sunCenter,
    )
    drawCircle(
        color = SunCol.copy(alpha = 0.65f),
        radius = sunRad,
        center = sunCenter,
    )

    val cloudCol = Color.White.copy(alpha = 0.40f)
    drawCircle(cloudCol, radius = 14.dp.toPx(), center = Offset(w * 0.25f, h * 0.16f))
    drawCircle(cloudCol, radius = 20.dp.toPx(), center = Offset(w * 0.30f, h * 0.14f))
    drawCircle(cloudCol, radius = 16.dp.toPx(), center = Offset(w * 0.35f, h * 0.16f))

    drawCircle(cloudCol, radius = 12.dp.toPx(), center = Offset(w * 0.62f, h * 0.22f))
    drawCircle(cloudCol, radius = 16.dp.toPx(), center = Offset(w * 0.66f, h * 0.20f))
    drawCircle(cloudCol, radius = 11.dp.toPx(), center = Offset(w * 0.70f, h * 0.22f))

    val bushBaseY = h * 0.56f
    val bushCnt = 9
    for (i in 0 until bushCnt) {
        val bx = w * (0.06f + i * 0.11f)
        val br = minOf(w, h) * (0.09f + (i % 3) * 0.018f)
        val bushCol = if (i % 2 == 0) colors.leafDark else colors.leaf
        drawCircle(
            color = bushCol,
            radius = br,
            center = Offset(bx, bushBaseY - (i % 2) * 4.dp.toPx()),
        )
    }

    val fenceTop = h * 0.45f
    val fenceBottom = h * 0.63f
    val fenceH = fenceBottom - fenceTop
    val outlStrk = 2.dp.toPx()

    val railH = fenceH * 0.16f
    val rail1Y = fenceTop + fenceH * 0.28f
    val rail2Y = fenceTop + fenceH * 0.68f
    drawRect(colors.wood, topLeft = Offset(0f, rail1Y), size = Size(w, railH))
    drawRect(colors.outline, topLeft = Offset(0f, rail1Y), size = Size(w, railH), style = Stroke(outlStrk))
    drawRect(colors.wood, topLeft = Offset(0f, rail2Y), size = Size(w, railH))
    drawRect(colors.outline, topLeft = Offset(0f, rail2Y), size = Size(w, railH), style = Stroke(outlStrk))

    val plankCnt = 14
    val slotW = w / plankCnt
    val plankW = slotW * 0.74f
    val tipH = fenceH * 0.22f
    for (i in 0 until plankCnt) {
        val left = i * slotW + (slotW - plankW) * 0.5f
        val right = left + plankW
        val midX = (left + right) * 0.5f
        val plankCol = if (i % 2 == 0) colors.woodLight else colors.wood

        plankPath.reset()
        plankPath.moveTo(left, fenceBottom)
        plankPath.lineTo(left, fenceTop + tipH)
        plankPath.lineTo(midX, fenceTop)
        plankPath.lineTo(right, fenceTop + tipH)
        plankPath.lineTo(right, fenceBottom)
        plankPath.close()

        drawPath(plankPath, color = plankCol)
        drawPath(
            plankPath,
            color = colors.outline,
            style = Stroke(width = outlStrk, join = StrokeJoin.Round),
        )
    }
}

/** Grass and the dirt fighting ring. */
internal fun DrawScope.drawGround(colors: GameCols) {
    val w = size.width
    val h = size.height
    val grassTop = h * 0.62f

    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(colors.leaf, colors.leafDark),
            startY = grassTop,
            endY = h,
        ),
        topLeft = Offset(0f, grassTop),
        size = Size(w, h - grassTop),
    )

    val ringW = w * 0.72f
    val ringH = h * 0.22f
    val ringTopLeft = Offset((w - ringW) * 0.5f, h * 0.80f - ringH * 0.5f)
    val ringSize = Size(ringW, ringH)
    drawOval(
        color = DirtFill,
        topLeft = ringTopLeft,
        size = ringSize,
    )
    drawOval(
        color = DirtOutl,
        topLeft = ringTopLeft,
        size = ringSize,
        style = Stroke(width = 3.dp.toPx()),
    )
}

/** Little sign board at the top of the stage that shows the round number. */
internal fun DrawScope.drawRoundSignBoard(colors: GameCols) {
    val w = size.width
    val postW = 8.dp.toPx()
    val postH = 16.dp.toPx()
    val boardW = 52.dp.toPx()
    val boardH = 28.dp.toPx()
    val boardTop = 4.dp.toPx()
    val outlStrk = 2.dp.toPx()

    drawRect(
        color = colors.woodDark,
        topLeft = Offset((w - postW) * 0.5f, 0f),
        size = Size(postW, postH),
    )
    val boardLeft = (w - boardW) * 0.5f
    val crnr = CornerRadius(8.dp.toPx())
    drawRoundRect(
        color = colors.wood,
        topLeft = Offset(boardLeft, boardTop),
        size = Size(boardW, boardH),
        cornerRadius = crnr,
    )
    drawRoundRect(
        color = colors.woodLight,
        topLeft = Offset(boardLeft + 2.dp.toPx(), boardTop + 2.dp.toPx()),
        size = Size(boardW - 4.dp.toPx(), boardH - 5.dp.toPx()),
        cornerRadius = crnr,
    )
    drawRoundRect(
        color = colors.outline,
        topLeft = Offset(boardLeft, boardTop),
        size = Size(boardW, boardH),
        cornerRadius = crnr,
        style = Stroke(width = outlStrk),
    )
}



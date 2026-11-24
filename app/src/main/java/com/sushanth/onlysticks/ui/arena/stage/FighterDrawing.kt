package com.sushanth.onlysticks.ui.arena.stage

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sushanth.onlysticks.domain.combat.Combatant
import com.sushanth.onlysticks.domain.model.Rarity
import com.sushanth.onlysticks.ui.theme.GameCols
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal fun DrawScope.drawFgtr(
    combatant: Combatant?,
    baseXFrac: Float,
    facing: Float,
    bandanaCol: Color,
    anim: FgtrAnimState,
    idlePhase: Float,
    colors: GameCols,
    bandanaPath: Path,
    flamePath: Path,
) {
    val w = size.width
    val h = size.height
    val groundY = h * 0.80f

    val lowStam = combatant != null && combatant.stam < 20
    val phase = if (lowStam) idlePhase * 0.5f else idlePhase
    val bobPx = sin(phase) * 2.dp.toPx()
    val crouchPx = anim.crouch.value * 10.dp.toPx()
    val droopPx = if (lowStam) 5.dp.toPx() else 0f

    val kb = anim.knockbackDp.value
    val hitJitterPx = if (kb > 0f) sin(kb * 2.2f) * 3.dp.toPx() * (kb / 10f) else 0f
    val centerX = w * (baseXFrac + facing * anim.lungeX.value) - facing * kb.dp.toPx() + hitJitterPx

    val shdwW = 46.dp.toPx()
    val shdwH = 12.dp.toPx()
    drawOval(
        color = colors.outline.copy(alpha = 0.28f),
        topLeft = Offset(centerX - shdwW * 0.5f, groundY - shdwH * 0.5f),
        size = Size(shdwW, shdwH),
    )

    val limbStrk = 6.dp.toPx()
    val headStrk = 2.dp.toPx()
    val figureH = h * 0.34f
    val hipY = groundY - figureH * 0.38f + bobPx * 0.5f + crouchPx
    val shoulderY = groundY - figureH * 0.76f + bobPx + crouchPx + droopPx
    val shoulderX = centerX + (if (lowStam) facing * 4.dp.toPx() else 0f)
    val headRad = minOf(w, h) * 0.055f
    val headCenter = Offset(shoulderX, shoulderY - headRad * 1.15f)

    val fallAngle = if (combatant?.alive == false && anim.fallDeg.value == 0f) 90f else anim.fallDeg.value

    withTransform({
        if (fallAngle != 0f) {
            rotate(degrees = -facing * fallAngle, pivot = Offset(centerX, groundY))
        }
    }) {
        val bodyCol = if (anim.flashAlpha.value > 0.4f) Color.White else colors.outline

        val footSpread = 12.dp.toPx()
        drawLine(
            color = bodyCol,
            start = Offset(centerX, hipY),
            end = Offset(centerX - footSpread, groundY),
            strokeWidth = limbStrk,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = bodyCol,
            start = Offset(centerX, hipY),
            end = Offset(centerX + footSpread, groundY),
            strokeWidth = limbStrk,
            cap = StrokeCap.Round,
        )

        drawLine(
            color = bodyCol,
            start = Offset(centerX, hipY),
            end = Offset(shoulderX, shoulderY),
            strokeWidth = limbStrk,
            cap = StrokeCap.Round,
        )

        val backHand = Offset(shoulderX - facing * 12.dp.toPx(), shoulderY + 14.dp.toPx())
        drawLine(
            color = bodyCol,
            start = Offset(shoulderX, shoulderY + 2.dp.toPx()),
            end = backHand,
            strokeWidth = limbStrk,
            cap = StrokeCap.Round,
        )

        val blockLift = anim.blockPose.value * 10.dp.toPx()
        val handX = shoulderX + facing * 16.dp.toPx()
        val handY = shoulderY + 8.dp.toPx() - blockLift + (if (lowStam) 4.dp.toPx() else 0f)
        val handPos = Offset(handX, handY)
        drawLine(
            color = bodyCol,
            start = Offset(shoulderX, shoulderY + 2.dp.toPx()),
            end = handPos,
            strokeWidth = limbStrk,
            cap = StrokeCap.Round,
        )

        drawCircle(
            color = if (anim.flashAlpha.value > 0.4f) Color.White else HeadFill,
            radius = headRad,
            center = headCenter,
        )
        drawCircle(
            color = bodyCol,
            radius = headRad,
            center = headCenter,
            style = Stroke(width = headStrk),
        )

        val bandTop = headCenter.y - headRad * 0.55f
        val bandH = headRad * 0.42f
        drawRoundRect(
            color = bandanaCol,
            topLeft = Offset(headCenter.x - headRad * 1.02f, bandTop),
            size = Size(headRad * 2.04f, bandH),
            cornerRadius = CornerRadius(3.dp.toPx()),
        )
        drawRoundRect(
            color = bodyCol,
            topLeft = Offset(headCenter.x - headRad * 1.02f, bandTop),
            size = Size(headRad * 2.04f, bandH),
            cornerRadius = CornerRadius(3.dp.toPx()),
            style = Stroke(width = 1.5.dp.toPx()),
        )
        val knotX = headCenter.x - facing * headRad * 1.02f
        val knotY = bandTop + bandH * 0.5f
        bandanaPath.reset()
        bandanaPath.moveTo(knotX, knotY)
        bandanaPath.lineTo(knotX - facing * 9.dp.toPx(), knotY - 4.dp.toPx() + bobPx * 0.4f)
        bandanaPath.moveTo(knotX, knotY)
        bandanaPath.lineTo(knotX - facing * 8.dp.toPx(), knotY + 5.dp.toPx() - bobPx * 0.4f)
        drawPath(
            path = bandanaPath,
            color = bandanaCol,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
        )

        val eyeY = headCenter.y + headRad * 0.08f
        val eye1X = headCenter.x + facing * headRad * 0.22f
        val eye2X = headCenter.x + facing * headRad * 0.58f
        val eyeR = 2.dp.toPx()
        drawCircle(color = colors.outline, radius = eyeR, center = Offset(eye1X, eyeY))
        drawCircle(color = colors.outline, radius = eyeR, center = Offset(eye2X, eyeY))

        if (anim.crouch.value > 0.1f) {
            val zProg = anim.crouch.value
            for (zIdx in 0..1) {
                val zSize = (5f + zIdx * 2f).dp.toPx()
                val zx = headCenter.x + facing * (headRad * 0.9f + zIdx * 8.dp.toPx())
                val zy = headCenter.y - headRad * 1.3f - zIdx * 8.dp.toPx() - zProg * 4.dp.toPx()
                val zStrk = 1.8.dp.toPx()
                drawLine(colors.stam, Offset(zx, zy), Offset(zx + zSize, zy), zStrk, StrokeCap.Round)
                drawLine(colors.stam, Offset(zx + zSize, zy), Offset(zx, zy + zSize), zStrk, StrokeCap.Round)
                drawLine(colors.stam, Offset(zx, zy + zSize), Offset(zx + zSize, zy + zSize), zStrk, StrokeCap.Round)
            }
        }

        val activeStick = combatant?.activeStick
        val hasStick = !anim.stickSnapped && (combatant == null || (!combatant.unarmed && activeStick != null))
        if (hasStick) {
            val rarCol = Color(activeStick?.stick?.rarity?.colHex ?: Rarity.COMMON.colHex)
            val isBurn = (activeStick?.burning == true) || anim.ignitedOverride
            val isWet = (activeStick?.wet == true) || anim.wetOverride

            val swayDeg = sin(phase) * 4f
            val baseAngleDeg = when {
                anim.blockPose.value > 0.1f -> -75f * anim.blockPose.value
                lowStam -> -15f
                else -> -42f
            } + swayDeg + anim.stickSwingDeg.value

            val angleRad = (baseAngleDeg * PI / 180.0).toFloat()
            val stickLen = figureH * 0.62f
            val dx = facing * cos(angleRad) * stickLen
            val dy = sin(angleRad) * stickLen
            val stickStart = Offset(handPos.x - dx * 0.18f, handPos.y - dy * 0.18f)
            val stickEnd = Offset(handPos.x + dx * 0.82f, handPos.y + dy * 0.82f)

            drawLine(
                color = colors.outline,
                start = stickStart,
                end = stickEnd,
                strokeWidth = 9.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawLine(
                color = colors.woodLight,
                start = stickStart,
                end = stickEnd,
                strokeWidth = 5.5.dp.toPx(),
                cap = StrokeCap.Round,
            )

            for (t in 1..2) {
                val frac = 0.35f + t * 0.22f
                val tx = stickStart.x + (stickEnd.x - stickStart.x) * frac
                val ty = stickStart.y + (stickEnd.y - stickStart.y) * frac
                drawLine(
                    color = colors.woodDark,
                    start = Offset(tx - 2.dp.toPx(), ty - 2.dp.toPx()),
                    end = Offset(tx + 2.dp.toPx(), ty + 2.dp.toPx()),
                    strokeWidth = 1.8.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }

            val gripEnd = Offset(
                stickStart.x + (stickEnd.x - stickStart.x) * 0.28f,
                stickStart.y + (stickEnd.y - stickStart.y) * 0.28f,
            )
            drawLine(
                color = rarCol,
                start = stickStart,
                end = gripEnd,
                strokeWidth = 6.5.dp.toPx(),
                cap = StrokeCap.Butt,
            )

            if (isBurn || anim.burnBurst.value > 0f) {
                val boost = 1f + anim.burnBurst.value * 0.9f
                for (f in 0 until 3) {
                    val frac = 0.45f + f * 0.22f
                    val fx = stickStart.x + (stickEnd.x - stickStart.x) * frac
                    val fy = stickStart.y + (stickEnd.y - stickStart.y) * frac
                    val flicker = sin(idlePhase * 3f + f * 1.7f) * 3.dp.toPx()
                    val r = (4.5.dp.toPx() + (f % 2) * 1.5.dp.toPx()) * boost
                    flamePath.reset()
                    flamePath.moveTo(fx - r * 0.7f, fy)
                    flamePath.quadraticTo(fx, fy - r * 2.2f + flicker, fx + r * 0.7f, fy)
                    flamePath.close()
                    drawPath(flamePath, color = Color(0xFFFF7A2F))
                    drawCircle(
                        color = colors.crit,
                        radius = r * 0.45f,
                        center = Offset(fx, fy - r * 0.4f + flicker * 0.4f),
                    )
                }
            }

            if (isWet || anim.waterSplash.value > 0f) {
                val dripTravel = ((idlePhase / (2f * PI.toFloat())) + anim.waterSplash.value) % 1f
                for (d in 0 until 2) {
                    val frac = 0.52f + d * 0.26f
                    val wx = stickStart.x + (stickEnd.x - stickStart.x) * frac
                    val wy = stickStart.y + (stickEnd.y - stickStart.y) * frac +
                        ((dripTravel + d * 0.45f) % 1f) * 12.dp.toPx()
                    drawCircle(
                        color = Color(0xFF4FB3E8),
                        radius = 2.8.dp.toPx(),
                        center = Offset(wx, wy),
                    )
                }
                if (anim.waterSplash.value > 0f) {
                    val sp = anim.waterSplash.value
                    val splashOrigin = Offset(
                        (stickStart.x + stickEnd.x) * 0.5f,
                        (stickStart.y + stickEnd.y) * 0.5f,
                    )
                    for (drop in 0 until 5) {
                        val a = (-0.8f + drop * 0.4f) * PI.toFloat()
                        val sx = splashOrigin.x + cos(a) * sp * 18.dp.toPx()
                        val sy = splashOrigin.y - absSin(a) * sp * 16.dp.toPx() + sp * sp * 14.dp.toPx()
                        drawCircle(
                            color = colors.water.copy(alpha = (1f - sp).coerceIn(0f, 1f)),
                            radius = 3.dp.toPx(),
                            center = Offset(sx, sy),
                        )
                    }
                }
            }
        }

        if (anim.splinters.value > 0f) {
            val t = anim.splinters.value
            val origin = Offset(handPos.x + facing * 14.dp.toPx(), handPos.y - 8.dp.toPx())
            val gravity = 65.dp.toPx()
            for (s in 0 until 6) {
                val angle = (-0.8f + s * 0.32f) * PI.toFloat()
                val vx = cos(angle) * 28.dp.toPx() * facing
                val vy = -absSin(angle) * 32.dp.toPx()
                val sx = origin.x + vx * t
                val sy = origin.y + vy * t + 0.5f * gravity * t * t
                withTransform({
                    rotate(degrees = (s * 45f + t * 180f), pivot = Offset(sx, sy))
                }) {
                    drawRect(
                        color = colors.woodLight,
                        topLeft = Offset(sx - 4.dp.toPx(), sy - 1.5.dp.toPx()),
                        size = Size(8.dp.toPx(), 3.dp.toPx()),
                    )
                }
            }
        }

        if (anim.poof.value > 0f) {
            val p = anim.poof.value
            val poofAlpha = (1f - p).coerceIn(0f, 1f)
            val spread = p * 22.dp.toPx()
            for (i in 0 until 5) {
                val ang = (i * 2.0 * PI / 5.0).toFloat()
                val px = centerX + cos(ang) * spread
                val py = shoulderY + 8.dp.toPx() + sin(ang) * spread * 0.7f
                drawCircle(
                    color = Color.White.copy(alpha = poofAlpha * 0.85f),
                    radius = (10.dp.toPx() * (1f - p * 0.4f)),
                    center = Offset(px, py),
                )
            }
        }
    }
}

internal fun absSin(rad: Float): Float {
    val s = sin(rad)
    return if (s < 0f) -s else s
}

internal fun DrawScope.drawSparkBurst(
    center: Offset,
    progress: Float,
    goldOnly: Boolean,
    colors: GameCols,
) {
    val innerR = progress * 8.dp.toPx()
    val outerR = innerR + (1f - progress * 0.5f) * 14.dp.toPx()
    val alpha = (1f - progress).coerceIn(0f, 1f)
    for (i in 0 until 8) {
        val angle = (i * PI / 4.0).toFloat()
        val c = if (goldOnly || i % 2 == 0) colors.gold else Color.White
        drawLine(
            color = c.copy(alpha = alpha),
            start = Offset(center.x + cos(angle) * innerR, center.y + sin(angle) * innerR),
            end = Offset(center.x + cos(angle) * outerR, center.y + sin(angle) * outerR),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

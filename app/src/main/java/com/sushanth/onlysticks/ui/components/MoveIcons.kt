// Vector icons for the combat moves.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/** Jab icon. */
internal fun buildJab(): ImageVector = icoBldr("Jab").apply {
        chunkyPath(fillCol = Color(0xFFA86B38)) {
            moveTo(2f, 10.5f)
            lineTo(18f, 10.5f)
            curveTo(19.5f, 10.5f, 20.5f, 11.2f, 20.5f, 12f)
            curveTo(20.5f, 12.8f, 19.5f, 13.5f, 18f, 13.5f)
            lineTo(2f, 13.5f)
            close()
        }
        chunkyPath(fillCol = Color(0xFF4FA83D), strkCol = DarkOutlCol, strokeWidth = 1f) {
            moveTo(3f, 10.5f)
            lineTo(7f, 10.5f)
            lineTo(7f, 13.5f)
            lineTo(3f, 13.5f)
            close()
        }
        chunkyPath(strkCol = DarkOutlCol, strokeWidth = 1.3f) {
            moveTo(21.5f, 9.5f)
            lineTo(23.5f, 8f)
            moveTo(22f, 12f)
            lineTo(24f, 12f)
            moveTo(21.5f, 14.5f)
            lineTo(23.5f, 16f)
        }
}.build()

/** Lunge icon. */
internal fun buildLunge(): ImageVector = icoBldr("Lunge").apply {
        chunkyPath(fillCol = Color(0xFFA86B38)) {
            moveTo(3f, 10.5f)
            lineTo(13.5f, 10.5f)
            lineTo(13.5f, 13.5f)
            lineTo(3f, 13.5f)
            close()
        }
        chunkyPath(fillCol = Color(0xFFCFD8DC)) {
            moveTo(13.5f, 7f)
            lineTo(22f, 12f)
            lineTo(13.5f, 17f)
            lineTo(15.5f, 12f)
            close()
        }
        chunkyPath(strkCol = DarkOutlCol, strokeWidth = 1.2f) {
            moveTo(14.5f, 12f)
            lineTo(21f, 12f)
            moveTo(2f, 7.5f)
            lineTo(8f, 7.5f)
            moveTo(1f, 16.5f)
            lineTo(7f, 16.5f)
        }
}.build()

/** Parry icon. */
internal fun buildParry(): ImageVector = icoBldr("Parry").apply {
        chunkyPath(fillCol = Color(0xFF7A4A24)) {
            moveTo(4f, 6.5f)
            curveTo(8f, 5f, 16f, 5f, 20f, 6.5f)
            curveTo(20f, 13.5f, 17f, 18.5f, 12f, 22f)
            curveTo(7f, 18.5f, 4f, 13.5f, 4f, 6.5f)
            close()
        }
        chunkyPath(fillCol = Color(0xFF4FA83D)) {
            moveTo(6.5f, 8f)
            curveTo(9f, 7f, 15f, 7f, 17.5f, 8f)
            curveTo(17.5f, 13f, 15.5f, 17f, 12f, 19.5f)
            curveTo(8.5f, 17f, 6.5f, 13f, 6.5f, 8f)
            close()
        }
        chunkyPath(fillCol = Color(0xFFF5C542), strkCol = DarkOutlCol, strokeWidth = 0.8f) {
            moveTo(12f, 10f)
            lineTo(13.2f, 12f)
            lineTo(12f, 14f)
            lineTo(10.8f, 12f)
            close()
        }
}.build()

/** Slash icon. */
internal fun buildSlash(): ImageVector = icoBldr("Slash").apply {
        chunkyPath(fillCol = Color(0xFFFFD23F)) {
            moveTo(3.5f, 17.5f)
            curveTo(5.5f, 10f, 12.5f, 4.5f, 21f, 3.5f)
            curveTo(16.5f, 7.5f, 10f, 12.5f, 6.5f, 20f)
            curveTo(4.8f, 20f, 3.8f, 19f, 3.5f, 17.5f)
            close()
        }
        chunkyPath(fillCol = Color(0xFFE0E0E0)) {
            moveTo(7f, 19f)
            curveTo(11f, 13f, 15.5f, 8f, 21f, 3.5f)
            curveTo(17.5f, 9f, 13f, 14.5f, 9.5f, 19.5f)
            close()
        }
        chunkyPath(strkCol = DarkOutlCol, strokeWidth = 1.8f) {
            moveTo(5f, 21.5f)
            lineTo(7.5f, 19f)
        }
}.build()

/** Rest icon. */
internal fun buildRest(): ImageVector = icoBldr("Rest").apply {
        chunkyPath(fillCol = Color(0xFFFFE08A)) {
            moveTo(13f, 3.5f)
            curveTo(7.5f, 4.5f, 3.5f, 9.5f, 4f, 15.5f)
            curveTo(4.5f, 19.5f, 8f, 22f, 12f, 22f)
            curveTo(15.5f, 22f, 18.5f, 20f, 19.5f, 17f)
            curveTo(13.5f, 18.2f, 8.5f, 13.5f, 10.5f, 7f)
            curveTo(11.2f, 5.2f, 12.2f, 4.2f, 13f, 3.5f)
            close()
        }
        chunkyPath(strkCol = DarkOutlCol, strokeWidth = 1.5f) {
            moveTo(15.5f, 5.5f)
            lineTo(19f, 5.5f)
            lineTo(15.5f, 9.5f)
            lineTo(19f, 9.5f)

            moveTo(18f, 11.5f)
            lineTo(21f, 11.5f)
            lineTo(18f, 14.5f)
            lineTo(21f, 14.5f)
        }
}.build()

/** Exhaustion strike icon. */
internal fun buildExhStrike(): ImageVector = icoBldr("ExhaustionStrike").apply {
        chunkyPath(fillCol = Color(0xFFC9A27A)) {
            moveTo(8.5f, 4f)
            lineTo(13.5f, 4f)
            curveTo(15f, 7f, 17.5f, 9.5f, 17.5f, 13.5f)
            curveTo(17.5f, 16.5f, 15.5f, 19.5f, 12.5f, 20.5f)
            curveTo(9f, 21.5f, 6f, 18.5f, 6f, 15f)
            curveTo(5.5f, 11f, 6.8f, 7f, 8.5f, 4f)
            close()
        }
        chunkyPath(strkCol = DarkOutlCol, strokeWidth = 1.5f) {
            moveTo(11.5f, 8f)
            lineTo(10f, 11f)
            lineTo(12.5f, 13f)
            lineTo(11f, 16f)
            moveTo(6.5f, 14.5f)
            lineTo(10f, 14.5f)
            moveTo(17f, 17.5f)
            lineTo(19f, 19.5f)
        }
}.build()

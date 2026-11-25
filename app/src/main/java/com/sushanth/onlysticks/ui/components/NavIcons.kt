// Vector icons for navigation tabs and trainers.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/** Cards / deck tab icon. */
internal fun buildCards(): ImageVector = icoBldr("Cards").apply {
        chunkyPath(fillCol = Color(0xFFFFF3D6)) {
            moveTo(4.5f, 9f)
            lineTo(12.5f, 4.5f)
            lineTo(17f, 12.5f)
            lineTo(9f, 17f)
            close()
        }
        chunkyPath(fillCol = Color(0xFFFFF1D6)) {
            moveTo(8.5f, 6.5f)
            lineTo(19f, 6.5f)
            curveTo(20f, 6.5f, 20.5f, 7.3f, 20.5f, 8.5f)
            lineTo(20.5f, 20f)
            curveTo(20.5f, 21.2f, 19.8f, 22f, 18.5f, 22f)
            lineTo(8.5f, 22f)
            curveTo(7.2f, 22f, 6.5f, 21.2f, 6.5f, 20f)
            lineTo(6.5f, 8.5f)
            curveTo(6.5f, 7.3f, 7.2f, 6.5f, 8.5f, 6.5f)
            close()
        }
        chunkyPath(fillCol = Color(0xFFA86B38)) {
            moveTo(9.5f, 18f)
            lineTo(17.5f, 10f)
            lineTo(18.5f, 11f)
            lineTo(10.5f, 19f)
            close()
        }
        chunkyPath(fillCol = Color(0xFF4FA83D), strkCol = DarkOutlCol, strokeWidth = 0.8f) {
            moveTo(14f, 8f)
            curveTo(16.5f, 8f, 17.5f, 9.5f, 17.5f, 11.5f)
            curveTo(15f, 11.5f, 14f, 10f, 14f, 8f)
            close()
        }
}.build()

/** Battle tab icon. */
internal fun buildBattle(): ImageVector = icoBldr("Battle").apply {
        chunkyPath(fillCol = Color(0xFFA86B38)) {
            moveTo(4.5f, 7f)
            lineTo(17.5f, 20f)
            curveTo(18.8f, 21.2f, 21f, 19f, 19.8f, 17.8f)
            lineTo(6.8f, 4.8f)
            curveTo(5.5f, 3.5f, 3.2f, 5.8f, 4.5f, 7f)
            close()
        }
        chunkyPath(fillCol = Color(0xFFC48B55)) {
            moveTo(17.5f, 4.8f)
            lineTo(4.5f, 17.8f)
            curveTo(3.2f, 19f, 5.5f, 21.2f, 6.8f, 20f)
            lineTo(19.8f, 7f)
            curveTo(21f, 5.8f, 18.8f, 3.5f, 17.5f, 4.8f)
            close()
        }
        chunkyPath(fillCol = Color(0xFF4FA83D), strkCol = DarkOutlCol, strokeWidth = 1f) {
            moveTo(6.5f, 9f)
            lineTo(9f, 6.5f)
            lineTo(10.2f, 7.7f)
            lineTo(7.7f, 10.2f)
            close()
        }
        chunkyPath(fillCol = Color(0xFFE5533D), strkCol = DarkOutlCol, strokeWidth = 1f) {
            moveTo(15f, 6.5f)
            lineTo(17.5f, 9f)
            lineTo(16.3f, 10.2f)
            lineTo(13.8f, 7.7f)
            close()
        }
        chunkyPath(fillCol = Color(0xFFF5C542), strkCol = DarkOutlCol, strokeWidth = 0.8f) {
            moveTo(12f, 10f)
            lineTo(12.7f, 11.3f)
            lineTo(14f, 12f)
            lineTo(12.7f, 12.7f)
            lineTo(12f, 14f)
            lineTo(11.3f, 12.7f)
            lineTo(10f, 12f)
            lineTo(11.3f, 11.3f)
            close()
        }
}.build()

/** Shop tab icon. */
internal fun buildShop(): ImageVector = icoBldr("Shop").apply {
        chunkyPath(fillCol = Color(0xFFE5533D)) {
            moveTo(3.5f, 5.5f)
            lineTo(20.5f, 5.5f)
            lineTo(19.5f, 10f)
            lineTo(4.5f, 10f)
            close()
        }
        chunkyPath(fillCol = Color(0xFFFFF3D6), strkCol = DarkOutlCol, strokeWidth = 0.8f) {
            moveTo(7.5f, 5.5f)
            lineTo(10.5f, 5.5f)
            lineTo(10f, 10f)
            lineTo(7f, 10f)
            close()
            moveTo(13.5f, 5.5f)
            lineTo(16.5f, 5.5f)
            lineTo(16f, 10f)
            lineTo(13f, 10f)
            close()
        }
        chunkyPath(fillCol = Color(0xFFA86B38)) {
            moveTo(5f, 11f)
            horizontalLineTo(19f)
            lineTo(17.5f, 21f)
            curveTo(17.3f, 21.8f, 16.5f, 22.2f, 15.5f, 22.2f)
            horizontalLineTo(8.5f)
            curveTo(7.5f, 22.2f, 6.7f, 21.8f, 6.5f, 21f)
            close()
        }
        chunkyPath(strkCol = DarkOutlCol, strokeWidth = 1.2f) {
            moveTo(5.5f, 15f)
            lineTo(18.5f, 15f)
            moveTo(6f, 18.5f)
            lineTo(18f, 18.5f)
            moveTo(12f, 11f)
            lineTo(12f, 22f)
        }
}.build()

/** Trainer icon. */
internal fun buildTrnr(): ImageVector = icoBldr("Trainer").apply {
        chunkyPath(fillCol = Color(0xFFD32F2F)) {
            moveTo(6f, 13f)
            curveTo(6f, 7f, 18f, 7f, 18f, 13f)
            close()
        }
        chunkyPath(fillCol = Color(0xFFB71C1C)) {
            moveTo(16f, 12f)
            lineTo(23f, 13.5f)
            lineTo(17f, 15f)
            close()
        }
        chunkyPath(fillCol = Color(0xFFF5C542)) {
            moveTo(8f, 16f)
            lineTo(13f, 16f)
            curveTo(15f, 16f, 16f, 17.5f, 16f, 19f)
            curveTo(16f, 20.5f, 14.5f, 21.5f, 13f, 21.5f)
            curveTo(11.5f, 21.5f, 10.5f, 20.5f, 10.5f, 19.5f)
            lineTo(8f, 19.5f)
            close()
        }
        chunkyPath(strkCol = DarkOutlCol, strokeWidth = 1.2f) {
            moveTo(8f, 16f)
            lineTo(8f, 19.5f)
            moveTo(11.5f, 13f)
            curveTo(11.5f, 14.5f, 12.5f, 15.5f, 12.5f, 16f)
        }
}.build()

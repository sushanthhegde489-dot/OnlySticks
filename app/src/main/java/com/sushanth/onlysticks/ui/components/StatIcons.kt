// Vector icons for stats.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/** Heart icon (HP). */
internal fun buildHeart(): ImageVector = icoBldr("Heart").apply {
        chunkyPath(fillCol = Color(0xFFE5533D)) {
            moveTo(12f, 21.5f)
            curveTo(6f, 17f, 3f, 13.5f, 3f, 9f)
            curveTo(3f, 5.5f, 5.8f, 3.5f, 9f, 3.5f)
            curveTo(10.8f, 3.5f, 12f, 4.8f, 12f, 5.5f)
            curveTo(12f, 4.8f, 13.2f, 3.5f, 15f, 3.5f)
            curveTo(18.2f, 3.5f, 21f, 5.5f, 21f, 9f)
            curveTo(21f, 13.5f, 18f, 17f, 12f, 21.5f)
            close()
        }
        chunkyPath(strkCol = Color(0xFFFFD23F), strokeWidth = 1.5f) {
            moveTo(6f, 8f)
            curveTo(6f, 6.5f, 7.5f, 5.5f, 9f, 5.5f)
        }
}.build()

/** Lightning bolt icon (stamina). */
internal fun buildBolt(): ImageVector = icoBldr("Bolt").apply {
        chunkyPath(fillCol = Color(0xFF3AB6C9)) {
            moveTo(13.5f, 2.5f)
            lineTo(6.5f, 12f)
            lineTo(11.5f, 12f)
            lineTo(9.5f, 21.5f)
            lineTo(18.5f, 10.5f)
            lineTo(13.5f, 10.5f)
            lineTo(15.5f, 2.5f)
            close()
        }
}.build()

/** Shield icon (durability / defence). */
internal fun buildShield(): ImageVector = icoBldr("Shield").apply {
        chunkyPath(fillCol = Color(0xFF7A4A24)) {
            moveTo(5f, 4.5f)
            lineTo(19f, 4.5f)
            curveTo(19.5f, 11f, 17f, 17.5f, 12f, 21.5f)
            curveTo(7f, 17.5f, 4.5f, 11f, 5f, 4.5f)
            close()
        }
        chunkyPath(strkCol = DarkOutlCol, strokeWidth = 1.3f) {
            moveTo(12f, 5.5f)
            lineTo(12f, 20f)
            moveTo(7.5f, 9f)
            lineTo(16.5f, 9f)
        }
}.build()

/** Speed icon. */
internal fun buildSpd(): ImageVector = icoBldr("Speed").apply {
        chunkyPath(fillCol = Color(0xFF3AB6C9)) {
            moveTo(7f, 9f)
            horizontalLineTo(11.5f)
            verticalLineTo(13.5f)
            lineTo(16f, 14f)
            curveTo(18.5f, 14.5f, 19.5f, 16.5f, 18.5f, 19f)
            curveTo(18f, 20f, 17f, 20.5f, 15.5f, 20.5f)
            lineTo(6.5f, 20.5f)
            curveTo(5f, 20.5f, 4.5f, 19f, 5f, 17.5f)
            lineTo(6f, 13f)
            lineTo(7f, 9f)
            close()
        }
        chunkyPath(fillCol = Color(0xFF3AB6C9)) {
            moveTo(9f, 9f)
            curveTo(7f, 5f, 3f, 5f, 2.5f, 6.5f)
            curveTo(4.5f, 8.5f, 6.5f, 10f, 8.5f, 11.5f)
            close()
            moveTo(8.5f, 11.5f)
            curveTo(6f, 10.5f, 3.5f, 11f, 3f, 12.5f)
            curveTo(5f, 13.5f, 7f, 13.5f, 9f, 13.5f)
            close()
        }
        chunkyPath(strkCol = DarkOutlCol, strokeWidth = 1.4f) {
            moveTo(19f, 12f)
            lineTo(22.5f, 12f)
            moveTo(19.5f, 15f)
            lineTo(23f, 15f)
        }
}.build()

/** Attack icon. */
internal fun buildAtk(): ImageVector = icoBldr("Attack").apply {
        chunkyPath(fillCol = Color(0xFFA86B38)) {
            moveTo(4.5f, 18.5f)
            lineTo(10f, 13f)
            curveTo(11f, 11f, 13f, 8f, 15.5f, 5.5f)
            curveTo(17.5f, 3.5f, 20.5f, 4.5f, 21f, 7f)
            curveTo(21f, 9.5f, 18.5f, 12.5f, 16f, 14.5f)
            lineTo(11f, 19f)
            curveTo(9.5f, 20.5f, 6f, 21.5f, 4f, 20f)
            curveTo(3.2f, 19.2f, 3.8f, 18f, 4.5f, 18.5f)
            close()
        }
        chunkyPath(strkCol = DarkOutlCol, strokeWidth = 1.3f) {
            moveTo(15f, 7.5f)
            curveTo(16.5f, 8.5f, 18f, 8f, 19f, 6.5f)
            moveTo(7.5f, 17f)
            lineTo(9f, 18.5f)
        }
}.build()

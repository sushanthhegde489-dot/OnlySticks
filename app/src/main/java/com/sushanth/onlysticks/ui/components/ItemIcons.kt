package com.sushanth.onlysticks.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

internal fun buildCoin(): ImageVector = icoBldr("Coin").apply {
        chunkyPath(fillCol = Color(0xFFA8790F)) {
            moveTo(12f, 3.5f)
            curveTo(16.7f, 3.5f, 20.5f, 7.3f, 20.5f, 12f)
            curveTo(20.5f, 16.7f, 16.7f, 20.5f, 12f, 20.5f)
            curveTo(7.3f, 20.5f, 3.5f, 16.7f, 3.5f, 12f)
            curveTo(3.5f, 7.3f, 7.3f, 3.5f, 12f, 3.5f)
            close()
        }
        chunkyPath(fillCol = Color(0xFFF5C542)) {
            moveTo(12f, 5.5f)
            curveTo(15.6f, 5.5f, 18.5f, 8.4f, 18.5f, 12f)
            curveTo(18.5f, 15.6f, 15.6f, 18.5f, 12f, 18.5f)
            curveTo(8.4f, 18.5f, 5.5f, 15.6f, 5.5f, 12f)
            curveTo(5.5f, 8.4f, 8.4f, 5.5f, 12f, 5.5f)
            close()
        }
        chunkyPath(fillCol = Color(0xFFE8B84A)) {
            moveTo(12f, 7.5f)
            curveTo(14.5f, 7.5f, 16.5f, 9.5f, 16.5f, 12f)
            curveTo(16.5f, 14.5f, 14.5f, 16.5f, 12f, 16.5f)
            curveTo(9.5f, 16.5f, 7.5f, 14.5f, 7.5f, 12f)
            curveTo(7.5f, 9.5f, 9.5f, 7.5f, 12f, 7.5f)
            close()
        }
        chunkyPath(strkCol = DarkOutlCol, strokeWidth = 1.2f) {
            moveTo(12f, 8.5f)
            lineTo(12f, 15.5f)
            moveTo(9.5f, 12f)
            lineTo(14.5f, 12f)
        }
}.build()

internal fun buildFlame(): ImageVector = icoBldr("Flame").apply {
        chunkyPath(fillCol = Color(0xFFFF7A2F)) {
            moveTo(12f, 3f)
            curveTo(14f, 6.5f, 18f, 9.5f, 18.5f, 14f)
            curveTo(19f, 18f, 16f, 21.5f, 12f, 21.5f)
            curveTo(8f, 21.5f, 5f, 18f, 5.5f, 14f)
            curveTo(6f, 10f, 9.5f, 7f, 12f, 3f)
            close()
        }
        chunkyPath(fillCol = Color(0xFFFFD23F)) {
            moveTo(12f, 10f)
            curveTo(13.5f, 12.5f, 15.5f, 14f, 15.5f, 17f)
            curveTo(15.5f, 19f, 14f, 20.5f, 12f, 20.5f)
            curveTo(10f, 20.5f, 8.5f, 19f, 8.5f, 17f)
            curveTo(8.5f, 14f, 10.5f, 12.5f, 12f, 10f)
            close()
        }
}.build()

internal fun buildDrop(): ImageVector = icoBldr("Drop").apply {
        chunkyPath(fillCol = Color(0xFF4FB3E8)) {
            moveTo(12f, 3.5f)
            curveTo(15f, 8f, 19f, 12.5f, 19f, 16f)
            curveTo(19f, 19.8f, 15.8f, 21.5f, 12f, 21.5f)
            curveTo(8.2f, 21.5f, 5f, 19.8f, 5f, 16f)
            curveTo(5f, 12.5f, 9f, 8f, 12f, 3.5f)
            close()
        }
        chunkyPath(strkCol = Color.White, strokeWidth = 1.5f) {
            moveTo(8.5f, 13f)
            curveTo(8.5f, 11f, 10f, 8.5f, 11.5f, 7f)
        }
}.build()

internal fun buildBrkn(): ImageVector = icoBldr("Broken").apply {
        chunkyPath(fillCol = Color(0xFFA86B38)) {
            moveTo(3.5f, 18f)
            lineTo(9.5f, 10.5f)
            lineTo(11.5f, 12f)
            lineTo(10.5f, 13.5f)
            lineTo(12f, 14.5f)
            lineTo(5.5f, 20.5f)
            close()
        }
        chunkyPath(fillCol = Color(0xFFA86B38)) {
            moveTo(13f, 9.5f)
            lineTo(14.5f, 8f)
            lineTo(20.5f, 14.5f)
            lineTo(18.5f, 16.5f)
            lineTo(13.5f, 11.5f)
            lineTo(14.5f, 10.5f)
            close()
        }
        chunkyPath(strkCol = DarkOutlCol, strokeWidth = 1.4f) {
            moveTo(10.5f, 8f)
            lineTo(11.5f, 6.5f)
            moveTo(12.5f, 7.5f)
            lineTo(14f, 6f)
        }
}.build()

internal fun buildStar(): ImageVector = icoBldr("Star").apply {
        chunkyPath(fillCol = Color(0xFFFFD23F)) {
            moveTo(12f, 2.5f)
            lineTo(14.5f, 8.5f)
            lineTo(21f, 9f)
            lineTo(16f, 13.5f)
            lineTo(17.8f, 20.5f)
            lineTo(12f, 17f)
            lineTo(6.2f, 20.5f)
            lineTo(8f, 13.5f)
            lineTo(3f, 9f)
            lineTo(9.5f, 8.5f)
            close()
        }
}.build()

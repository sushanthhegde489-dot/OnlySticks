package com.sushanth.onlysticks.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object GameIcos {
    val Jab: ImageVector by lazy { buildJab() }
    val Lunge: ImageVector by lazy { buildLunge() }
    val Parry: ImageVector by lazy { buildParry() }
    val Slash: ImageVector by lazy { buildSlash() }
    val Rest: ImageVector by lazy { buildRest() }
    val ExhStrike: ImageVector by lazy { buildExhStrike() }
    val Coin: ImageVector by lazy { buildCoin() }
    val Flame: ImageVector by lazy { buildFlame() }
    val Drop: ImageVector by lazy { buildDrop() }
    val Cards: ImageVector by lazy { buildCards() }
    val Battle: ImageVector by lazy { buildBattle() }
    val Shop: ImageVector by lazy { buildShop() }
    val Heart: ImageVector by lazy { buildHeart() }
    val Bolt: ImageVector by lazy { buildBolt() }
    val Shield: ImageVector by lazy { buildShield() }
    val Spd: ImageVector by lazy { buildSpd() }
    val Atk: ImageVector by lazy { buildAtk() }
    val Trnr: ImageVector by lazy { buildTrnr() }
    val Brkn: ImageVector by lazy { buildBrkn() }
    val Star: ImageVector by lazy { buildStar() }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true)
@Composable
fun GameIcosPrvw() {
    val icos = listOf(
        "Jab" to GameIcos.Jab,
        "Lunge" to GameIcos.Lunge,
        "Parry" to GameIcos.Parry,
        "Slash" to GameIcos.Slash,
        "Rest" to GameIcos.Rest,
        "ExhaustionStrike" to GameIcos.ExhStrike,
        "Coin" to GameIcos.Coin,
        "Flame" to GameIcos.Flame,
        "Drop" to GameIcos.Drop,
        "Cards" to GameIcos.Cards,
        "Battle" to GameIcos.Battle,
        "Shop" to GameIcos.Shop,
        "Heart" to GameIcos.Heart,
        "Bolt" to GameIcos.Bolt,
        "Shield" to GameIcos.Shield,
        "Speed" to GameIcos.Spd,
        "Attack" to GameIcos.Atk,
        "Trainer" to GameIcos.Trnr,
        "Broken" to GameIcos.Brkn,
        "Star" to GameIcos.Star,
    )

    Box(
        modifier = Modifier
            .background(Color(0xFF7A4A24))
            .padding(16.dp)
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            icos.forEach { (name, icon) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = name,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = name,
                        color = Color(0xFFFFF3D6),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

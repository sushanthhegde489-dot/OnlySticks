package com.sushanth.onlysticks.ui.shop

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.sushanth.onlysticks.audio.SoundFx
import com.sushanth.onlysticks.audio.SoundMgr
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.domain.model.Card
import com.sushanth.onlysticks.ui.card.GameCard
import com.sushanth.onlysticks.ui.components.ChunkyBtn
import com.sushanth.onlysticks.ui.components.OutlinedTxt
import com.sushanth.onlysticks.ui.theme.GameTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PackShow(cardList: List<Card>, onDone: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(remember { MutableInteractionSource() }, indication = null) { },
        contentAlignment = Alignment.Center,
    ) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
            OutlinedTxt(stringResource(R.string.shop_show_title), style = MaterialTheme.typography.headlineMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                cardList.forEachIndexed { i, card -> FlipCard(card, i) }
            }
            ChunkyBtn(
                text = stringResource(R.string.shop_collect),
                onClick = onDone,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
internal fun FlipCard(card: Card, index: Int) {
    val context = LocalContext.current
    val soundMgr = remember { SoundMgr.get(context) }
    val turn = remember(card.id) { Animatable(0f) }
    LaunchedEffect(card.id) {
        delay(300L + index * 280L)
        soundMgr.play(SoundFx.PACK_OPEN, volume = 0.7f, rate = 1.0f + index * 0.05f)
        turn.animateTo(180f, tween(450))
    }
    val c = GameTheme.colors
    Box(Modifier.graphicsLayer { rotationY = turn.value; cameraDistance = 12 * density }) {
        if (turn.value < 90f) {
            Box(
                Modifier.width(88.dp).aspectRatio(0.72f).clip(RoundedCornerShape(10.dp)).background(c.woodDark),
                contentAlignment = Alignment.Center,
            ) {
                OutlinedTxt("?", style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center)
            }
        } else {
            GameCard(card, width = 88.dp, modifier = Modifier.graphicsLayer { rotationY = 180f })
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun PackShowPrvw() {
    val sampleStick = com.sushanth.onlysticks.domain.model.Stick(
        "s1", com.sushanth.onlysticks.domain.model.Rarity.RARE, com.sushanth.onlysticks.domain.model.BaseStats(100, 50, 40)
    )
    com.sushanth.onlysticks.ui.theme.OnlySticksTheme {
        PackShow(cardList = listOf(sampleStick), onDone = {})
    }
}


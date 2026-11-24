package com.sushanth.onlysticks.ui.onboarding

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.ui.components.BtnStyle
import com.sushanth.onlysticks.ui.components.ChunkyBtn
import com.sushanth.onlysticks.ui.components.GameIcos
import com.sushanth.onlysticks.ui.components.Medallion
import com.sushanth.onlysticks.ui.components.OutlinedTxt
import com.sushanth.onlysticks.ui.components.WoodPanel
import com.sushanth.onlysticks.ui.theme.GameTheme
import kotlinx.coroutines.launch

private data class TutPage(val icon: ImageVector, @StringRes val title: Int, @StringRes val body: Int)

private val pages = listOf(
    TutPage(GameIcos.Cards, R.string.tut_deck_title, R.string.tut_deck_body),
    TutPage(GameIcos.Battle, R.string.tut_rounds_title, R.string.tut_rounds_body),
    TutPage(GameIcos.Parry, R.string.tut_moves_title, R.string.tut_moves_body),
    TutPage(GameIcos.Flame, R.string.tut_items_title, R.string.tut_items_body),
    TutPage(GameIcos.Shop, R.string.tut_collect_title, R.string.tut_collect_body),
    TutPage(GameIcos.Trnr, R.string.tut_friends_title, R.string.tut_friends_body),
)

@Composable
fun TutScr(onDone: () -> Unit, modifier: Modifier = Modifier) {
    val pager = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    val c = GameTheme.colors
    val last = pager.currentPage == pages.lastIndex
    Column(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTxt(stringResource(R.string.tut_title), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            if (!last) {
                ChunkyBtn(
                    stringResource(R.string.tut_skip), onDone, Modifier,
                    style = BtnStyle.Wood, minHeight = 40.dp, textStyle = MaterialTheme.typography.titleSmall,
                )
            }
        }
        HorizontalPager(pager, Modifier.weight(1f).fillMaxWidth()) { index ->
            val page = pages[index]
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                WoodPanel(Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Medallion(size = 96.dp, fill = c.leaf) {
                            Icon(page.icon, contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(56.dp))
                        }
                        OutlinedTxt(stringResource(page.title), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                        Text(stringResource(page.body), style = MaterialTheme.typography.bodyLarge, color = c.onWood, textAlign = TextAlign.Center)
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
            pages.indices.forEach { i ->
                Box(Modifier.size(if (i == pager.currentPage) 12.dp else 8.dp).clip(CircleShape).background(if (i == pager.currentPage) c.gold else c.woodDark))
            }
        }
        ChunkyBtn(
            text = stringResource(if (last) R.string.tut_done else R.string.tut_next),
            onClick = { if (last) onDone() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun TutScrPrvw() {
    com.sushanth.onlysticks.ui.theme.OnlySticksTheme {
        TutScr(onDone = {})
    }
}


package com.sushanth.onlysticks.ui.arena

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.ui.common.asStr
import com.sushanth.onlysticks.ui.theme.GameTheme

@Composable
internal fun LogTicker(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = GameTheme.colors
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = c.ink,
        maxLines = 1,
        textAlign = TextAlign.Center,
        modifier = modifier
            .clip(CircleShape)
            .background(c.parchment.copy(alpha = 0.92f))
            .border(2.dp, c.woodLight, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
            ,
    )
}

@Composable
internal fun NoticeChip(text: String) {
    val c = GameTheme.colors
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = Color.White,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE5533D))
            .border(2.dp, c.outline, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BattleLogSheet(state: ArenaUiState, onDismiss: () -> Unit) {
    val c = GameTheme.colors
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = c.parchment, contentColor = c.ink) {
        Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp)) {
            Text(stringResource(R.string.arena_log), style = MaterialTheme.typography.headlineSmall, color = c.ink)
            Spacer(Modifier.height(8.dp))
            val listState = rememberLazyListState()
            LaunchedEffect(state.log.size) { if (state.log.isNotEmpty()) listState.scrollToItem(state.log.lastIndex) }
            LazyColumn(state = listState, modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp)) {
                itemsIndexed(state.log) { _, line ->
                    Text(line.asStr(), style = MaterialTheme.typography.bodyMedium, color = c.ink, modifier = Modifier.padding(vertical = 3.dp))
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun DuelLogPrvw() {
    com.sushanth.onlysticks.ui.theme.OnlySticksTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            LogTicker(text = "Player 1 used Jab for 12 damage!", onClick = {})
            NoticeChip(text = "Your turn!")
        }
    }
}


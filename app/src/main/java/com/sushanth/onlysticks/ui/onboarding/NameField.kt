package com.sushanth.onlysticks.ui.onboarding

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.domain.config.Cfg
import com.sushanth.onlysticks.domain.player.DispNameRes
import com.sushanth.onlysticks.ui.components.ChunkyBtn
import com.sushanth.onlysticks.ui.theme.GameTheme
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp

@StringRes
fun DispNameRes.errRes(): Int? = when (this) {
    is DispNameRes.Valid -> null
    DispNameRes.TooShort -> R.string.name_too_short
    DispNameRes.TooLong -> R.string.name_too_long
    DispNameRes.BadCharacters -> R.string.name_bad_chars
}

@Composable
fun NameForm(
    initial: String,
    @StringRes btnTxt: Int,
    onSave: suspend (String) -> DispNameRes,
    modifier: Modifier = Modifier,
    onSaved: () -> Unit = {},
) {
    var text by rememberSaveable { mutableStateOf(initial) }
    var error by rememberSaveable { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()
    val c = GameTheme.colors
    val sbmt: () -> Unit = {
        scope.launch {
            val result = onSave(text)
            error = result.errRes()
            if (error == null) onSaved()
        }
    }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = text,
            onValueChange = { if (it.length <= Cfg.Prof.NAME_MAX + 4) text = it; error = null },
            label = { Text(stringResource(R.string.name_lbl)) },
            supportingText = { Text(error?.let { stringResource(it, Cfg.Prof.NAME_MIN, Cfg.Prof.NAME_MAX) } ?: stringResource(R.string.name_hint)) },
            isError = error != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { sbmt() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = c.ink, unfocusedTextColor = c.ink,
                focusedContainerColor = c.parchment, unfocusedContainerColor = c.parchment,
                focusedLabelColor = c.ink, unfocusedLabelColor = c.inkSoft,
                focusedSupportingTextColor = c.onWood, unfocusedSupportingTextColor = c.onWood,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        ChunkyBtn(
            text = stringResource(btnTxt),
            onClick = sbmt,
            enabled = text.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun NameFormPrvw() {
    com.sushanth.onlysticks.ui.theme.OnlySticksTheme {
        NameForm(
            initial = "StickMaster",
            btnTxt = R.string.acct_save_name,
            onSave = { DispNameRes.Valid(it) },
        )
    }
}


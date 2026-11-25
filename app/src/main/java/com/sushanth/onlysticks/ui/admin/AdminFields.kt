// Small reusable input widgets for the admin screens.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sushanth.onlysticks.ui.theme.GameTheme

/** Styled text field. [numeric] shows the number keyboard. */
@Composable
internal fun AdminTxtField(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier, numeric: Boolean = false) {
    val c = GameTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = { if (!numeric || it.all { ch -> ch.isDigit() || ch == '-' }) onChange(it.take(if (numeric) 9 else 32)) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (numeric) KeyboardType.Number else KeyboardType.Text),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = c.ink, unfocusedTextColor = c.ink,
            focusedContainerColor = c.parchment, unfocusedContainerColor = c.parchment,
            focusedLabelColor = c.ink, unfocusedLabelColor = c.inkSoft,
        ),
        modifier = modifier,
    )
}

/** Row of toggle chips, one per option. */
@Composable
internal fun <T> ChipRow(opts: List<T>, selected: (T) -> Boolean, label: (T) -> String, onToggle: (T) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        opts.forEach { o -> FilterChip(selected = selected(o), onClick = { onToggle(o) }, label = { Text(label(o)) }) }
    }
}

/** Android Studio preview. */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun AdminFieldsPrvw() {
    com.sushanth.onlysticks.ui.theme.OnlySticksTheme {
        androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AdminTxtField(label = "Amount", value = "100", onChange = {})
            ChipRow(
                opts = listOf("Option A", "Option B", "Option C"),
                selected = { it == "Option A" },
                label = { it },
                onToggle = {},
            )
        }
    }
}


// App theme: Material 3 colours plus the game's own palette.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Provides the current [GameCols] to composables. */
private val LocalGameCols = staticCompositionLocalOf { DayGameCols }

/** Builds a Material colour scheme from the game palette. */
private fun schemeFor(c: GameCols, dark: Boolean) = if (dark) {
    darkColorScheme(
        primary = c.leafLight, onPrimary = c.outline,
        secondary = c.amber, onSecondary = c.outline,
        tertiary = c.gold, onTertiary = c.outline,
        background = c.meadowBottom, onBackground = c.ink,
        surface = c.parchment, onSurface = c.ink,
        surfaceVariant = c.parchmentDeep, onSurfaceVariant = c.inkSoft,
        outline = c.woodLight, error = Color(0xFFFFB4A6), onError = c.outline,
    )
} else {
    lightColorScheme(
        primary = c.leafDark, onPrimary = Color.White,
        secondary = c.amberDark, onSecondary = Color.White,
        tertiary = c.goldDark, onTertiary = Color.White,
        background = c.meadowTop, onBackground = c.ink,
        surface = c.parchment, onSurface = c.ink,
        surfaceVariant = c.parchmentDeep, onSurfaceVariant = c.inkSoft,
        outline = c.wood, error = Color(0xFFB3261E), onError = Color.White,
    )
}

/** Wraps content in the game's theme; switches to the night palette in dark mode. */
@Composable
fun OnlySticksTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) NightGameCols else DayGameCols
    CompositionLocalProvider(LocalGameCols provides colors) {
        MaterialTheme(
            colorScheme = schemeFor(colors, darkTheme),
            typography = OnlySticksTypography,
            content = content,
        )
    }
}

/** Access to the game palette from any composable: GameTheme.colors. */
object GameTheme {
    val colors: GameCols
        @Composable @ReadOnlyComposable get() = LocalGameCols.current
}

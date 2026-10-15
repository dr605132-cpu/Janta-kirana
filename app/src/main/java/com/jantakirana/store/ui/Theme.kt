package com.jantakirana.store.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val PrimaryRed = Color(0xFFD32F2F)
val TextWhite = Color(0xFFFFFFFF)
val TextGrey = Color(0xFFAAAAAA)
val SuccessGreen = Color(0xFF388E3C)
val WarningYellow = Color(0xFFFBC02D)

private val DarkColorScheme = darkColorScheme(primary = PrimaryRed, background = DarkBackground, surface = DarkSurface, onPrimary = TextWhite, onBackground = TextWhite, onSurface = TextWhite, error = PrimaryRed, tertiary = SuccessGreen, secondary = WarningYellow)

@Composable
fun JantaKiranaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColorScheme, content = content)
}

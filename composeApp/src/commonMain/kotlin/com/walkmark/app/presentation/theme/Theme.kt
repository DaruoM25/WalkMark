package com.walkmark.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xF81C784),
    secondary = Color(0xA5D6A7),
    tertiary = Color(0xC8E6C9),
    background = Color(0x121212),
    surface = Color(0x1E1E1E)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0x2E7D32),
    secondary = Color(0x388E3C),
    tertiary = Color(0x4CAF50),
    background = Color(0xFAFAFA),
    surface = Color(0xFFFFFF)
)

@Composable
fun WalkMarkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
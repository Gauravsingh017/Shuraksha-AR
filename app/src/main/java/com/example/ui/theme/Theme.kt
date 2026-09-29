package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SurakshaColorScheme = darkColorScheme(
    primary = Amber,
    onPrimary = Color(0xFF141414),
    secondary = Steel,
    onSecondary = Ink,
    tertiary = Safe,
    onTertiary = Color.White,
    background = Graphite,
    onBackground = Ink,
    surface = Graphite2,
    onSurface = Ink,
    surfaceVariant = Panel,
    onSurfaceVariant = InkDim,
    error = Danger,
    onError = Color.White,
    outline = Hairline
)

@Composable
fun SurakshaTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = SurakshaColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) = SurakshaTheme(content = content)

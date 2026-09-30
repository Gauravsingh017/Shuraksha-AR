package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

val SurakshaLightColorScheme = lightColorScheme(
    primary = SafetyPrimaryContainer, // #F97316
    onPrimary = SafetyOnPrimary,
    primaryContainer = SafetyPrimary,
    onPrimaryContainer = SafetyOnPrimaryContainer,
    secondary = SafetySecondary,      // #006398
    onSecondary = SafetyOnSecondary,
    secondaryContainer = SafetySecondaryFixed,
    onSecondaryContainer = SafetyOnSecondaryContainer,
    tertiary = SafetyTertiary,       // #006E2F
    onTertiary = SafetyOnTertiary,
    tertiaryContainer = SafetyTertiaryContainer, // #00B251
    background = SafetyBackground,   // #EBF0F6 soft industrial slate
    onBackground = SafetyOnBackground,
    surface = SafetySurface,         // #FFFFFF crisp white cards
    onSurface = SafetyOnSurface,     // #131B2E
    surfaceVariant = SafetySurfaceContainerLow, // #F8FAFC
    onSurfaceVariant = SafetyOnSurfaceVariant,
    error = SafetyError,
    onError = SafetyOnError,
    errorContainer = SafetyErrorContainer,
    onErrorContainer = SafetyOnErrorContainer,
    outline = SafetyOutlineVariant,  // #CBD5E1 crisp card border
    outlineVariant = SafetySurfaceContainerHighest
)

@Composable
fun SurakshaTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = SurakshaLightColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) = SurakshaTheme(content = content)

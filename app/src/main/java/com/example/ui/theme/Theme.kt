package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PipOSColorScheme = darkColorScheme(
    primary = PipPhosphorGreen,
    onPrimary = PipPrimaryBg,
    primaryContainer = PipElevatedPanel,
    onPrimaryContainer = PipSecondaryGreen,
    secondary = PipSecondaryGreen,
    onSecondary = PipPrimaryBg,
    secondaryContainer = PipPanelBg,
    onSecondaryContainer = PipTextMain,
    tertiary = PipInfoCyan,
    onTertiary = PipPrimaryBg,
    background = PipPrimaryBg,
    onBackground = PipTextMain,
    surface = PipPanelBg,
    onSurface = PipTextMain,
    surfaceVariant = PipElevatedPanel,
    onSurfaceVariant = PipTextSecondary,
    outline = PipBorderGreen,
    outlineVariant = PipDarkBorderGreen,
    error = PipCriticalRed,
    onError = PipPrimaryBg
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PipOSColorScheme,
        typography = Typography,
        content = content
    )
}

package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Monospace typography for terminal Pip-Boy aesthetic
val PipMonospace = FontFamily.Monospace

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = PipMonospace,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 1.sp,
        color = PipPhosphorGreen
    ),
    displayMedium = TextStyle(
        fontFamily = PipMonospace,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 1.sp,
        color = PipPhosphorGreen
    ),
    headlineMedium = TextStyle(
        fontFamily = PipMonospace,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.8.sp,
        color = PipPhosphorGreen
    ),
    titleLarge = TextStyle(
        fontFamily = PipMonospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.6.sp,
        color = PipTextMain
    ),
    titleMedium = TextStyle(
        fontFamily = PipMonospace,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp,
        color = PipSecondaryGreen
    ),
    bodyLarge = TextStyle(
        fontFamily = PipMonospace,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.4.sp,
        color = PipTextMain
    ),
    bodyMedium = TextStyle(
        fontFamily = PipMonospace,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.3.sp,
        color = PipTextSecondary
    ),
    labelLarge = TextStyle(
        fontFamily = PipMonospace,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.8.sp,
        color = PipPhosphorGreen
    ),
    labelMedium = TextStyle(
        fontFamily = PipMonospace,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp,
        color = PipTextSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = PipMonospace,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        letterSpacing = 0.4.sp,
        color = PipTextDisabled
    )
)

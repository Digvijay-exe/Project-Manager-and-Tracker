package com.example.pipos.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PipAmberWarning
import com.example.ui.theme.PipBorderGreen
import com.example.ui.theme.PipCriticalRed
import com.example.ui.theme.PipMonospace
import com.example.ui.theme.PipPhosphorGreen
import com.example.ui.theme.PipTextDisabled

enum class TerminalButtonVariant {
    PRIMARY,
    SECONDARY,
    WARNING,
    DANGER
}

@Composable
fun TerminalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: TerminalButtonVariant = TerminalButtonVariant.PRIMARY,
    enabled: Boolean = true,
    testTag: String = "terminal_btn"
) {
    val accentColor = when {
        !enabled -> PipTextDisabled
        variant == TerminalButtonVariant.PRIMARY -> PipPhosphorGreen
        variant == TerminalButtonVariant.SECONDARY -> PipBorderGreen
        variant == TerminalButtonVariant.WARNING -> PipAmberWarning
        variant == TerminalButtonVariant.DANGER -> PipCriticalRed
        else -> PipBorderGreen
    }

    val backgroundColor = when {
        !enabled -> Color.Transparent
        variant == TerminalButtonVariant.PRIMARY -> PipPhosphorGreen.copy(alpha = 0.12f)
        variant == TerminalButtonVariant.DANGER -> PipCriticalRed.copy(alpha = 0.12f)
        variant == TerminalButtonVariant.WARNING -> PipAmberWarning.copy(alpha = 0.12f)
        else -> PipBorderGreen.copy(alpha = 0.08f)
    }

    Box(
        modifier = modifier
            .testTag(testTag)
            .defaultMinSize(minWidth = 48.dp, minHeight = 44.dp)
            .background(backgroundColor, shape = RoundedCornerShape(6.dp))
            .border(1.dp, accentColor.copy(alpha = if (enabled) 0.6f else 0.3f), shape = RoundedCornerShape(6.dp))
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = accentColor),
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = PipMonospace,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            letterSpacing = 0.5.sp
        )
    }
}

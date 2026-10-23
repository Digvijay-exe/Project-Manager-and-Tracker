package com.example.pipos.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PipAmberWarning
import com.example.ui.theme.PipBorderGreen
import com.example.ui.theme.PipCriticalRed
import com.example.ui.theme.PipMonospace
import com.example.ui.theme.PipPhosphorGreen
import com.example.ui.theme.PipSecondaryGreen

@Composable
fun TerminalBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = PipPhosphorGreen,
    filled: Boolean = false
) {
    val bgColor = if (filled) color.copy(alpha = 0.16f) else Color.Transparent

    Box(
        modifier = modifier
            .background(bgColor, shape = RoundedCornerShape(4.dp))
            .border(1.dp, color.copy(alpha = 0.5f), shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontFamily = PipMonospace,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            letterSpacing = 0.4.sp
        )
    }
}

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val (color, label) = when (status.uppercase()) {
        "DONE", "COMPLETED", "RESOLVED", "PASSED" -> PipPhosphorGreen to status
        "IN_PROGRESS", "MITIGATING" -> PipSecondaryGreen to status
        "BLOCKED", "HIGH", "CRITICAL", "FAILED" -> PipCriticalRed to status
        "WARNING", "MEDIUM", "ON_HOLD", "PENDING" -> PipAmberWarning to status
        else -> PipBorderGreen to status
    }
    TerminalBadge(text = label, color = color, filled = true, modifier = modifier)
}

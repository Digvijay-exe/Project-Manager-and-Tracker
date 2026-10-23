package com.example.pipos.core.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PipMonospace
import com.example.ui.theme.PipPhosphorGreen

/**
 * Lightweight CRT Scanlines overlay that draws subtle horizontal lines.
 * Doesn't consume touch events.
 */
@Composable
fun CRTScanlineOverlay(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    alpha: Float = 0.05f
) {
    if (!enabled) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val step = 4.dp.toPx()
        val lineCount = (size.height / step).toInt()
        for (i in 0..lineCount) {
            val y = i * step
            drawLine(
                color = Color.Black.copy(alpha = alpha),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

/**
 * Blinking terminal cursor widget (_)
 */
@Composable
fun BlinkingCursor(
    color: Color = PipPhosphorGreen,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor_blink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_alpha"
    )

    Text(
        text = "█",
        color = color.copy(alpha = alpha),
        fontFamily = PipMonospace,
        fontSize = fontSize
    )
}

/**
 * Modifier that draws technical corner brackets: ┌ ┐ └ ┘
 */
fun Modifier.terminalCornerBrackets(
    bracketColor: Color = PipPhosphorGreen,
    bracketLength: Float = 20f,
    strokeWidth: Float = 3f
): Modifier = this.drawBehind {
    val w = size.width
    val h = size.height

    // Top-Left ┌
    drawLine(bracketColor, Offset(0f, 0f), Offset(bracketLength, 0f), strokeWidth)
    drawLine(bracketColor, Offset(0f, 0f), Offset(0f, bracketLength), strokeWidth)

    // Top-Right ┐
    drawLine(bracketColor, Offset(w, 0f), Offset(w - bracketLength, 0f), strokeWidth)
    drawLine(bracketColor, Offset(w, 0f), Offset(w, bracketLength), strokeWidth)

    // Bottom-Left └
    drawLine(bracketColor, Offset(0f, h), Offset(bracketLength, h), strokeWidth)
    drawLine(bracketColor, Offset(0f, h), Offset(0f, h - bracketLength), strokeWidth)

    // Bottom-Right ┘
    drawLine(bracketColor, Offset(w, h), Offset(w - bracketLength, h), strokeWidth)
    drawLine(bracketColor, Offset(w, h), Offset(w, h - bracketLength), strokeWidth)
}

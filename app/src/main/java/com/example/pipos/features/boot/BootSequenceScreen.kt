package com.example.pipos.features.boot

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pipos.core.components.TerminalButton
import com.example.pipos.core.theme.BlinkingCursor
import com.example.pipos.core.theme.terminalCornerBrackets
import com.example.ui.theme.PipBorderGreen
import com.example.ui.theme.PipMonospace
import com.example.ui.theme.PipPanelBg
import com.example.ui.theme.PipPhosphorGreen
import com.example.ui.theme.PipPrimaryBg
import com.example.ui.theme.PipSecondaryGreen
import com.example.ui.theme.PipTextSecondary
import kotlinx.coroutines.delay

@Composable
fun BootSequenceScreen(
    onBootFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bootLines = listOf(
        "PIP-OS KERNEL v2.4.19 (x86_64/arm64-v8a)",
        "INITIALIZING LOCAL SQLITE DATA ENGINE...",
        "VERIFYING ZERO-FABRICATION PROTOCOL...",
        "MOUNTING PROJECT SURVIVAL PROTOCOL WORKSPACE...",
        "CHECKING THREAT MATRICES & RELEASE GATE...",
        "SYSTEM ONLINE. READY FOR COMMANDER INPUT."
    )

    var currentLineIndex by remember { mutableIntStateOf(0) }
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        for (i in bootLines.indices) {
            currentLineIndex = i
            progress = (i + 1).toFloat() / bootLines.size
            delay(400)
        }
        delay(500)
        onBootFinished()
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "boot_progress"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PipPrimaryBg)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(PipPanelBg)
                .border(1.dp, PipBorderGreen)
                .terminalCornerBrackets(bracketColor = PipPhosphorGreen, bracketLength = 24f)
                .padding(24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "PIP-OS",
                fontFamily = PipMonospace,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = PipPhosphorGreen,
                letterSpacing = 2.sp
            )

            Text(
                text = "PROJECT SURVIVAL PROTOCOL",
                fontFamily = PipMonospace,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = PipSecondaryGreen,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Diagnostic boot output lines
            Column(modifier = Modifier.fillMaxWidth()) {
                for (i in 0..currentLineIndex) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "► ${bootLines[i]}",
                            fontFamily = PipMonospace,
                            fontSize = 12.sp,
                            color = if (i == currentLineIndex) PipPhosphorGreen else PipTextSecondary,
                            lineHeight = 18.sp
                        )
                        if (i == currentLineIndex && currentLineIndex < bootLines.size - 1) {
                            Spacer(modifier = Modifier.width(4.dp))
                            BlinkingCursor()
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .border(1.dp, PipBorderGreen),
                color = PipPhosphorGreen,
                trackColor = PipPrimaryBg
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TerminalButton(
                    text = "SKIP BOOT",
                    onClick = onBootFinished,
                    testTag = "skip_boot_btn"
                )
            }
        }
    }
}

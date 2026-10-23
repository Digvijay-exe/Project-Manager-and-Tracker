package com.example.pipos.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PipBorderGreen
import com.example.ui.theme.PipMonospace
import com.example.ui.theme.PipPanelBg
import com.example.ui.theme.PipPhosphorGreen

@Composable
fun TerminalPanel(
    modifier: Modifier = Modifier,
    title: String? = null,
    statusIndicator: String? = null,
    borderColor: Color = PipBorderGreen,
    showCornerAccents: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(PipPanelBg, shape = RoundedCornerShape(8.dp))
            .border(BorderStroke(1.dp, borderColor.copy(alpha = 0.5f)), shape = RoundedCornerShape(8.dp))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (title != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontFamily = PipMonospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PipPhosphorGreen,
                        letterSpacing = 0.5.sp
                    )
                    if (statusIndicator != null) {
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = statusIndicator,
                            fontFamily = PipMonospace,
                            fontSize = 10.sp,
                            color = borderColor
                        )
                    }
                }
            }
            content()
        }
    }
}

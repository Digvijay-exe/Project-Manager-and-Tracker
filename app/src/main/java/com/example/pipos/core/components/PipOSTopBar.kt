package com.example.pipos.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PipAmberWarning
import com.example.ui.theme.PipBorderGreen
import com.example.ui.theme.PipMonospace
import com.example.ui.theme.PipPhosphorGreen
import com.example.ui.theme.PipSecondaryBg
import com.example.ui.theme.PipSecondaryGreen
import com.example.ui.theme.PipTextSecondary

@Composable
fun PipOSTopBar(
    moduleTitle: String,
    activeProjectName: String? = null,
    operatorName: String? = null,
    isCloudConnected: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(PipSecondaryBg)
            .border(width = 1.dp, color = PipBorderGreen.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Text(
                text = "PIP-OS",
                fontFamily = PipMonospace,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = PipPhosphorGreen,
                letterSpacing = 1.sp
            )
            Text(
                text = " / ",
                fontFamily = PipMonospace,
                fontSize = 13.sp,
                color = PipBorderGreen
            )
            Text(
                text = activeProjectName ?: moduleTitle,
                fontFamily = PipMonospace,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = PipSecondaryGreen,
                maxLines = 1
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = if (isCloudConnected) PipPhosphorGreen else PipAmberWarning,
                        shape = CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isCloudConnected) (operatorName ?: "CLOUD") else "LOCAL",
                fontFamily = PipMonospace,
                fontSize = 11.sp,
                color = PipTextSecondary,
                maxLines = 1
            )
        }
    }
}

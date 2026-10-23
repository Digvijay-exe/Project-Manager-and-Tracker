package com.example.pipos.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PipBorderGreen
import com.example.ui.theme.PipMonospace
import com.example.ui.theme.PipPhosphorGreen
import com.example.ui.theme.PipSecondaryBg
import com.example.ui.theme.PipTextDisabled

enum class PipOSScreen(val label: String, val icon: ImageVector) {
    HOME("Dashboard", Icons.Default.Dashboard),
    PROJECTS("Projects", Icons.Default.Folder),
    SIM("Simulator", Icons.Default.PlayArrow),
    SYSTEM("System", Icons.Default.Settings)
}

@Composable
fun PipOSBottomNavigation(
    currentScreen: PipOSScreen,
    onScreenSelected: (PipOSScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(PipSecondaryBg)
            .border(width = 1.dp, color = PipBorderGreen.copy(alpha = 0.4f))
            .navigationBarsPadding()
            .height(56.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PipOSScreen.entries.forEach { screen ->
            val isSelected = screen == currentScreen
            val contentColor = if (isSelected) PipPhosphorGreen else PipTextDisabled
            val bg = if (isSelected) PipPhosphorGreen.copy(alpha = 0.12f) else Color.Transparent

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .padding(horizontal = 4.dp)
                    .background(bg, shape = RoundedCornerShape(8.dp))
                    .testTag("nav_tab_${screen.name.lowercase()}")
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = PipPhosphorGreen),
                        onClick = { onScreenSelected(screen) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.label,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = screen.label,
                        fontFamily = PipMonospace,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = contentColor
                    )
                }
            }
        }
    }
}

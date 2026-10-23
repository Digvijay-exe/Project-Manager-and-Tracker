package com.example.pipos.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PipBorderGreen
import com.example.ui.theme.PipMonospace
import com.example.ui.theme.PipPhosphorGreen
import com.example.ui.theme.PipTextSecondary

@Composable
fun TerminalEmptyState(
    title: String = "NO ACTIVE OPERATIONS",
    message: String = "Your workspace is waiting for its first project.",
    actionLabel: String? = "INITIALIZE FIRST PROJECT",
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    TerminalPanel(
        modifier = modifier.fillMaxWidth(),
        title = "SYS.EMPTY_STATE"
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "◄ [ SYSTEM OFFLINE / STANDBY ] ►",
                fontFamily = PipMonospace,
                fontSize = 11.sp,
                color = PipBorderGreen,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontFamily = PipMonospace,
                fontSize = 16.sp,
                color = PipPhosphorGreen,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontFamily = PipMonospace,
                fontSize = 12.sp,
                color = PipTextSecondary,
                lineHeight = 18.sp
            )
            if (actionLabel != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(18.dp))
                TerminalButton(
                    text = actionLabel,
                    onClick = onActionClick,
                    variant = TerminalButtonVariant.PRIMARY,
                    testTag = "empty_state_action_btn"
                )
            }
        }
    }
}

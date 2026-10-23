package com.example.pipos.features.risks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pipos.core.components.StatusBadge
import com.example.pipos.core.components.TerminalBadge
import com.example.pipos.core.components.TerminalButton
import com.example.pipos.core.components.TerminalButtonVariant
import com.example.pipos.core.components.TerminalEmptyState
import com.example.pipos.core.components.TerminalPanel
import com.example.pipos.data.local.entity.ProjectEntity
import com.example.pipos.data.local.entity.RiskEntity
import com.example.pipos.data.local.entity.TeamMemberEntity
import com.example.pipos.features.projects.CreateRiskDialog
import com.example.ui.theme.PipAmberWarning
import com.example.ui.theme.PipBorderGreen
import com.example.ui.theme.PipCriticalRed
import com.example.ui.theme.PipInfoCyan
import com.example.ui.theme.PipMonospace
import com.example.ui.theme.PipPanelBg
import com.example.ui.theme.PipPhosphorGreen
import com.example.ui.theme.PipPrimaryBg
import com.example.ui.theme.PipSecondaryBg
import com.example.ui.theme.PipSecondaryGreen
import com.example.ui.theme.PipTextDisabled
import com.example.ui.theme.PipTextMain
import com.example.ui.theme.PipTextSecondary

@Composable
fun RiskRegisterScreen(
    risks: List<RiskEntity>,
    projects: List<ProjectEntity>,
    teamMembers: List<TeamMemberEntity>,
    onCreateRisk: (projectId: String, title: String, description: String, likelihood: Int, impact: Int, mitigationPlan: String, ownerId: String?) -> Unit,
    onUpdateRiskStatus: (RiskEntity, String) -> Unit,
    onDeleteRisk: (RiskEntity) -> Unit,
    onNavigateToProjects: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSeverityFilter by remember { mutableStateOf("ALL") }
    var selectedMatrixCell by remember { mutableStateOf<Pair<Int, Int>?>(null) } // (likelihood, impact)
    var showCreateDialog by remember { mutableStateOf(false) }

    val filteredRisks = risks.filter { risk ->
        val matchesSeverity = when (selectedSeverityFilter) {
            "HIGH" -> risk.exposure >= 13
            "MEDIUM" -> risk.exposure in 5..12
            "LOW" -> risk.exposure <= 4
            else -> true
        }
        val matchesCell = if (selectedMatrixCell != null) {
            risk.likelihood == selectedMatrixCell!!.first && risk.impact == selectedMatrixCell!!.second
        } else true

        matchesSeverity && matchesCell
    }

    val totalRisks = risks.size
    val highCount = risks.count { it.exposure >= 13 && it.status != "RESOLVED" }
    val medCount = risks.count { it.exposure in 5..12 && it.status != "RESOLVED" }
    val lowCount = risks.count { it.exposure <= 4 && it.status != "RESOLVED" }
    val resolvedCount = risks.count { it.status == "RESOLVED" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PipPrimaryBg)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "► THREAT MATRIX // RISK REGISTER",
                    fontFamily = PipMonospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PipPhosphorGreen
                )
                if (projects.isNotEmpty()) {
                    TerminalButton(
                        text = "+ LOG THREAT",
                        onClick = { showCreateDialog = true },
                        testTag = "log_threat_btn"
                    )
                }
            }
        }

        // Threat Telemetry Overview
        item {
            TerminalPanel(title = "THREAT TELEMETRY & SEVERITY THRESHOLDS") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ThreatMetric(label = "TOTAL", value = "$totalRisks", color = PipPhosphorGreen, modifier = Modifier.weight(1f))
                    ThreatMetric(label = "HIGH (13-25)", value = "$highCount", color = if (highCount > 0) PipCriticalRed else PipTextSecondary, modifier = Modifier.weight(1f))
                    ThreatMetric(label = "MED (5-12)", value = "$medCount", color = if (medCount > 0) PipAmberWarning else PipTextSecondary, modifier = Modifier.weight(1f))
                    ThreatMetric(label = "LOW (1-4)", value = "$lowCount", color = PipSecondaryGreen, modifier = Modifier.weight(1f))
                    ThreatMetric(label = "RESOLVED", value = "$resolvedCount", color = PipPhosphorGreen, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "EXPOSURE = LIKELIHOOD x IMPACT | DOCUMENTED THRESHOLDS: [LOW: 1-4] [MED: 5-12] [HIGH: 13-25]",
                    fontFamily = PipMonospace,
                    fontSize = 9.sp,
                    color = PipTextSecondary
                )
            }
        }

        // 5x5 Likelihood x Impact Matrix (Responsive for phone)
        item {
            TerminalPanel(
                title = "5x5 THREAT PROBABILITY-IMPACT MATRIX",
                statusIndicator = if (selectedMatrixCell != null) "[FILTER: L${selectedMatrixCell!!.first} x I${selectedMatrixCell!!.second}]" else "[TOUCH CELL TO FILTER]"
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "▲ LIKELIHOOD (PROBABILITY 5 down to 1)",
                        fontFamily = PipMonospace,
                        fontSize = 9.sp,
                        color = PipTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Matrix Grid
                    for (l in 5 downTo 1) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "L$l ",
                                fontFamily = PipMonospace,
                                fontSize = 9.sp,
                                color = PipTextSecondary,
                                modifier = Modifier.width(22.dp)
                            )

                            for (i in 1..5) {
                                val cellExposure = l * i
                                val cellRisks = risks.filter { it.likelihood == l && it.impact == i }
                                val cellCount = cellRisks.size
                                val isSelected = selectedMatrixCell?.first == l && selectedMatrixCell?.second == i

                                val cellColor = when {
                                    cellExposure >= 13 -> PipCriticalRed
                                    cellExposure >= 5 -> PipAmberWarning
                                    else -> PipBorderGreen
                                }

                                val cellBg = when {
                                    isSelected -> cellColor.copy(alpha = 0.5f)
                                    cellCount > 0 -> cellColor.copy(alpha = 0.25f)
                                    else -> PipSecondaryBg
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                        .padding(2.dp)
                                        .background(cellBg, shape = RoundedCornerShape(4.dp))
                                        .border(1.dp, if (isSelected) PipPhosphorGreen else cellColor.copy(alpha = 0.6f), shape = RoundedCornerShape(4.dp))
                                        .clickable {
                                            selectedMatrixCell = if (isSelected) null else (l to i)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (cellCount > 0) {
                                        Text(
                                            text = "$cellCount",
                                            fontFamily = PipMonospace,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (cellExposure >= 13) PipCriticalRed else if (cellExposure >= 5) PipAmberWarning else PipPhosphorGreen
                                        )
                                    } else {
                                        Text(
                                            text = "$cellExposure",
                                            fontFamily = PipMonospace,
                                            fontSize = 8.sp,
                                            color = PipTextDisabled
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "IMPACT SEVERITY (1 to 5) ►",
                        fontFamily = PipMonospace,
                        fontSize = 9.sp,
                        color = PipTextSecondary
                    )

                    if (selectedMatrixCell != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "FILTERED: L=${selectedMatrixCell!!.first}, I=${selectedMatrixCell!!.second} (EXP: ${selectedMatrixCell!!.first * selectedMatrixCell!!.second})",
                                fontFamily = PipMonospace,
                                fontSize = 10.sp,
                                color = PipPhosphorGreen
                            )
                            TerminalButton(
                                text = "CLEAR FILTER",
                                onClick = { selectedMatrixCell = null },
                                variant = TerminalButtonVariant.SECONDARY
                            )
                        }
                    }
                }
            }
        }

        if (projects.isEmpty()) {
            item {
                TerminalEmptyState(
                    title = "ZERO WORKSPACES REGISTERED",
                    message = "Risks are associated with project workspaces. Initialize a project first.",
                    actionLabel = "CREATE WORKSPACE FIRST",
                    onActionClick = onNavigateToProjects
                )
            }
        } else if (filteredRisks.isEmpty()) {
            item {
                TerminalEmptyState(
                    title = "NO THREATS RECORDED",
                    message = if (risks.isEmpty()) "Risk registry is clear. Log potential failure modes and mitigations." else "No threats match current matrix cell filter.",
                    actionLabel = if (risks.isEmpty()) "+ LOG FIRST THREAT" else null,
                    onActionClick = { showCreateDialog = true }
                )
            }
        } else {
            items(filteredRisks, key = { it.id }) { risk ->
                val project = projects.find { it.id == risk.projectId }
                val owner = teamMembers.find { it.id == risk.ownerMemberId }?.name ?: "UNASSIGNED"
                val riskColor = when {
                    risk.exposure >= 13 -> PipCriticalRed
                    risk.exposure >= 5 -> PipAmberWarning
                    else -> PipPhosphorGreen
                }

                TerminalPanel(
                    title = risk.title,
                    borderColor = riskColor,
                    statusIndicator = "[EXP: ${risk.exposure} | ${risk.severityLevel}]"
                ) {
                    Text(
                        text = "PRJ: ${project?.name ?: "UNKNOWN"} | OWNER: $owner",
                        fontFamily = PipMonospace,
                        fontSize = 10.sp,
                        color = PipTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "PROBABILITY: ${risk.likelihood}/5 | IMPACT: ${risk.impact}/5 | SCORE: ${risk.exposure}",
                        fontFamily = PipMonospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = riskColor
                    )
                    if (risk.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "HAZARD: ${risk.description}",
                            fontFamily = PipMonospace,
                            fontSize = 11.sp,
                            color = PipTextMain
                        )
                    }
                    if (risk.mitigationPlan.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "MITIGATION PLAN: ${risk.mitigationPlan}",
                            fontFamily = PipMonospace,
                            fontSize = 10.sp,
                            color = PipSecondaryGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusBadge(status = risk.status)

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (risk.status != "RESOLVED") {
                                TerminalButton(
                                    text = "RESOLVE",
                                    onClick = { onUpdateRiskStatus(risk, "RESOLVED") }
                                )
                            }
                            TerminalButton(
                                text = "DEL",
                                onClick = { onDeleteRisk(risk) },
                                variant = TerminalButtonVariant.DANGER
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog && projects.isNotEmpty()) {
        CreateRiskDialog(
            teamMembers = teamMembers,
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, desc, like, imp, plan, ownerId ->
                val targetProjectId = projects.first().id
                onCreateRisk(targetProjectId, title, desc, like, imp, plan, ownerId)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun ThreatMetric(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(PipSecondaryBg, shape = RoundedCornerShape(6.dp))
            .border(1.dp, PipBorderGreen.copy(alpha = 0.6f), shape = RoundedCornerShape(6.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                fontFamily = PipMonospace,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                fontFamily = PipMonospace,
                fontSize = 8.sp,
                color = PipTextSecondary,
                maxLines = 1
            )
        }
    }
}

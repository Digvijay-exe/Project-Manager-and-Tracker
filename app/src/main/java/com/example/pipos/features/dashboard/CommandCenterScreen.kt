package com.example.pipos.features.dashboard

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pipos.core.components.StatusBadge
import com.example.pipos.core.components.TerminalBadge
import com.example.pipos.core.components.TerminalButton
import com.example.pipos.core.components.TerminalButtonVariant
import com.example.pipos.core.components.TerminalEmptyState
import com.example.pipos.core.components.TerminalPanel
import com.example.pipos.data.local.entity.MilestoneEntity
import com.example.pipos.data.local.entity.ProjectEntity
import com.example.pipos.data.local.entity.RiskEntity
import com.example.pipos.data.local.entity.TaskEntity
import com.example.ui.theme.PipAmberWarning
import com.example.ui.theme.PipBorderGreen
import com.example.ui.theme.PipCriticalRed
import com.example.ui.theme.PipInfoCyan
import com.example.ui.theme.PipMonospace
import com.example.ui.theme.PipPanelBg
import com.example.ui.theme.PipPhosphorGreen
import com.example.ui.theme.PipPrimaryBg
import com.example.ui.theme.PipSecondaryGreen
import com.example.ui.theme.PipTextSecondary

@Composable
fun CommandCenterScreen(
    projects: List<ProjectEntity>,
    tasks: List<TaskEntity>,
    risks: List<RiskEntity>,
    milestones: List<MilestoneEntity>,
    overdueTasks: List<TaskEntity>,
    onCreateProjectClick: () -> Unit,
    onProjectSelected: (ProjectEntity) -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToRisks: () -> Unit,
    onNavigateToSim: () -> Unit,
    modifier: Modifier = Modifier
) {
    val openTasks = tasks.filter { it.status != "DONE" }
    val highRisks = risks.filter { it.exposure >= 13 && it.status != "RESOLVED" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PipPrimaryBg)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Clean 3-Metric KPI Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    label = "Projects",
                    value = projects.size.toString(),
                    color = PipPhosphorGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "Open Tasks",
                    value = openTasks.size.toString(),
                    color = if (openTasks.isEmpty()) PipTextSecondary else PipInfoCyan,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "High Risks",
                    value = highRisks.size.toString(),
                    color = if (highRisks.isEmpty()) PipSecondaryGreen else PipCriticalRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Action Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TerminalButton(
                    text = "+ New Project",
                    onClick = onCreateProjectClick,
                    modifier = Modifier.weight(1.2f),
                    variant = TerminalButtonVariant.PRIMARY,
                    testTag = "cmd_new_proj_btn"
                )
                TerminalButton(
                    text = "Simulator",
                    onClick = onNavigateToSim,
                    modifier = Modifier.weight(1f),
                    variant = TerminalButtonVariant.SECONDARY,
                    testTag = "cmd_sim_btn"
                )
            }
        }

        // Active Projects Section
        if (projects.isEmpty()) {
            item {
                TerminalEmptyState(
                    title = "No Active Projects",
                    message = "Your workspace is waiting for its first project. Initialize one to start tracking tasks and risks.",
                    actionLabel = "Initialize First Project",
                    onActionClick = onCreateProjectClick
                )
            }
        } else {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Projects (${projects.size})",
                        fontFamily = PipMonospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PipPhosphorGreen
                    )
                }
            }

            items(projects, key = { it.id }) { project ->
                val projectTasks = tasks.filter { it.projectId == project.id }
                val projectDone = projectTasks.count { it.status == "DONE" }
                val projectTotal = projectTasks.size
                val projectProgress = if (projectTotal > 0) projectDone.toFloat() / projectTotal else 0f
                val projectRisks = risks.filter { it.projectId == project.id }
                val projectHighRisks = projectRisks.count { it.exposure >= 13 && it.status != "RESOLVED" }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PipPanelBg, shape = RoundedCornerShape(8.dp))
                        .border(1.dp, PipBorderGreen.copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp))
                        .clickable { onProjectSelected(project) }
                        .padding(14.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = project.name,
                                fontFamily = PipMonospace,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PipPhosphorGreen,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            StatusBadge(status = project.status)
                        }

                        if (project.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = project.description,
                                fontFamily = PipMonospace,
                                fontSize = 11.sp,
                                color = PipTextSecondary,
                                maxLines = 2
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { projectProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp),
                            color = PipPhosphorGreen,
                            trackColor = PipPrimaryBg
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$projectDone/$projectTotal tasks done",
                                fontFamily = PipMonospace,
                                fontSize = 11.sp,
                                color = PipTextSecondary
                            )

                            if (projectHighRisks > 0) {
                                TerminalBadge(
                                    text = "$projectHighRisks critical risk",
                                    color = PipCriticalRed,
                                    filled = true
                                )
                            } else {
                                Text(
                                    text = "Open details ►",
                                    fontFamily = PipMonospace,
                                    fontSize = 11.sp,
                                    color = PipPhosphorGreen
                                )
                            }
                        }
                    }
                }
            }
        }

        // Threat & Overdue Alert (Only shown if anomalies exist)
        if (overdueTasks.isNotEmpty() || highRisks.isNotEmpty()) {
            item {
                TerminalPanel(
                    title = "Attention Required",
                    borderColor = PipCriticalRed
                ) {
                    if (overdueTasks.isNotEmpty()) {
                        Text(
                            text = "• ${overdueTasks.size} overdue task(s)",
                            fontFamily = PipMonospace,
                            fontSize = 11.sp,
                            color = PipAmberWarning
                        )
                    }
                    if (highRisks.isNotEmpty()) {
                        Text(
                            text = "• ${highRisks.size} unmitigated high risk(s)",
                            fontFamily = PipMonospace,
                            fontSize = 11.sp,
                            color = PipCriticalRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(PipPanelBg, shape = RoundedCornerShape(8.dp))
            .border(1.dp, PipBorderGreen.copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                fontFamily = PipMonospace,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                fontFamily = PipMonospace,
                fontSize = 10.sp,
                color = PipTextSecondary,
                maxLines = 1
            )
        }
    }
}

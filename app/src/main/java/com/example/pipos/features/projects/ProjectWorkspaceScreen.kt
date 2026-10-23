package com.example.pipos.features.projects

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.example.pipos.data.local.entity.TeamMemberEntity
import com.example.pipos.data.local.entity.TestCaseEntity
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProjectWorkspaceScreen(
    projects: List<ProjectEntity>,
    activeProject: ProjectEntity?,
    tasks: List<TaskEntity>,
    risks: List<RiskEntity>,
    milestones: List<MilestoneEntity>,
    teamMembers: List<TeamMemberEntity>,
    testCases: List<TestCaseEntity>,
    onSelectProject: (ProjectEntity?) -> Unit,
    onCreateProject: (name: String, description: String, deadlineDate: Long?, budget: Double?) -> Unit,
    onUpdateProject: (ProjectEntity) -> Unit,
    onDeleteProject: (ProjectEntity) -> Unit,
    onCreateTask: (projectId: String, title: String, description: String, priority: String, dueDate: Long?, assignedMemberId: String?, isBlocker: Boolean) -> Unit,
    onUpdateTaskStatus: (TaskEntity, String) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onCreateRisk: (projectId: String, title: String, description: String, likelihood: Int, impact: Int, mitigationPlan: String, ownerId: String?) -> Unit,
    onUpdateRiskStatus: (RiskEntity, String) -> Unit,
    onDeleteRisk: (RiskEntity) -> Unit,
    onCreateMilestone: (projectId: String, title: String, description: String, targetDate: Long) -> Unit,
    onUpdateMilestoneStatus: (MilestoneEntity, String) -> Unit,
    onDeleteMilestone: (MilestoneEntity) -> Unit,
    onCreateTestCase: (projectId: String, name: String, component: String, notes: String) -> Unit,
    onUpdateTestCaseStatus: (TestCaseEntity, String) -> Unit,
    onDeleteTestCase: (TestCaseEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateProjectDialog by remember { mutableStateOf(false) }

    if (activeProject != null) {
        BackHandler { onSelectProject(null) }
        ProjectDossierView(
            project = activeProject,
            allTasks = tasks.filter { it.projectId == activeProject.id },
            allRisks = risks.filter { it.projectId == activeProject.id },
            allMilestones = milestones.filter { it.projectId == activeProject.id },
            allTestCases = testCases.filter { it.projectId == activeProject.id },
            teamMembers = teamMembers,
            onBack = { onSelectProject(null) },
            onUpdateProject = onUpdateProject,
            onDeleteProject = onDeleteProject,
            onCreateTask = onCreateTask,
            onUpdateTaskStatus = onUpdateTaskStatus,
            onDeleteTask = onDeleteTask,
            onCreateRisk = onCreateRisk,
            onUpdateRiskStatus = onUpdateRiskStatus,
            onDeleteRisk = onDeleteRisk,
            onCreateMilestone = onCreateMilestone,
            onUpdateMilestoneStatus = onUpdateMilestoneStatus,
            onDeleteMilestone = onDeleteMilestone,
            onCreateTestCase = onCreateTestCase,
            onUpdateTestCaseStatus = onUpdateTestCaseStatus,
            onDeleteTestCase = onDeleteTestCase,
            modifier = modifier
        )
    } else {
        // Project Overview List
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(PipPrimaryBg)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "► PROJECT DOSSIER REPOSITORY",
                        fontFamily = PipMonospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PipPhosphorGreen
                    )
                    TerminalButton(
                        text = "+ NEW PROJECT",
                        onClick = { showCreateProjectDialog = true },
                        testTag = "workspace_new_proj_btn"
                    )
                }
            }

            if (projects.isEmpty()) {
                item {
                    TerminalEmptyState(
                        title = "ZERO WORKSPACES REGISTERED",
                        message = "No software projects exist in local database. Click below to initialize a managed project workspace.",
                        actionLabel = "INITIALIZE FIRST PROJECT",
                        onActionClick = { showCreateProjectDialog = true }
                    )
                }
            } else {
                items(projects, key = { it.id }) { project ->
                    val projTasks = tasks.filter { it.projectId == project.id }
                    val doneCount = projTasks.count { it.status == "DONE" }
                    val totalTasks = projTasks.size
                    val projRisks = risks.filter { it.projectId == project.id }
                    val highRiskCount = projRisks.count { it.exposure >= 13 && it.status != "RESOLVED" }
                    val progress = if (totalTasks > 0) doneCount.toFloat() / totalTasks else 0f

                    TerminalPanel(
                        title = project.name,
                        statusIndicator = "[${project.status}]"
                    ) {
                        if (project.description.isNotBlank()) {
                            Text(
                                text = project.description,
                                fontFamily = PipMonospace,
                                fontSize = 11.sp,
                                color = PipTextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        // Progress
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .border(1.dp, PipBorderGreen),
                                color = PipPhosphorGreen,
                                trackColor = PipPrimaryBg
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                fontFamily = PipMonospace,
                                fontSize = 11.sp,
                                color = PipSecondaryGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "TASKS: $doneCount/$totalTasks | RISKS: ${projRisks.size}",
                                    fontFamily = PipMonospace,
                                    fontSize = 10.sp,
                                    color = PipTextSecondary
                                )
                                if (project.budget != null) {
                                    Text(
                                        text = "BUDGET: $${project.budget.toInt()}",
                                        fontFamily = PipMonospace,
                                        fontSize = 10.sp,
                                        color = PipInfoCyan
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (highRiskCount > 0) {
                                    TerminalBadge(
                                        text = "! $highRiskCount HIGH",
                                        color = PipCriticalRed,
                                        filled = true
                                    )
                                }
                                TerminalButton(
                                    text = "OPEN",
                                    onClick = { onSelectProject(project) },
                                    testTag = "open_project_${project.id}"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateProjectDialog) {
        CreateProjectDialog(
            onDismiss = { showCreateProjectDialog = false },
            onConfirm = { name, desc, deadline, budget ->
                onCreateProject(name, desc, deadline, budget)
                showCreateProjectDialog = false
            }
        )
    }
}

@Composable
fun ProjectDossierView(
    project: ProjectEntity,
    allTasks: List<TaskEntity>,
    allRisks: List<RiskEntity>,
    allMilestones: List<MilestoneEntity>,
    allTestCases: List<TestCaseEntity>,
    teamMembers: List<TeamMemberEntity>,
    onBack: () -> Unit,
    onUpdateProject: (ProjectEntity) -> Unit,
    onDeleteProject: (ProjectEntity) -> Unit,
    onCreateTask: (projectId: String, title: String, description: String, priority: String, dueDate: Long?, assignedMemberId: String?, isBlocker: Boolean) -> Unit,
    onUpdateTaskStatus: (TaskEntity, String) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onCreateRisk: (projectId: String, title: String, description: String, likelihood: Int, impact: Int, mitigationPlan: String, ownerId: String?) -> Unit,
    onUpdateRiskStatus: (RiskEntity, String) -> Unit,
    onDeleteRisk: (RiskEntity) -> Unit,
    onCreateMilestone: (projectId: String, title: String, description: String, targetDate: Long) -> Unit,
    onUpdateMilestoneStatus: (MilestoneEntity, String) -> Unit,
    onDeleteMilestone: (MilestoneEntity) -> Unit,
    onCreateTestCase: (projectId: String, name: String, component: String, notes: String) -> Unit,
    onUpdateTestCaseStatus: (TestCaseEntity, String) -> Unit,
    onDeleteTestCase: (TestCaseEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("TASKS (${allTasks.size})", "RISKS (${allRisks.size})", "MILESTONES (${allMilestones.size})", "TESTS (${allTestCases.size})")

    var showNewTaskDialog by remember { mutableStateOf(false) }
    var showNewRiskDialog by remember { mutableStateOf(false) }
    var showNewMilestoneDialog by remember { mutableStateOf(false) }
    var showNewTestCaseDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val completedTasks = allTasks.count { it.status == "DONE" }
    val progress = if (allTasks.isNotEmpty()) completedTasks.toFloat() / allTasks.size else 0f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PipPrimaryBg)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header with Back & Delete
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TerminalButton(
                    text = "◄ BACK",
                    onClick = onBack,
                    testTag = "dossier_back_btn"
                )
                TerminalButton(
                    text = "DELETE PRJ",
                    onClick = { showDeleteConfirmDialog = true },
                    variant = TerminalButtonVariant.DANGER,
                    testTag = "delete_project_btn"
                )
            }
        }

        // Project Overview Card
        item {
            TerminalPanel(
                title = "DOSSIER // ${project.name}",
                statusIndicator = "[${project.status}]"
            ) {
                if (project.description.isNotBlank()) {
                    Text(
                        text = project.description,
                        fontFamily = PipMonospace,
                        fontSize = 12.sp,
                        color = PipTextMain
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "START: ${sdf.format(Date(project.startDate))}",
                        fontFamily = PipMonospace,
                        fontSize = 10.sp,
                        color = PipTextSecondary
                    )
                    val deadlineText = project.deadlineDate?.let { sdf.format(Date(it)) } ?: "NOT SET"
                    Text(
                        text = "DEADLINE: $deadlineText",
                        fontFamily = PipMonospace,
                        fontSize = 10.sp,
                        color = if (project.deadlineDate != null) PipAmberWarning else PipTextDisabled
                    )
                }

                if (project.budget != null) {
                    Text(
                        text = "BUDGET ALLOCATION: $${project.budget.toInt()}",
                        fontFamily = PipMonospace,
                        fontSize = 10.sp,
                        color = PipInfoCyan
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .border(1.dp, PipBorderGreen),
                    color = PipPhosphorGreen,
                    trackColor = PipPrimaryBg
                )
                Text(
                    text = "EXECUTION PROGRESS: ${(progress * 100).toInt()}% ($completedTasks / ${allTasks.size} COMPLETE)",
                    fontFamily = PipMonospace,
                    fontSize = 10.sp,
                    color = PipSecondaryGreen
                )
            }
        }

        // Section Tabs
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PipSecondaryBg, shape = RoundedCornerShape(8.dp))
                    .border(1.dp, PipBorderGreen, shape = RoundedCornerShape(8.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    val textColor = if (isSelected) PipPhosphorGreen else PipTextDisabled
                    val bg = if (isSelected) PipPhosphorGreen.copy(alpha = 0.16f) else Color.Transparent

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(bg, shape = RoundedCornerShape(6.dp))
                            .clickable { selectedTab = index }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontFamily = PipMonospace,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = textColor,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> {
                // Tasks
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "► PROJECT TASKS",
                            fontFamily = PipMonospace,
                            fontSize = 12.sp,
                            color = PipPhosphorGreen,
                            fontWeight = FontWeight.Bold
                        )
                        TerminalButton(
                            text = "+ CREATE TASK",
                            onClick = { showNewTaskDialog = true },
                            testTag = "dossier_new_task_btn"
                        )
                    }
                }

                if (allTasks.isEmpty()) {
                    item {
                        TerminalEmptyState(
                            title = "NO TASKS DEFINED",
                            message = "Break down requirements into actionable engineering tasks.",
                            actionLabel = "+ ADD FIRST TASK",
                            onActionClick = { showNewTaskDialog = true }
                        )
                    }
                } else {
                    items(allTasks, key = { it.id }) { task ->
                        val assignee = teamMembers.find { it.id == task.assignedMemberId }?.name ?: "UNASSIGNED"
                        TerminalPanel(
                            title = task.title,
                            statusIndicator = "[${task.priority}]"
                        ) {
                            if (task.description.isNotBlank()) {
                                Text(
                                    text = task.description,
                                    fontFamily = PipMonospace,
                                    fontSize = 11.sp,
                                    color = PipTextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "RESP: $assignee",
                                    fontFamily = PipMonospace,
                                    fontSize = 10.sp,
                                    color = PipTextSecondary
                                )
                                StatusBadge(status = task.status)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (task.status != "DONE") {
                                        TerminalButton(
                                            text = "DONE",
                                            onClick = { onUpdateTaskStatus(task, "DONE") },
                                            testTag = "mark_done_${task.id}"
                                        )
                                    } else {
                                        TerminalButton(
                                            text = "REOPEN",
                                            onClick = { onUpdateTaskStatus(task, "TODO") },
                                            variant = TerminalButtonVariant.SECONDARY,
                                            testTag = "reopen_${task.id}"
                                        )
                                    }
                                    if (task.status != "BLOCKED" && task.status != "DONE") {
                                        TerminalButton(
                                            text = "BLOCK",
                                            onClick = { onUpdateTaskStatus(task, "BLOCKED") },
                                            variant = TerminalButtonVariant.DANGER,
                                            testTag = "block_${task.id}"
                                        )
                                    }
                                }

                                TerminalButton(
                                    text = "DEL",
                                    onClick = { onDeleteTask(task) },
                                    variant = TerminalButtonVariant.DANGER,
                                    testTag = "del_task_${task.id}"
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                // Risks
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "► PROJECT THREAT REGISTER",
                            fontFamily = PipMonospace,
                            fontSize = 12.sp,
                            color = PipPhosphorGreen,
                            fontWeight = FontWeight.Bold
                        )
                        TerminalButton(
                            text = "+ LOG RISK",
                            onClick = { showNewRiskDialog = true },
                            testTag = "dossier_new_risk_btn"
                        )
                    }
                }

                if (allRisks.isEmpty()) {
                    item {
                        TerminalEmptyState(
                            title = "NO RISKS RECORDED",
                            message = "Perform a threat assessment to identify technical, schedule, or cost hazards.",
                            actionLabel = "+ LOG INITIAL RISK",
                            onActionClick = { showNewRiskDialog = true }
                        )
                    }
                } else {
                    items(allRisks, key = { it.id }) { risk ->
                        val riskColor = when {
                            risk.exposure >= 13 -> PipCriticalRed
                            risk.exposure >= 5 -> PipAmberWarning
                            else -> PipPhosphorGreen
                        }
                        TerminalPanel(
                            title = risk.title,
                            borderColor = riskColor,
                            statusIndicator = "[EXPOSURE: ${risk.exposure}]"
                        ) {
                            Text(
                                text = "LIKELIHOOD: ${risk.likelihood}/5 | IMPACT: ${risk.impact}/5 | SEVERITY: ${risk.severityLevel}",
                                fontFamily = PipMonospace,
                                fontSize = 10.sp,
                                color = riskColor,
                                fontWeight = FontWeight.Bold
                            )
                            if (risk.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "HAZARD: ${risk.description}",
                                    fontFamily = PipMonospace,
                                    fontSize = 11.sp,
                                    color = PipTextSecondary
                                )
                            }
                            if (risk.mitigationPlan.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "MITIGATION: ${risk.mitigationPlan}",
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
                                            onClick = { onUpdateRiskStatus(risk, "RESOLVED") },
                                            testTag = "resolve_risk_${risk.id}"
                                        )
                                    }
                                    TerminalButton(
                                        text = "DEL",
                                        onClick = { onDeleteRisk(risk) },
                                        variant = TerminalButtonVariant.DANGER,
                                        testTag = "del_risk_${risk.id}"
                                    )
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Milestones
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "► PROJECT MILESTONES",
                            fontFamily = PipMonospace,
                            fontSize = 12.sp,
                            color = PipPhosphorGreen,
                            fontWeight = FontWeight.Bold
                        )
                        TerminalButton(
                            text = "+ ADD MILESTONE",
                            onClick = { showNewMilestoneDialog = true },
                            testTag = "dossier_new_milestone_btn"
                        )
                    }
                }

                if (allMilestones.isEmpty()) {
                    item {
                        TerminalEmptyState(
                            title = "NO MILESTONES SET",
                            message = "Set key release gates and deliverable targets.",
                            actionLabel = "+ ADD MILESTONE",
                            onActionClick = { showNewMilestoneDialog = true }
                        )
                    }
                } else {
                    items(allMilestones, key = { it.id }) { milestone ->
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val isOverdue = milestone.targetDate < System.currentTimeMillis() && milestone.status != "COMPLETED"

                        TerminalPanel(
                            title = milestone.title,
                            borderColor = if (isOverdue) PipCriticalRed else PipBorderGreen,
                            statusIndicator = if (isOverdue) "! OVERDUE" else "[${milestone.status}]"
                        ) {
                            Text(
                                text = "TARGET DATE: ${sdf.format(Date(milestone.targetDate))}",
                                fontFamily = PipMonospace,
                                fontSize = 10.sp,
                                color = if (isOverdue) PipCriticalRed else PipTextSecondary
                            )
                            if (milestone.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = milestone.description,
                                    fontFamily = PipMonospace,
                                    fontSize = 11.sp,
                                    color = PipTextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatusBadge(status = milestone.status)

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (milestone.status != "COMPLETED") {
                                        TerminalButton(
                                            text = "COMPLETE",
                                            onClick = { onUpdateMilestoneStatus(milestone, "COMPLETED") },
                                            testTag = "complete_milestone_${milestone.id}"
                                        )
                                    }
                                    TerminalButton(
                                        text = "DEL",
                                        onClick = { onDeleteMilestone(milestone) },
                                        variant = TerminalButtonVariant.DANGER,
                                        testTag = "del_milestone_${milestone.id}"
                                    )
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // Test Cases
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "► VERIFICATION TEST CASES",
                            fontFamily = PipMonospace,
                            fontSize = 12.sp,
                            color = PipPhosphorGreen,
                            fontWeight = FontWeight.Bold
                        )
                        TerminalButton(
                            text = "+ ADD TEST",
                            onClick = { showNewTestCaseDialog = true },
                            testTag = "dossier_new_test_btn"
                        )
                    }
                }

                if (allTestCases.isEmpty()) {
                    item {
                        TerminalEmptyState(
                            title = "NO TEST CASES LOGGED",
                            message = "Track real verification results rather than assuming quality passes.",
                            actionLabel = "+ ADD TEST CASE",
                            onActionClick = { showNewTestCaseDialog = true }
                        )
                    }
                } else {
                    items(allTestCases, key = { it.id }) { testCase ->
                        TerminalPanel(
                            title = testCase.name,
                            statusIndicator = "[${testCase.component}]"
                        ) {
                            if (testCase.notes.isNotBlank()) {
                                Text(
                                    text = testCase.notes,
                                    fontFamily = PipMonospace,
                                    fontSize = 10.sp,
                                    color = PipTextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatusBadge(status = testCase.status)

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    TerminalButton(
                                        text = "PASS",
                                        onClick = { onUpdateTestCaseStatus(testCase, "PASSED") },
                                        testTag = "pass_test_${testCase.id}"
                                    )
                                    TerminalButton(
                                        text = "FAIL",
                                        onClick = { onUpdateTestCaseStatus(testCase, "FAILED") },
                                        variant = TerminalButtonVariant.DANGER,
                                        testTag = "fail_test_${testCase.id}"
                                    )
                                    TerminalButton(
                                        text = "DEL",
                                        onClick = { onDeleteTestCase(testCase) },
                                        variant = TerminalButtonVariant.SECONDARY,
                                        testTag = "del_test_${testCase.id}"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewTaskDialog) {
        CreateTaskDialog(
            teamMembers = teamMembers,
            onDismiss = { showNewTaskDialog = false },
            onConfirm = { title, desc, prio, days, memberId, isBlocker ->
                val due = if (days != null && days > 0) System.currentTimeMillis() + (days * 86400000L) else null
                onCreateTask(project.id, title, desc, prio, due, memberId, isBlocker)
                showNewTaskDialog = false
            }
        )
    }

    if (showNewRiskDialog) {
        CreateRiskDialog(
            teamMembers = teamMembers,
            onDismiss = { showNewRiskDialog = false },
            onConfirm = { title, desc, like, imp, plan, ownerId ->
                onCreateRisk(project.id, title, desc, like, imp, plan, ownerId)
                showNewRiskDialog = false
            }
        )
    }

    if (showNewMilestoneDialog) {
        CreateMilestoneDialog(
            onDismiss = { showNewMilestoneDialog = false },
            onConfirm = { title, desc, targetDays ->
                val targetDate = System.currentTimeMillis() + (targetDays * 86400000L)
                onCreateMilestone(project.id, title, desc, targetDate)
                showNewMilestoneDialog = false
            }
        )
    }

    if (showNewTestCaseDialog) {
        CreateTestCaseDialog(
            onDismiss = { showNewTestCaseDialog = false },
            onConfirm = { name, comp, notes ->
                onCreateTestCase(project.id, name, comp, notes)
                showNewTestCaseDialog = false
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            containerColor = PipPanelBg,
            title = {
                Text(
                    text = "► CONFIRM DOSSIER PURGE",
                    fontFamily = PipMonospace,
                    fontSize = 14.sp,
                    color = PipCriticalRed,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you certain you wish to delete '${project.name}'? This will permanently delete all associated tasks, risks, milestones, and test verification records from SQLite store.",
                    fontFamily = PipMonospace,
                    fontSize = 12.sp,
                    color = PipTextMain
                )
            },
            confirmButton = {
                TerminalButton(
                    text = "CONFIRM DELETE",
                    onClick = {
                        onDeleteProject(project)
                        showDeleteConfirmDialog = false
                    },
                    variant = TerminalButtonVariant.DANGER,
                    testTag = "confirm_delete_project_btn"
                )
            },
            dismissButton = {
                TerminalButton(
                    text = "CANCEL",
                    onClick = { showDeleteConfirmDialog = false }
                )
            }
        )
    }
}

// Dialog Composables
@Composable
fun CreateProjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, desc: String, deadline: Long?, budget: Double?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var daysText by remember { mutableStateOf("") }
    var budgetText by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PipPanelBg,
        title = {
            Text(
                text = "► INITIALIZE NEW PROJECT",
                fontFamily = PipMonospace,
                fontSize = 14.sp,
                color = PipPhosphorGreen,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("PROJECT NAME *", fontFamily = PipMonospace, fontSize = 10.sp) },
                    singleLine = true,
                    colors = terminalTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("proj_name_input")
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("MISSION DESCRIPTION", fontFamily = PipMonospace, fontSize = 10.sp) },
                    maxLines = 2,
                    colors = terminalTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = daysText,
                    onValueChange = { daysText = it },
                    label = { Text("TARGET DURATION (DAYS)", fontFamily = PipMonospace, fontSize = 10.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = terminalTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { budgetText = it },
                    label = { Text("BUDGET ALLOCATION ($)", fontFamily = PipMonospace, fontSize = 10.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = terminalTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg != null) {
                    Text(
                        text = "! $errorMsg",
                        fontFamily = PipMonospace,
                        fontSize = 11.sp,
                        color = PipCriticalRed
                    )
                }
            }
        },
        confirmButton = {
            TerminalButton(
                text = "CREATE WORKSPACE",
                onClick = {
                    if (name.isBlank()) {
                        errorMsg = "Project name cannot be empty."
                        return@TerminalButton
                    }
                    val days = daysText.toLongOrNull()
                    val deadline = if (days != null && days > 0) System.currentTimeMillis() + (days * 86400000L) else null
                    val budget = budgetText.toDoubleOrNull()
                    onConfirm(name, desc, deadline, budget)
                },
                testTag = "submit_create_project_btn"
            )
        },
        dismissButton = {
            TerminalButton(text = "CANCEL", onClick = onDismiss)
        }
    )
}

@Composable
fun CreateTaskDialog(
    teamMembers: List<TeamMemberEntity>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String, priority: String, days: Long?, memberId: String?, isBlocker: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("MEDIUM") }
    var daysText by remember { mutableStateOf("") }
    var selectedMemberId by remember { mutableStateOf<String?>(null) }
    var isBlocker by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PipPanelBg,
        title = {
            Text(
                text = "► CREATE OPERATIONAL TASK",
                fontFamily = PipMonospace,
                fontSize = 14.sp,
                color = PipPhosphorGreen,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("TASK TITLE *", fontFamily = PipMonospace, fontSize = 10.sp) },
                    singleLine = true,
                    colors = terminalTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("task_title_input")
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("TASK SPECIFICATION", fontFamily = PipMonospace, fontSize = 10.sp) },
                    maxLines = 2,
                    colors = terminalTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                // Priority selector
                Text("PRIORITY LEVEL:", fontFamily = PipMonospace, fontSize = 10.sp, color = PipTextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("LOW", "MEDIUM", "HIGH", "CRITICAL").forEach { prio ->
                        val isSel = priority == prio
                        Box(
                            modifier = Modifier
                                .border(1.dp, if (isSel) PipPhosphorGreen else PipBorderGreen)
                                .background(if (isSel) PipPhosphorGreen.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { priority = prio }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(prio, fontFamily = PipMonospace, fontSize = 9.sp, color = if (isSel) PipPhosphorGreen else PipTextSecondary)
                        }
                    }
                }

                OutlinedTextField(
                    value = daysText,
                    onValueChange = { daysText = it },
                    label = { Text("DUE IN DAYS (OPTIONAL)", fontFamily = PipMonospace, fontSize = 10.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = terminalTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Assignee
                if (teamMembers.isNotEmpty()) {
                    Text("ASSIGN TO TEAM MEMBER:", fontFamily = PipMonospace, fontSize = 10.sp, color = PipTextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .border(1.dp, if (selectedMemberId == null) PipPhosphorGreen else PipBorderGreen)
                                .clickable { selectedMemberId = null }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text("NONE", fontFamily = PipMonospace, fontSize = 9.sp, color = if (selectedMemberId == null) PipPhosphorGreen else PipTextDisabled)
                        }
                        teamMembers.forEach { member ->
                            val isSel = selectedMemberId == member.id
                            Box(
                                modifier = Modifier
                                    .border(1.dp, if (isSel) PipPhosphorGreen else PipBorderGreen)
                                    .clickable { selectedMemberId = member.id }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(member.name, fontFamily = PipMonospace, fontSize = 9.sp, color = if (isSel) PipPhosphorGreen else PipTextSecondary)
                            }
                        }
                    }
                }

                // Release Blocker toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isBlocker = !isBlocker }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBlocker) "[X] RELEASE BLOCKER" else "[ ] RELEASE BLOCKER",
                        fontFamily = PipMonospace,
                        fontSize = 11.sp,
                        color = if (isBlocker) PipCriticalRed else PipTextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (errorMsg != null) {
                    Text("! $errorMsg", fontFamily = PipMonospace, fontSize = 10.sp, color = PipCriticalRed)
                }
            }
        },
        confirmButton = {
            TerminalButton(
                text = "CONFIRM TASK",
                onClick = {
                    if (title.isBlank()) {
                        errorMsg = "Task title is required."
                        return@TerminalButton
                    }
                    onConfirm(title, desc, priority, daysText.toLongOrNull(), selectedMemberId, isBlocker)
                },
                testTag = "submit_task_btn"
            )
        },
        dismissButton = {
            TerminalButton(text = "CANCEL", onClick = onDismiss)
        }
    )
}

@Composable
fun CreateRiskDialog(
    teamMembers: List<TeamMemberEntity>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String, likelihood: Int, impact: Int, mitigationPlan: String, ownerId: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var likelihood by remember { mutableIntStateOf(3) }
    var impact by remember { mutableIntStateOf(3) }
    var mitigationPlan by remember { mutableStateOf("") }
    var ownerId by remember { mutableStateOf<String?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val exposure = likelihood * impact
    val severity = when {
        exposure >= 13 -> "HIGH"
        exposure >= 5 -> "MEDIUM"
        else -> "LOW"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PipPanelBg,
        title = {
            Text(
                text = "► REGISTER THREAT ANOMALY",
                fontFamily = PipMonospace,
                fontSize = 14.sp,
                color = PipPhosphorGreen,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("RISK TITLE *", fontFamily = PipMonospace, fontSize = 10.sp) },
                    singleLine = true,
                    colors = terminalTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("risk_title_input")
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("HAZARD SCENARIO DESCRIPTION", fontFamily = PipMonospace, fontSize = 10.sp) },
                    maxLines = 2,
                    colors = terminalTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Likelihood selector 1..5
                Text("LIKELIHOOD: $likelihood / 5", fontFamily = PipMonospace, fontSize = 10.sp, color = PipTextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..5).forEach { num ->
                        val isSel = likelihood == num
                        Box(
                            modifier = Modifier
                                .border(1.dp, if (isSel) PipPhosphorGreen else PipBorderGreen)
                                .background(if (isSel) PipPhosphorGreen.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { likelihood = num }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(num.toString(), fontFamily = PipMonospace, fontSize = 10.sp, color = if (isSel) PipPhosphorGreen else PipTextSecondary)
                        }
                    }
                }

                // Impact selector 1..5
                Text("IMPACT: $impact / 5", fontFamily = PipMonospace, fontSize = 10.sp, color = PipTextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..5).forEach { num ->
                        val isSel = impact == num
                        Box(
                            modifier = Modifier
                                .border(1.dp, if (isSel) PipPhosphorGreen else PipBorderGreen)
                                .background(if (isSel) PipPhosphorGreen.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { impact = num }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(num.toString(), fontFamily = PipMonospace, fontSize = 10.sp, color = if (isSel) PipPhosphorGreen else PipTextSecondary)
                        }
                    }
                }

                // Exposure calculation display
                Text(
                    text = "EXPOSURE: $likelihood x $impact = $exposure [$severity SEVERITY]",
                    fontFamily = PipMonospace,
                    fontSize = 11.sp,
                    color = if (exposure >= 13) PipCriticalRed else if (exposure >= 5) PipAmberWarning else PipPhosphorGreen,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = mitigationPlan,
                    onValueChange = { mitigationPlan = it },
                    label = { Text("MITIGATION PROTOCOL PLAN", fontFamily = PipMonospace, fontSize = 10.sp) },
                    maxLines = 2,
                    colors = terminalTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg != null) {
                    Text("! $errorMsg", fontFamily = PipMonospace, fontSize = 10.sp, color = PipCriticalRed)
                }
            }
        },
        confirmButton = {
            TerminalButton(
                text = "RECORD RISK",
                onClick = {
                    if (title.isBlank()) {
                        errorMsg = "Risk title is required."
                        return@TerminalButton
                    }
                    onConfirm(title, desc, likelihood, impact, mitigationPlan, ownerId)
                },
                testTag = "submit_risk_btn"
            )
        },
        dismissButton = {
            TerminalButton(text = "CANCEL", onClick = onDismiss)
        }
    )
}

@Composable
fun CreateMilestoneDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String, targetDays: Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var daysText by remember { mutableStateOf("14") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PipPanelBg,
        title = {
            Text(
                text = "► SET MILESTONE GATE",
                fontFamily = PipMonospace,
                fontSize = 14.sp,
                color = PipPhosphorGreen,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("MILESTONE NAME *", fontFamily = PipMonospace, fontSize = 10.sp) },
                    singleLine = true,
                    colors = terminalTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("CRITERIA / DELIVERABLES", fontFamily = PipMonospace, fontSize = 10.sp) },
                    maxLines = 2,
                    colors = terminalTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = daysText,
                    onValueChange = { daysText = it },
                    label = { Text("TARGET DEADLINE IN DAYS *", fontFamily = PipMonospace, fontSize = 10.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = terminalTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMsg != null) {
                    Text("! $errorMsg", fontFamily = PipMonospace, fontSize = 10.sp, color = PipCriticalRed)
                }
            }
        },
        confirmButton = {
            TerminalButton(
                text = "SET GATE",
                onClick = {
                    if (title.isBlank()) {
                        errorMsg = "Milestone title required."
                        return@TerminalButton
                    }
                    val days = daysText.toLongOrNull() ?: 14L
                    onConfirm(title, desc, days)
                }
            )
        },
        dismissButton = {
            TerminalButton(text = "CANCEL", onClick = onDismiss)
        }
    )
}

@Composable
fun CreateTestCaseDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, component: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var component by remember { mutableStateOf("CORE") }
    var notes by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PipPanelBg,
        title = {
            Text(
                text = "► ADD VERIFICATION TEST CASE",
                fontFamily = PipMonospace,
                fontSize = 14.sp,
                color = PipPhosphorGreen,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("TEST SUITE / CASE NAME *", fontFamily = PipMonospace, fontSize = 10.sp) },
                    singleLine = true,
                    colors = terminalTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = component,
                    onValueChange = { component = it },
                    label = { Text("SUBSYSTEM (e.g. AUTH, PAYMENTS)", fontFamily = PipMonospace, fontSize = 10.sp) },
                    singleLine = true,
                    colors = terminalTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ACCEPTANCE CRITERIA NOTES", fontFamily = PipMonospace, fontSize = 10.sp) },
                    maxLines = 2,
                    colors = terminalTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMsg != null) {
                    Text("! $errorMsg", fontFamily = PipMonospace, fontSize = 10.sp, color = PipCriticalRed)
                }
            }
        },
        confirmButton = {
            TerminalButton(
                text = "SAVE TEST CASE",
                onClick = {
                    if (name.isBlank()) {
                        errorMsg = "Test case name required."
                        return@TerminalButton
                    }
                    onConfirm(name, component, notes)
                }
            )
        },
        dismissButton = {
            TerminalButton(text = "CANCEL", onClick = onDismiss)
        }
    )
}

@Composable
fun terminalTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PipPhosphorGreen,
    unfocusedBorderColor = PipBorderGreen,
    focusedTextColor = PipTextMain,
    unfocusedTextColor = PipTextMain,
    focusedLabelColor = PipPhosphorGreen,
    unfocusedLabelColor = PipTextSecondary,
    cursorColor = PipPhosphorGreen,
    focusedContainerColor = PipSecondaryBg,
    unfocusedContainerColor = PipPanelBg
)

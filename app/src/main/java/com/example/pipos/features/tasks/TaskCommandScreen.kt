package com.example.pipos.features.tasks

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pipos.core.components.StatusBadge
import com.example.pipos.core.components.TerminalBadge
import com.example.pipos.core.components.TerminalButton
import com.example.pipos.core.components.TerminalButtonVariant
import com.example.pipos.core.components.TerminalEmptyState
import com.example.pipos.core.components.TerminalPanel
import com.example.pipos.data.local.entity.ProjectEntity
import com.example.pipos.data.local.entity.TaskEntity
import com.example.pipos.data.local.entity.TeamMemberEntity
import com.example.pipos.features.projects.CreateTaskDialog
import com.example.pipos.features.projects.terminalTextFieldColors
import com.example.ui.theme.PipAmberWarning
import com.example.ui.theme.PipBorderGreen
import com.example.ui.theme.PipCriticalRed
import com.example.ui.theme.PipMonospace
import com.example.ui.theme.PipPanelBg
import com.example.ui.theme.PipPhosphorGreen
import com.example.ui.theme.PipPrimaryBg
import com.example.ui.theme.PipSecondaryGreen
import com.example.ui.theme.PipTextDisabled
import com.example.ui.theme.PipTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TaskCommandScreen(
    tasks: List<TaskEntity>,
    projects: List<ProjectEntity>,
    teamMembers: List<TeamMemberEntity>,
    onCreateTask: (projectId: String, title: String, description: String, priority: String, dueDate: Long?, assignedMemberId: String?, isBlocker: Boolean) -> Unit,
    onUpdateTaskStatus: (TaskEntity, String) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onNavigateToProjects: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var selectedProjectFilterId by remember { mutableStateOf<String?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val filteredTasks = tasks.filter { task ->
        val matchesSearch = task.title.contains(searchQuery, ignoreCase = true) ||
                task.description.contains(searchQuery, ignoreCase = true)
        val matchesStatus = selectedStatusFilter == "ALL" || task.status == selectedStatusFilter
        val matchesProject = selectedProjectFilterId == null || task.projectId == selectedProjectFilterId
        matchesSearch && matchesStatus && matchesProject
    }

    val openCount = tasks.count { it.status != "DONE" }
    val doneCount = tasks.count { it.status == "DONE" }
    val blockedCount = tasks.count { it.status == "BLOCKED" }

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
                    text = "► TASK COMMAND // MISSION LOG",
                    fontFamily = PipMonospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PipPhosphorGreen
                )
                if (projects.isNotEmpty()) {
                    TerminalButton(
                        text = "+ NEW TASK",
                        onClick = { showCreateDialog = true },
                        testTag = "tasks_new_btn"
                    )
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("FILTER TASKS BY KEYWORD...", fontFamily = PipMonospace, fontSize = 10.sp) },
                singleLine = true,
                colors = terminalTextFieldColors(),
                modifier = Modifier.fillMaxWidth().testTag("task_search_field")
            )
        }

        // Status Filter Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val filters = listOf(
                    "ALL" to "${tasks.size}",
                    "TODO" to "${tasks.count { it.status == "TODO" }}",
                    "IN_PROGRESS" to "${tasks.count { it.status == "IN_PROGRESS" }}",
                    "BLOCKED" to "$blockedCount",
                    "DONE" to "$doneCount"
                )
                items(filters) { (status, count) ->
                    val isSel = selectedStatusFilter == status
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSel) PipPhosphorGreen.copy(alpha = 0.18f) else PipPanelBg,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSel) PipPhosphorGreen else PipBorderGreen.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedStatusFilter = status }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$status ($count)",
                            fontFamily = PipMonospace,
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSel) PipPhosphorGreen else PipTextSecondary
                        )
                    }
                }
            }
        }

        if (projects.isEmpty()) {
            item {
                TerminalEmptyState(
                    title = "ZERO WORKSPACES REGISTERED",
                    message = "Tasks must belong to a project workspace. Initialize your first project to manage tasks.",
                    actionLabel = "CREATE WORKSPACE FIRST",
                    onActionClick = onNavigateToProjects
                )
            }
        } else if (filteredTasks.isEmpty()) {
            item {
                TerminalEmptyState(
                    title = "NO TASKS FOUND",
                    message = if (tasks.isEmpty()) "Your tasks list is currently clean." else "No tasks matched the applied filter criteria.",
                    actionLabel = if (tasks.isEmpty()) "+ CREATE FIRST TASK" else null,
                    onActionClick = { showCreateDialog = true }
                )
            }
        } else {
            items(filteredTasks, key = { it.id }) { task ->
                val project = projects.find { it.id == task.projectId }
                val assignee = teamMembers.find { it.id == task.assignedMemberId }?.name ?: "UNASSIGNED"
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val isOverdue = task.dueDate != null && task.dueDate < System.currentTimeMillis() && task.status != "DONE"

                TerminalPanel(
                    title = task.title,
                    borderColor = if (isOverdue || task.isReleaseBlocker) PipCriticalRed else PipBorderGreen,
                    statusIndicator = if (isOverdue) "! OVERDUE" else "[${task.priority}]"
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
                            text = "PRJ: ${project?.name ?: "UNKNOWN"} | RESP: $assignee",
                            fontFamily = PipMonospace,
                            fontSize = 10.sp,
                            color = PipTextSecondary
                        )
                        StatusBadge(status = task.status)
                    }

                    if (task.dueDate != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "DUE: ${sdf.format(Date(task.dueDate))}",
                            fontFamily = PipMonospace,
                            fontSize = 10.sp,
                            color = if (isOverdue) PipCriticalRed else PipSecondaryGreen
                        )
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
                                    onClick = { onUpdateTaskStatus(task, "DONE") }
                                )
                            } else {
                                TerminalButton(
                                    text = "REOPEN",
                                    onClick = { onUpdateTaskStatus(task, "TODO") },
                                    variant = TerminalButtonVariant.SECONDARY
                                )
                            }
                            if (task.status != "BLOCKED" && task.status != "DONE") {
                                TerminalButton(
                                    text = "BLOCK",
                                    onClick = { onUpdateTaskStatus(task, "BLOCKED") },
                                    variant = TerminalButtonVariant.DANGER
                                )
                            }
                        }

                        TerminalButton(
                            text = "DEL",
                            onClick = { onDeleteTask(task) },
                            variant = TerminalButtonVariant.DANGER
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog && projects.isNotEmpty()) {
        CreateTaskDialog(
            teamMembers = teamMembers,
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, desc, prio, days, memberId, isBlocker ->
                val due = if (days != null && days > 0) System.currentTimeMillis() + (days * 86400000L) else null
                val targetProjectId = selectedProjectFilterId ?: projects.first().id
                onCreateTask(targetProjectId, title, desc, prio, due, memberId, isBlocker)
                showCreateDialog = false
            }
        )
    }
}

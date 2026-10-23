package com.example.pipos.features.system

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
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
import com.example.pipos.data.local.entity.MilestoneEntity
import com.example.pipos.data.local.entity.ProjectEntity
import com.example.pipos.data.local.entity.RiskEntity
import com.example.pipos.data.local.entity.TaskEntity
import com.example.pipos.data.local.entity.TeamMemberEntity
import com.example.pipos.data.local.entity.TestCaseEntity
import com.example.pipos.features.projects.terminalTextFieldColors
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
fun SystemScreen(
    projects: List<ProjectEntity>,
    tasks: List<TaskEntity>,
    risks: List<RiskEntity>,
    milestones: List<MilestoneEntity>,
    teamMembers: List<TeamMemberEntity>,
    testCases: List<TestCaseEntity>,
    currentUser: com.google.firebase.auth.FirebaseUser?,
    cloudSyncMessage: String?,
    onSyncAllToCloud: () -> Unit,
    onRestoreFromCloud: () -> Unit,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    scanlinesEnabled: Boolean,
    onToggleScanlines: (Boolean) -> Unit,
    onCreateTeamMember: (name: String, role: String, email: String) -> Unit,
    onDeleteTeamMember: (TeamMemberEntity) -> Unit,
    onClearAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("READINESS", "REPORTS", "TEAM", "SETTINGS")
    val context = LocalContext.current

    var showNewMemberDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PipPrimaryBg)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Tab Selector Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PipSecondaryBg, shape = RoundedCornerShape(8.dp))
                    .border(1.dp, PipBorderGreen, shape = RoundedCornerShape(8.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tabs.forEachIndexed { index, tabTitle ->
                    val isSel = selectedSubTab == index
                    val textColor = if (isSel) PipPhosphorGreen else PipTextDisabled
                    val bg = if (isSel) PipPhosphorGreen.copy(alpha = 0.16f) else Color.Transparent

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(bg, shape = RoundedCornerShape(6.dp))
                            .clickable { selectedSubTab = index }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabTitle,
                            fontFamily = PipMonospace,
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            color = textColor
                        )
                    }
                }
            }
        }

        when (selectedSubTab) {
            0 -> {
                // 1. RELEASE READINESS
                item {
                    val criticalRisks = risks.filter { it.exposure >= 13 && it.status != "RESOLVED" }
                    val blockerTasks = tasks.filter { it.isReleaseBlocker && it.status != "DONE" }
                    val overdueMilestones = milestones.filter { it.targetDate < System.currentTimeMillis() && it.status != "COMPLETED" }
                    val failedTests = testCases.filter { it.status == "FAILED" }
                    val passedTests = testCases.filter { it.status == "PASSED" }

                    val isReady = projects.isNotEmpty() &&
                            criticalRisks.isEmpty() &&
                            blockerTasks.isEmpty() &&
                            overdueMilestones.isEmpty() &&
                            failedTests.isEmpty()

                    val overallColor = when {
                        projects.isEmpty() -> PipTextDisabled
                        !isReady -> PipCriticalRed
                        testCases.isEmpty() -> PipAmberWarning
                        else -> PipPhosphorGreen
                    }

                    val overallVerdict = when {
                        projects.isEmpty() -> "INSUFFICIENT DATA (NO PROJECTS CREATED)"
                        !isReady -> "RELEASE BLOCKED — CRITICAL HAZARDS PRESENT"
                        testCases.isEmpty() -> "CONDITIONALLY PASS (NO RECORDED TEST RUNS)"
                        else -> "SYSTEM READY FOR FLIGHT / PRODUCTION SHIPMENT"
                    }

                    TerminalPanel(
                        title = "PRE-FLIGHT RELEASE GATE // DIAGNOSTICS",
                        borderColor = overallColor
                    ) {
                        Text(
                            text = overallVerdict,
                            fontFamily = PipMonospace,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = overallColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "STRICT PRE-RELEASE VERIFICATION EVALUATED AGAINST REAL SQLITE DATABASE.",
                            fontFamily = PipMonospace,
                            fontSize = 10.sp,
                            color = PipTextSecondary
                        )
                    }
                }

                item {
                    val criticalRisks = risks.filter { it.exposure >= 13 && it.status != "RESOLVED" }
                    val blockerTasks = tasks.filter { it.isReleaseBlocker && it.status != "DONE" }
                    val overdueMilestones = milestones.filter { it.targetDate < System.currentTimeMillis() && it.status != "COMPLETED" }
                    val failedTests = testCases.filter { it.status == "FAILED" }
                    val passedTests = testCases.filter { it.status == "PASSED" }

                    TerminalPanel(title = "READINESS CHECKLIST VERIFICATION") {
                        // Check 1: Unresolved Critical Risks
                        ReadinessItem(
                            title = "CRITICAL THREAT REGISTER",
                            status = if (criticalRisks.isEmpty()) "PASS" else "BLOCKED",
                            description = if (criticalRisks.isEmpty()) "Zero open high/critical risks." else "${criticalRisks.size} unmitigated critical risks detected.",
                            isPass = criticalRisks.isEmpty()
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Check 2: Release Blocker Tasks
                        ReadinessItem(
                            title = "RELEASE BLOCKER TASKS",
                            status = if (blockerTasks.isEmpty()) "PASS" else "BLOCKED",
                            description = if (blockerTasks.isEmpty()) "Zero open release-blocker tasks." else "${blockerTasks.size} release-blocker tasks still open.",
                            isPass = blockerTasks.isEmpty()
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Check 3: Milestones
                        ReadinessItem(
                            title = "PROJECT MILESTONES GATE",
                            status = if (overdueMilestones.isEmpty()) "PASS" else "BLOCKED",
                            description = if (overdueMilestones.isEmpty()) "No overdue milestones." else "${overdueMilestones.size} milestones past deadline.",
                            isPass = overdueMilestones.isEmpty()
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Check 4: Test Cases
                        val testPass = failedTests.isEmpty()
                        ReadinessItem(
                            title = "RECORDED TEST VERIFICATION",
                            status = if (testCases.isEmpty()) "UNKNOWN" else if (failedTests.isEmpty()) "PASS" else "FAILED",
                            description = if (testCases.isEmpty()) "No tests entered in database." else "${passedTests.size} passed, ${failedTests.size} failed.",
                            isPass = testPass && testCases.isNotEmpty(),
                            isUnknown = testCases.isEmpty()
                        )
                    }
                }
            }

            1 -> {
                // 2. REPORTS
                item {
                    val completedTasks = tasks.count { it.status == "DONE" }
                    val taskPercent = if (tasks.isNotEmpty()) (completedTasks * 100) / tasks.size else 0

                    TerminalPanel(title = "AUDIT REPORT // REAL TELEMETRY SUMMARY") {
                        Text(
                            text = "ACTIVE PROJECTS: ${projects.size}",
                            fontFamily = PipMonospace,
                            fontSize = 11.sp,
                            color = PipPhosphorGreen
                        )
                        Text(
                            text = "TOTAL TASKS LOGGED: ${tasks.size} (${completedTasks} COMPLETE - $taskPercent%)",
                            fontFamily = PipMonospace,
                            fontSize = 11.sp,
                            color = PipSecondaryGreen
                        )
                        Text(
                            text = "TOTAL RISKS REGISTERED: ${risks.size}",
                            fontFamily = PipMonospace,
                            fontSize = 11.sp,
                            color = PipAmberWarning
                        )
                        Text(
                            text = "TOTAL MILESTONES: ${milestones.size}",
                            fontFamily = PipMonospace,
                            fontSize = 11.sp,
                            color = PipInfoCyan
                        )
                        Text(
                            text = "TEAM PERSONNEL: ${teamMembers.size}",
                            fontFamily = PipMonospace,
                            fontSize = 11.sp,
                            color = PipTextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        TerminalButton(
                            text = "EXPORT CSV DOSSIER TO CLIPBOARD",
                            onClick = {
                                val csv = buildCsvExport(projects, tasks, risks, milestones)
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("PIP-OS CSV Report", csv)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "CSV Report copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            testTag = "export_csv_btn"
                        )
                    }
                }
            }

            2 -> {
                // 3. TEAM MANAGEMENT
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "► REAL TEAM ROSTER (${teamMembers.size})",
                            fontFamily = PipMonospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PipPhosphorGreen
                        )
                        TerminalButton(
                            text = "+ ADD MEMBER",
                            onClick = { showNewMemberDialog = true },
                            testTag = "add_team_member_btn"
                        )
                    }
                }

                if (teamMembers.isEmpty()) {
                    item {
                        TerminalEmptyState(
                            title = "NO PERSONNEL REGISTERED",
                            message = "Add team members to assign responsibility for tasks and risk ownership.",
                            actionLabel = "+ ENROLL TEAM MEMBER",
                            onActionClick = { showNewMemberDialog = true }
                        )
                    }
                } else {
                    items(teamMembers, key = { it.id }) { member ->
                        val memberTasks = tasks.count { it.assignedMemberId == member.id }
                        val memberRisks = risks.count { it.ownerMemberId == member.id }

                        TerminalPanel(title = member.name) {
                            Text(
                                text = "ROLE: ${member.role} | EMAIL: ${member.email.ifBlank { "NONE" }}",
                                fontFamily = PipMonospace,
                                fontSize = 11.sp,
                                color = PipSecondaryGreen
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ASSIGNED TASKS: $memberTasks | RISK OWNER: $memberRisks",
                                    fontFamily = PipMonospace,
                                    fontSize = 10.sp,
                                    color = PipTextSecondary
                                )
                                TerminalButton(
                                    text = "REMOVE",
                                    onClick = { onDeleteTeamMember(member) },
                                    variant = TerminalButtonVariant.DANGER
                                )
                            }
                        }
                    }
                }
            }

            3 -> {
                // 4. SETTINGS & SPM ACADEMIC REFERENCE
                item {
                    TerminalPanel(title = "SYSTEM PREFERENCES") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "CRT SCANLINES EFFECT",
                                    fontFamily = PipMonospace,
                                    fontSize = 12.sp,
                                    color = PipPhosphorGreen,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Horizontal CRT monitor raster overlay",
                                    fontFamily = PipMonospace,
                                    fontSize = 10.sp,
                                    color = PipTextSecondary
                                )
                            }
                            Switch(
                                checked = scanlinesEnabled,
                                onCheckedChange = onToggleScanlines,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PipPhosphorGreen,
                                    checkedTrackColor = PipBorderGreen,
                                    uncheckedThumbColor = PipTextDisabled,
                                    uncheckedTrackColor = PipPanelBg
                                )
                            )
                        }
                    }
                }

                item {
                    TerminalPanel(
                        title = "CLOUD FIRESTORE // REMOTE SYNCHRONIZATION",
                        borderColor = if (currentUser != null) PipPhosphorGreen else PipAmberWarning
                    ) {
                        if (currentUser != null) {
                            Text(
                                text = "OPERATOR: ${currentUser.displayName ?: currentUser.email ?: "AUTHENTICATED"}",
                                fontFamily = PipMonospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PipPhosphorGreen
                            )
                            Text(
                                text = "UID: ${currentUser.uid}",
                                fontFamily = PipMonospace,
                                fontSize = 9.sp,
                                color = PipTextSecondary
                            )
                            Text(
                                text = "DATABASE: Firestore Enterprise (Hardened ABAC Rules)",
                                fontFamily = PipMonospace,
                                fontSize = 10.sp,
                                color = PipInfoCyan
                            )

                            if (cloudSyncMessage != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "► $cloudSyncMessage",
                                    fontFamily = PipMonospace,
                                    fontSize = 10.sp,
                                    color = PipSecondaryGreen
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                TerminalButton(
                                    text = "BACKUP TO CLOUD",
                                    onClick = onSyncAllToCloud,
                                    modifier = Modifier.weight(1f),
                                    testTag = "backup_cloud_btn"
                                )
                                TerminalButton(
                                    text = "RESTORE CLOUD",
                                    onClick = onRestoreFromCloud,
                                    modifier = Modifier.weight(1f),
                                    variant = TerminalButtonVariant.SECONDARY,
                                    testTag = "restore_cloud_btn"
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            TerminalButton(
                                text = "SIGN OUT OF GOOGLE",
                                onClick = onSignOutClick,
                                variant = TerminalButtonVariant.DANGER,
                                testTag = "sign_out_google_btn"
                            )
                        } else {
                            Text(
                                text = "STATUS: OFFLINE / ISOLATED WORKSPACE",
                                fontFamily = PipMonospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PipAmberWarning
                            )
                            Text(
                                text = "Connect your Google Account to enable automatic cloud backup and cross-device sync with Firebase Firestore.",
                                fontFamily = PipMonospace,
                                fontSize = 10.sp,
                                color = PipTextSecondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            TerminalButton(
                                text = "AUTHENTICATE GOOGLE ACCOUNT",
                                onClick = onSignInClick,
                                variant = TerminalButtonVariant.PRIMARY,
                                testTag = "connect_google_btn"
                            )
                        }
                    }
                }

                item {
                    TerminalPanel(title = "LOCAL SQLITE DATABASE METRICS") {
                        Text(
                            text = "STORAGE: SQLite Room Persistence Engine",
                            fontFamily = PipMonospace,
                            fontSize = 11.sp,
                            color = PipTextMain
                        )
                        Text(
                            text = "PROJECTS: ${projects.size} | TASKS: ${tasks.size} | RISKS: ${risks.size}",
                            fontFamily = PipMonospace,
                            fontSize = 10.sp,
                            color = PipSecondaryGreen
                        )
                        Text(
                            text = "MILESTONES: ${milestones.size} | TEAM: ${teamMembers.size} | TESTS: ${testCases.size}",
                            fontFamily = PipMonospace,
                            fontSize = 10.sp,
                            color = PipSecondaryGreen
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        TerminalButton(
                            text = "PURGE ALL DATABASE RECORDS",
                            onClick = { showClearConfirmDialog = true },
                            variant = TerminalButtonVariant.DANGER,
                            testTag = "clear_all_data_btn"
                        )
                    }
                }

                item {
                    TerminalPanel(title = "SPM ACADEMIC REFERENCE & MAPPING") {
                        Text(
                            text = "► SOFTWARE PROJECT MANAGEMENT PRINCIPLES:",
                            fontFamily = PipMonospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PipPhosphorGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "1. TRIPLE CONSTRAINT (IRON TRIANGLE): Scope, Time, and Budget trade-offs are explicitly modeled in projects & simulator.",
                            fontFamily = PipMonospace,
                            fontSize = 10.sp,
                            color = PipTextMain
                        )
                        Text(
                            text = "2. RISK MANAGEMENT: Threat exposure = Likelihood x Impact with 1-4/5-12/13-25 thresholds and mitigation plans.",
                            fontFamily = PipMonospace,
                            fontSize = 10.sp,
                            color = PipTextMain
                        )
                        Text(
                            text = "3. QUALITY ASSURANCE: Verification test cases gate pre-flight release readiness without assuming automatic success.",
                            fontFamily = PipMonospace,
                            fontSize = 10.sp,
                            color = PipTextMain
                        )
                        Text(
                            text = "4. BROOKS' LAW & HUMAN RESOURCES: Adding personnel to late projects introduces communication overhead, demonstrated in the survival simulator.",
                            fontFamily = PipMonospace,
                            fontSize = 10.sp,
                            color = PipTextMain
                        )
                    }
                }
            }
        }
    }

    if (showNewMemberDialog) {
        CreateTeamMemberDialog(
            onDismiss = { showNewMemberDialog = false },
            onConfirm = { name, role, email ->
                onCreateTeamMember(name, role, email)
                showNewMemberDialog = false
            }
        )
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            containerColor = PipPanelBg,
            title = {
                Text(
                    text = "► PURGE ENTIRE SQLITE DATABASE?",
                    fontFamily = PipMonospace,
                    fontSize = 14.sp,
                    color = PipCriticalRed,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "WARNING: This will permanently delete all projects, tasks, risks, milestones, and team members from local SQLite storage. This operation is irreversible.",
                    fontFamily = PipMonospace,
                    fontSize = 11.sp,
                    color = PipTextMain
                )
            },
            confirmButton = {
                TerminalButton(
                    text = "CONFIRM COMPLETE PURGE",
                    onClick = {
                        onClearAllData()
                        showClearConfirmDialog = false
                    },
                    variant = TerminalButtonVariant.DANGER,
                    testTag = "confirm_clear_all_btn"
                )
            },
            dismissButton = {
                TerminalButton(text = "CANCEL", onClick = { showClearConfirmDialog = false })
            }
        )
    }
}

@Composable
private fun ReadinessItem(
    title: String,
    status: String,
    description: String,
    isPass: Boolean,
    isUnknown: Boolean = false
) {
    val statusColor = when {
        isUnknown -> PipAmberWarning
        isPass -> PipPhosphorGreen
        else -> PipCriticalRed
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = PipMonospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PipTextMain
            )
            Text(
                text = description,
                fontFamily = PipMonospace,
                fontSize = 10.sp,
                color = PipTextSecondary
            )
        }
        TerminalBadge(
            text = status,
            color = statusColor,
            filled = true
        )
    }
}

@Composable
fun CreateTeamMemberDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, role: String, email: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Lead Architect") }
    var email by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val roleOptions = listOf("Lead Architect", "QA Engineer", "DevOps Specialist", "Frontend Engineer", "Scrum Master", "Product Owner")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PipPanelBg,
        title = {
            Text(
                text = "► ENROLL TEAM PERSONNEL",
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
                    label = { Text("MEMBER NAME *", fontFamily = PipMonospace, fontSize = 10.sp) },
                    singleLine = true,
                    colors = terminalTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("member_name_input")
                )

                Text("SPECIALIZATION ROLE:", fontFamily = PipMonospace, fontSize = 10.sp, color = PipTextSecondary)
                roleOptions.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        row.forEach { r ->
                            val isSel = role == r
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, if (isSel) PipPhosphorGreen else PipBorderGreen)
                                    .background(if (isSel) PipPhosphorGreen.copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable { role = r }
                                    .padding(horizontal = 4.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(r, fontFamily = PipMonospace, fontSize = 9.sp, color = if (isSel) PipPhosphorGreen else PipTextSecondary)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("EMAIL ADDRESS (OPTIONAL)", fontFamily = PipMonospace, fontSize = 10.sp) },
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
                text = "SAVE PERSONNEL",
                onClick = {
                    if (name.isBlank()) {
                        errorMsg = "Member name required."
                        return@TerminalButton
                    }
                    onConfirm(name, role, email)
                },
                testTag = "submit_member_btn"
            )
        },
        dismissButton = {
            TerminalButton(text = "CANCEL", onClick = onDismiss)
        }
    )
}

private fun buildCsvExport(
    projects: List<ProjectEntity>,
    tasks: List<TaskEntity>,
    risks: List<RiskEntity>,
    milestones: List<MilestoneEntity>
): String {
    val sb = StringBuilder()
    sb.append("--- PIP-OS PROJECT DOSSIER AUDIT CSV ---\n\n")

    sb.append("PROJECTS:\n")
    sb.append("ID,Name,Status,Budget\n")
    projects.forEach {
        sb.append("\"${it.id}\",\"${it.name.replace("\"", "\"\"")}\",\"${it.status}\",\"${it.budget ?: "N/A"}\"\n")
    }

    sb.append("\nTASKS:\n")
    sb.append("ID,ProjectID,Title,Status,Priority,IsReleaseBlocker\n")
    tasks.forEach {
        sb.append("\"${it.id}\",\"${it.projectId}\",\"${it.title.replace("\"", "\"\"")}\",\"${it.status}\",\"${it.priority}\",${it.isReleaseBlocker}\n")
    }

    sb.append("\nRISKS:\n")
    sb.append("ID,ProjectID,Title,Likelihood,Impact,Exposure,Severity,Status\n")
    risks.forEach {
        sb.append("\"${it.id}\",\"${it.projectId}\",\"${it.title.replace("\"", "\"\"")}\",${it.likelihood},${it.impact},${it.exposure},\"${it.severityLevel}\",\"${it.status}\"\n")
    }

    sb.append("\nMILESTONES:\n")
    sb.append("ID,ProjectID,Title,Status\n")
    milestones.forEach {
        sb.append("\"${it.id}\",\"${it.projectId}\",\"${it.title.replace("\"", "\"\"")}\",\"${it.status}\"\n")
    }

    return sb.toString()
}

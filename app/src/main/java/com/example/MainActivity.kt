package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pipos.MainViewModel
import com.example.pipos.core.components.PipOSBottomNavigation
import com.example.pipos.core.components.PipOSScreen
import com.example.pipos.core.components.PipOSTopBar
import com.example.pipos.core.theme.CRTScanlineOverlay
import com.example.pipos.features.auth.AuthTerminalScreen
import com.example.pipos.features.auth.attemptAutoSignIn
import com.example.pipos.features.auth.signOut
import com.example.pipos.features.boot.BootSequenceScreen
import com.example.pipos.features.dashboard.CommandCenterScreen
import com.example.pipos.features.projects.CreateProjectDialog
import com.example.pipos.features.projects.ProjectWorkspaceScreen
import com.example.pipos.features.risks.RiskRegisterScreen
import com.example.pipos.features.simulator.ProjectSurvivalSimulatorScreen
import com.example.pipos.features.system.SystemScreen
import com.example.pipos.features.tasks.TaskCommandScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PipPrimaryBg

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PipOSApp()
            }
        }
    }
}

@Composable
fun PipOSApp(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val isBootCompleted by viewModel.isBootCompleted.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val scanlinesEnabled by viewModel.scanlinesEnabled.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isCloudSyncEnabled by viewModel.isCloudSyncEnabled.collectAsState()
    val cloudSyncMessage by viewModel.cloudSyncMessage.collectAsState()

    val projects by viewModel.projects.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val risks by viewModel.risks.collectAsState()
    val milestones by viewModel.milestones.collectAsState()
    val teamMembers by viewModel.teamMembers.collectAsState()
    val overdueTasks by viewModel.overdueTasks.collectAsState()
    val activeProject by viewModel.activeProject.collectAsState()

    val activeScenario by viewModel.activeScenario.collectAsState()
    val simState by viewModel.simState.collectAsState()

    var userChoseOffline by remember { mutableStateOf(false) }

    // Sub-screen overlays from Command Center
    var activeSubScreen by remember { mutableStateOf<String?>(null) } // "TASKS", "RISKS"
    var showDirectCreateProjectDialog by remember { mutableStateOf(false) }

    // Attempt silent auto-sign-in on startup
    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = { /* signed in */ },
            onUnauthenticated = { /* user will see login UI or continue offline */ },
            scope = coroutineScope
        )
    }

    if (!isBootCompleted) {
        BootSequenceScreen(
            onBootFinished = { viewModel.isBootCompleted.value = true }
        )
    } else if (currentUser == null && !userChoseOffline) {
        AuthTerminalScreen(
            onAuthSuccess = { /* User authenticated successfully */ },
            onContinueOffline = { userChoseOffline = true }
        )
    } else {
        // Handle Back navigation cleanly
        BackHandler(enabled = activeSubScreen != null || activeProject != null || currentScreen != PipOSScreen.HOME) {
            when {
                activeSubScreen != null -> activeSubScreen = null
                activeProject != null -> viewModel.selectProject(null)
                currentScreen != PipOSScreen.HOME -> viewModel.currentScreen.value = PipOSScreen.HOME
            }
        }

        val moduleTitle = when {
            activeSubScreen == "TASKS" -> "TASK COMMAND LOG"
            activeSubScreen == "RISKS" -> "THREAT MATRIX"
            currentScreen == PipOSScreen.HOME -> "COMMAND CENTER"
            currentScreen == PipOSScreen.PROJECTS -> if (activeProject != null) "DOSSIER // ${activeProject?.name}" else "PROJECT WORKSPACE"
            currentScreen == PipOSScreen.SIM -> "SURVIVAL SIMULATOR"
            currentScreen == PipOSScreen.SYSTEM -> "SYSTEM GATE & AUDIT"
            else -> "PIP-OS TERMINAL"
        }

        val operatorName = currentUser?.displayName?.take(16) ?: currentUser?.email?.substringBefore("@")

        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PipPrimaryBg),
                topBar = {
                    PipOSTopBar(
                        moduleTitle = moduleTitle,
                        activeProjectName = activeProject?.name,
                        operatorName = operatorName,
                        isCloudConnected = isCloudSyncEnabled
                    )
                },
                bottomBar = {
                    PipOSBottomNavigation(
                        currentScreen = currentScreen,
                        onScreenSelected = { screen ->
                            activeSubScreen = null
                            viewModel.currentScreen.value = screen
                        }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(PipPrimaryBg)
                ) {
                    when {
                        activeSubScreen == "TASKS" -> {
                            TaskCommandScreen(
                                tasks = tasks,
                                projects = projects,
                                teamMembers = teamMembers,
                                onCreateTask = { pId, title, desc, prio, due, mId, isBlocker ->
                                    viewModel.createTask(pId, title, desc, prio, due, mId, isBlocker)
                                },
                                onUpdateTaskStatus = { task, status ->
                                    viewModel.updateTaskStatus(task, status)
                                },
                                onDeleteTask = { task -> viewModel.deleteTask(task) },
                                onNavigateToProjects = {
                                    activeSubScreen = null
                                    viewModel.currentScreen.value = PipOSScreen.PROJECTS
                                }
                            )
                        }

                        activeSubScreen == "RISKS" -> {
                            RiskRegisterScreen(
                                risks = risks,
                                projects = projects,
                                teamMembers = teamMembers,
                                onCreateRisk = { pId, title, desc, like, imp, plan, ownerId ->
                                    viewModel.createRisk(pId, title, desc, like, imp, plan, ownerId)
                                },
                                onUpdateRiskStatus = { risk, status ->
                                    viewModel.updateRiskStatus(risk, status)
                                },
                                onDeleteRisk = { risk -> viewModel.deleteRisk(risk) },
                                onNavigateToProjects = {
                                    activeSubScreen = null
                                    viewModel.currentScreen.value = PipOSScreen.PROJECTS
                                }
                            )
                        }

                        currentScreen == PipOSScreen.HOME -> {
                            CommandCenterScreen(
                                projects = projects,
                                tasks = tasks,
                                risks = risks,
                                milestones = milestones,
                                overdueTasks = overdueTasks,
                                onCreateProjectClick = { showDirectCreateProjectDialog = true },
                                onProjectSelected = { project ->
                                    viewModel.selectProject(project)
                                    viewModel.currentScreen.value = PipOSScreen.PROJECTS
                                },
                                onNavigateToTasks = { activeSubScreen = "TASKS" },
                                onNavigateToRisks = { activeSubScreen = "RISKS" },
                                onNavigateToSim = { viewModel.currentScreen.value = PipOSScreen.SIM }
                            )
                        }

                        currentScreen == PipOSScreen.PROJECTS -> {
                            val activeProjTestCases by if (activeProject != null) {
                                viewModel.getTestCasesForProject(activeProject!!.id).collectAsState(initial = emptyList())
                            } else {
                                remember { mutableStateOf(emptyList()) }
                            }

                            ProjectWorkspaceScreen(
                                projects = projects,
                                activeProject = activeProject,
                                tasks = tasks,
                                risks = risks,
                                milestones = milestones,
                                teamMembers = teamMembers,
                                testCases = activeProjTestCases,
                                onSelectProject = { p -> viewModel.selectProject(p) },
                                onCreateProject = { name, desc, deadline, budget ->
                                    viewModel.createProject(name, desc, deadline, budget)
                                },
                                onUpdateProject = { p -> viewModel.updateProject(p) },
                                onDeleteProject = { p -> viewModel.deleteProject(p) },
                                onCreateTask = { pId, title, desc, prio, due, mId, isBlocker ->
                                    viewModel.createTask(pId, title, desc, prio, due, mId, isBlocker)
                                },
                                onUpdateTaskStatus = { task, status ->
                                    viewModel.updateTaskStatus(task, status)
                                },
                                onDeleteTask = { task -> viewModel.deleteTask(task) },
                                onCreateRisk = { pId, title, desc, like, imp, plan, ownerId ->
                                    viewModel.createRisk(pId, title, desc, like, imp, plan, ownerId)
                                },
                                onUpdateRiskStatus = { risk, status ->
                                    viewModel.updateRiskStatus(risk, status)
                                },
                                onDeleteRisk = { risk -> viewModel.deleteRisk(risk) },
                                onCreateMilestone = { pId, title, desc, date ->
                                    viewModel.createMilestone(pId, title, desc, date)
                                },
                                onUpdateMilestoneStatus = { m, status ->
                                    viewModel.updateMilestoneStatus(m, status)
                                },
                                onDeleteMilestone = { m -> viewModel.deleteMilestone(m) },
                                onCreateTestCase = { pId, name, comp, notes ->
                                    viewModel.createTestCase(pId, name, comp, notes)
                                },
                                onUpdateTestCaseStatus = { t, status ->
                                    viewModel.updateTestCaseStatus(t, status)
                                },
                                onDeleteTestCase = { t -> viewModel.deleteTestCase(t) }
                            )
                        }

                        currentScreen == PipOSScreen.SIM -> {
                            ProjectSurvivalSimulatorScreen(
                                activeScenario = activeScenario,
                                simState = simState,
                                onStartScenario = { scenario -> viewModel.startSimulation(scenario) },
                                onChooseOption = { opt -> viewModel.chooseSimulationOption(opt) },
                                onResetSimulation = { viewModel.resetSimulation() },
                                onExitSimulation = { viewModel.exitSimulation() },
                                onCreateRealProject = { name -> viewModel.createRealProjectFromScenario(name) }
                            )
                        }

                        currentScreen == PipOSScreen.SYSTEM -> {
                            val allTestCases by remember { mutableStateOf(emptyList<com.example.pipos.data.local.entity.TestCaseEntity>()) }
                            SystemScreen(
                                projects = projects,
                                tasks = tasks,
                                risks = risks,
                                milestones = milestones,
                                teamMembers = teamMembers,
                                testCases = allTestCases,
                                currentUser = currentUser,
                                cloudSyncMessage = cloudSyncMessage,
                                onSyncAllToCloud = { viewModel.syncAllToCloud() },
                                onRestoreFromCloud = { viewModel.restoreFromCloud() },
                                onSignInClick = { userChoseOffline = false },
                                onSignOutClick = {
                                    signOut(
                                        context = context,
                                        credentialManager = credentialManager,
                                        onSignOutComplete = { userChoseOffline = true },
                                        scope = coroutineScope
                                    )
                                },
                                scanlinesEnabled = scanlinesEnabled,
                                onToggleScanlines = { viewModel.scanlinesEnabled.value = it },
                                onCreateTeamMember = { name, role, email ->
                                    viewModel.createTeamMember(name, role, email)
                                },
                                onDeleteTeamMember = { m -> viewModel.deleteTeamMember(m) },
                                onClearAllData = { viewModel.clearAllData() }
                            )
                        }
                    }
                }
            }

            // CRT Scanline raster effect layer
            if (scanlinesEnabled) {
                CRTScanlineOverlay(
                    enabled = true,
                    alpha = 0.05f
                )
            }

            if (showDirectCreateProjectDialog) {
                CreateProjectDialog(
                    onDismiss = { showDirectCreateProjectDialog = false },
                    onConfirm = { name, desc, deadline, budget ->
                        viewModel.createProject(name, desc, deadline, budget)
                        showDirectCreateProjectDialog = false
                    }
                )
            }
        }
    }
}

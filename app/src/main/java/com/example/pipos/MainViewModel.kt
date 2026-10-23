package com.example.pipos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pipos.core.components.PipOSScreen
import com.example.pipos.data.local.PipOSDatabase
import com.example.pipos.data.local.entity.MilestoneEntity
import com.example.pipos.data.local.entity.ProjectEntity
import com.example.pipos.data.local.entity.RiskEntity
import com.example.pipos.data.local.entity.TaskEntity
import com.example.pipos.data.local.entity.TeamMemberEntity
import com.example.pipos.data.local.entity.TestCaseEntity
import com.example.pipos.data.remote.FirestoreSyncManager
import com.example.pipos.data.repository.PipOSRepository
import com.example.pipos.features.simulator.ScenarioDefinition
import com.example.pipos.features.simulator.SimDecisionRecord
import com.example.pipos.features.simulator.SimOption
import com.example.pipos.features.simulator.SimState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PipOSRepository(PipOSDatabase.getDatabase(application))
    val firestoreSyncManager = FirestoreSyncManager(application)

    // Navigation and Boot state
    val isBootCompleted = MutableStateFlow(false)
    val currentScreen = MutableStateFlow(PipOSScreen.HOME)
    val scanlinesEnabled = MutableStateFlow(true)
    val activeProject = MutableStateFlow<ProjectEntity?>(null)

    // Firebase Auth State
    val currentUser = MutableStateFlow<FirebaseUser?>(Firebase.auth.currentUser)
    val isCloudSyncEnabled = MutableStateFlow(Firebase.auth.currentUser != null)
    val cloudSyncMessage = MutableStateFlow<String?>(null)

    private val authListener = FirebaseAuth.AuthStateListener { auth ->
        val user = auth.currentUser
        currentUser.value = user
        isCloudSyncEnabled.value = user != null
    }

    init {
        Firebase.auth.addAuthStateListener(authListener)
    }

    override fun onCleared() {
        super.onCleared()
        Firebase.auth.removeAuthStateListener(authListener)
    }

    // Reactive streams from SQLite Room Database
    val projects: StateFlow<List<ProjectEntity>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val risks: StateFlow<List<RiskEntity>> = repository.allRisks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val milestones: StateFlow<List<MilestoneEntity>> = repository.allMilestones
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val teamMembers: StateFlow<List<TeamMemberEntity>> = repository.allTeamMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val overdueTasks: StateFlow<List<TaskEntity>> = repository.getOverdueTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Simulation Engine State (STRICTLY HYPOTHETICAL & ISOLATED)
    val activeScenario = MutableStateFlow<ScenarioDefinition?>(null)
    val simState = MutableStateFlow<SimState?>(null)

    // Project Operations
    fun createProject(name: String, description: String, deadlineDate: Long?, budget: Double?) {
        viewModelScope.launch {
            val project = ProjectEntity(
                name = name.trim(),
                description = description.trim(),
                status = "ACTIVE",
                startDate = System.currentTimeMillis(),
                deadlineDate = deadlineDate,
                budget = budget
            )
            repository.insertProject(project)
            activeProject.value = project

            if (isCloudSyncEnabled.value) {
                firestoreSyncManager.syncProjectToCloud(project)
            }
        }
    }

    fun updateProject(project: ProjectEntity) {
        viewModelScope.launch {
            val updated = project.copy(updatedAt = System.currentTimeMillis())
            repository.updateProject(updated)
            if (activeProject.value?.id == project.id) {
                activeProject.value = updated
            }
            if (isCloudSyncEnabled.value) {
                firestoreSyncManager.syncProjectToCloud(updated)
            }
        }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch {
            repository.deleteProject(project)
            if (activeProject.value?.id == project.id) {
                activeProject.value = null
            }
            if (isCloudSyncEnabled.value) {
                firestoreSyncManager.deleteProjectFromCloud(project.id)
            }
        }
    }

    fun selectProject(project: ProjectEntity?) {
        activeProject.value = project
    }

    // Task Operations
    fun createTask(
        projectId: String,
        title: String,
        description: String,
        priority: String,
        dueDate: Long?,
        assignedMemberId: String?,
        isReleaseBlocker: Boolean
    ) {
        viewModelScope.launch {
            val task = TaskEntity(
                projectId = projectId,
                title = title.trim(),
                description = description.trim(),
                status = "TODO",
                priority = priority,
                dueDate = dueDate,
                assignedMemberId = assignedMemberId,
                isReleaseBlocker = isReleaseBlocker
            )
            repository.insertTask(task)
            if (isCloudSyncEnabled.value) {
                firestoreSyncManager.syncTaskToCloud(task)
            }
        }
    }

    fun updateTaskStatus(task: TaskEntity, newStatus: String) {
        viewModelScope.launch {
            val updated = task.copy(status = newStatus, updatedAt = System.currentTimeMillis())
            repository.updateTask(updated)
            if (isCloudSyncEnabled.value) {
                firestoreSyncManager.syncTaskToCloud(updated)
            }
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // Risk Operations
    fun createRisk(
        projectId: String,
        title: String,
        description: String,
        likelihood: Int,
        impact: Int,
        mitigationPlan: String,
        ownerMemberId: String?
    ) {
        viewModelScope.launch {
            val risk = RiskEntity(
                projectId = projectId,
                title = title.trim(),
                description = description.trim(),
                likelihood = likelihood.coerceIn(1, 5),
                impact = impact.coerceIn(1, 5),
                mitigationPlan = mitigationPlan.trim(),
                ownerMemberId = ownerMemberId,
                status = "OPEN"
            )
            repository.insertRisk(risk)
            if (isCloudSyncEnabled.value) {
                firestoreSyncManager.syncRiskToCloud(risk)
            }
        }
    }

    fun updateRiskStatus(risk: RiskEntity, newStatus: String) {
        viewModelScope.launch {
            val updated = risk.copy(status = newStatus, updatedAt = System.currentTimeMillis())
            repository.updateRisk(updated)
            if (isCloudSyncEnabled.value) {
                firestoreSyncManager.syncRiskToCloud(updated)
            }
        }
    }

    fun deleteRisk(risk: RiskEntity) {
        viewModelScope.launch {
            repository.deleteRisk(risk)
        }
    }

    // Milestone Operations
    fun createMilestone(
        projectId: String,
        title: String,
        description: String,
        targetDate: Long
    ) {
        viewModelScope.launch {
            val milestone = MilestoneEntity(
                projectId = projectId,
                title = title.trim(),
                description = description.trim(),
                targetDate = targetDate,
                status = "PENDING"
            )
            repository.insertMilestone(milestone)
            if (isCloudSyncEnabled.value) {
                firestoreSyncManager.syncMilestoneToCloud(milestone)
            }
        }
    }

    fun updateMilestoneStatus(milestone: MilestoneEntity, newStatus: String) {
        viewModelScope.launch {
            val updated = milestone.copy(status = newStatus)
            repository.updateMilestone(updated)
            if (isCloudSyncEnabled.value) {
                firestoreSyncManager.syncMilestoneToCloud(updated)
            }
        }
    }

    fun deleteMilestone(milestone: MilestoneEntity) {
        viewModelScope.launch {
            repository.deleteMilestone(milestone)
        }
    }

    // Team Member Operations
    fun createTeamMember(name: String, role: String, email: String) {
        viewModelScope.launch {
            val member = TeamMemberEntity(
                name = name.trim(),
                role = role.trim(),
                email = email.trim()
            )
            repository.insertTeamMember(member)
            if (isCloudSyncEnabled.value) {
                firestoreSyncManager.syncTeamMemberToCloud(member)
            }
        }
    }

    fun deleteTeamMember(member: TeamMemberEntity) {
        viewModelScope.launch {
            repository.deleteTeamMember(member)
        }
    }

    // Test Case Operations
    fun createTestCase(projectId: String, name: String, component: String, notes: String) {
        viewModelScope.launch {
            val testCase = TestCaseEntity(
                projectId = projectId,
                name = name.trim(),
                component = component.trim(),
                status = "UNTESTED",
                notes = notes.trim()
            )
            repository.insertTestCase(testCase)
        }
    }

    fun updateTestCaseStatus(testCase: TestCaseEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updateTestCase(testCase.copy(status = newStatus, testedAt = System.currentTimeMillis()))
        }
    }

    fun deleteTestCase(testCase: TestCaseEntity) {
        viewModelScope.launch {
            repository.deleteTestCase(testCase)
        }
    }

    fun getTestCasesForProject(projectId: String) = repository.getTestCasesByProject(projectId)

    // Complete Database Wipe (Clear all data)
    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            activeProject.value = null
        }
    }

    // Cloud Backup & Restore
    fun syncAllToCloud() {
        viewModelScope.launch {
            cloudSyncMessage.value = "UPLOADING ALL WORKSPACES TO FIRESTORE..."
            var count = 0
            projects.value.forEach {
                firestoreSyncManager.syncProjectToCloud(it)
                count++
            }
            tasks.value.forEach {
                firestoreSyncManager.syncTaskToCloud(it)
            }
            risks.value.forEach {
                firestoreSyncManager.syncRiskToCloud(it)
            }
            milestones.value.forEach {
                firestoreSyncManager.syncMilestoneToCloud(it)
            }
            teamMembers.value.forEach {
                firestoreSyncManager.syncTeamMemberToCloud(it)
            }
            cloudSyncMessage.value = "CLOUD SYNC COMPLETE: $count WORKSPACES BACKED UP."
        }
    }

    fun restoreFromCloud() {
        viewModelScope.launch {
            cloudSyncMessage.value = "DOWNLOADING WORKSPACES FROM FIRESTORE..."
            val result = firestoreSyncManager.fetchCloudProjects()
            result.onSuccess { cloudProjs ->
                cloudProjs.forEach {
                    repository.insertProject(it)
                }
                cloudSyncMessage.value = "RESTORE COMPLETE: ${cloudProjs.size} WORKSPACES RESTORED."
            }.onFailure {
                cloudSyncMessage.value = "RESTORE FAILED: ${it.localizedMessage}"
            }
        }
    }

    // Simulation Engine Controls (Separated from Real Workspace)
    fun startSimulation(scenario: ScenarioDefinition) {
        activeScenario.value = scenario
        simState.value = SimState(
            scheduleDaysRemaining = scenario.initialDays,
            initialScheduleDays = scenario.initialDays,
            budgetRemaining = scenario.initialBudget,
            initialBudget = scenario.initialBudget,
            scopePercent = scenario.initialScope,
            qualityIndex = scenario.initialQuality,
            riskExposure = scenario.initialRisk,
            currentDecisionIndex = 0,
            decisionHistory = emptyList(),
            isFinished = false
        )
    }

    fun chooseSimulationOption(option: SimOption) {
        val current = simState.value ?: return
        val scenario = activeScenario.value ?: return
        val currentStep = scenario.steps.getOrNull(current.currentDecisionIndex) ?: return

        val newHistory = current.decisionHistory + SimDecisionRecord(
            promptText = currentStep.incidentTitle,
            chosenOptionTitle = option.title,
            consequenceExplanation = option.explanation,
            spmConcept = option.spmConcept
        )

        val nextIndex = current.currentDecisionIndex + 1
        val isFinished = nextIndex >= scenario.steps.size

        simState.value = current.copy(
            scheduleDaysRemaining = (current.scheduleDaysRemaining + option.scheduleDeltaDays).coerceAtLeast(0),
            budgetRemaining = (current.budgetRemaining + option.budgetDelta).coerceAtLeast(0.0),
            scopePercent = (current.scopePercent + option.scopeDeltaPercent).coerceIn(0, 100),
            qualityIndex = (current.qualityIndex + option.qualityDeltaIndex).coerceIn(0, 100),
            riskExposure = (current.riskExposure + option.riskDelta).coerceIn(1, 25),
            currentDecisionIndex = nextIndex,
            decisionHistory = newHistory,
            isFinished = isFinished
        )
    }

    fun resetSimulation() {
        val scenario = activeScenario.value
        if (scenario != null) {
            startSimulation(scenario)
        } else {
            simState.value = null
        }
    }

    fun exitSimulation() {
        activeScenario.value = null
        simState.value = null
    }

    fun createRealProjectFromScenario(projectName: String) {
        val scenario = activeScenario.value ?: return
        viewModelScope.launch {
            val newProject = ProjectEntity(
                name = projectName.ifBlank { "SIM: ${scenario.title}" },
                description = "Initialized from educational simulation scenario: ${scenario.subtitle}",
                status = "ACTIVE",
                startDate = System.currentTimeMillis(),
                deadlineDate = System.currentTimeMillis() + (scenario.initialDays * 86400000L),
                budget = scenario.initialBudget
            )
            repository.insertProject(newProject)
            activeProject.value = newProject
            currentScreen.value = PipOSScreen.PROJECTS
            exitSimulation()
            if (isCloudSyncEnabled.value) {
                firestoreSyncManager.syncProjectToCloud(newProject)
            }
        }
    }
}

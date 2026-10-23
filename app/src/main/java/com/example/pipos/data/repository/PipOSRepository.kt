package com.example.pipos.data.repository

import com.example.pipos.data.local.PipOSDatabase
import com.example.pipos.data.local.entity.MilestoneEntity
import com.example.pipos.data.local.entity.ProjectEntity
import com.example.pipos.data.local.entity.RiskEntity
import com.example.pipos.data.local.entity.TaskEntity
import com.example.pipos.data.local.entity.TeamMemberEntity
import com.example.pipos.data.local.entity.TestCaseEntity
import kotlinx.coroutines.flow.Flow

class PipOSRepository(private val database: PipOSDatabase) {
    private val projectDao = database.projectDao()
    private val taskDao = database.taskDao()
    private val riskDao = database.riskDao()
    private val milestoneDao = database.milestoneDao()
    private val teamMemberDao = database.teamMemberDao()
    private val testCaseDao = database.testCaseDao()

    // Projects
    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()
    fun getProject(id: String): Flow<ProjectEntity?> = projectDao.getProjectById(id)
    suspend fun insertProject(project: ProjectEntity) = projectDao.insertProject(project)
    suspend fun updateProject(project: ProjectEntity) = projectDao.updateProject(project)
    suspend fun deleteProject(project: ProjectEntity) = projectDao.deleteProject(project)
    suspend fun deleteProjectById(id: String) = projectDao.deleteProjectById(id)

    // Tasks
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    fun getTasksByProject(projectId: String): Flow<List<TaskEntity>> = taskDao.getTasksByProject(projectId)
    fun getOverdueTasks(currentTime: Long = System.currentTimeMillis()): Flow<List<TaskEntity>> =
        taskDao.getOverdueTasks(currentTime)
    suspend fun insertTask(task: TaskEntity) = taskDao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)
    suspend fun deleteTaskById(id: String) = taskDao.deleteTaskById(id)

    // Risks
    val allRisks: Flow<List<RiskEntity>> = riskDao.getAllRisks()
    fun getRisksByProject(projectId: String): Flow<List<RiskEntity>> = riskDao.getRisksByProject(projectId)
    suspend fun insertRisk(risk: RiskEntity) = riskDao.insertRisk(risk)
    suspend fun updateRisk(risk: RiskEntity) = riskDao.updateRisk(risk)
    suspend fun deleteRisk(risk: RiskEntity) = riskDao.deleteRisk(risk)
    suspend fun deleteRiskById(id: String) = riskDao.deleteRiskById(id)

    // Milestones
    val allMilestones: Flow<List<MilestoneEntity>> = milestoneDao.getAllMilestones()
    fun getMilestonesByProject(projectId: String): Flow<List<MilestoneEntity>> =
        milestoneDao.getMilestonesByProject(projectId)
    suspend fun insertMilestone(milestone: MilestoneEntity) = milestoneDao.insertMilestone(milestone)
    suspend fun updateMilestone(milestone: MilestoneEntity) = milestoneDao.updateMilestone(milestone)
    suspend fun deleteMilestone(milestone: MilestoneEntity) = milestoneDao.deleteMilestone(milestone)
    suspend fun deleteMilestoneById(id: String) = milestoneDao.deleteMilestoneById(id)

    // Team Members
    val allTeamMembers: Flow<List<TeamMemberEntity>> = teamMemberDao.getAllTeamMembers()
    suspend fun insertTeamMember(member: TeamMemberEntity) = teamMemberDao.insertTeamMember(member)
    suspend fun updateTeamMember(member: TeamMemberEntity) = teamMemberDao.updateTeamMember(member)
    suspend fun deleteTeamMember(member: TeamMemberEntity) = teamMemberDao.deleteTeamMember(member)
    suspend fun deleteTeamMemberById(id: String) = teamMemberDao.deleteTeamMemberById(id)

    // Test Cases
    fun getTestCasesByProject(projectId: String): Flow<List<TestCaseEntity>> =
        testCaseDao.getTestCasesByProject(projectId)
    suspend fun insertTestCase(testCase: TestCaseEntity) = testCaseDao.insertTestCase(testCase)
    suspend fun updateTestCase(testCase: TestCaseEntity) = testCaseDao.updateTestCase(testCase)
    suspend fun deleteTestCase(testCase: TestCaseEntity) = testCaseDao.deleteTestCase(testCase)

    // Total database reset (explicit user confirmation only)
    suspend fun clearAllData() {
        database.clearAllTables()
    }
}

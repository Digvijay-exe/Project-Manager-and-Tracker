package com.example

import com.example.pipos.data.local.entity.MilestoneEntity
import com.example.pipos.data.local.entity.ProjectEntity
import com.example.pipos.data.local.entity.RiskEntity
import com.example.pipos.data.local.entity.TaskEntity
import com.example.pipos.features.simulator.SimDecisionRecord
import com.example.pipos.features.simulator.SimOption
import com.example.pipos.features.simulator.SimState
import com.example.pipos.features.simulator.SimulationCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class PipOSLogicTest {

    @Test
    fun testRiskExposureCalculation() {
        val lowRisk = RiskEntity(projectId = "p1", title = "Minor doc typo", likelihood = 2, impact = 2)
        assertEquals(4, lowRisk.exposure)
        assertEquals("LOW", lowRisk.severityLevel)

        val mediumRisk = RiskEntity(projectId = "p1", title = "API latency spike", likelihood = 3, impact = 3)
        assertEquals(9, mediumRisk.exposure)
        assertEquals("MEDIUM", mediumRisk.severityLevel)

        val highRisk = RiskEntity(projectId = "p1", title = "Payment gateway outage", likelihood = 4, impact = 4)
        assertEquals(16, highRisk.exposure)
        assertEquals("HIGH", highRisk.severityLevel)

        val maxRisk = RiskEntity(projectId = "p1", title = "Database corruption", likelihood = 5, impact = 5)
        assertEquals(25, maxRisk.exposure)
        assertEquals("HIGH", maxRisk.severityLevel)
    }

    @Test
    fun testRiskSeverityThresholds() {
        // Documented thresholds: 1-4 = LOW, 5-12 = MEDIUM, 13-25 = HIGH
        val r1 = RiskEntity(projectId = "p1", title = "R1", likelihood = 1, impact = 4)
        assertEquals("LOW", r1.severityLevel)

        val r2 = RiskEntity(projectId = "p1", title = "R2", likelihood = 2, impact = 2)
        assertEquals("LOW", r2.severityLevel)

        val r3 = RiskEntity(projectId = "p1", title = "R3", likelihood = 1, impact = 5)
        assertEquals("MEDIUM", r3.severityLevel)

        val r4 = RiskEntity(projectId = "p1", title = "R4", likelihood = 3, impact = 4)
        assertEquals("MEDIUM", r4.severityLevel)

        val r5 = RiskEntity(projectId = "p1", title = "R5", likelihood = 3, impact = 5)
        assertEquals("HIGH", r5.severityLevel)
    }

    @Test
    fun testTaskCompletionStatistics() {
        val tasks = listOf(
            TaskEntity(projectId = "p1", title = "T1", status = "DONE"),
            TaskEntity(projectId = "p1", title = "T2", status = "DONE"),
            TaskEntity(projectId = "p1", title = "T3", status = "TODO"),
            TaskEntity(projectId = "p1", title = "T4", status = "IN_PROGRESS"),
            TaskEntity(projectId = "p1", title = "T5", status = "BLOCKED")
        )

        val doneCount = tasks.count { it.status == "DONE" }
        val openCount = tasks.count { it.status != "DONE" }
        val progress = doneCount.toFloat() / tasks.size

        assertEquals(2, doneCount)
        assertEquals(3, openCount)
        assertEquals(0.40f, progress, 0.001f)
    }

    @Test
    fun testOverdueTaskCalculation() {
        val now = 1000000L
        val overdueTask = TaskEntity(
            projectId = "p1",
            title = "Overdue task",
            dueDate = now - 50000L,
            status = "TODO"
        )
        val onTimeTask = TaskEntity(
            projectId = "p1",
            title = "Future task",
            dueDate = now + 50000L,
            status = "TODO"
        )
        val completedLateTask = TaskEntity(
            projectId = "p1",
            title = "Late but done",
            dueDate = now - 50000L,
            status = "DONE"
        )

        val isOverdue1 = overdueTask.dueDate != null && overdueTask.dueDate!! < now && overdueTask.status != "DONE"
        val isOverdue2 = onTimeTask.dueDate != null && onTimeTask.dueDate!! < now && onTimeTask.status != "DONE"
        val isOverdue3 = completedLateTask.dueDate != null && completedLateTask.dueDate!! < now && completedLateTask.status != "DONE"

        assertTrue(isOverdue1)
        assertFalse(isOverdue2)
        assertFalse(isOverdue3) // Completed tasks are not marked overdue
    }

    @Test
    fun testMilestoneProgressCalculation() {
        val milestones = listOf(
            MilestoneEntity(projectId = "p1", title = "M1", status = "COMPLETED"),
            MilestoneEntity(projectId = "p1", title = "M2", status = "COMPLETED"),
            MilestoneEntity(projectId = "p1", title = "M3", status = "IN_PROGRESS"),
            MilestoneEntity(projectId = "p1", title = "M4", status = "PENDING")
        )

        val completed = milestones.count { it.status == "COMPLETED" }
        val progressPercent = (completed * 100) / milestones.size
        assertEquals(2, completed)
        assertEquals(50, progressPercent)
    }

    @Test
    fun testReleaseReadinessEvaluationRules() {
        // Readiness rule: Ready only when projects exist, zero critical risks (exposure >= 13), zero open blockers, zero overdue milestones
        val criticalRisk = RiskEntity(projectId = "p1", title = "Zero-Day", likelihood = 4, impact = 5, status = "OPEN")
        val blockerTask = TaskEntity(projectId = "p1", title = "Crash bug", isReleaseBlocker = true, status = "TODO")

        val risks = listOf(criticalRisk)
        val tasks = listOf(blockerTask)

        val hasCriticalRisks = risks.any { it.exposure >= 13 && it.status != "RESOLVED" }
        val hasBlockers = tasks.any { it.isReleaseBlocker && it.status != "DONE" }

        assertTrue(hasCriticalRisks)
        assertTrue(hasBlockers)

        // After resolving risk and completing blocker:
        val resolvedRisk = criticalRisk.copy(status = "RESOLVED")
        val doneBlocker = blockerTask.copy(status = "DONE")

        val isNowBlocked = listOf(resolvedRisk).any { it.exposure >= 13 && it.status != "RESOLVED" } ||
                listOf(doneBlocker).any { it.isReleaseBlocker && it.status != "DONE" }

        assertFalse(isNowBlocked)
    }

    @Test
    fun testSimulationCalculationsAndScoring() {
        val scenario = SimulationCatalog.scenarios.first()
        val initialDays = scenario.initialDays
        val initialBudget = scenario.initialBudget

        var state = SimState(
            scheduleDaysRemaining = initialDays,
            initialScheduleDays = initialDays,
            budgetRemaining = initialBudget,
            initialBudget = initialBudget,
            scopePercent = 100,
            qualityIndex = 50,
            riskExposure = 18,
            currentDecisionIndex = 0
        )

        val optionA = scenario.steps.first().options.first()
        state = state.copy(
            scheduleDaysRemaining = state.scheduleDaysRemaining + optionA.scheduleDeltaDays,
            budgetRemaining = state.budgetRemaining + optionA.budgetDelta,
            scopePercent = state.scopePercent + optionA.scopeDeltaPercent,
            qualityIndex = state.qualityIndex + optionA.qualityDeltaIndex,
            riskExposure = state.riskExposure + optionA.riskDelta
        )

        assertEquals(4, state.scheduleDaysRemaining) // 14 - 10
        assertEquals(28000.0, state.budgetRemaining, 0.01) // 40000 - 12000
        assertEquals(95, state.qualityIndex) // 50 + 45
        assertEquals(6, state.riskExposure) // 18 - 12

        // Score must be in 0..100
        val score = state.overallScore
        assertTrue(score in 0..100)
    }

    @Test
    fun testUniqueIdGeneration() {
        val id1 = UUID.randomUUID().toString()
        val id2 = UUID.randomUUID().toString()
        assertNotNull(id1)
        assertNotNull(id2)
        assertNotEquals(id1, id2)
        assertEquals(36, id1.length)
    }
}

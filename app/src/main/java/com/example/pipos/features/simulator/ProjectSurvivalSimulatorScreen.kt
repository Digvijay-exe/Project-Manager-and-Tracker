package com.example.pipos.features.simulator

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.pipos.core.components.TerminalBadge
import com.example.pipos.core.components.TerminalButton
import com.example.pipos.core.components.TerminalButtonVariant
import com.example.pipos.core.components.TerminalPanel
import com.example.pipos.features.projects.terminalTextFieldColors
import com.example.ui.theme.PipAmberWarning
import com.example.ui.theme.PipBorderGreen
import com.example.ui.theme.PipCriticalRed
import com.example.ui.theme.PipElevatedPanel
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
fun ProjectSurvivalSimulatorScreen(
    activeScenario: ScenarioDefinition?,
    simState: SimState?,
    onStartScenario: (ScenarioDefinition) -> Unit,
    onChooseOption: (SimOption) -> Unit,
    onResetSimulation: () -> Unit,
    onExitSimulation: () -> Unit,
    onCreateRealProject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showConvertDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PipPrimaryBg)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // MANDATORY PERSISTENT SIMULATION BANNER
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PipAmberWarning.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp))
                    .border(1.dp, PipAmberWarning.copy(alpha = 0.6f), shape = RoundedCornerShape(8.dp))
                    .padding(vertical = 10.dp, horizontal = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "⚠ SIMULATION MODE — HYPOTHETICAL DATA",
                            fontFamily = PipMonospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = PipAmberWarning,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "ISOLATED SANDBOX FOR SOFTWARE PROJECT MANAGEMENT EDUCATION",
                            fontFamily = PipMonospace,
                            fontSize = 9.sp,
                            color = PipTextSecondary
                        )
                    }
                    if (simState != null) {
                        TerminalButton(
                            text = "EXIT SIM",
                            onClick = onExitSimulation,
                            variant = TerminalButtonVariant.SECONDARY,
                            testTag = "sim_exit_btn"
                        )
                    }
                }
            }
        }

        if (activeScenario == null || simState == null) {
            // Scenario Selection Menu
            item {
                TerminalPanel(title = "SIMULATOR // SCENARIO CATALOG") {
                    Text(
                        text = "SELECT AN SPM CRISIS SCENARIO TO TEST CRISIS RESOLUTION, TRADE-OFFS & TRIAGE SKILLS:",
                        fontFamily = PipMonospace,
                        fontSize = 11.sp,
                        color = PipTextSecondary
                    )
                }
            }

            items(SimulationCatalog.scenarios, key = { it.id }) { scenario ->
                TerminalPanel(
                    title = scenario.title,
                    statusIndicator = "[${scenario.initialDays} DAYS]"
                ) {
                    Text(
                        text = scenario.subtitle,
                        fontFamily = PipMonospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PipSecondaryGreen
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "CHALLENGE: ${scenario.coreChallenge}",
                        fontFamily = PipMonospace,
                        fontSize = 11.sp,
                        color = PipTextMain
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "KEY SPM CONCEPT: ${scenario.primarySpmConcept}",
                        fontFamily = PipMonospace,
                        fontSize = 10.sp,
                        color = PipInfoCyan
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BUDGET: $${scenario.initialBudget.toInt()} | STEPS: ${scenario.steps.size}",
                            fontFamily = PipMonospace,
                            fontSize = 10.sp,
                            color = PipTextSecondary
                        )

                        TerminalButton(
                            text = "COMMENCE SIM ►",
                            onClick = { onStartScenario(scenario) },
                            testTag = "start_scenario_${scenario.id}"
                        )
                    }
                }
            }
        } else if (simState.isFinished) {
            // Final Simulation Outcome & Post-Mortem Report
            item {
                TerminalPanel(
                    title = "SIMULATION TERMINATED // POST-MORTEM DOSSIER",
                    borderColor = if (simState.overallScore >= 70) PipPhosphorGreen else PipAmberWarning
                ) {
                    Text(
                        text = "FINAL SCORE: ${simState.overallScore} / 100",
                        fontFamily = PipMonospace,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (simState.overallScore >= 70) PipPhosphorGreen else PipAmberWarning
                    )
                    Text(
                        text = "VERDICT: ${simState.outcomeGrade}",
                        fontFamily = PipMonospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PipTextMain
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Final Metrics Breakdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MetricHUD(
                            label = "DAYS REMAINING",
                            value = "${simState.scheduleDaysRemaining}d",
                            color = if (simState.scheduleDaysRemaining > 0) PipPhosphorGreen else PipCriticalRed,
                            modifier = Modifier.weight(1f)
                        )
                        MetricHUD(
                            label = "BUDGET LEFT",
                            value = "$${simState.budgetRemaining.toInt()}",
                            color = if (simState.budgetRemaining > 0) PipPhosphorGreen else PipCriticalRed,
                            modifier = Modifier.weight(1f)
                        )
                        MetricHUD(
                            label = "QUALITY INDEX",
                            value = "${simState.qualityIndex}%",
                            color = if (simState.qualityIndex >= 70) PipPhosphorGreen else PipAmberWarning,
                            modifier = Modifier.weight(1f)
                        )
                        MetricHUD(
                            label = "RESIDUAL RISK",
                            value = "${simState.riskExposure}/25",
                            color = if (simState.riskExposure <= 10) PipPhosphorGreen else PipCriticalRed,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "► SCORING FORMULA SPECIFICATION:",
                        fontFamily = PipMonospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PipPhosphorGreen
                    )
                    Text(
                        text = "Score = (Schedule Efficiency * 25%) + (Budget Preservation * 25%) + (Scope Delivery * 25%) + (Quality Index * 25%) - (Residual Risk Penalty * 20%)",
                        fontFamily = PipMonospace,
                        fontSize = 9.sp,
                        color = PipTextSecondary,
                        lineHeight = 13.sp
                    )
                }
            }

            // Step-by-Step Decision History
            item {
                TerminalPanel(title = "DECISION AUDIT TRAIL") {
                    simState.decisionHistory.forEachIndexed { i, record ->
                        Text(
                            text = "${i + 1}. ${record.promptText}",
                            fontFamily = PipMonospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PipPhosphorGreen
                        )
                        Text(
                            text = "   CHOICE: ${record.chosenOptionTitle}",
                            fontFamily = PipMonospace,
                            fontSize = 10.sp,
                            color = PipSecondaryGreen
                        )
                        Text(
                            text = "   CONSEQUENCE: ${record.consequenceExplanation}",
                            fontFamily = PipMonospace,
                            fontSize = 10.sp,
                            color = PipTextMain
                        )
                        Text(
                            text = "   SPM CONCEPT: ${record.spmConcept}",
                            fontFamily = PipMonospace,
                            fontSize = 9.sp,
                            color = PipInfoCyan
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }

            // End Actions
            item {
                TerminalPanel(title = "ACTIONS") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TerminalButton(
                            text = "RESTART SIM",
                            onClick = onResetSimulation,
                            modifier = Modifier.weight(1f),
                            testTag = "sim_restart_btn"
                        )
                        TerminalButton(
                            text = "CREATE REAL PRJ",
                            onClick = { showConvertDialog = true },
                            modifier = Modifier.weight(1f),
                            variant = TerminalButtonVariant.WARNING,
                            testTag = "sim_convert_real_btn"
                        )
                    }
                }
            }
        } else {
            // Live Simulation Interactive Step
            val step = activeScenario.steps.getOrNull(simState.currentDecisionIndex)
            if (step != null) {
                // Tactical HUD Gauges
                item {
                    TerminalPanel(title = "HYPOTHETICAL TELEMETRY HUD") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            MetricHUD(
                                label = "SCHEDULE",
                                value = "${simState.scheduleDaysRemaining}d",
                                color = if (simState.scheduleDaysRemaining > 3) PipPhosphorGreen else PipCriticalRed,
                                modifier = Modifier.weight(1f)
                            )
                            MetricHUD(
                                label = "BUDGET",
                                value = "$${simState.budgetRemaining.toInt()}",
                                color = if (simState.budgetRemaining > 5000) PipPhosphorGreen else PipAmberWarning,
                                modifier = Modifier.weight(1f)
                            )
                            MetricHUD(
                                label = "SCOPE",
                                value = "${simState.scopePercent}%",
                                color = PipInfoCyan,
                                modifier = Modifier.weight(1f)
                            )
                            MetricHUD(
                                label = "QUALITY",
                                value = "${simState.qualityIndex}",
                                color = if (simState.qualityIndex >= 60) PipPhosphorGreen else PipAmberWarning,
                                modifier = Modifier.weight(1f)
                            )
                            MetricHUD(
                                label = "THREAT",
                                value = "${simState.riskExposure}",
                                color = if (simState.riskExposure >= 13) PipCriticalRed else PipSecondaryGreen,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Incident Details
                item {
                    TerminalPanel(
                        title = step.incidentTitle,
                        borderColor = PipAmberWarning
                    ) {
                        Text(
                            text = step.incidentDescription,
                            fontFamily = PipMonospace,
                            fontSize = 12.sp,
                            color = PipTextMain,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "KNOWN ASSUMPTIONS: ${step.initialAssumptions}",
                            fontFamily = PipMonospace,
                            fontSize = 10.sp,
                            color = PipSecondaryGreen
                        )
                    }
                }

                // Decision Options
                item {
                    Text(
                        text = "► AVAILABLE STRATEGIC RESPONSES:",
                        fontFamily = PipMonospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PipPhosphorGreen
                    )
                }

                items(step.options, key = { it.key }) { opt ->
                    TerminalPanel(
                        title = "[ OPTION ${opt.key} ] ${opt.title}",
                        borderColor = PipBorderGreen
                    ) {
                        Text(
                            text = opt.description,
                            fontFamily = PipMonospace,
                            fontSize = 11.sp,
                            color = PipTextMain,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Delta Preview
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "SCHED: ${if (opt.scheduleDeltaDays >= 0) "+${opt.scheduleDeltaDays}" else opt.scheduleDeltaDays}d | COST: $${opt.budgetDelta.toInt()}",
                                fontFamily = PipMonospace,
                                fontSize = 9.sp,
                                color = PipAmberWarning
                            )
                            Text(
                                text = "QUAL: ${if (opt.qualityDeltaIndex >= 0) "+${opt.qualityDeltaIndex}" else opt.qualityDeltaIndex} | THREAT: ${if (opt.riskDelta >= 0) "+${opt.riskDelta}" else opt.riskDelta}",
                                fontFamily = PipMonospace,
                                fontSize = 9.sp,
                                color = PipInfoCyan
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "DEMONSTRATES: ${opt.spmConcept}",
                            fontFamily = PipMonospace,
                            fontSize = 9.sp,
                            color = PipTextSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TerminalButton(
                                text = "EXECUTE OPTION [${opt.key}]",
                                onClick = { onChooseOption(opt) },
                                testTag = "exec_option_${opt.key}"
                            )
                        }
                    }
                }
            }
        }
    }

    if (showConvertDialog) {
        var newProjectName by remember { mutableStateOf(activeScenario?.title ?: "Scenario Project") }
        AlertDialog(
            onDismissRequest = { showConvertDialog = false },
            containerColor = PipPanelBg,
            title = {
                Text(
                    text = "► CONVERT SCENARIO TO REAL WORKSPACE",
                    fontFamily = PipMonospace,
                    fontSize = 13.sp,
                    color = PipAmberWarning,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "This will create a genuine, persistent SQLite project record with a newly generated UUID based on this scenario's parameters.",
                        fontFamily = PipMonospace,
                        fontSize = 11.sp,
                        color = PipTextMain
                    )
                    OutlinedTextField(
                        value = newProjectName,
                        onValueChange = { newProjectName = it },
                        label = { Text("REAL PROJECT NAME", fontFamily = PipMonospace, fontSize = 10.sp) },
                        colors = terminalTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TerminalButton(
                    text = "CREATE REAL PROJECT",
                    onClick = {
                        onCreateRealProject(newProjectName)
                        showConvertDialog = false
                    },
                    variant = TerminalButtonVariant.PRIMARY,
                    testTag = "confirm_convert_proj_btn"
                )
            },
            dismissButton = {
                TerminalButton(text = "CANCEL", onClick = { showConvertDialog = false })
            }
        )
    }
}

@Composable
fun MetricHUD(
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

package com.example.pipos.features.simulator

data class SimState(
    val scheduleDaysRemaining: Int,
    val initialScheduleDays: Int,
    val budgetRemaining: Double,
    val initialBudget: Double,
    val scopePercent: Int, // 0 to 100
    val qualityIndex: Int, // 0 to 100 (100 = flawless)
    val riskExposure: Int, // 1 to 25
    val currentDecisionIndex: Int = 0,
    val decisionHistory: List<SimDecisionRecord> = emptyList(),
    val isFinished: Boolean = false
) {
    val overallScore: Int
        get() {
            val scheduleScore = ((scheduleDaysRemaining.toDouble() / initialScheduleDays.coerceAtLeast(1)) * 100).coerceIn(0.0, 100.0)
            val budgetScore = ((budgetRemaining / initialBudget.coerceAtLeast(1.0)) * 100).coerceIn(0.0, 100.0)
            val scopeScore = scopePercent.toDouble().coerceIn(0.0, 100.0)
            val qualityScore = qualityIndex.toDouble().coerceIn(0.0, 100.0)
            val riskPenalty = (riskExposure * 2.5).coerceIn(0.0, 50.0)

            val raw = (scheduleScore * 0.25) + (budgetScore * 0.25) + (scopeScore * 0.25) + (qualityScore * 0.25) - (riskPenalty * 0.2)
            return raw.toInt().coerceIn(0, 100)
        }

    val outcomeGrade: String
        get() = when {
            overallScore >= 85 -> "EXEMPLARY (SURVIVAL CONFIRMED)"
            overallScore >= 70 -> "STABLE RELEASE (MISSION ACCOMPLISHED)"
            overallScore >= 50 -> "COMPROMISED (TECHNICAL DEBT ACCUMULATED)"
            else -> "PROJECT COLLAPSE (CRITICAL FAILURE)"
        }
}

data class SimDecisionRecord(
    val promptText: String,
    val chosenOptionTitle: String,
    val consequenceExplanation: String,
    val spmConcept: String
)

data class SimOption(
    val key: String, // e.g. "A", "B", "C"
    val title: String,
    val description: String,
    val scheduleDeltaDays: Int,
    val budgetDelta: Double,
    val scopeDeltaPercent: Int,
    val qualityDeltaIndex: Int,
    val riskDelta: Int,
    val spmConcept: String,
    val explanation: String
)

data class SimStep(
    val incidentTitle: String,
    val incidentDescription: String,
    val initialAssumptions: String,
    val options: List<SimOption>
)

data class ScenarioDefinition(
    val id: String,
    val title: String,
    val subtitle: String,
    val coreChallenge: String,
    val primarySpmConcept: String,
    val initialDays: Int,
    val initialBudget: Double,
    val initialScope: Int,
    val initialQuality: Int,
    val initialRisk: Int,
    val steps: List<SimStep>
)

object SimulationCatalog {
    val scenarios: List<ScenarioDefinition> = listOf(
        ScenarioDefinition(
            id = "SIM-01-FAIL-TEST",
            title = "CRITICAL MODULE FAILS INTEGRATION TESTING",
            subtitle = "Quality Assurance Crisis 2 Weeks Before Release",
            coreChallenge = "Integration test suite reveals race conditions in payment pipeline.",
            primarySpmConcept = "Software Quality Management & Verification vs Schedule Crashing",
            initialDays = 14,
            initialBudget = 40000.0,
            initialScope = 100,
            initialQuality = 45,
            initialRisk = 18,
            steps = listOf(
                SimStep(
                    incidentTitle = "INCIDENT #1: RACE CONDITION IN CORE PIPELINE",
                    incidentDescription = "Stress test shows 4.2% transaction failure rate under peak load. The scheduled release is in 14 days. Marketing has already launched launch campaigns.",
                    initialAssumptions = "Assumes team has 3 backend engineers familiar with the synchronization module.",
                    options = listOf(
                        SimOption(
                            key = "A",
                            title = "HALT RELEASE & REFACTOR SYNCHRONIZATION",
                            description = "Delay shipment by 10 days, mobilize senior engineers to redesign the locking mechanism.",
                            scheduleDeltaDays = -10,
                            budgetDelta = -12000.0,
                            scopeDeltaPercent = 0,
                            qualityDeltaIndex = +45,
                            riskDelta = -12,
                            spmConcept = "Cost of Quality (Prevention Cost vs External Failure Cost)",
                            explanation = "Fixing before shipment prevents catastrophic post-release downtime and user churn, despite schedule delay."
                        ),
                        SimOption(
                            key = "B",
                            title = "DESCOPE CONCURRENT PROCESSING TO SERIAL QUEUE",
                            description = "Ship on time by downgrading to single-thread queue processing with reduced throughput capacity.",
                            scheduleDeltaDays = -2,
                            budgetDelta = -3000.0,
                            scopeDeltaPercent = -15,
                            qualityDeltaIndex = +25,
                            riskDelta = -6,
                            spmConcept = "Scope Trimming / Minimum Viable Product (MVP) De-risking",
                            explanation = "Preserves release deadline and stability at the expense of peak throughput performance."
                        ),
                        SimOption(
                            key = "C",
                            title = "SHIP WITH HOTFIX TELEMETRY & POST-LAUNCH MONITORING",
                            description = "Deploy on schedule with rate-limiters and prepare an on-call emergency hotfix team.",
                            scheduleDeltaDays = 0,
                            budgetDelta = -5000.0,
                            scopeDeltaPercent = 0,
                            qualityDeltaIndex = -15,
                            riskDelta = +6,
                            spmConcept = "Technical Debt & Risk Acceptance",
                            explanation = "Meets market window but knowingly carries high risk of real user transaction anomalies."
                        )
                    )
                ),
                SimStep(
                    incidentTitle = "INCIDENT #2: REGRESSION IN AUTHENTICATION SUITE",
                    incidentDescription = "While resolving the queue, automated regression tests flagged a session token expiry issue affecting 2FA users.",
                    initialAssumptions = "Client authentication tokens expire prematurely during retried requests.",
                    options = listOf(
                        SimOption(
                            key = "A",
                            title = "PAIR-PROGRAMMING STRIKE TEAM",
                            description = "Pair the security lead with a backend dev for 48 hours continuous triage.",
                            scheduleDeltaDays = -3,
                            budgetDelta = -4000.0,
                            scopeDeltaPercent = 0,
                            qualityDeltaIndex = +20,
                            riskDelta = -8,
                            spmConcept = "Extreme Programming (XP) Pair Programming & Rapid Defect Resolution",
                            explanation = "Quickly closes security exposure with verified peer code reviews."
                        ),
                        SimOption(
                            key = "B",
                            title = "EXTEND TOKEN LIFETIME TEMPORARILY",
                            description = "Patch token expiry interval from 15 mins to 24 hours to hide the timeout anomaly.",
                            scheduleDeltaDays = 0,
                            budgetDelta = 0.0,
                            scopeDeltaPercent = 0,
                            qualityDeltaIndex = -10,
                            riskDelta = +10,
                            spmConcept = "Workaround vs Root Cause Analysis (Security Anti-Pattern)",
                            explanation = "Stops apparent errors but introduces token hijacking risk."
                        ),
                        SimOption(
                            key = "C",
                            title = "DISABLE 2FA REQUIREMENT TEMPORARILY FOR BETA",
                            description = "Bypass 2FA on initial release and defer to Day 30 maintenance patch.",
                            scheduleDeltaDays = -1,
                            budgetDelta = -1000.0,
                            scopeDeltaPercent = -10,
                            qualityDeltaIndex = 0,
                            riskDelta = +5,
                            spmConcept = "Scope Deferral to Maintenance Phase",
                            explanation = "Reduces testing surface area but exposes users to single-factor vulnerability."
                        )
                    )
                )
            )
        ),
        ScenarioDefinition(
            id = "SIM-02-ARCHITECT-LEAVES",
            title = "KEY ARCHITECT LEAVES THE PROJECT",
            subtitle = "Sudden Resignation of Lead Engineer During Sprint 4",
            coreChallenge = "Lead architect resigned; vital architecture knowledge was undocumented.",
            primarySpmConcept = "Human Resource Management, Bus Factor & Brooks' Law",
            initialDays = 30,
            initialBudget = 60000.0,
            initialScope = 90,
            initialQuality = 70,
            initialRisk = 16,
            steps = listOf(
                SimStep(
                    incidentTitle = "INCIDENT #1: KNOWLEDGE SILO EXPOSURE",
                    incidentDescription = "The outgoing architect leaves in 5 business days. Critical cloud microservices have no architecture diagrams or deployment playbooks.",
                    initialAssumptions = "Remaining team members are mid-level developers unfamiliar with cloud orchestration.",
                    options = listOf(
                        SimOption(
                            key = "A",
                            title = "HALT FEATURE DEV FOR 1-WEEK KNOWLEDGE TRANSFER",
                            description = "Dedicate the architect's final 5 days exclusively to mob architecture walkthroughs and wiki documentation.",
                            scheduleDeltaDays = -7,
                            budgetDelta = -5000.0,
                            scopeDeltaPercent = -5,
                            qualityDeltaIndex = +20,
                            riskDelta = -10,
                            spmConcept = "Knowledge Management & Tacit to Explicit Knowledge Conversion (SECI Model)",
                            explanation = "Trading immediate sprint velocity preserves long-term project survivability."
                        ),
                        SimOption(
                            key = "B",
                            title = "HIRE EXTERNAL CLOUD CONTRACTOR IMMEDIATELY",
                            description = "Bring in a senior consultant at $250/hr to overlap with the architect for 3 days.",
                            scheduleDeltaDays = -2,
                            budgetDelta = -15000.0,
                            scopeDeltaPercent = 0,
                            qualityDeltaIndex = +10,
                            riskDelta = -6,
                            spmConcept = "Staff Augmentation & Ramp-up Overhead (Brooks' Law)",
                            explanation = "Fast domain onboarding comes at a steep financial premium."
                        ),
                        SimOption(
                            key = "C",
                            title = "RUSH ARCHITECT TO CODE REMAINING CORE MODULES",
                            description = "Demand the architect write as much code as possible before departing.",
                            scheduleDeltaDays = 0,
                            budgetDelta = 0.0,
                            scopeDeltaPercent = +5,
                            qualityDeltaIndex = -25,
                            riskDelta = +12,
                            spmConcept = "Hero Culture / Technical Isolation Trap",
                            explanation = "Creates orphaned code that no remaining developer knows how to maintain or debug."
                        )
                    )
                )
            )
        ),
        ScenarioDefinition(
            id = "SIM-03-SCOPE-CREEP",
            title = "SCOPE CREEP: EMERGENCY STAKEHOLDER DEMANDS",
            subtitle = "C-Suite Mandates AI & Multi-Tenant Support Mid-Sprint",
            coreChallenge = "Project is midway through development when high-priority unplanned features are forced into the sprint.",
            primarySpmConcept = "The Iron Triangle (Scope, Time, Cost) & Change Control Governance",
            initialDays = 25,
            initialBudget = 50000.0,
            initialScope = 80,
            initialQuality = 80,
            initialRisk = 10,
            steps = listOf(
                SimStep(
                    incidentTitle = "INCIDENT #1: MANDATORY EXECUTIVE CHANGE REQUEST",
                    incidentDescription = "The VP of Sales promises enterprise clients custom analytics dashboards not included in the original project charter.",
                    initialAssumptions = "Original sprint capacity is 100% committed to core CRUD and reporting.",
                    options = listOf(
                        SimOption(
                            key = "A",
                            title = "FORMAL CHANGE CONTROL: SWAP EQUAL STORY POINTS",
                            description = "Accept analytics only by dropping legacy PDF export and secondary reporting from v1.0.",
                            scheduleDeltaDays = 0,
                            budgetDelta = -2000.0,
                            scopeDeltaPercent = 0,
                            qualityDeltaIndex = +5,
                            riskDelta = -2,
                            spmConcept = "Change Control Board (CCB) & Capacity Neutral Trade-offs",
                            explanation = "Strictly enforces the Iron Triangle by preserving team velocity and delivery date."
                        ),
                        SimOption(
                            key = "B",
                            title = "NEGOTIATE FOR SPRINT 2 (VERSION 1.1 ROADMAP)",
                            description = "Firmly preserve v1.0 release baseline; schedule enterprise analytics for rapid follow-up patch.",
                            scheduleDeltaDays = 0,
                            budgetDelta = 0.0,
                            scopeDeltaPercent = -5,
                            qualityDeltaIndex = +15,
                            riskDelta = -4,
                            spmConcept = "Scope Baselines & Phased Release Management",
                            explanation = "Protects the integrity of the initial release while managing stakeholder expectations."
                        ),
                        SimOption(
                            key = "C",
                            title = "ABSORB SCOPE & AUTHORIZE WEEKEND OVERTIME",
                            description = "Accept all features into the current release and mandate 60-hour workweeks.",
                            scheduleDeltaDays = -4,
                            budgetDelta = -10000.0,
                            scopeDeltaPercent = +20,
                            qualityDeltaIndex = -30,
                            riskDelta = +14,
                            spmConcept = "Developer Burnout, Defect Multiplier & Scope Gold-Plating",
                            explanation = "Overtime initially boosts output but causes severe defect spikes and team fatigue."
                        )
                    )
                )
            )
        ),
        ScenarioDefinition(
            id = "SIM-04-BUDGET-CUT",
            title = "BUDGET SLASHED BY 25%",
            subtitle = "Corporate Reorganization Cuts Available Funding",
            coreChallenge = "Finance freezes hiring and cuts cloud infrastructure budget by a quarter.",
            primarySpmConcept = "Cost Management, Resource Leveling & Earned Value Optimization",
            initialDays = 40,
            initialBudget = 30000.0,
            initialScope = 90,
            initialQuality = 75,
            initialRisk = 12,
            steps = listOf(
                SimStep(
                    incidentTitle = "INCIDENT #1: CLOUD INFRASTRUCTURE DOWNSIZING",
                    incidentDescription = "Staging environments and multi-region failover are flagged as too expensive by finance.",
                    initialAssumptions = "Current cloud spend is $3,500/month across dev, staging, and load test clusters.",
                    options = listOf(
                        SimOption(
                            key = "A",
                            title = "MIGRATE STAGING TO LOCAL CONTAINERS & PRESERVE PROD FAILOVER",
                            description = "Engineers run test clusters on local workstations; production redundancy is kept intact.",
                            scheduleDeltaDays = -3,
                            budgetDelta = +8000.0,
                            scopeDeltaPercent = 0,
                            qualityDeltaIndex = -5,
                            riskDelta = +2,
                            spmConcept = "Value Engineering & Non-Production Cost Reduction",
                            explanation = "Achieves the 25% target without risking production availability."
                        ),
                        SimOption(
                            key = "B",
                            title = "DOWNSIZE PRODUCTION SERVERS & DROP HIGH AVAILABILITY",
                            description = "Run single instance production server with nightly automated backups.",
                            scheduleDeltaDays = 0,
                            budgetDelta = +12000.0,
                            scopeDeltaPercent = 0,
                            qualityDeltaIndex = -20,
                            riskDelta = +10,
                            spmConcept = "Calculated Risk Exposure vs Single Point of Failure (SPOF)",
                            explanation = "High cost savings, but a single server crash takes down all active clients."
                        ),
                        SimOption(
                            key = "C",
                            title = "REPLACE LICENSED THIRD-PARTY TOOLS WITH OPEN-SOURCE",
                            description = "Switch proprietary APM and logging suites to self-hosted open-source tools.",
                            scheduleDeltaDays = -8,
                            budgetDelta = +6000.0,
                            scopeDeltaPercent = 0,
                            qualityDeltaIndex = 0,
                            riskDelta = -1,
                            spmConcept = "Build vs Buy Analysis & Total Cost of Ownership (TCO)",
                            explanation = "Saves license fees at the cost of development engineering time for setup."
                        )
                    )
                )
            )
        )
    )
}

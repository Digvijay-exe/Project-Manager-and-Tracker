# PIP-OS: Project Survival Protocol
### Retro-Futuristic Terminal for Software Project Management & Crisis Survival Simulation

PIP-OS is an offline-first Android application designed for Software Project Management (SPM) education, project triage, threat analysis, and crisis decision-making. It fuses a Pip-Boy-inspired retro-futuristic CRT terminal design system with strict software engineering rigor and Room/SQLite local persistence.

---

## 1. Zero-Fabrication Architecture Rule

PIP-OS strictly adheres to the **Zero-Fabrication Data Protocol**:
- **No Seed/Fake Data:** On fresh install, workspaces, tasks, risks, and telemetry start completely empty.
- **Genuine UUIDs:** Real records use dynamically generated `UUID.randomUUID()` identifiers.
- **Deterministic Simulation Isolation:** The Project Survival Simulator runs in a sandboxed, explicitly labeled `[SIMULATION MODE — HYPOTHETICAL DATA]` environment. Fictional scenarios never contaminate the real SQLite workspace unless the user explicitly triggers "Convert Scenario to Real Project".
- **Truthful Metrics:** All Command Center gauges, completion percentages, overdue indicators, and release checklists derive strictly from SQLite Room queries.

---

## 2. Core Architecture & Tech Stack

```
com.example
├── MainActivity.kt                 # Edge-to-edge host, shell navigation, CRT scanlines
├── pipos
│   ├── MainViewModel.kt            # AndroidViewModel uniting reactive Room Flows with UI
│   ├── core
│   │   ├── components/             # Reusable terminal UI (TerminalPanel, TerminalButton, Badges, EmptyState, TopBar, BottomNav)
│   │   └── theme/                  # CRT scanlines canvas, blinking cursor, corner bracket modifiers
│   ├── data
│   │   ├── local/
│   │   │   ├── PipOSDatabase.kt    # Room database (Version 1)
│   │   │   ├── entity/             # ProjectEntity, TaskEntity, RiskEntity, MilestoneEntity, TeamMemberEntity, TestCaseEntity
│   │   │   └── dao/                # DAOs with reactive Kotlin Coroutine Flow queries
│   │   └── repository/             # PipOSRepository pattern isolating SQLite access
│   └── features
│       ├── boot/                   # BootSequenceScreen (skippable retro CRT typewriter)
│       ├── dashboard/              # CommandCenterScreen (real telemetry, quick alerts, empty states)
│       ├── projects/               # ProjectWorkspaceScreen (dossier tabs: Tasks, Risks, Milestones, Tests)
│       ├── tasks/                  # TaskCommandScreen (filter by status/priority, search, assignees)
│       ├── risks/                  # RiskRegisterScreen (5x5 responsive Likelihood x Impact matrix)
│       ├── simulator/              # SimulatorEngine & ProjectSurvivalSimulatorScreen (deterministic educational SPM branching)
│       └── system/                 # SystemScreen (pre-flight release readiness, CSV report export, team roster, settings)
```

- **Runtime Target:** Android (Min SDK 24, Target SDK 36, Compile SDK 36)
- **UI Toolkit:** Jetpack Compose + Material 3 custom CRT tokens
- **Persistence:** AndroidX Room (`2.7.0`) with KSP compiler and SQLite
- **Concurrency:** Kotlin Coroutines (`StateFlow`, `WhileSubscribed(5000)`)
- **Testing:** Local JVM Unit Tests + Robolectric

---

## 3. Mapping of Features to Software Project Management (SPM) Concepts

| Feature | SPM Concept / Standard Alignment | Description |
|---|---|---|
| **Command Center Telemetry** | Earned Value Management (EVM) & Project Dashboards | Real-time visibility into planned vs actual work without vanity metrics. |
| **Task Priority & Blocker Flags** | Critical Path Method (CPM) & Blocker Triage | Identifying critical tasks directly impacting release delivery. |
| **Risk Register & 5x5 Matrix** | Qualitative Risk Analysis (PMBOK Guide) | Exposure = Likelihood x Impact (1-25) with documented thresholds: Low (1-4), Medium (5-12), High (13-25). |
| **Milestone Tracker** | Schedule Baseline & Stage-Gate Process | Deliverable verification gates with automatic overdue flagging. |
| **Project Survival Simulator** | Trade-Off Analysis & The Iron Triangle | Demonstrates Brooks' Law, Cost of Quality (Prevention vs Failure), MVP Descoping, and Technical Debt accumulation. |
| **Release Readiness Gate** | Quality Gate & Verification Sign-Off | Prevents unverified or critical-risk software from declaring itself "Ready". |
| **Team Roster** | Human Resource Management & RACI Matrix | Explicit developer responsibility allocation for tasks and risks. |

---

## 4. Exact Commands to Run

### Run Unit Tests
```bash
gradle :app:testDebugUnitTest
```

### Build APK
```bash
gradle :app:assembleDebug
```
Output APK location:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 5. Verification Checklist

- [x] **Firebase Firestore & Authentication**: Configured with Google Sign-In via Jetpack Credential Manager, hardened ABAC security rules, local JS rules tests, and remote cloud synchronization for workspaces and tasks.
- [x] **Zero Fake Data Rule**: Verified empty first-launch state and dynamic UUID creation.
- [x] **Phosphor Green Retro-Terminal**: Custom palette (`#050805`, `#39FF78`, `#245B32`), CRT scanline overlay, blinking cursor, tactile bracket buttons.
- [x] **Room Local Persistence**: Fully functioning SQLite Room database with 6 entities, DAOs, and repository.
- [x] **Interactive 5x5 Risk Matrix**: Touch-responsive Likelihood x Impact matrix with color thresholds.
- [x] **Interactive Project Survival Simulator**: 4 multi-branching SPM crisis scenarios with deterministic scoring and post-mortem review.
- [x] **Release Readiness Gate**: Objective checks on open blockers, unresolved critical threats, and recorded test cases.
- [x] **CSV Export & Team Management**: Copyable RFC 4180 CSV audit reports and real team roster.
- [x] **Custom Adaptive Icon**: Generated retro Pip-Boy terminal launcher icon with all density mipmaps.
- [x] **Automated Test Suite**: Tested risk calculations, thresholds, overdue tasks, milestone progress, and simulation scores.

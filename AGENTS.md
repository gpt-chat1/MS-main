# AGENTS.md — Project Knowledge Base

## Overview
- **App**: "المدير الذكي" (Smart Manager) — Android management app
- **Stack**: Kotlin, Jetpack Compose, Room (SQLite), Material3
- **Package**: `com.example`, namespace `com.example`
- **Min SDK**: 24, Target: 36, Compile: 36
- **Theme**: `MyApplicationTheme` — gold + deep green luxury palette
- **Font**: Qamra (`res/font/qomra.ttf`) via `QamraFont` in `Type.kt`

---

## Build & Run
```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat assembleDebug
$adb = "$env:USERPROFILE\AppData\Local\Android\Sdk\platform-tools\adb.exe"
& $adb install -r app\build\outputs\apk\debug\app-debug.apk
```
- **Git** (portable): `$env:TEMP\git-portable\bin\git.exe`
- **GitHub**: `https://github.com/gpt-chat1/MS-main.git` (use token from env/AGENTS)
- **ADB device**: `8HRGEMNNV4BUFAUO`

---

## Architecture
```
MainActivity
  └─ AppNavigation(viewModel: MainViewModel)
        ├─ SetupAdminScreen / LoginScreen / DashboardScreen
        ├─ OrgStructureScreen / StaffManagementScreen
        ├─ EmployeeDetailScreen / ProjectsScreen / ProjectDetailScreen
        ├─ ReportsAnalyticsScreen / SettingsScreen
        └─ Dialogs (AddXxxDialog, EditXxxDialog, ...)

MainViewModel ──> AppRepository ──> Dao ──> Room DB (managerhub_database)
```

### Files Layout
| Purpose | Path |
|---------|------|
| Entities | `data/entity/Entities.kt` |
| DAOs | `data/dao/Daos.kt` (6 interfaces: Admin, Dept, Office, Employee, Task, Attendance, Penalty, Invoice, Project, ProjectMember, Evaluation) |
| Repository | `data/repository/AppRepository.kt` |
| DB | `data/db/AppDatabase.kt` |
| ViewModel | `ui/MainViewModel.kt` |
| Screens | `ui/Screens.kt` (~4600 lines, 25+ composables) |
| UI Components | `ui/UiComponents.kt` (ShaheenOutlinedTextField, DatePickerField, helpers) |
| Theme | `ui/theme/Color.kt`, `Type.kt`, `Theme.kt` |
| Export | `util/ReportExporter.kt` |
| Resources | `res/font/qomra.ttf`, `res/drawable/app_logo.png`, `res/values/strings.xml` |

---

## Database (Room, version = 8, fallbackToDestructiveMigration = true)

### Entities (all in Entities.kt)
| Entity | Key fields |
|--------|-----------|
| `Admin` | id, name, surname, selfId (unique), passwordHash |
| `Department` | id, name (unique), description, location, teamCount |
| `Office` | id, name, departmentId (FK→Dept), managerName, status |
| `Employee` | id, name, role, officeId (FK→Office), branchLocation, avatarUrl, status, selfId, achievements, tags, isOfficeManager, isDepartmentManager |
| `Project` | id, name, description, startDate, dueDate, status, priority, progress, departmentId, officeId, scopeType, budget |
| `ProjectMember` | id, projectId (FK→Project), employeeId (FK→Employee), role |
| `Task` | id, title, description, employeeId (FK→Employee), projectId (FK→Project), progress, notes, urgency, importance, status, dueDate, sortOrder |
| `Attendance` | id, employeeId (FK→Employee), date (unique per emp), status |
| `Penalty` | id, employeeId (FK→Employee), amount, reason, date, status |
| `Invoice` | id, officeId (FK→Office), amount, trackingNumber (unique), description, date, type, imageUrl, projectId |
| `EmployeeEvaluation` | id, employeeId, evaluatorAdminId, periodStart, periodEnd, scores, totalScore, rating, notes, createdAt |

### DAO Methods Summary
- **Admin**: getBySelfId, insert, count
- **Department**: getAll, insert, delete, count, getById
- **Office**: getAll, getByDept, insert, delete, getOfficeCountByDept, getOfficeById
- **Employee**: getAll, getActive, getByOffice, searchEmployees(name/role/tags LIKE), insert, update, delete, getById, getBySelfId, getByDept
- **Task**: getAll, getByEmployee, getByProjectOrdered, searchTasks(title/desc LIKE), insert, updateTaskOrder, updateTaskStatus, delete
- **Attendance**: getAll, getByEmployee, insert, getByDate, getStatusForEmployee, getPresentDaysCount, getTotalDaysCount
- **Penalty**: getAll, insert
- **Invoice**: getAll, insert, update, delete, getTotalSum, getByProject, getProjectSum
- **Project**: getAll, getById, searchProjects(name/desc LIKE), getByEmployee, insert, update, delete, updateProgress, updateStatus
- **ProjectMember**: getMembersByProject (returns ProjectMember), getEmployeesByProject (returns Employee via JOIN), insert, delete, getMemberByProjectAndEmployee, removeMember
- **Evaluation**: getByEmployee, insert, delete, getAll, getOfficeAvgScore, getDeptAvgScore, getTop/BottomEmployeeInOffice

---

## ViewModel (MainViewModel)

### StateFlows (reactive)
```kotlin
departments, offices, employees, tasks, invoices, penalties, attendanceList, projects
timelineFeed, overdueCount, employeeSearchQuery, activeDepartmentFilterId, filteredEmployees
totalInvoiceSum, overallBranchPerformance, averageAttendanceRate, overallEfficiencyScore
logoUri, orgName, branchName, exportedFile, savedReports
currentScreen, loggedInAdmin, uiMessage, selectedEvaluationEmployee
```

### Key Methods
```kotlin
navigateTo(AppScreen), performLogin, logout, setupAdmin
addDepartment, addOffice(name, deptId, managerName, managerId?)
addEmployee(name, role, officeId, branch, selfId, achievements, tags)
updateEmployeeRole, updateEmployeeFull, dismissEmployee, deleteEmployee
deleteOffice, setEmployeeAsOfficeManager
addProject, updateProject, deleteProject, updateProjectStatus, selectProject
addProjectMember, removeProjectMember
assignTaskToProject, reorderTask
linkInvoiceToProject(invoiceId, projectId)
searchTasks(query), searchProjects(query)
backupDatabase(context, onComplete), restoreDatabase(context, uri, onComplete)
saveReportCopy(context, file), refreshSavedReports(context)
exportPdf, exportExcel, exportEmployeePdf, exportOfficePdf, exportDepartmentPdf, exportProjectPdf, exportInvoicesPdf, exportFinancialPdf
```

---

## UI Screens & Dialogs

### Screens (all in Screens.kt)
| Composable | Signature | Description |
|-----------|-----------|-------------|
| `AppNavigation` | `(viewModel)` | Root navigation, routes to current screen |
| `SetupAdminScreen` | `(viewModel)` | First-time admin creation |
| `LoginScreen` | `(viewModel)` | Admin login via selfId + password |
| `DashboardScreen` | `(viewModel)` | Overview: tasks, timeline, alerts |
| `OrgStructureScreen` | `(viewModel)` | Tree view: depts → offices → employees |
| `StaffManagementScreen` | `(viewModel)` | Employee CRUD, search, department filter |
| `EmployeeDetailScreen` | `(viewModel, employeeId)` | Employee tasks, penalties, attendance, actions |
| `ProjectsScreen` | `(viewModel)` | Project list with search bar |
| `ProjectDetailScreen` | `(viewModel, projectId)` | Full project detail, edit, tasks, members, invoices |
| `ReportsAnalyticsScreen` | `(viewModel)` | Analytics with period filter (day/week/month) |
| `SettingsScreen` | `(viewModel)` | Logo, org info, export, backup/restore, saved reports |

### Dialogs
| Dialog | Purpose |
|--------|---------|
| `AddDepartmentDialog` | Create department |
| `AddOfficeDialog` | Create office with employee dropdown for manager |
| `AddEmployeeDialog` | Create employee with tags field |
| `AddProjectDialog` | Create project with scope/priority/dates |
| `EditProjectDialog` | Full project edit: fields, budget card, members, tasks, invoices |
| `AddProjectTaskDialog` | Create task linked to project |
| `AddProjectMemberDialog` | Add member to project |
| `EditEmployeeDialog` | Edit name, selfId, role, branch, achievements, tags |
| `EditEmployeeRoleDialog` | Edit just the role |
| `EmployeeActionsDialog` | Quick actions: task, penalty, attendance |
| `EmployeeEvaluationDialog` | Evaluation scoring form |
| `InvoiceDialog` | Create/edit invoice |
| `ShareFileDialog` | Share file via system intent |
| `DepartmentOfficesDialog` | View offices within a department |
| `OfficeMembersDialog` | View/edit/delete employees in an office |
| `AddTaskDialog` | Assign task to employee |

### UI Components (UiComponents.kt)
- `ShaheenOutlinedTextField` — styled text field with gold/green theme
- `DatePickerField` — date picker with ISO conversion
- `todayIsoDate()` — returns current date as "yyyy-MM-dd"
- `isoToDisplay(iso)` — converts "yyyy-MM-dd" to "dd/MM/yyyy"
- `periodDateRange(period)` — returns (start, end) for اليوم/الأسبوع/الشهر

---

## Key Patterns & Conventions

### Imports (conventional)
```kotlin
import com.example.data.entity.*
import com.example.data.repository.*
import com.example.ui.theme.*
import com.example.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
```

### Custom Text Field
Always use `ShaheenOutlinedTextField` instead of plain `OutlinedTextField` for consistent theming.

### Color Names (Color.kt)
- `DeepGreen`, `Gold`, `WarmBackground`, `TextDark`, `TextMuted`
- `CoralRed` (error/danger), `White`
- Priority: `PriorityCritical` (red), `PriorityHigh` (orange), `PriorityMedium` (yellow), `PriorityLow` (green)
- Status: `StatusNotStarted` (grey), `StatusInProgress` (blue), `StatusCompleted` (green), `StatusDelayed` (red)

### Date Format
All dates stored as ISO `yyyy-MM-dd` strings. Display via `isoToDisplay()`.

### Repository Pattern
- ViewModel calls `repository.method()` inside `viewModelScope.launch { }` for suspend calls
- Flows use `.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())`
- Insert/update operations return `RepositoryResult<T>` with Success/Failure sealed class
- On failure, ViewModel calls `publishResult()` which sets `_uiMessage` → Toast

### Database Version Bumps
When entities change, bump version in `AppDatabase.kt`. Currently v8 with destructive migration.

### Seed Data
`repository.seedInitialData()` checks `departmentDao.getDepartmentCount() == 0`. If true, seeds departments, offices, 3 employees, tasks, attendance, penalties, invoices, 3 projects, project members, project tasks. Evaluation seeding is skipped if no admin exists (avoids FK crash after destructive migration).

### Employee Manager Display
Display manager name via `employees.find { it.officeId == office.id }?.name ?: "لا يوجد مدير"` — not from `office.managerName`.

### Logo
`painterResource(R.drawable.app_logo)` for default logo; `AsyncImage` with URI for user-selected logo. Stored in settings via `logoUri` StateFlow.

---

## Common Fixes
- **DB crash on startup (schema/identity hash)** → bump DB version in `AppDatabase.kt` + ensure `fallbackToDestructiveMigration(true)`
- **FK constraint crashes in seed data** → guard seed inserts (e.g., check admin exists before inserting evaluations)
- **Font crash** → verify `res/font/` has valid TTF, check import `androidx.compose.ui.text.font.Font`

---

## Naming Conventions
- All UI text in Arabic (including hardcoded strings)
- Button labels: `Text("حفظ", color = Color.White)`
- Dialog titles: `Text("تعديل: ${entity.name}", ...)`
- Method names in camelCase English
- XML resources use snake_case

package com.example.data.repository

import com.example.data.dao.*
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

data class TimelineActivity(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val timeLabel: String,
    val isPrimary: Boolean = true // Green vs Gold indicator
)

sealed class RepositoryResult<out T> {
    data class Success<T>(val value: T) : RepositoryResult<T>()
    data class Failure(val message: String) : RepositoryResult<Nothing>()
}

class AppRepository(
    private val adminDao: AdminDao,
    private val departmentDao: DepartmentDao,
    private val officeDao: OfficeDao,
    private val employeeDao: EmployeeDao,
    private val projectDao: ProjectDao,
    private val projectMemberDao: ProjectMemberDao,
    private val taskDao: TaskDao,
    private val attendanceDao: AttendanceDao,
    private val penaltyDao: PenaltyDao,
    private val invoiceDao: InvoiceDao,
    private val reportDao: ReportDao,
    private val evaluationDao: EvaluationDao
) {
    // Dynamic Live Timeline State Feed
    private val _timelineFeed = MutableStateFlow<List<TimelineActivity>>(emptyList())
    val timelineFeed: StateFlow<List<TimelineActivity>> = _timelineFeed.asStateFlow()

    init {
        // Initialize timeline feed with elegant default actions
        _timelineFeed.value = listOf(
            TimelineActivity(
                text = "قام أحمد منصور بإرسال تقرير الأداء المالي للمكتب الرئيسي",
                timeLabel = "منذ 15 دقيقة",
                isPrimary = true
            ),
            TimelineActivity(
                text = "تمت الموافقة على طلب إجازة طارئة لـ سارة العتيبي",
                timeLabel = "منذ ساعة واحدة",
                isPrimary = false
            ),
            TimelineActivity(
                text = "سجل أحمد منصور حضور كامل لأعضاء مكتب التدقيق اليوم",
                timeLabel = "منذ ساعتين",
                isPrimary = true
            ),
            TimelineActivity(
                text = "تم إصدار فاتورة خدمات بمبلغ 1,200 ر.س لمكتب العمليات",
                timeLabel = "منذ 3 ساعات",
                isPrimary = false
            )
        )
    }

    fun addTimelineActivity(text: String, timeLabel: String = "الآن", isPrimary: Boolean = true) {
        val newActivity = TimelineActivity(text = text, timeLabel = timeLabel, isPrimary = isPrimary)
        _timelineFeed.value = listOf(newActivity) + _timelineFeed.value
    }

    // Admin DAOs
    suspend fun getAdminBySelfId(selfId: String) = adminDao.getAdminBySelfId(selfId)
    suspend fun insertAdmin(admin: Admin) = adminDao.insertAdmin(admin)
    suspend fun getAdminCount() = adminDao.getAdminCount()

    // Department DAOs
    fun getAllDepartments(): Flow<List<Department>> = departmentDao.getAllDepartments()
    suspend fun insertDepartment(department: Department): RepositoryResult<Long> {
        if (department.name.isBlank()) return RepositoryResult.Failure("اسم القسم مطلوب")
        if (department.teamCount < 0) return RepositoryResult.Failure("عدد الفرق لا يمكن أن يكون سالباً")
        return runCatching {
            val id = departmentDao.insertDepartment(department)
            addTimelineActivity("تمت إضافة قسم جديد: ${department.name}", "الآن", true)
            RepositoryResult.Success(id)
        }.getOrElse { RepositoryResult.Failure("تعذر إضافة القسم. قد يكون الاسم مكرراً.") }
    }
    suspend fun deleteDepartment(department: Department) = departmentDao.deleteDepartment(department)

    // Office DAOs
    fun getAllOffices(): Flow<List<Office>> = officeDao.getAllOffices()
    fun getOfficesByDepartment(deptId: Int): Flow<List<Office>> = officeDao.getOfficesByDepartment(deptId)
    suspend fun insertOffice(office: Office): RepositoryResult<Long> {
        if (office.name.isBlank()) return RepositoryResult.Failure("اسم الفريق / المكتب مطلوب")
        val department = departmentDao.getDepartmentById(office.departmentId)
            ?: return RepositoryResult.Failure("القسم المحدد غير موجود")
        val currentTeams = officeDao.getOfficeCountByDepartment(office.departmentId)
        if (department.teamCount > 0 && currentTeams >= department.teamCount) {
            return RepositoryResult.Failure("لا يمكن إضافة فريق جديد. القسم وصل إلى الحد المحدد: ${department.teamCount}")
        }
        return runCatching {
            val id = officeDao.insertOffice(office)
            addTimelineActivity("تمت إضافة فريق / مكتب جديد: ${office.name}", "الآن", false)
            RepositoryResult.Success(id)
        }.getOrElse { RepositoryResult.Failure("تعذر إضافة الفريق. قد يكون الاسم مكرراً داخل نفس القسم.") }
    }
    suspend fun deleteOffice(office: Office) = officeDao.deleteOffice(office)

    // Employee DAOs
    fun getAllEmployees(): Flow<List<Employee>> = employeeDao.getAllEmployees()
    fun getActiveEmployees(): Flow<List<Employee>> = employeeDao.getActiveEmployees()
    fun getEmployeesByOffice(officeId: Int): Flow<List<Employee>> = employeeDao.getEmployeesByOffice(officeId)
    fun searchEmployees(query: String): Flow<List<Employee>> = employeeDao.searchEmployees(query)
    fun searchTasks(query: String): Flow<List<Task>> = taskDao.searchTasks(query)
    fun searchProjects(query: String): Flow<List<Project>> = projectDao.searchProjects(query)
    suspend fun insertEmployee(employee: Employee): RepositoryResult<Long> {
        if (employee.name.isBlank()) return RepositoryResult.Failure("اسم الموظف مطلوب")
        if (employee.role.isBlank()) return RepositoryResult.Failure("منصب الموظف مطلوب")
        if (employee.selfId.isNotBlank() && employeeDao.getEmployeeBySelfId(employee.selfId) != null) {
            return RepositoryResult.Failure("الرقم الذاتي ${employee.selfId} موجود مسبقاً لموظف آخر")
        }
        officeDao.getOfficeById(employee.officeId) ?: return RepositoryResult.Failure("الفريق / المكتب المحدد غير موجود")
        return runCatching {
            val id = employeeDao.insertEmployee(employee)
            addTimelineActivity("تم تسجيل موظف جديد: ${employee.name} بمنصب ${employee.role}", "الآن", true)
            RepositoryResult.Success(id)
        }.getOrElse { RepositoryResult.Failure("تعذر إضافة الموظف. لا يمكن تكرار نفس الموظف داخل نفس الفريق.") }
    }
    suspend fun deleteEmployee(employee: Employee) = employeeDao.deleteEmployee(employee)

    suspend fun updateEmployee(employee: Employee): RepositoryResult<Unit> {
        if (employee.role.isBlank()) return RepositoryResult.Failure("منصب الموظف مطلوب")
        return runCatching {
            employeeDao.updateEmployee(employee)
            addTimelineActivity("تم تحديث بيانات الموظف: ${employee.name}", "الآن", true)
            RepositoryResult.Success(Unit)
        }.getOrElse { RepositoryResult.Failure("تعذر تحديث بيانات الموظف") }
    }

    suspend fun setEmployeeAsOfficeManager(employeeId: Int, isManager: Boolean) {
        val emp = employeeDao.getEmployeeById(employeeId) ?: return
        employeeDao.updateEmployee(emp.copy(isOfficeManager = isManager))
    }

    suspend fun dismissEmployee(employee: Employee) {
        employeeDao.updateEmployee(employee.copy(status = "Dismissed"))
        addTimelineActivity("تم تسريح الموظف: ${employee.name}", "الآن", false)
    }

    fun getEmployeesByDepartment(deptId: Int): Flow<List<Employee>> =
        employeeDao.getEmployeesByDepartment(deptId)

    fun getEmployeesByDepartmentAll(deptId: Int): Flow<List<Employee>> =
        employeeDao.getEmployeesByDepartmentAll(deptId)

    fun getAttendanceByDate(date: String): Flow<List<Attendance>> =
        attendanceDao.getAttendanceByDate(date)

    suspend fun getAttendanceStatusForEmployee(employeeId: Int, date: String): String? =
        attendanceDao.getAttendanceStatusForEmployee(employeeId, date)

    // Task DAOs
    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()
    fun getTasksByEmployee(employeeId: Int): Flow<List<Task>> = taskDao.getTasksByEmployee(employeeId)
    suspend fun insertTask(task: Task): Long {
        val id = taskDao.insertTask(task)
        addTimelineActivity("تم تعيين مهمة جديدة: '${task.title}'", "الآن", true)
        return id
    }
    fun getTasksByProjectOrdered(projectId: Int): Flow<List<Task>> = taskDao.getTasksByProjectOrdered(projectId)
    suspend fun updateTaskOrder(taskId: Int, order: Int) = taskDao.updateTaskOrder(taskId, order)
    suspend fun updateTaskStatus(taskId: Int, progress: Int, status: String, notes: String, taskTitle: String) {
        taskDao.updateTaskStatus(taskId, progress, status, notes)
        val statusText = if (status == "Completed") "مكتملة" else "تحت المراجعة ($progress%)"
        addTimelineActivity("تم تحديث حالة المهمة '${taskTitle}' إلى $statusText", "الآن", status == "Completed")
    }
    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    // Attendance DAOs
    fun getAllAttendance(): Flow<List<Attendance>> = attendanceDao.getAllAttendance()
    suspend fun insertAttendance(attendance: Attendance): Long {
        val id = attendanceDao.insertAttendance(attendance)
        val statusArabic = when (attendance.status) {
            "Present" -> "حاضر"
            "Absent" -> "غائب"
            else -> "متأخر"
        }
        addTimelineActivity("تم تسجيل حالة الدوام كـ ($statusArabic) اليوم", "الآن", attendance.status == "Present")
        return id
    }

    // Penalty DAOs
    fun getAllPenalties(): Flow<List<Penalty>> = penaltyDao.getAllPenalties()
    suspend fun insertPenalty(penalty: Penalty): Long {
        val id = penaltyDao.insertPenalty(penalty)
        addTimelineActivity("تم تسجيل جزاء مالي بقيمة ${penalty.amount} ر.س بسبب: ${penalty.reason}", "الآن", false)
        return id
    }

    // Invoice DAOs
    fun getAllInvoices(): Flow<List<Invoice>> = invoiceDao.getAllInvoices()
    suspend fun insertInvoice(invoice: Invoice): Long {
        val id = invoiceDao.insertInvoice(invoice)
        addTimelineActivity("تم إصدار فاتورة جديدة بقيمة ${invoice.amount} ر.س - ${invoice.description}", "الآن", false)
        return id
    }

    suspend fun updateInvoice(invoice: Invoice) {
        invoiceDao.updateInvoice(invoice)
        addTimelineActivity("تم تحديث الفاتورة: ${invoice.trackingNumber}", "الآن", false)
    }

    suspend fun deleteInvoice(invoice: Invoice) = invoiceDao.deleteInvoice(invoice)
    fun getTotalInvoiceSumFlow(): Flow<Double?> = invoiceDao.getTotalInvoiceSumFlow()
    fun getInvoicesByProject(projectId: Int): Flow<List<Invoice>> = invoiceDao.getInvoicesByProject(projectId)
    suspend fun getProjectInvoicesSum(projectId: Int): Double? = invoiceDao.getProjectInvoicesSum(projectId)

    // ──────────── PROJECT DAOs ────────────
    fun getAllProjects(): Flow<List<Project>> = projectDao.getAllProjects()
    suspend fun getProjectById(id: Int) = projectDao.getProjectById(id)

    suspend fun insertProject(project: Project): Long {
        val id = projectDao.insertProject(project)
        addTimelineActivity("تم إطلاق مشروع جديد: ${project.name}", "الآن", true)
        return id
    }

    suspend fun deleteProject(project: Project) = projectDao.deleteProject(project)

    suspend fun updateProject(project: Project) {
        projectDao.updateProject(project)
        addTimelineActivity("تم تحديث المشروع: ${project.name}", "الآن", true)
    }
    suspend fun updateProjectStatus(id: Int, status: String) {
        projectDao.updateProjectStatus(id, status)
        addTimelineActivity("تم تحديث حالة المشروع إلى $status", "الآن", true)
    }

    suspend fun recalculateProjectProgress(projectId: Int) {
        val completed = projectDao.getProjectCompletedTasks(projectId)
        val total = projectDao.getProjectTotalTasks(projectId)
        val progress = if (total > 0) (completed * 100) / total else 0
        projectDao.updateProjectProgress(projectId, progress)

        val status = when {
            progress == 100 && total > 0 -> "Completed"
            progress > 0 -> "InProgress"
            else -> "NotStarted"
        }
        projectDao.updateProjectStatus(projectId, status)
    }

    suspend fun getProjectMemberCount(projectId: Int) = projectDao.getProjectMemberCount(projectId)
    suspend fun getProjectCompletedTasks(projectId: Int) = projectDao.getProjectCompletedTasks(projectId)
    suspend fun getProjectTotalTasks(projectId: Int) = projectDao.getProjectTotalTasks(projectId)

    // ──────────── PROJECT MEMBER DAOs ────────────
    fun getMembersByProject(projectId: Int): Flow<List<ProjectMember>> =
        projectMemberDao.getMembersByProject(projectId)

    fun getEmployeesByProject(projectId: Int): Flow<List<Employee>> =
        projectMemberDao.getEmployeesByProject(projectId)

    suspend fun insertProjectMember(member: ProjectMember): RepositoryResult<Long> {
        val employee = employeeDao.getEmployeeById(member.employeeId)
            ?: return RepositoryResult.Failure("الموظف المحدد غير موجود")
        if (employee.status != "Active") return RepositoryResult.Failure("لا يمكن إضافة موظف غير نشط إلى مشروع")
        if (projectMemberDao.getMemberByProjectAndEmployee(member.projectId, member.employeeId) != null) {
            return RepositoryResult.Failure("هذا الموظف موجود مسبقاً في فريق المشروع")
        }
        return runCatching {
            val id = projectMemberDao.insertMember(member)
            addTimelineActivity("تمت إضافة عضو إلى فريق المشروع", "الآن", false)
            RepositoryResult.Success(id)
        }.getOrElse { RepositoryResult.Failure("تعذر إضافة عضو المشروع") }
    }

    suspend fun removeProjectMember(projectId: Int, employeeId: Int) {
        projectMemberDao.removeMember(projectId, employeeId)
        addTimelineActivity("تم إزالة عضو من فريق المشروع", "الآن", false)
    }

    // ──────────── EVALUATION DAOs ────────────
    fun getEvaluationsByEmployee(employeeId: Int): Flow<List<EmployeeEvaluation>> =
        evaluationDao.getEvaluationsByEmployee(employeeId)

    suspend fun insertEvaluation(evaluation: EmployeeEvaluation): Long {
        val id = evaluationDao.insertEvaluation(evaluation)
        addTimelineActivity("تم إجراء تقييم أداء للموظف برصيد ${evaluation.totalScore} نقطة", "الآن", true)
        return id
    }

    suspend fun deleteEvaluation(evaluation: EmployeeEvaluation) =
        evaluationDao.deleteEvaluation(evaluation)

    suspend fun getAllEvaluations(): List<EmployeeEvaluation> =
        evaluationDao.getAllEvaluations()

    suspend fun getOfficeAvgScore(officeId: Int, since: String) =
        evaluationDao.getOfficeAvgScore(officeId, since)

    suspend fun getDepartmentAvgScore(deptId: Int, since: String) =
        evaluationDao.getDepartmentAvgScore(deptId, since)

    suspend fun getTopEmployeeInOffice(officeId: Int, since: String) =
        evaluationDao.getTopEmployeeInOffice(officeId, since)

    suspend fun getBottomEmployeeInOffice(officeId: Int, since: String) =
        evaluationDao.getBottomEmployeeInOffice(officeId, since)

    // ──────────── DEADLINE ALERTS ────────────
    suspend fun getOverdueTasks(): List<Task> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return taskDao.getAllTasks().let { flow ->
            flow.first().filter { task ->
                task.status != "Completed" && task.dueDate.isNotBlank() && task.dueDate < today
            }
        }
    }

    // Reporting Engine Transaction calls
    suspend fun getEmployeeReportCards(): List<EmployeeReportCard> = reportDao.getEmployeeReportCards()
    suspend fun getOfficeReportCards(): List<OfficeReportCard> = reportDao.getOfficeReportCards()
    suspend fun getDepartmentReportCards(): List<DepartmentReportCard> = reportDao.getDepartmentReportCards()
    suspend fun getEmployeeReportCard(employeeId: Int): EmployeeReportCard? =
        reportDao.getEmployeeReportCards().find { it.employeeId == employeeId }
    suspend fun getOfficeReportCard(officeId: Int): OfficeReportCard? =
        reportDao.getOfficeReportCards().find { it.officeId == officeId }

    // Direct seeds
    suspend fun seedInitialData() {
        if (departmentDao.getDepartmentCount() == 0) {
            val operationsId = departmentDao.insertDepartment(
                Department(name = "قسم العمليات واللوجستيات", description = "إدارة كافة العمليات الأرضية والنقل والتوصيل الميداني", location = "المبنى الرئيسي، الطابق الثالث", teamCount = 3)
            ).toInt()

            val financeId = departmentDao.insertDepartment(
                Department(name = "قسم الشؤون المالية", description = "التدقيق والموازنة العامة والمصروفات والرواتب", location = "الجناح الغربي، الطابق الثاني", teamCount = 2)
            ).toInt()

            val hrId = departmentDao.insertDepartment(
                Department(name = "قسم الموارد البشرية", description = "شؤون الكوادر والتوظيف وتدريب الطواقم", location = "المبنى الرئيسي، الطابق الأول", teamCount = 2)
            ).toInt()

            // Seed Offices
            val auditOfficeId = officeDao.insertOffice(
                Office(name = "مكتب التدقيق الداخلي", departmentId = financeId, managerName = "أحمد المنصور", status = "Active")
            ).toInt()

            val logOfficeId = officeDao.insertOffice(
                Office(name = "مكتب العمليات الإقليمي", departmentId = operationsId, managerName = "أحمد منصور", status = "Active")
            ).toInt()

            val hrOfficeId = officeDao.insertOffice(
                Office(name = "مكتب شؤون الكوادر والتدريب", departmentId = hrId, managerName = "سارة العتيبي", status = "Active")
            ).toInt()

            // Seed Employees
            val emp1Id = employeeDao.insertEmployee(
                Employee(name = "أحمد منصور", role = "مدير العمليات الإقليمي", officeId = logOfficeId, branchLocation = "المكتب الرئيسي - الرياض", avatarUrl = "", selfId = "EMP-001", achievements = "قيادة فريق التتبع اللوجستي، تطوير نظام تشغيل الأسطول", tags = "قيادة, لوجستيات, تخطيط استراتيجي", isOfficeManager = true)
            ).toInt()

            val emp2Id = employeeDao.insertEmployee(
                Employee(name = "سارة العتيبي", role = "أخصائي موارد بشرية", officeId = hrOfficeId, branchLocation = "فرع جدة - المنطقة الغربية", avatarUrl = "", selfId = "EMP-002", achievements = "إعداد 3 برامج تدريبية، تطوير سياسة الموارد البشرية", tags = "موارد بشرية, توظيف, تدريب")
            ).toInt()

            val emp3Id = employeeDao.insertEmployee(
                Employee(name = "فهد القحطاني", role = "مدقق مالي أول", officeId = auditOfficeId, branchLocation = "المكتب الرئيسي - الرياض", avatarUrl = "", selfId = "EMP-003", achievements = "تدقيق مالي 2026، إعداد تقارير الربع السنوي", tags = "تدقيق, مالية, محاسبة", isOfficeManager = true)
            ).toInt()

            // Seed Tasks with Eisenhower metrics
            taskDao.insertTask(
                Task(
                    title = "تقرير الأداء الشهري",
                    description = "تقديم تقرير شامل عن أداء جميع أقسام الفرع وتسليمه اليوم للمدير العام",
                    employeeId = emp1Id,
                    progress = 40,
                    notes = "موعد التسليم اليوم",
                    urgency = "Urgent",
                    importance = "Important",
                    status = "Pending",
                    dueDate = "اليوم"
                )
            )

            taskDao.insertTask(
                Task(
                    title = "مراجعة جزاءات معلقة",
                    description = "طلب من قسم الموارد البشرية لتدقيق الخصومات المترتبة على الغيابات",
                    employeeId = emp2Id,
                    progress = 10,
                    notes = "تحت المراجعة من الكادر",
                    urgency = "Urgent",
                    importance = "Important",
                    status = "Pending",
                    dueDate = "غداً"
                )
            )

            taskDao.insertTask(
                Task(
                    title = "التدقيق السنوي للمصروفات التشغيلية",
                    description = "إعداد التقرير المالي الختامي لكافة المكاتب والخدمات الميدانية",
                    employeeId = emp3Id,
                    progress = 95,
                    notes = "مكتمل تقريباً",
                    urgency = "Not Urgent",
                    importance = "Important",
                    status = "Pending",
                    dueDate = "نهاية الأسبوع"
                )
            )

            // Seed Attendances (including today for org structure view)
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val dates = listOf("2026-06-25", "2026-06-26", "2026-06-27", "2026-06-28", "2026-06-29", today)
            val emps = listOf(emp1Id, emp2Id, emp3Id)
            for (emp in emps) {
                for (d in dates) {
                    val status = when {
                        d == today && emp == emp2Id -> "Absent"
                        d == "2026-06-27" && emp == emp2Id -> "Absent"
                        d == today && emp == emp3Id -> "Late"
                        else -> "Present"
                    }
                    attendanceDao.insertAttendance(Attendance(employeeId = emp, date = d, status = status))
                }
            }

            // Seed Penalties
            penaltyDao.insertPenalty(
                Penalty(employeeId = emp2Id, amount = 120.0, reason = "تأخير غير مبرر في تسليم تقرير المباشرة", date = "2026-06-28", status = "Pending")
            )

            penaltyDao.insertPenalty(
                Penalty(employeeId = emp1Id, amount = 0.0, reason = "إنذار شفوي للالتزام بمواعيد الإغلاق", date = "2026-06-26", status = "Reviewed")
            )

            // Seed Invoices
            invoiceDao.insertInvoice(
                Invoice(officeId = logOfficeId, amount = 85000.0, trackingNumber = "INV-2026-001", description = "صيانة أسطول الشاحنات اللوجستية", date = "2026-06-20", type = "Operations")
            )
            invoiceDao.insertInvoice(
                Invoice(officeId = auditOfficeId, amount = 39500.0, trackingNumber = "INV-2026-002", description = "برمجيات وأجهزة التدقيق المالي للمقر", date = "2026-06-22", type = "Supplier")
            )

            // ──────────── Seed Projects ────────────
            val proj1Id = projectDao.insertProject(
                Project(name = "نظام التتبع اللوجستي", description = "تطوير وتنفيذ نظام تتبع ذكي للشاحنات والمقطورات", startDate = "2026-06-01", dueDate = "2026-09-30", status = "InProgress", priority = "High", progress = 40, departmentId = operationsId, officeId = logOfficeId, scopeType = "SingleOffice")
            ).toInt()

            val proj2Id = projectDao.insertProject(
                Project(name = "تدقيق مالي 2026", description = "مراجعة شاملة للحسابات المالية للعام 2026", startDate = "2026-07-01", dueDate = "2026-08-15", status = "NotStarted", priority = "Critical", progress = 0, departmentId = financeId, officeId = null, scopeType = "SingleDepartment")
            ).toInt()

            val proj3Id = projectDao.insertProject(
                Project(name = "برنامج تطوير الكوادر", description = "تدريب وتأهيل الموظفين على أنظمة الجودة", startDate = "2026-05-01", dueDate = "2026-07-15", status = "InProgress", priority = "Medium", progress = 65, departmentId = hrId, officeId = null, scopeType = "Shared")
            ).toInt()

            // Seed Project Members
            projectMemberDao.insertMember(ProjectMember(projectId = proj1Id.toInt(), employeeId = emp1Id, role = "Lead"))
            projectMemberDao.insertMember(ProjectMember(projectId = proj1Id.toInt(), employeeId = emp3Id, role = "Member"))
            projectMemberDao.insertMember(ProjectMember(projectId = proj2Id.toInt(), employeeId = emp3Id, role = "Lead"))
            projectMemberDao.insertMember(ProjectMember(projectId = proj2Id.toInt(), employeeId = emp1Id, role = "Member"))
            projectMemberDao.insertMember(ProjectMember(projectId = proj3Id.toInt(), employeeId = emp2Id, role = "Lead"))
            projectMemberDao.insertMember(ProjectMember(projectId = proj3Id.toInt(), employeeId = emp1Id, role = "Reviewer"))

            // Seed Project Tasks (ربط المهام بالمشاريع)
            taskDao.insertTask(
                Task(title = "تحليل المتطلبات اللوجستية", description = "دراسة احتياجات الأسطول", employeeId = emp1Id, projectId = proj1Id.toInt(), progress = 100, notes = "مكتمل", urgency = "Urgent", importance = "Important", status = "Completed", dueDate = "2026-06-15")
            )
            taskDao.insertTask(
                Task(title = "تصميم واجهة التتبع", description = "تصميم واجهة المستخدم للنظام", employeeId = emp1Id, projectId = proj1Id.toInt(), progress = 30, notes = "قيد التصميم", urgency = "Urgent", importance = "Important", status = "Pending", dueDate = "2026-07-20")
            )
            taskDao.insertTask(
                Task(title = "إعداد قوائم التدقيق", description = "إعداد قوائم التدقيق المالي", employeeId = emp3Id, projectId = proj2Id.toInt(), progress = 0, notes = "", urgency = "Urgent", importance = "Important", status = "Pending", dueDate = "2026-07-25")
            )
            taskDao.insertTask(
                Task(title = "إعداد الحقائب التدريبية", description = "تجهيز مواد تدريب الموظفين", employeeId = emp2Id, projectId = proj3Id.toInt(), progress = 70, notes = "متبقي المراجعة النهائية", urgency = "Not Urgent", importance = "Important", status = "Pending", dueDate = "2026-07-10")
            )

            // ──────────── Seed Sample Evaluations ────────────
            evaluationDao.insertEvaluation(
                EmployeeEvaluation(employeeId = emp1Id, evaluatorAdminId = 1, periodStart = "2026-06-01", periodEnd = "2026-06-30",
                    taskTimelinessScore = 25.0, qualityScore = 22.0, attendanceScore = 20.0, teamworkScore = 9.0, innovationScore = 8.0, penaltyDeduction = 0.0,
                    totalScore = 84.0, rating = "جيد جداً", notes = "أداء متميز في إدارة العمليات", createdAt = "2026-06-30")
            )
            evaluationDao.insertEvaluation(
                EmployeeEvaluation(employeeId = emp2Id, evaluatorAdminId = 1, periodStart = "2026-06-01", periodEnd = "2026-06-30",
                    taskTimelinessScore = 18.0, qualityScore = 20.0, attendanceScore = 16.0, teamworkScore = 8.0, innovationScore = 6.0, penaltyDeduction = -3.0,
                    totalScore = 65.0, rating = "جيد", notes = "تحتاج تحسين في الالتزام بالمواعيد", createdAt = "2026-06-30")
            )
            evaluationDao.insertEvaluation(
                EmployeeEvaluation(employeeId = emp3Id, evaluatorAdminId = 1, periodStart = "2026-06-01", periodEnd = "2026-06-30",
                    taskTimelinessScore = 28.0, qualityScore = 24.0, attendanceScore = 20.0, teamworkScore = 9.0, innovationScore = 7.0, penaltyDeduction = 0.0,
                    totalScore = 88.0, rating = "جيد جداً", notes = "مدقق مالي ممتاز", createdAt = "2026-06-30")
            )
        }
    }
}

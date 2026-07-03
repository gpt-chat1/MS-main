package com.example.data.dao

import androidx.room.*
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AdminDao {
    @Query("SELECT * FROM admins WHERE selfId = :selfId LIMIT 1")
    suspend fun getAdminBySelfId(selfId: String): Admin?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAdmin(admin: Admin)

    @Query("SELECT COUNT(*) FROM admins")
    suspend fun getAdminCount(): Int
}

@Dao
interface DepartmentDao {
    @Query("SELECT * FROM departments ORDER BY name ASC")
    fun getAllDepartments(): Flow<List<Department>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDepartment(department: Department): Long

    @Delete
    suspend fun deleteDepartment(department: Department)

    @Query("SELECT COUNT(*) FROM departments")
    suspend fun getDepartmentCount(): Int

    @Query("SELECT * FROM departments WHERE id = :id LIMIT 1")
    suspend fun getDepartmentById(id: Int): Department?
}

@Dao
interface OfficeDao {
    @Query("SELECT * FROM offices ORDER BY name ASC")
    fun getAllOffices(): Flow<List<Office>>

    @Query("SELECT * FROM offices WHERE departmentId = :deptId ORDER BY name ASC")
    fun getOfficesByDepartment(deptId: Int): Flow<List<Office>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertOffice(office: Office): Long

    @Delete
    suspend fun deleteOffice(office: Office)

    @Query("SELECT SUM(amount) FROM invoices WHERE officeId = :officeId")
    suspend fun getOfficeInvoiceSum(officeId: Int): Double?

    @Query("SELECT COUNT(*) FROM offices WHERE departmentId = :deptId")
    suspend fun getOfficeCountByDepartment(deptId: Int): Int

    @Query("SELECT * FROM offices WHERE id = :id LIMIT 1")
    suspend fun getOfficeById(id: Int): Office?
}

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees ORDER BY name ASC")
    fun getAllEmployees(): Flow<List<Employee>>

    @Query("""
        SELECT e.* FROM employees e 
        INNER JOIN offices o ON e.officeId = o.id 
        WHERE o.departmentId = :deptId 
        ORDER BY e.name ASC
    """)
    fun getEmployeesByDepartmentAll(deptId: Int): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE officeId = :officeId ORDER BY name ASC")
    fun getEmployeesByOffice(officeId: Int): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE officeId = :officeId AND status = 'Active' ORDER BY name ASC")
    fun getEmployeesByOfficeActive(officeId: Int): Flow<List<Employee>>

    @Query("""
        SELECT * FROM employees 
        WHERE name LIKE '%' || :query || '%' OR role LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchEmployees(query: String): Flow<List<Employee>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEmployee(employee: Employee): Long

    @Update
    suspend fun updateEmployee(employee: Employee)

    @Delete
    suspend fun deleteEmployee(employee: Employee)

    @Query("SELECT * FROM employees WHERE status = 'Active' ORDER BY name ASC")
    fun getActiveEmployees(): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE id = :id LIMIT 1")
    suspend fun getEmployeeById(id: Int): Employee?

    @Query("SELECT * FROM employees WHERE selfId = :selfId LIMIT 1")
    suspend fun getEmployeeBySelfId(selfId: String): Employee?

    @Query("""
        SELECT e.* FROM employees e 
        INNER JOIN offices o ON e.officeId = o.id 
        WHERE o.departmentId = :deptId AND e.status = 'Active'
        ORDER BY e.name ASC
    """)
    fun getEmployeesByDepartment(deptId: Int): Flow<List<Employee>>
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE employeeId = :employeeId ORDER BY id DESC")
    fun getTasksByEmployee(employeeId: Int): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY id DESC")
    fun searchTasks(query: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY sortOrder ASC, id ASC")
    fun getTasksByProjectOrdered(projectId: Int): Flow<List<Task>>

    @Query("UPDATE tasks SET sortOrder = :order WHERE id = :taskId")
    suspend fun updateTaskOrder(taskId: Int, order: Int)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTask(task: Task): Long

    @Query("UPDATE tasks SET progress = :progress, status = :status, notes = :notes WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Int, progress: Int, status: String, notes: String)

    @Delete
    suspend fun deleteTask(task: Task)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance ORDER BY date DESC")
    fun getAllAttendance(): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE employeeId = :employeeId ORDER BY date DESC")
    fun getAttendanceByEmployee(employeeId: Int): Flow<List<Attendance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance): Long

    @Query("SELECT * FROM attendance WHERE date = :date")
    fun getAttendanceByDate(date: String): Flow<List<Attendance>>

    @Query("SELECT status FROM attendance WHERE employeeId = :employeeId AND date = :date LIMIT 1")
    suspend fun getAttendanceStatusForEmployee(employeeId: Int, date: String): String?

    @Query("SELECT COUNT(*) FROM attendance WHERE employeeId = :employeeId AND status = 'Present'")
    suspend fun getPresentDays(employeeId: Int): Int

    @Query("SELECT COUNT(*) FROM attendance WHERE employeeId = :employeeId")
    suspend fun getTotalDays(employeeId: Int): Int
}

@Dao
interface PenaltyDao {
    @Query("SELECT * FROM penalties ORDER BY date DESC")
    fun getAllPenalties(): Flow<List<Penalty>>

    @Query("SELECT * FROM penalties WHERE employeeId = :employeeId ORDER BY date DESC")
    fun getPenaltiesByEmployee(employeeId: Int): Flow<List<Penalty>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPenalty(penalty: Penalty): Long

    @Query("SELECT SUM(amount) FROM penalties WHERE employeeId = :employeeId")
    suspend fun getTotalPenaltiesByEmployee(employeeId: Int): Double?
}

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY date DESC")
    fun getAllInvoices(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE officeId = :officeId ORDER BY date DESC")
    fun getInvoicesByOffice(officeId: Int): Flow<List<Invoice>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertInvoice(invoice: Invoice): Long

    @Update
    suspend fun updateInvoice(invoice: Invoice)

    @Delete
    suspend fun deleteInvoice(invoice: Invoice)

    @Query("SELECT SUM(amount) FROM invoices")
    fun getTotalInvoiceSumFlow(): Flow<Double?>

    @Query("SELECT * FROM invoices WHERE projectId = :projectId ORDER BY date DESC")
    fun getInvoicesByProject(projectId: Int): Flow<List<Invoice>>

    @Query("SELECT SUM(amount) FROM invoices WHERE projectId = :projectId")
    suspend fun getProjectInvoicesSum(projectId: Int): Double?
}

@Dao
interface ReportDao {
    // Transaction query to aggregate branch productivity and financial costs
    @Transaction
    @Query("SELECT id FROM employees WHERE status = 'Active'")
    suspend fun getEmployeeIds(): List<Int>

    @Query("SELECT * FROM employees WHERE id = :id")
    suspend fun getEmployeeById(id: Int): Employee?

    @Query("SELECT COUNT(*) FROM tasks WHERE employeeId = :empId AND status = 'Completed'")
    suspend fun getCompletedTasksCount(empId: Int): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE employeeId = :empId")
    suspend fun getTotalTasksCount(empId: Int): Int

    @Query("SELECT SUM(amount) FROM penalties WHERE employeeId = :empId")
    suspend fun getPenaltiesSum(empId: Int): Double?

    @Transaction
    suspend fun getEmployeeReportCards(): List<EmployeeReportCard> {
        val ids = getEmployeeIds()
        val cards = mutableListOf<EmployeeReportCard>()
        for (id in ids) {
            val emp = getEmployeeById(id) ?: continue
            val completed = getCompletedTasksCount(id)
            val total = getTotalTasksCount(id)
            
            // Attendance Rate
            val presentCount = getPresentDaysCount(id)
            val totalDays = getAttendanceDaysCount(id)
            val rate = if (totalDays > 0) (presentCount.toDouble() / totalDays.toDouble()) * 100.0 else 100.0
            
            // Penalty Sum
            val penalties = getPenaltiesSum(id) ?: 0.0

            cards.add(
                EmployeeReportCard(
                    employeeId = id,
                    employeeName = emp.name,
                    employeeRole = emp.role,
                    branchLocation = emp.branchLocation,
                    completedTasks = completed,
                    totalTasks = total,
                    attendanceRate = rate,
                    totalPenalties = penalties,
                    totalInvoices = 0.0 // Costs associated directly with employees are tracking under invoices/office
                )
            )
        }
        return cards
    }

    @Query("SELECT COUNT(*) FROM attendance WHERE employeeId = :empId AND status = 'Present'")
    suspend fun getPresentDaysCount(empId: Int): Int

    @Query("SELECT COUNT(*) FROM attendance WHERE employeeId = :empId")
    suspend fun getAttendanceDaysCount(empId: Int): Int

    @Transaction
    @Query("SELECT * FROM offices")
    suspend fun getOfficeReportCardsRaw(): List<Office>

    @Query("SELECT COUNT(*) FROM employees WHERE officeId = :officeId")
    suspend fun getEmployeeCountInOffice(officeId: Int): Int

    @Query("SELECT SUM(amount) FROM invoices WHERE officeId = :officeId")
    suspend fun getOfficeInvoicesSum(officeId: Int): Double?

    @Query("SELECT name FROM departments WHERE id = :deptId LIMIT 1")
    suspend fun getDepartmentName(deptId: Int): String?

    @Query("SELECT COUNT(*) FROM tasks t INNER JOIN employees e ON t.employeeId = e.id WHERE e.officeId = :officeId")
    suspend fun getOfficeTotalTasks(officeId: Int): Int

    @Query("SELECT COUNT(*) FROM tasks t INNER JOIN employees e ON t.employeeId = e.id WHERE e.officeId = :officeId AND t.status = 'Completed'")
    suspend fun getOfficeCompletedTasks(officeId: Int): Int

    @Transaction
    suspend fun getOfficeReportCards(): List<OfficeReportCard> {
        val offices = getOfficeReportCardsRaw()
        val list = mutableListOf<OfficeReportCard>()
        for (office in offices) {
            val empCount = getEmployeeCountInOffice(office.id)
            val invoicesSum = getOfficeInvoicesSum(office.id) ?: 0.0
            val deptName = getDepartmentName(office.departmentId) ?: "غير معروف"
            val totalTasks = getOfficeTotalTasks(office.id)
            val completedTasks = getOfficeCompletedTasks(office.id)
            list.add(
                OfficeReportCard(
                    officeId = office.id,
                    officeName = office.name,
                    departmentName = deptName,
                    managerName = office.managerName,
                    employeeCount = empCount,
                    totalInvoices = invoicesSum,
                    totalTasks = totalTasks,
                    completedTasks = completedTasks
                )
            )
        }
        return list
    }

    @Query("SELECT * FROM departments")
    suspend fun getAllDepartmentsRaw(): List<Department>

    @Query("SELECT COUNT(*) FROM offices WHERE departmentId = :deptId")
    suspend fun getOfficeCountInDepartment(deptId: Int): Int

    @Query("""
        SELECT COUNT(*) FROM employees e 
        INNER JOIN offices o ON e.officeId = o.id 
        WHERE o.departmentId = :deptId AND e.status = 'Active'
    """)
    suspend fun getEmployeeCountInDepartment(deptId: Int): Int

    @Query("""
        SELECT COUNT(*) FROM tasks t 
        INNER JOIN employees e ON t.employeeId = e.id 
        INNER JOIN offices o ON e.officeId = o.id 
        WHERE o.departmentId = :deptId
    """)
    suspend fun getDepartmentTotalTasks(deptId: Int): Int

    @Query("""
        SELECT COUNT(*) FROM tasks t 
        INNER JOIN employees e ON t.employeeId = e.id 
        INNER JOIN offices o ON e.officeId = o.id 
        WHERE o.departmentId = :deptId AND t.status = 'Completed'
    """)
    suspend fun getDepartmentCompletedTasks(deptId: Int): Int

    @Query("""
        SELECT SUM(i.amount) FROM invoices i 
        INNER JOIN offices o ON i.officeId = o.id 
        WHERE o.departmentId = :deptId
    """)
    suspend fun getDepartmentInvoicesSum(deptId: Int): Double?

    @Transaction
    suspend fun getDepartmentReportCards(): List<DepartmentReportCard> {
        val departments = getAllDepartmentsRaw()
        return departments.map { dept ->
            DepartmentReportCard(
                departmentId = dept.id,
                departmentName = dept.name,
                officeCount = getOfficeCountInDepartment(dept.id),
                employeeCount = getEmployeeCountInDepartment(dept.id),
                completedTasks = getDepartmentCompletedTasks(dept.id),
                totalTasks = getDepartmentTotalTasks(dept.id),
                totalInvoices = getDepartmentInvoicesSum(dept.id) ?: 0.0
            )
        }
    }
}

// ──────────── PROJECT DAO ────────────
@Dao
interface ProjectDao {
    @Query("""
        SELECT * FROM projects ORDER BY 
        CASE WHEN status = 'InProgress' THEN 0 
             WHEN status = 'NotStarted' THEN 1 
             WHEN status = 'Delayed' THEN 2 
             ELSE 3 END, dueDate ASC
    """)
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: Int): Project?

    @Query("SELECT * FROM projects WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY dueDate ASC")
    fun searchProjects(query: String): Flow<List<Project>>

    @Query("""
        SELECT p.* FROM projects p 
        INNER JOIN project_members pm ON p.id = pm.projectId 
        WHERE pm.employeeId = :employeeId 
        ORDER BY p.dueDate ASC
    """)
    fun getProjectsByEmployee(employeeId: Int): Flow<List<Project>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProject(project: Project): Long

    @Update
    suspend fun updateProject(project: Project)

    @Delete
    suspend fun deleteProject(project: Project)

    @Query("UPDATE projects SET progress = :progress WHERE id = :id")
    suspend fun updateProjectProgress(id: Int, progress: Int)

    @Query("UPDATE projects SET status = :status WHERE id = :id")
    suspend fun updateProjectStatus(id: Int, status: String)

    @Query("SELECT COUNT(*) FROM tasks WHERE projectId = :projectId AND status = 'Completed'")
    suspend fun getProjectCompletedTasks(projectId: Int): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE projectId = :projectId")
    suspend fun getProjectTotalTasks(projectId: Int): Int

    @Query("SELECT COUNT(*) FROM project_members WHERE projectId = :projectId")
    suspend fun getProjectMemberCount(projectId: Int): Int

    @Query("SELECT * FROM projects WHERE dueDate < :today AND status != 'Completed'")
    suspend fun getOverdueProjects(today: String): List<Project>
}

// ──────────── PROJECT MEMBER DAO ────────────
@Dao
interface ProjectMemberDao {
    @Query("SELECT * FROM project_members WHERE projectId = :projectId")
    fun getMembersByProject(projectId: Int): Flow<List<ProjectMember>>

    @Query("SELECT * FROM project_members WHERE employeeId = :employeeId")
    fun getProjectsByEmployee(employeeId: Int): Flow<List<ProjectMember>>

    @Query("SELECT e.* FROM employees e INNER JOIN project_members pm ON e.id = pm.employeeId WHERE pm.projectId = :projectId")
    fun getEmployeesByProject(projectId: Int): Flow<List<Employee>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMember(member: ProjectMember): Long

    @Delete
    suspend fun deleteMember(member: ProjectMember)

    @Query("DELETE FROM project_members WHERE projectId = :projectId AND employeeId = :employeeId")
    suspend fun removeMember(projectId: Int, employeeId: Int)

    @Query("SELECT * FROM project_members WHERE projectId = :projectId AND employeeId = :employeeId LIMIT 1")
    suspend fun getMemberByProjectAndEmployee(projectId: Int, employeeId: Int): ProjectMember?
}

// ──────────── EVALUATION DAO ────────────
@Dao
interface EvaluationDao {
    @Query("SELECT * FROM employee_evaluations WHERE employeeId = :employeeId ORDER BY createdAt DESC")
    fun getEvaluationsByEmployee(employeeId: Int): Flow<List<EmployeeEvaluation>>

    @Query("""
        SELECT * FROM employee_evaluations 
        WHERE periodStart >= :start AND periodEnd <= :end 
        ORDER BY totalScore DESC
    """)
    fun getEvaluationsByPeriod(start: String, end: String): Flow<List<EmployeeEvaluation>>

    @Query("SELECT * FROM employee_evaluations ORDER BY totalScore DESC")
    suspend fun getAllEvaluations(): List<EmployeeEvaluation>

    @Query("SELECT * FROM employee_evaluations WHERE id = :id")
    suspend fun getEvaluationById(id: Int): EmployeeEvaluation?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEvaluation(evaluation: EmployeeEvaluation): Long

    @Delete
    suspend fun deleteEvaluation(evaluation: EmployeeEvaluation)

    @Query("SELECT AVG(totalScore) FROM employee_evaluations WHERE employeeId IN (" +
        "SELECT e.id FROM employees e INNER JOIN offices o ON e.officeId = o.id WHERE o.id = :officeId" +
        ") AND createdAt >= :since")
    suspend fun getOfficeAvgScore(officeId: Int, since: String): Double?

    @Query("SELECT AVG(totalScore) FROM employee_evaluations WHERE employeeId IN (" +
        "SELECT e.id FROM employees e INNER JOIN offices o ON e.officeId = o.id INNER JOIN departments d ON o.departmentId = d.id WHERE d.id = :deptId" +
        ") AND createdAt >= :since")
    suspend fun getDepartmentAvgScore(deptId: Int, since: String): Double?

    @Query("SELECT AVG(totalScore) FROM employee_evaluations WHERE createdAt >= :since")
    suspend fun getOverallAvgScore(since: String): Double?

    @Query("""
        SELECT e.id, e.name, ev.totalScore FROM employees e 
        INNER JOIN employee_evaluations ev ON e.id = ev.employeeId 
        WHERE e.officeId = :officeId AND ev.createdAt >= :since 
        ORDER BY ev.totalScore DESC LIMIT 1
    """)
    suspend fun getTopEmployeeInOffice(officeId: Int, since: String): TopEmployeeResult?

    @Query("""
        SELECT e.id, e.name, ev.totalScore FROM employees e 
        INNER JOIN employee_evaluations ev ON e.id = ev.employeeId 
        WHERE e.officeId = :officeId AND ev.createdAt >= :since 
        ORDER BY ev.totalScore ASC LIMIT 1
    """)
    suspend fun getBottomEmployeeInOffice(officeId: Int, since: String): TopEmployeeResult?
}

data class TopEmployeeResult(
    val id: Int,
    val name: String,
    val totalScore: Double
)

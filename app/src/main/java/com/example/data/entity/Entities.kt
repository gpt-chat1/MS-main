package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "admins", indices = [Index(value = ["selfId"], unique = true)])
data class Admin(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val surname: String,
    val selfId: String,
    val passwordHash: String
)

@Entity(tableName = "departments", indices = [Index(value = ["name"], unique = true)])
data class Department(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val location: String,
    val teamCount: Int = 0
)

@Entity(
    tableName = "offices",
    foreignKeys = [
        ForeignKey(
            entity = Department::class,
            parentColumns = ["id"],
            childColumns = ["departmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["departmentId"]), Index(value = ["departmentId", "name"], unique = true)]
)
data class Office(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val departmentId: Int,
    val managerName: String,
    val status: String // Active, Inactive, Maintenance
)

@Entity(
    tableName = "employees",
    foreignKeys = [
        ForeignKey(
            entity = Office::class,
            parentColumns = ["id"],
            childColumns = ["officeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["officeId"]), Index(value = ["officeId", "name"], unique = true)]
)
data class Employee(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val role: String,
    val officeId: Int,
    val branchLocation: String,
    val avatarUrl: String,
    val status: String = "Active", // Active, Dismissed
    val selfId: String = "",
    val achievements: String = "", // JSON or comma-separated achievements
    val tags: String = "", // comma-separated skills/tags
    val isOfficeManager: Boolean = false,
    val isDepartmentManager: Boolean = false
)

@Entity(
    tableName = "projects",
    foreignKeys = [
        ForeignKey(
            entity = Department::class,
            parentColumns = ["id"],
            childColumns = ["departmentId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Office::class,
            parentColumns = ["id"],
            childColumns = ["officeId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["departmentId"]), Index(value = ["officeId"])]
)
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val startDate: String,
    val dueDate: String,
    val status: String, // NotStarted, InProgress, Completed, Delayed
    val priority: String, // Low, Medium, High, Critical
    val progress: Int = 0, // 0-100
    val departmentId: Int? = null,
    val officeId: Int? = null,
    val scopeType: String = "SingleOffice", // SingleOffice, SingleDepartment, Shared
    val budget: Double = 0.0
)

@Entity(
    tableName = "project_members",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Employee::class,
            parentColumns = ["id"],
            childColumns = ["employeeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"]), Index(value = ["employeeId"]), Index(value = ["projectId", "employeeId"], unique = true)]
)
data class ProjectMember(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val employeeId: Int,
    val role: String // Lead, Member, Reviewer
)

@Entity(
    tableName = "employee_evaluations",
    foreignKeys = [
        ForeignKey(
            entity = Employee::class,
            parentColumns = ["id"],
            childColumns = ["employeeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Admin::class,
            parentColumns = ["id"],
            childColumns = ["evaluatorAdminId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["employeeId"]), Index(value = ["evaluatorAdminId"])]
)
data class EmployeeEvaluation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: Int,
    val evaluatorAdminId: Int,
    val periodStart: String,
    val periodEnd: String,
    val taskTimelinessScore: Double, // 0-30
    val qualityScore: Double, // 0-25
    val attendanceScore: Double, // 0-20
    val teamworkScore: Double, // 0-10
    val innovationScore: Double, // 0-10
    val penaltyDeduction: Double, // 0 to -15
    val totalScore: Double,
    val rating: String, // ممتاز, جيد جداً, جيد, مقبول, ضعيف
    val notes: String,
    val createdAt: String
)

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Employee::class,
            parentColumns = ["id"],
            childColumns = ["employeeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["employeeId"]), Index(value = ["projectId"])]
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val employeeId: Int,
    val projectId: Int? = null,
    val progress: Int, // 0 to 100
    val notes: String,
    val urgency: String, // Urgent, Not Urgent
    val importance: String, // Important, Not Important
    val status: String, // Pending, Completed, Critical
    val dueDate: String,
    val sortOrder: Int = 0
)

@Entity(
    tableName = "attendance",
    foreignKeys = [
        ForeignKey(
            entity = Employee::class,
            parentColumns = ["id"],
            childColumns = ["employeeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["employeeId"]), Index(value = ["employeeId", "date"], unique = true)]
)
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: Int,
    val date: String, // YYYY-MM-DD
    val status: String // Present, Absent, Late
)

@Entity(
    tableName = "penalties",
    foreignKeys = [
        ForeignKey(
            entity = Employee::class,
            parentColumns = ["id"],
            childColumns = ["employeeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["employeeId"])]
)
data class Penalty(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: Int,
    val amount: Double,
    val reason: String,
    val date: String, // YYYY-MM-DD
    val status: String // Pending, Reviewed
)

@Entity(
    tableName = "invoices",
    foreignKeys = [
        ForeignKey(
            entity = Office::class,
            parentColumns = ["id"],
            childColumns = ["officeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["officeId"]), Index(value = ["trackingNumber"], unique = true)]
)
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val officeId: Int,
    val amount: Double,
    val trackingNumber: String,
    val description: String,
    val date: String, // YYYY-MM-DD
    val type: String, // Operations, Utility, Supplier
    val imageUrl: String = "",
    val projectId: Int? = null
)

// Aggregate Entity models for reporting
data class EmployeeReportCard(
    val employeeId: Int,
    val employeeName: String,
    val employeeRole: String,
    val branchLocation: String,
    val completedTasks: Int,
    val totalTasks: Int,
    val attendanceRate: Double,
    val totalPenalties: Double,
    val totalInvoices: Double
)

data class OfficeReportCard(
    val officeId: Int,
    val officeName: String,
    val departmentName: String,
    val managerName: String,
    val employeeCount: Int,
    val totalInvoices: Double,
    val totalTasks: Int,
    val completedTasks: Int
)

data class DepartmentReportCard(
    val departmentId: Int,
    val departmentName: String,
    val officeCount: Int,
    val employeeCount: Int,
    val completedTasks: Int,
    val totalTasks: Int,
    val totalInvoices: Double
)

data class ProjectWithProgress(
    val project: Project,
    val memberCount: Int,
    val completedTasks: Int,
    val totalTasks: Int
)

data class OfficeEvaluationSummary(
    val officeId: Int,
    val officeName: String,
    val departmentName: String,
    val employeeCount: Int,
    val averageScore: Double,
    val averageRating: String,
    val topEmployeeName: String,
    val topEmployeeScore: Double
)

data class EmployeeEvaluationCard(
    val evaluation: EmployeeEvaluation,
    val employeeName: String,
    val employeeRole: String,
    val officeName: String
)

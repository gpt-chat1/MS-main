package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AdminDao
import com.example.data.dao.AttendanceDao
import com.example.data.dao.DepartmentDao
import com.example.data.dao.EmployeeDao
import com.example.data.dao.EvaluationDao
import com.example.data.dao.InvoiceDao
import com.example.data.dao.OfficeDao
import com.example.data.dao.PenaltyDao
import com.example.data.dao.ProjectDao
import com.example.data.dao.ProjectMemberDao
import com.example.data.dao.ReportDao
import com.example.data.dao.TaskDao
import com.example.data.entity.Admin
import com.example.data.entity.Attendance
import com.example.data.entity.Department
import com.example.data.entity.Employee
import com.example.data.entity.EmployeeEvaluation
import com.example.data.entity.Invoice
import com.example.data.entity.Office
import com.example.data.entity.Penalty
import com.example.data.entity.Project
import com.example.data.entity.ProjectMember
import com.example.data.entity.Task

@Database(
    entities = [
        Admin::class,
        Department::class,
        Office::class,
        Employee::class,
        Project::class,
        ProjectMember::class,
        Task::class,
        Attendance::class,
        Penalty::class,
        Invoice::class,
        EmployeeEvaluation::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun adminDao(): AdminDao
    abstract fun departmentDao(): DepartmentDao
    abstract fun officeDao(): OfficeDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun projectDao(): ProjectDao
    abstract fun projectMemberDao(): ProjectMemberDao
    abstract fun taskDao(): TaskDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun penaltyDao(): PenaltyDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun reportDao(): ReportDao
    abstract fun evaluationDao(): EvaluationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "managerhub_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        fun closeInstance() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
            }
        }
    }
}

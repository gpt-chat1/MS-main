package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.db.AppDatabase
import com.example.data.repository.AppRepository
import com.example.ui.AppNavigation
import com.example.ui.MainViewModel
import com.example.ui.MainViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize Database and Repository
    val database = AppDatabase.getDatabase(applicationContext)
    val repository = AppRepository(
      adminDao = database.adminDao(),
      departmentDao = database.departmentDao(),
      officeDao = database.officeDao(),
      employeeDao = database.employeeDao(),
      projectDao = database.projectDao(),
      projectMemberDao = database.projectMemberDao(),
      taskDao = database.taskDao(),
      attendanceDao = database.attendanceDao(),
      penaltyDao = database.penaltyDao(),
      invoiceDao = database.invoiceDao(),
      reportDao = database.reportDao(),
      evaluationDao = database.evaluationDao()
    )

    // Instantiate MainViewModel via Factory
    val viewModel: MainViewModel by viewModels {
      MainViewModelFactory(repository)
    }

    setContent {
      MyApplicationTheme {
        AppNavigation(viewModel)
      }
    }
  }
}

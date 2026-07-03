package com.example.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import android.widget.Toast
import coil.compose.AsyncImage
import com.example.R
import com.example.data.repository.TimelineActivity
import com.example.data.entity.*
import com.example.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val loggedInAdmin by viewModel.loggedInAdmin.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearUiMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBackground)
    ) {
        Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
            when (screen) {
                is AppScreen.SetupAdmin -> SetupAdminScreen(viewModel)
                is AppScreen.Login -> LoginScreen(viewModel)
                is AppScreen.Dashboard -> AppScaffold(viewModel, AppScreen.Dashboard) { DashboardScreen(viewModel) }
                is AppScreen.OrgStructure -> AppScaffold(viewModel, AppScreen.OrgStructure) { OrgStructureScreen(viewModel) }
                is AppScreen.StaffManagement -> AppScaffold(viewModel, AppScreen.StaffManagement) { StaffManagementScreen(viewModel) }
                is AppScreen.Projects -> AppScaffold(viewModel, AppScreen.Projects) { ProjectsScreen(viewModel) }
                is AppScreen.ProjectDetail -> {
                    AppScaffold(viewModel, AppScreen.Projects) { ProjectDetailScreen(viewModel, screen.projectId) }
                }
                is AppScreen.EmployeeDetail -> {
                    AppScaffold(viewModel, AppScreen.StaffManagement) { EmployeeDetailScreen(viewModel, screen.employeeId) }
                }
                is AppScreen.ReportsAnalytics -> AppScaffold(viewModel, AppScreen.ReportsAnalytics) { ReportsAnalyticsScreen(viewModel) }
                is AppScreen.Settings -> SettingsScreen(viewModel)
            }
        }
    }
}

// Custom corporate layout scaffold with a top bar, bottom bar, and active state pill
@Composable
fun AppScaffold(
    viewModel: MainViewModel,
    currentTab: AppScreen,
    content: @Composable () -> Unit
) {
    val admin by viewModel.loggedInAdmin.collectAsState()

    Scaffold(
        topBar = {
            TopAppBarCustom(
                adminName = admin?.name ?: "المدير",
                adminRole = "المدير العام",
                onLogout = { viewModel.logout() },
                onNavigateToSettings = { viewModel.navigateTo(AppScreen.Settings) }
            )
        },
        bottomBar = {
            BottomNavBarCustom(
                currentTab = currentTab,
                onTabSelect = { viewModel.navigateTo(it) }
            )
        },
        containerColor = WarmBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            content()
        }
    }
}

@Composable
fun TopAppBarCustom(adminName: String, adminRole: String, onLogout: () -> Unit, onNavigateToSettings: () -> Unit = {}) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(64.dp)
            .background(BentoNavBackground)
            .border(1.dp, BentoBlueBorder.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Al-Shaheen Premium Brand Logo Circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(DeepGreen)
                    .border(1.5.dp, Gold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AppLogo(
                    modifier = Modifier.size(28.dp)
                )
            }
            Column {
                Text(
                    text = "المدير الذكي",
                    color = DeepGreen,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "المدير: $adminName • $adminRole",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Box {
            IconButton(
                onClick = { expanded = true },
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BentoSlate)
                    .testTag("appbar_profile_btn")
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = BentoDark,
                    modifier = Modifier.size(20.dp)
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(WarmBackground)
            ) {
                DropdownMenuItem(
                    text = { Text("الإعدادات", fontWeight = FontWeight.Bold) },
                    onClick = {
                        expanded = false
                        onNavigateToSettings()
                    },
                    leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = null, tint = DeepGreen) }
                )
                DropdownMenuItem(
                    text = { Text("تسجيل الخروج ($adminName)", color = CoralRed, fontWeight = FontWeight.Bold) },
                    onClick = {
                        expanded = false
                        onLogout()
                    },
                    leadingIcon = { Icon(Icons.Filled.ExitToApp, contentDescription = null, tint = CoralRed) }
                )
            }
        }
    }
}

@Composable
fun BottomNavBarCustom(currentTab: AppScreen, onTabSelect: (AppScreen) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(72.dp)
            .background(BentoNavBackground)
            .border(BorderStroke(1.dp, BentoSlateBorder))
            .padding(vertical = 4.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val tabs = listOf<Triple<AppScreen, ImageVector, String>>(
            Triple(AppScreen.Dashboard, Icons.Filled.Home, "الرئيسية"),
            Triple(AppScreen.OrgStructure, Icons.Filled.List, "الهيكل"),
            Triple(AppScreen.StaffManagement, Icons.Filled.Person, "الموظفين"),
            Triple(AppScreen.Projects, Icons.Filled.Folder, "المشاريع"),
            Triple(AppScreen.ReportsAnalytics, Icons.Filled.Info, "التقارير")
        )

        tabs.forEach { (screen, icon, label) ->
            val isSelected = currentTab == screen
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelect(screen) }
                    .padding(vertical = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 48.dp, height = 32.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) BentoLightBlue else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) BentoBlue else BentoSlateText,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = label,
                    color = if (isSelected) BentoBlue else BentoSlateText,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

// ------------------ 1. SETUP ADMIN SCREEN ------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupAdminScreen(viewModel: MainViewModel) {
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var selfId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AppLogo(
            modifier = Modifier
                .size(100.dp)
                .padding(bottom = 12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "إعداد حساب المدير لأول مرة",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = DeepGreen
        )
        Text(
            text = "Al-Mudeer Al-Dhaki - Smart Admin Panel",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Gold,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, AlShaheenBorderGold.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ShaheenOutlinedTextField(
                    value = name,
                    onValueChange = { 
                        name = it
                        errorMessage = null 
                    },
                    label = { Text("الاسم الأول (First Name)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Text
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                ShaheenOutlinedTextField(
                    value = surname,
                    onValueChange = { 
                        surname = it
                        errorMessage = null 
                    },
                    label = { Text("اللقب / العائلة (Surname)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Text
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                ShaheenOutlinedTextField(
                    value = selfId,
                    onValueChange = { 
                        selfId = it
                        errorMessage = null 
                    },
                    label = { Text("الرقم الذاتي (Self-ID)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Number
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                ShaheenOutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        errorMessage = null 
                    },
                    label = { Text("كلمة المرور / الرقم السري") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Password
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CoralRed.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .border(1.dp, CoralRed.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "تحذير",
                            tint = CoralRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = errorMessage!!,
                            color = CoralRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Button(
                    onClick = {
                        if (name.isBlank() || surname.isBlank() || selfId.isBlank() || password.isBlank()) {
                            errorMessage = "الرجاء ملء كافة الحقول لإنشاء الحساب بنجاح."
                        } else {
                            errorMessage = null
                            viewModel.setupAdmin(name, surname, selfId, password) {}
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("admin_setup_submit"),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("تهيئة الحساب والدخول الآمن", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ------------------ 2. LOGIN SCREEN ------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: MainViewModel) {
    var selfId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AppLogo(
            modifier = Modifier
                .size(100.dp)
                .padding(bottom = 12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "تسجيل الدخول للمدير",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = DeepGreen
        )
        Text(
            text = "Al-Mudeer Al-Dhaki - Smart Admin Login",
            fontSize = 11.sp,
            color = Gold,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, AlShaheenBorderGold.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ShaheenOutlinedTextField(
                    value = selfId,
                    onValueChange = { selfId = it },
                    label = { Text("الرقم الذاتي (Personal ID)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Number
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_self_id_input"),
                    leadingIcon = { Icon(Icons.Filled.AccountBox, contentDescription = null, tint = Gold) }
                )

                ShaheenOutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("كلمة المرور / الرمز السري") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Password
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_password_input"),
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = Gold) }
                )

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        if (selfId.isNotBlank() && password.isNotBlank()) {
                            viewModel.performLogin(selfId, password) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "الرجاء إدخال كافة الحقول للتحقق", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("login_submit_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("تسجيل الدخول الآمن", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(DeepGreen.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = DeepGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("نظام حماية وتشفير البيانات المحلي المدمج", fontSize = 11.sp, color = DeepGreen, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ------------------ 3. DASHBOARD SCREEN ------------------
@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val loggedInAdmin by viewModel.loggedInAdmin.collectAsState()
    val logoUri by viewModel.logoUri.collectAsState()
    val overallPerformance by viewModel.overallBranchPerformance.collectAsState()
    val attendanceRate by viewModel.averageAttendanceRate.collectAsState()
    val efficiencyScore by viewModel.overallEfficiencyScore.collectAsState()
    val tasksList by viewModel.tasks.collectAsState()
    val employeesList by viewModel.employees.collectAsState()
    val projectsList by viewModel.projects.collectAsState()
    val feed by viewModel.timelineFeed.collectAsState()

    var showAddTaskDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Welcome text
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(DeepGreen),
                    contentAlignment = Alignment.Center
                ) {
                    if (logoUri != null) {
                        AsyncImage(model = logoUri, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape))
                    } else {
                        AppLogo(modifier = Modifier.size(32.dp))
                    }
                }
                Column {
                    Text(
                        text = "أهلاً، ${loggedInAdmin?.name ?: "المدير"}",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepGreen
                    )
                    Text(
                        text = "إليك نظرة سريعة على أداء فريقك والفرع اليوم.",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }
            }

            // Bento Grid Row 1: Achievement & Efficiency
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Overall Achievement (Bento Light Purple Theme)
                Card(
                    modifier = Modifier
                        .weight(1.1f)
                        .height(160.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoLightPurple),
                    border = BorderStroke(1.dp, BentoPurpleBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = BentoPurple, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("الإنجاز الكلي", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoDarkPurpleText)
                        }
                        Text("أداء القسم هذا الأسبوع", fontSize = 10.sp, color = BentoDarkPurpleText.copy(alpha = 0.7f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(overallPerformance, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = BentoDarkPurpleText)
                            // Draw circular miniature indicator
                            Box(
                                modifier = Modifier.size(50.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    progress = 0.84f,
                                    color = BentoPurple,
                                    trackColor = Color.White.copy(alpha = 0.5f),
                                    strokeWidth = 5.dp
                                )
                            }
                        }
                    }
                }

                // Card 2: Efficiency Score (Bento Dark Theme)
                Card(
                    modifier = Modifier
                        .weight(0.9f)
                        .height(160.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("مؤشر الكفاءة", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoLightPurple)
                            Icon(Icons.Filled.Star, contentDescription = null, tint = BentoPurple, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(efficiencyScore, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(" / 10", fontSize = 13.sp, color = BentoLightPurple, modifier = Modifier.padding(bottom = 6.dp))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = 0.92f,
                                color = BentoPurple,
                                trackColor = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("نمو بنسبة 4% عن الأمس", fontSize = 9.sp, color = BentoLightPurple)
                        }
                    }
                }
            }

            // Bento Grid Row 2: Active Tasks & Attendance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 3: Active Tasks Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(135.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BentoSlateBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(BentoSlate.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.List, contentDescription = null, tint = BentoBlue, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("المهام النشطة", fontSize = 11.sp, color = BentoSlateText, fontWeight = FontWeight.Medium)
                            Text("${tasksList.filter { it.status != "Completed" }.size} مهمة", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BentoDark)
                        }
                    }
                }

                // Card 4: Attendance Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(135.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BentoSlateBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BentoLightBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Person, contentDescription = null, tint = BentoBlue, modifier = Modifier.size(20.dp))
                            }
                            Text(
                                text = "${employeesList.size}/${employeesList.size} موظف",
                                fontSize = 9.sp,
                                color = BentoBlue,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(BentoLightBlue, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Column {
                            Text("الحضور اليوم", fontSize = 11.sp, color = BentoSlateText, fontWeight = FontWeight.Medium)
                            Text(attendanceRate, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BentoDark)
                        }
                    }
                }
            }

            // Eisenhower Matrix view button / direct dashboard element
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = BentoSlate),
                border = BorderStroke(1.dp, BentoSlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("مصفوفة إيزنهاور للمهام العاجلة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BentoDark)
                        Text(
                            text = "تحديث فوري",
                            fontSize = 10.sp,
                            color = BentoPurple,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(BentoLightPurple, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Simplified Quadrant list representation
                    val qDoFirst = tasksList.filter { it.urgency == "Urgent" && it.importance == "Important" }
                    val qSchedule = tasksList.filter { it.urgency == "Not Urgent" && it.importance == "Important" }
                    val qDelegate = tasksList.filter { it.urgency == "Urgent" && it.importance == "Not Important" }
                    val qEliminate = tasksList.filter { it.urgency == "Not Urgent" && it.importance == "Not Important" }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            QuadrantBox("1. افعل فوراً (Do First)", qDoFirst, BentoBlue)
                            Spacer(modifier = Modifier.height(8.dp))
                            QuadrantBox("3. فوّض الآخرين (Delegate)", qDelegate, BentoPurple)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            QuadrantBox("2. جدول وقتك (Schedule)", qSchedule, BentoPurple)
                            Spacer(modifier = Modifier.height(8.dp))
                            QuadrantBox("4. احذفها (Eliminate)", qEliminate, BentoSlateText)
                        }
                    }
                }
            }

            // Row 3: Pending Alerts List
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, BentoSlateBorder), RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    // Accent border line
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(6.dp)
                            .background(BentoPurple)
                    )
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Warning, contentDescription = null, tint = BentoPurple, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تنبيهات معلقة", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoDark)
                            }
                            Text(
                                "3 مهام حرجة",
                                color = BentoPurple,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(BentoLightPurple, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        AlertItem("تقرير الأداء الشهري", "موعد التسليم: اليوم")
                        Spacer(modifier = Modifier.height(8.dp))
                        AlertItem("مراجعة جزاءات معلقة", "طلب من قسم الموارد البشرية")
                    }
                }
            }

            // Row 4: Live Timeline Updates (آخر التحديثات)
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("آخر التحديثات (Live Timeline)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BentoDark)
                    Text("عرض الكل", fontSize = 11.sp, color = BentoBlue, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoLightBlue),
                    border = BorderStroke(1.dp, BentoBlueBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LIVE TIMELINE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier
                                    .background(BentoBlue, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                            Text("SYNCED: 2M AGO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BentoBlue)
                        }

                        feed.take(4).forEach { item ->
                            TimelineRow(item)
                        }
                    }
                }
            }

            // Project Overview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BentoBlueBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("المشاريع النشطة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                        Text("${projectsList.size} مشروع", fontSize = 11.sp, color = BentoBlue, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    val activeProjects = projectsList.filter { it.status == "InProgress" || it.status == "NotStarted" }
                    if (activeProjects.isEmpty()) {
                        Text("لا توجد مشاريع نشطة", fontSize = 12.sp, color = TextMuted)
                    } else {
                        activeProjects.take(3).forEach { project ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(project.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                    Text("${project.progress}% • ${project.dueDate}", fontSize = 10.sp, color = TextMuted)
                                }
                                LinearProgressIndicator(
                                    progress = project.progress / 100f,
                                    color = if (project.status == "Delayed") OverdueRed else DeepGreen,
                                    trackColor = BentoSlate,
                                    modifier = Modifier.width(60.dp).height(4.dp).clip(CircleShape)
                                )
                            }
                        }
                        if (activeProjects.size > 3) {
                            Text("و ${activeProjects.size - 3} مشاريع أخرى...", fontSize = 10.sp, color = BentoBlue)
                        }
                    }
                }
            }

            // Nearest 3 Upcoming Deadlines
            val upcomingTasks = tasksList
                .filter { it.status != "Completed" && it.dueDate.isNotEmpty() }
                .sortedBy { it.dueDate }
                .take(3)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BentoBlueBorder.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.DateRange, contentDescription = null, tint = BentoBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("أقرب المواعيد النهائية", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BentoBlue)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (upcomingTasks.isEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = StatusCompleted, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("لا توجد مواعيد قادمة", fontSize = 12.sp, color = StatusCompleted)
                        }
                    } else {
                        upcomingTasks.forEach { task ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = PriorityMedium, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(task.title, fontSize = 11.sp, color = TextDark, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                Text(task.dueDate, fontSize = 9.sp, color = if (task.dueDate < todayIsoDate()) CoralRed else DeepGreen)
                            }
                        }
                    }
                }
            }

            // Deadline Alerts Card
            val overdue = tasksList.filter { it.status != "Completed" && it.dueDate.isNotEmpty() && it.dueDate < todayIsoDate() }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, CoralRed.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Warning, contentDescription = null, tint = CoralRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تنبيهات المواعيد المتأخرة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CoralRed)
                        }
                        if (overdue.isNotEmpty()) {
                            Text("${overdue.size} متأخرة", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                modifier = Modifier.background(CoralRed, RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (overdue.isEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = StatusCompleted, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("لا توجد مهام متأخرة", fontSize = 12.sp, color = StatusCompleted)
                        }
                    } else {
                        overdue.take(3).forEach { task ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Warning, contentDescription = null, tint = WarningOrange, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(task.title, fontSize = 11.sp, color = TextDark, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                Text(task.dueDate, fontSize = 9.sp, color = CoralRed)
                            }
                        }
                        if (overdue.size > 3) {
                            Text("و ${overdue.size - 3} مهام أخرى...", fontSize = 10.sp, color = CoralRed)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        // Contextual Floating Action Button
        FloatingActionButton(
            onClick = { showAddTaskDialog = true },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 20.dp, start = 20.dp)
                .size(56.dp)
                .testTag("dashboard_add_task_fab"),
            containerColor = DeepGreen,
            contentColor = Gold,
            shape = CircleShape
        ) {
            Icon(Icons.Filled.Add, contentDescription = "إضافة مهمة جديدة", modifier = Modifier.size(28.dp))
        }

        if (showAddTaskDialog) {
            AddTaskDialog(
                employees = employeesList,
                onDismiss = { showAddTaskDialog = false },
                onAdd = { title, desc, empId, urg, imp, date ->
                    viewModel.assignTask(title, desc, empId, urg, imp, date)
                    showAddTaskDialog = false
                }
            )
        }
    }
}

@Composable
fun QuadrantBox(title: String, tasks: List<Task>, accentColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, BentoSlateBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor)
        Spacer(modifier = Modifier.height(6.dp))
        if (tasks.isEmpty()) {
            Text("لا توجد مهام حالية", fontSize = 10.sp, color = BentoSlateText)
        } else {
            tasks.take(2).forEach {
                Text("• ${it.title}", fontSize = 10.sp, color = BentoDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (tasks.size > 2) {
                Text("و ${tasks.size - 2} مهام أخرى", fontSize = 9.sp, color = BentoPurple, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AlertItem(title: String, sub: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ContainerLow, RoundedCornerShape(8.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Info, contentDescription = null, tint = DeepGreen, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text(sub, fontSize = 9.sp, color = TextMuted)
            }
        }
        Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun TimelineRow(item: TimelineActivity) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(if (item.isPrimary) DeepGreen else Gold)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(item.text, fontSize = 12.sp, color = TextDark, lineHeight = 16.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(item.timeLabel, fontSize = 10.sp, color = TextMuted)
        }
    }
}

// ------------------ 4. ORG STRUCTURE SCREEN ------------------
@Composable
fun OrgStructureScreen(viewModel: MainViewModel) {
    val depts by viewModel.departments.collectAsState()
    val officesList by viewModel.offices.collectAsState()
    val employeesList by viewModel.employees.collectAsState()
    val todayAttendance by viewModel.getAttendanceByDate(todayIsoDate()).collectAsState(initial = emptyList())

    var activeTab by remember { mutableStateOf("departments") } // departments vs offices

    var showAddDeptDialog by remember { mutableStateOf(false) }
    var showAddOfficeDialog by remember { mutableStateOf(false) }
    var selectedDeptForMembers by remember { mutableStateOf<Department?>(null) }
    var selectedOfficeForMembers by remember { mutableStateOf<Office?>(null) }
    var showBranchTree by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text("الهيكل التنظيمي للفرع", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
            Text("إدارة وتنسيق الأقسام الإدارية والمكاتب التابعة", fontSize = 12.sp, color = TextMuted)
        }

        // Tabs switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ContainerLow, RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            Button(
                onClick = { activeTab = "departments" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == "departments") DeepGreen else Color.Transparent,
                    contentColor = if (activeTab == "departments") Color.White else TextMuted
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("الأقسام (${depts.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Button(
                onClick = { activeTab = "offices" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == "offices") DeepGreen else Color.Transparent,
                    contentColor = if (activeTab == "offices") Color.White else TextMuted
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("المكاتب (${officesList.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // Add item button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
        ) {
            OutlinedButton(
                onClick = { showBranchTree = !showBranchTree },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, DeepGreen.copy(alpha = 0.35f))
            ) {
                Icon(Icons.Filled.List, contentDescription = null, tint = DeepGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (showBranchTree) "إخفاء الشجرة" else "عرض الشجرة", color = DeepGreen, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = {
                    if (activeTab == "departments") showAddDeptDialog = true else showAddOfficeDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = Gold),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (activeTab == "departments") "قسم جديد" else "مكتب جديد",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (showBranchTree) {
            BranchTreeCard(
                departments = depts,
                offices = officesList,
                employees = employeesList
            )
        }

        // Render sections
        if (activeTab == "departments") {
            depts.forEach { dept ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedDeptForMembers = dept },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Gold.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Home, contentDescription = null, tint = DeepGreen)
                            }
                            Column {
                                Text(dept.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                                Text(dept.location, fontSize = 11.sp, color = TextMuted)
                            }
                        }
                        val officeCount = officesList.count { it.departmentId == dept.id }
                        val employeeCount = employeesList.count { emp -> officesList.any { it.departmentId == dept.id && it.id == emp.officeId } }
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "${dept.teamCount} فريق محدد",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepGreen,
                                modifier = Modifier
                                    .background(Gold.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                            Text("$officeCount مكتب • $employeeCount موظف", fontSize = 10.sp, color = TextMuted)
                        }
                    }
                }
            }
        } else {
            officesList.forEach { office ->
                val dept = depts.find { it.id == office.departmentId }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedOfficeForMembers = office },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(office.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Icon(Icons.Filled.Home, contentDescription = null, tint = Gold, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(dept?.name ?: "قسم فرعي", fontSize = 11.sp, color = TextMuted)
                                }
                            }
                            Text(
                                text = "نشط",
                                fontSize = 10.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(DeepGreen, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = ContainerHigh)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Gold.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Person, contentDescription = null, tint = DeepGreen, modifier = Modifier.size(16.dp))
                            }
                            Column {
                                val officeManager = employeesList.find { it.officeId == office.id }
                                Text(officeManager?.name ?: "لا يوجد مدير", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (officeManager != null) TextDark else CoralRed)
                                Text(officeManager?.role ?: "المكتب بحاجة لتعيين مدير", fontSize = 9.sp, color = if (officeManager != null) TextMuted else CoralRed)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    if (showAddDeptDialog) {
        AddDepartmentDialog(
            onDismiss = { showAddDeptDialog = false },
            onAdd = { name, desc, loc, teamCount ->
                viewModel.addDepartment(name, desc, loc, teamCount)
                showAddDeptDialog = false
            }
        )
    }

    if (showAddOfficeDialog) {
        AddOfficeDialog(
            departments = depts,
            employees = employeesList,
            onDismiss = { showAddOfficeDialog = false },
            onAdd = { name, deptId, mgr, mgrId ->
                viewModel.addOffice(name, deptId, mgr, mgrId)
                showAddOfficeDialog = false
            }
        )
    }

    selectedDeptForMembers?.let { dept ->
        DepartmentOfficesDialog(
            department = dept,
            viewModel = viewModel,
            onDismiss = { selectedDeptForMembers = null }
        )
    }

    selectedOfficeForMembers?.let { office ->
        OfficeMembersDialog(
            office = office,
            viewModel = viewModel,
            onDismiss = { selectedOfficeForMembers = null }
        )
    }
}

// ------------------ 5. STAFF SCREEN ------------------

@Composable
fun DepartmentOfficesDialog(
    department: Department,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val offices by viewModel.offices.collectAsState()
    val employees by viewModel.employees.collectAsState()
    val deptOffices = offices.filter { it.departmentId == department.id }
    var showAddOffice by remember { mutableStateOf(false) }
    var deleteConfirmOffice by remember { mutableStateOf<Office?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WarmBackground),
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(department.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                    IconButton(onClick = { showAddOffice = true }) { Icon(Icons.Filled.Add, contentDescription = "إضافة فريق", tint = DeepGreen) }
                }
                Text("الموقع: ${department.location} • عدد الفرق المحدد: ${department.teamCount}", fontSize = 11.sp, color = TextMuted)

                if (deptOffices.isEmpty()) {
                    Text("لا توجد فرق/مكاتب في هذا القسم بعد", color = TextMuted, fontSize = 13.sp)
                } else {
                    deptOffices.forEach { office ->
                        val empCount = employees.count { it.officeId == office.id && it.status == "Active" }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Gold.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(office.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        val mgr = employees.firstOrNull { it.officeId == office.id }
                                        Text(if (mgr != null) "مدير: ${mgr.name}" else "لا يوجد مدير", fontSize = 10.sp, color = if (mgr != null) TextMuted else CoralRed)
                                        Text("$empCount موظف", fontSize = 10.sp, color = DeepGreen, fontWeight = FontWeight.Bold)
                                    }
                                }
                                IconButton(onClick = { deleteConfirmOffice = office }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = CoralRed, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }

                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = DeepGreen), modifier = Modifier.fillMaxWidth()) {
                    Text("إغلاق", color = Color.White)
                }
            }
        }
    }

    if (showAddOffice) {
        AddOfficeDialog(
            departments = listOf(department),
            employees = employees,
            onDismiss = { showAddOffice = false },
            onAdd = { name, deptId, mgr, mgrId ->
                viewModel.addOffice(name, deptId, mgr, mgrId)
                showAddOffice = false
            }
        )
    }

    deleteConfirmOffice?.let { office ->
        AlertDialog(
            onDismissRequest = { deleteConfirmOffice = null },
            title = { Text("حذف المكتب") },
            text = { Text("هل أنت متأكد من حذف '${office.name}'؟ سيتم حذف جميع الموظفين المرتبطين به.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteOffice(office)
                    deleteConfirmOffice = null
                }) { Text("حذف", color = CoralRed) }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmOffice = null }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
fun OfficeMembersDialog(
    office: Office,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val employees by viewModel.employees.collectAsState()
    val officeEmployees = employees.filter { it.officeId == office.id }
    val activeEmployees = officeEmployees.filter { it.status == "Active" }
    var deleteConfirmEmp by remember { mutableStateOf<Employee?>(null) }
    var editRoleEmp by remember { mutableStateOf<Employee?>(null) }
    var editEmpFull by remember { mutableStateOf<Employee?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WarmBackground),
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(office.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                        val at = activeEmployees.firstOrNull()
                        Text(if (at != null) "مدير: ${at.name} • ${activeEmployees.size} موظف نشط" else "لا يوجد مدير • ${activeEmployees.size} موظف نشط", fontSize = 11.sp, color = if (at != null) TextMuted else CoralRed)
                    }
                }

                if (activeEmployees.isEmpty()) {
                    Text("لا يوجد موظفون في هذا المكتب", color = TextMuted, fontSize = 13.sp)
                } else {
                    activeEmployees.forEach { emp ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, DeepGreen.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(emp.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDark)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(emp.role, fontSize = 11.sp, color = TextMuted)
                                        if (emp.selfId.isNotBlank()) {
                                            Text("| ${emp.selfId}", fontSize = 10.sp, color = DeepGreen)
                                        }
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(onClick = { editRoleEmp = emp }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Filled.Edit, contentDescription = "تعديل المنصب", tint = Gold, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(onClick = { editEmpFull = emp }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Filled.Person, contentDescription = "تعديل البيانات", tint = DeepGreen, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(onClick = { deleteConfirmEmp = emp }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Filled.PersonOff, contentDescription = "حذف", tint = CoralRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = DeepGreen), modifier = Modifier.fillMaxWidth()) {
                    Text("إغلاق", color = Color.White)
                }
            }
        }
    }

    editRoleEmp?.let { emp ->
        EditEmployeeRoleDialog(
            employee = emp,
            onDismiss = { editRoleEmp = null },
            onSave = { newRole ->
                viewModel.updateEmployeeRole(emp, newRole)
                editRoleEmp = null
            }
        )
    }

    editEmpFull?.let { emp ->
        EditEmployeeDialog(
            employee = emp,
            onDismiss = { editEmpFull = null },
            onSave = { updated ->
                viewModel.updateEmployeeFull(updated)
                editEmpFull = null
            }
        )
    }

    deleteConfirmEmp?.let { emp ->
        AlertDialog(
            onDismissRequest = { deleteConfirmEmp = null },
            title = { Text("حذف الموظف") },
            text = { Text("هل أنت متأكد من حذف ${emp.name}؟ سيتم حذف جميع بياناته.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteEmployee(emp)
                    deleteConfirmEmp = null
                }) { Text("حذف", color = CoralRed) }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmEmp = null }) { Text("إلغاء") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffManagementScreen(viewModel: MainViewModel) {
    val searchVal by viewModel.employeeSearchQuery.collectAsState()
    val filteredEmployeesList by viewModel.filteredEmployees.collectAsState()
    val tasksList by viewModel.tasks.collectAsState()
    val projectsList by viewModel.projects.collectAsState()
    val depts by viewModel.departments.collectAsState()
    val selectedDeptFilterId by viewModel.activeDepartmentFilterId.collectAsState()
    val officesList by viewModel.offices.collectAsState()

    var showAddEmployeeDialog by remember { mutableStateOf(false) }

    // Clicked employee action sheets
    var clickedEmployeeForActions by remember { mutableStateOf<Employee?>(null) }
    var actionType by remember { mutableStateOf("") } // tasks, penalties, attendance
    var employeeToEdit by remember { mutableStateOf<Employee?>(null) }
    var employeeToDismiss by remember { mutableStateOf<Employee?>(null) }
    var showEmployeeMenuFor by remember { mutableStateOf<Employee?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("إدارة الكوادر والموظفين", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                Text("إدارة شؤون الطواقم وتفويض المهام والخصومات للفرع", fontSize = 11.sp, color = TextMuted)
            }
        }

        // Search bar
        ShaheenOutlinedTextField(
            value = searchVal,
            onValueChange = { viewModel.employeeSearchQuery.value = it },
            placeholder = { Text("البحث عن موظف بالاسم أو المنصب...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = DeepGreen) }
        )

        // Filters horizontal strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // All pill
            FilterPill(
                label = "الكل",
                isSelected = selectedDeptFilterId == null,
                onClick = { viewModel.activeDepartmentFilterId.value = null }
            )

            depts.forEach { dept ->
                FilterPill(
                    label = dept.name,
                    isSelected = selectedDeptFilterId == dept.id,
                    onClick = { viewModel.activeDepartmentFilterId.value = dept.id }
                )
            }
        }

        // Employee Cards List
        if (filteredEmployeesList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("لا يوجد موظفين يطابقون شروط البحث", color = TextMuted, fontSize = 13.sp)
            }
        } else {
            filteredEmployeesList.forEach { emp ->
                val office = officesList.find { it.id == emp.officeId }
                val empTasks = tasksList.filter { it.employeeId == emp.id }
                val projectTasks = empTasks.count { it.projectId != null }
                val standaloneTasks = empTasks.count { it.projectId == null }
                val avgProgress = if (empTasks.isNotEmpty()) empTasks.map { it.progress }.average().toInt() else 0
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.navigateTo(AppScreen.EmployeeDetail(emp.id)) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Gold.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Person, contentDescription = null, tint = DeepGreen, modifier = Modifier.size(24.dp))
                                }
                                Column {
                                    Text(emp.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                    Text(emp.role, fontSize = 12.sp, color = TextMuted)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Gold, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(office?.name ?: emp.branchLocation, fontSize = 10.sp, color = DeepGreen, fontWeight = FontWeight.SemiBold)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "${empTasks.size} مهمة • $projectTasks مشروع • $standaloneTasks مستقلة • اضغط لفتح صفحة المعلومات",
                                        fontSize = 10.sp,
                                        color = DeepGreen,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Box {
                                IconButton(onClick = { showEmployeeMenuFor = emp }) {
                                    Icon(Icons.Filled.MoreVert, contentDescription = "خيارات إضافية")
                                }
                                DropdownMenu(
                                    expanded = showEmployeeMenuFor?.id == emp.id,
                                    onDismissRequest = { showEmployeeMenuFor = null }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("تعديل المنصب") },
                                        onClick = {
                                            employeeToEdit = emp
                                            showEmployeeMenuFor = null
                                        },
                                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("تسريح الموظف", color = CoralRed) },
                                        onClick = {
                                            employeeToDismiss = emp
                                            showEmployeeMenuFor = null
                                        },
                                        leadingIcon = { Icon(Icons.Filled.PersonOff, contentDescription = null, tint = CoralRed) }
                                    )
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = ContainerHigh)

                        // Action Buttons strip under card
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            IconButtonWithLabel(
                                imageVector = Icons.Filled.List,
                                label = "المهام",
                                onClick = {
                                    clickedEmployeeForActions = emp
                                    actionType = "tasks"
                                }
                            )
                            IconButtonWithLabel(
                                imageVector = Icons.Filled.Warning,
                                label = "جزاءات",
                                onClick = {
                                    clickedEmployeeForActions = emp
                                    actionType = "penalties"
                                }
                            )
                            IconButtonWithLabel(
                                imageVector = Icons.Filled.DateRange,
                                label = "الحضور",
                                onClick = {
                                    clickedEmployeeForActions = emp
                                    actionType = "attendance"
                                }
                            )
                            IconButtonWithLabel(
                                imageVector = Icons.Filled.Star,
                                label = "التقييم",
                                onClick = {
                                    viewModel.selectEmployeeForEvaluation(emp.id, emp.name)
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // Contextual floating trigger for adding employee
    Box(modifier = Modifier.fillMaxSize()) {
        FloatingActionButton(
            onClick = { showAddEmployeeDialog = true },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 20.dp, start = 20.dp)
                .size(56.dp)
                .testTag("add_employee_fab"),
            containerColor = DeepGreen,
            contentColor = Gold,
            shape = CircleShape
        ) {
            Icon(Icons.Filled.Add, contentDescription = "تسجيل موظف جديد", modifier = Modifier.size(28.dp))
        }
    }

    if (showAddEmployeeDialog) {
        AddEmployeeDialog(
            viewModel = viewModel,
            offices = officesList,
            onDismiss = { showAddEmployeeDialog = false }
        )
    }

    // Actions popup modals
    clickedEmployeeForActions?.let { emp ->
        EmployeeActionsDialog(
            employee = emp,
            actionType = actionType,
            viewModel = viewModel,
            onDismiss = { clickedEmployeeForActions = null }
        )
    }

    // Evaluation dialog
    val evalEmployee by viewModel.selectedEvaluationEmployee.collectAsState()
    evalEmployee?.let { args ->
        EmployeeEvaluationDialog(
            employeeId = args.employeeId,
            employeeName = args.employeeName,
            adminId = viewModel.loggedInAdmin.value?.id ?: 1,
            viewModel = viewModel,
            onDismiss = { viewModel.clearEvaluationSelection() }
        )
    }

    employeeToEdit?.let { emp ->
        EditEmployeeRoleDialog(
            employee = emp,
            onDismiss = { employeeToEdit = null },
            onSave = { newRole ->
                viewModel.updateEmployeeRole(emp, newRole)
                employeeToEdit = null
            }
        )
    }

    employeeToDismiss?.let { emp ->
        AlertDialog(
            onDismissRequest = { employeeToDismiss = null },
            title = { Text("تسريح الموظف") },
            text = { Text("هل أنت متأكد من تسريح ${emp.name}؟ لن يظهر في القوائم النشطة.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissEmployee(emp)
                    employeeToDismiss = null
                }) { Text("تسريح", color = CoralRed) }
            },
            dismissButton = {
                TextButton(onClick = { employeeToDismiss = null }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
fun EmployeeDetailScreen(viewModel: MainViewModel, employeeId: Int) {
    val employees by viewModel.employees.collectAsState()
    val offices by viewModel.offices.collectAsState()
    val departments by viewModel.departments.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val employee = employees.find { it.id == employeeId }

    if (employee == null) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { viewModel.navigateTo(AppScreen.StaffManagement) }, colors = ButtonDefaults.buttonColors(containerColor = DeepGreen)) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("رجوع", color = Color.White)
            }
            Text("لم يتم العثور على الموظف", color = TextMuted)
        }
        return
    }

    val office = offices.find { it.id == employee.officeId }
    val department = departments.find { it.id == office?.departmentId }
    val employeeTasks = tasks.filter { it.employeeId == employee.id }
    val completedTasks = employeeTasks.count { it.status == "Completed" }
    val avgProgress = if (employeeTasks.isNotEmpty()) employeeTasks.map { it.progress }.average().toInt() else 0

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Button(onClick = { viewModel.navigateTo(AppScreen.StaffManagement) }, colors = ButtonDefaults.buttonColors(containerColor = DeepGreen), shape = RoundedCornerShape(10.dp)) {
            Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text("رجوع للموظفين", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Gold.copy(alpha = 0.2f))) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.size(58.dp).clip(RoundedCornerShape(12.dp)).background(Gold.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = DeepGreen, modifier = Modifier.size(30.dp))
                    }
                    Column {
                        Text(employee.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                        Text(employee.role, fontSize = 13.sp, color = TextMuted)
                    }
                }
                Divider(color = ContainerHigh)
                DetailLine("الرقم الذاتي", employee.selfId.ifBlank { "—" })
                DetailLine("القسم", department?.name ?: "غير محدد")
                DetailLine("الفريق / المكتب", office?.name ?: employee.branchLocation)
                DetailLine("الموقع", employee.branchLocation)
                DetailLine("الحالة", if (employee.status == "Active") "نشط" else "مسرح")
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Gold.copy(alpha = 0.2f))) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("الإنجازات", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                if (employee.achievements.isNotBlank()) {
                    employee.achievements.split(",").forEach { ach ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = Gold, modifier = Modifier.size(14.dp))
                            Text(ach.trim(), fontSize = 12.sp, color = TextDark)
                        }
                    }
                } else {
                    Text("لا توجد إنجازات مسجلة", color = TextMuted, fontSize = 13.sp)
                }
                Divider(color = ContainerHigh, modifier = Modifier.padding(vertical = 4.dp))
                Text("المهام", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                DetailLine("إجمالي المهام", employeeTasks.size.toString())
                DetailLine("المكتملة", completedTasks.toString())
                DetailLine("نسبة الإنجاز", "$avgProgress%")
                if (employeeTasks.isEmpty()) {
                    Text("لا توجد مهام مسندة لهذا الموظف", color = TextMuted, fontSize = 13.sp)
                } else {
                    employeeTasks.forEach { task ->
                        val projectName = projects.find { it.id == task.projectId }?.name ?: "مستقلة"
                        Column(modifier = Modifier.fillMaxWidth().background(ContainerLow, RoundedCornerShape(10.dp)).padding(10.dp)) {
                            Text(task.title, color = TextDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("$projectName • ${task.progress}% • ${task.status}", color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = TextMuted, fontSize = 12.sp)
        Text(value, color = TextDark, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
    }
}

@Composable
fun BranchTreeCard(departments: List<Department>, offices: List<Office>, employees: List<Employee>) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Gold.copy(alpha = 0.2f))) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("التقسيم الشجري للفرع", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
            Text("الفرع الرئيسي", color = DeepGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            departments.forEach { dept ->
                val deptOffices = offices.filter { it.departmentId == dept.id }
                Column(modifier = Modifier.padding(start = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("├─ ${dept.name} (${dept.teamCount} فرق محددة)", color = TextDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    deptOffices.forEach { office ->
                        val count = employees.count { it.officeId == office.id && it.status == "Active" }
                        Text("│  └─ ${office.name} • $count موظف", color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(start = 14.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun FilterPill(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) DeepGreen else Color.White)
            .border(1.dp, if (isSelected) DeepGreen else Gold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun IconButtonWithLabel(imageVector: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector, contentDescription = label, tint = DeepGreen, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, fontSize = 10.sp, color = TextMuted)
    }
}

// ------------------ 6. REPORTS SCREEN ------------------
@Composable
fun ReportsAnalyticsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val totalSum by viewModel.totalInvoiceSum.collectAsState()
    val averageAttendance by viewModel.averageAttendanceRate.collectAsState()
    val overallPerformance by viewModel.overallBranchPerformance.collectAsState()
    val feed by viewModel.timelineFeed.collectAsState()
    val invoicesList by viewModel.invoices.collectAsState()
    val officesList by viewModel.offices.collectAsState()
    val depts by viewModel.departments.collectAsState()
    val employeesList by viewModel.employees.collectAsState()

    var showShareSheet by remember { mutableStateOf(false) }
    var shareFilePath by remember { mutableStateOf<File?>(null) }
    var shareMsg by remember { mutableStateOf("") }
    var showInvoiceDialog by remember { mutableStateOf(false) }
    var invoiceToEdit by remember { mutableStateOf<Invoice?>(null) }
    var showPrintTargetDialog by remember { mutableStateOf(false) }
    var selectedPeriod by remember { mutableStateOf("اليوم") }
    val (periodStart, _) = remember(selectedPeriod) { periodDateRange(selectedPeriod) }
    val filteredInvoices = remember(selectedPeriod, invoicesList) {
        val (start, _) = periodDateRange(selectedPeriod)
        invoicesList.filter { it.date >= start }
    }
    val filteredTotal = remember(filteredInvoices) { filteredInvoices.sumOf { it.amount } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text("التقارير والتحليلات الشاملة", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
            Text("نظرة شاملة على أداء الفرع، الكوادر والمصروفات المالية", fontSize = 11.sp, color = TextMuted)
        }

        // Export Actions Section (PDF / Excel)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    viewModel.exportPdf(context) { file ->
                        shareFilePath = file
                        shareMsg = "تصدير تقرير الأداء PDF بنجاح"
                        showShareSheet = true
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_export_pdf"),
                colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Filled.Share, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("تصدير PDF", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    viewModel.exportExcel(context) { file ->
                        shareFilePath = file
                        shareMsg = "تصدير ورقة بيانات Excel CSV بنجاح"
                        showShareSheet = true
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_export_excel"),
                colors = ButtonDefaults.buttonColors(containerColor = Gold),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Filled.Share, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("تصدير Excel", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // Print / targeted reports
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { showPrintTargetDialog = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = DeepGreen.copy(alpha = 0.85f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Filled.Print, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("طباعة تقرير", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = {
                    viewModel.exportInvoicesPdf(context) { file ->
                        shareFilePath = file
                        shareMsg = "تقرير الفواتير"
                        showShareSheet = true
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Gold.copy(alpha = 0.9f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("تقرير الفواتير", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = {
                    viewModel.exportFinancialPdf(context) { file ->
                        shareFilePath = file
                        shareMsg = "التقرير المالي"
                        showShareSheet = true
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = CoralRed.copy(alpha = 0.85f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("المالي", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Invoices section (filtered by period)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Gold.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("الفواتير (${filteredInvoices.size})", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                    IconButton(onClick = { invoiceToEdit = null; showInvoiceDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "إضافة فاتورة", tint = DeepGreen)
                    }
                }
                Text("عرض آخر ${selectedPeriod} • ${filteredInvoices.size} فاتورة", fontSize = 10.sp, color = TextMuted)
                if (filteredInvoices.isEmpty()) {
                    Text("لا توجد فواتير في هذه الفترة", fontSize = 12.sp, color = TextMuted)
                } else {
                    filteredInvoices.forEach { inv ->
                        val officeName = officesList.find { it.id == inv.officeId }?.name ?: "—"
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { invoiceToEdit = inv; showInvoiceDialog = true },
                            colors = CardDefaults.cardColors(containerColor = WarmBackground),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (inv.imageUrl.isNotBlank()) {
                                    AsyncImage(model = inv.imageUrl, contentDescription = null, modifier = Modifier.size(40.dp).clip(RoundedCornerShape(6.dp)))
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(inv.trackingNumber, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DeepGreen)
                                    Text("$officeName • ${inv.amount} ر.س", fontSize = 10.sp, color = TextMuted)
                                    Text(inv.description, fontSize = 10.sp, color = TextDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                Text(inv.date, fontSize = 9.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }
        }

        // Period switcher visual bar (Days, Weeks, Months)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(1.dp, Gold.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.DateRange, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("الفترة الزمنية:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("اليوم", "الأسبوع", "الشهر").forEach { t ->
                    Text(
                        text = t,
                        fontSize = 10.sp,
                        color = if (selectedPeriod == t) Color.White else TextMuted,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(if (selectedPeriod == t) DeepGreen else Color.Transparent, RoundedCornerShape(20.dp))
                            .clickable { selectedPeriod = t }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Summary Bento statistics Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryBentoBox(
                title = "الفواتير ($selectedPeriod)",
                value = "${filteredTotal} ر.س",
                badge = "${filteredInvoices.size} فاتورة",
                modifier = Modifier.weight(1f),
                bg = DeepGreen,
                textColor = Color.White
            )

            SummaryBentoBox(
                title = "متوسط الحضور",
                value = averageAttendance,
                badge = "ثابت",
                modifier = Modifier.weight(1.5f),
                bg = Gold,
                textColor = DeepGreen
            )
        }
        // Top-rated Employees (filtered by period)
        var topEvals by remember { mutableStateOf<List<EmployeeEvaluation>>(emptyList()) }
        LaunchedEffect(selectedPeriod) {
            val all = viewModel.getAllEvaluations()
            val (start, _) = periodDateRange(selectedPeriod)
            topEvals = all.filter { it.createdAt >= start }.sortedByDescending { it.totalScore }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Gold.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("أعلى الموظفين تقييماً", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                Text("بناءً على نتائج التقييم الأخير", fontSize = 11.sp, color = TextMuted)
                Spacer(modifier = Modifier.height(12.dp))

                if (topEvals.isEmpty()) {
                    Text("لا توجد تقييمات بعد", color = TextMuted, fontSize = 12.sp)
                } else {
                    topEvals.take(5).forEach { ev ->
                        val empName = employeesList.find { it.id == ev.employeeId }?.name ?: "موظف"
                        EliteRow(
                            name = empName,
                            percentage = "${ev.totalScore}/100",
                            rating = ev.rating
                        )
                        Divider(color = ContainerHigh, modifier = Modifier.padding(vertical = 6.dp))
                    }
                }
            }
        }

        // Employee Evaluation Reports Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, DeepGreen.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تقييم الأداء الوظيفي", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("نظام تقييم إلكتروني بنظام النقاط (100 نقطة)", fontSize = 11.sp, color = TextMuted)
                Spacer(modifier = Modifier.height(12.dp))

                // Evaluation criteria summary
                listOf(
                    "إنجاز المهام في الوقت المحدد" to "30 نقطة",
                    "جودة العمل" to "25 نقطة",
                    "الالتزام بالدوام" to "20 نقطة",
                    "العمل الجماعي" to "10 نقاط",
                    "الإبداع والمبادرات" to "10 نقاط",
                    "خصم الجزاءات" to "-15 نقطة"
                ).forEach { (criteria, points) ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(criteria, fontSize = 11.sp, color = TextDark)
                        Text(points, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            color = if (points.startsWith("-")) CoralRed else DeepGreen)
                    }
                    Divider(color = ContainerHigh, modifier = Modifier.height(1.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("التصنيف: 90+ ممتاز • 75-89 جيد جداً • 60-74 جيد • 45-59 مقبول • <45 ضعيف",
                    fontSize = 9.sp, color = TextMuted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // Trigger share dialog if file is exported
    if (showShareSheet && shareFilePath != null) {
        ShareFileDialog(
            file = shareFilePath!!,
            message = shareMsg,
            onDismiss = { showShareSheet = false }
        )
    }

    if (showInvoiceDialog) {
        InvoiceDialog(
            offices = officesList,
            existing = invoiceToEdit,
            onDismiss = { showInvoiceDialog = false; invoiceToEdit = null },
            onSave = { invoice ->
                if (invoiceToEdit != null) viewModel.updateInvoice(invoice) else viewModel.createInvoice(
                    invoice.officeId, invoice.amount, invoice.trackingNumber, invoice.description, invoice.date, invoice.type, invoice.imageUrl
                )
                showInvoiceDialog = false
                invoiceToEdit = null
            },
            onDelete = invoiceToEdit?.let { inv ->
                { viewModel.deleteInvoice(inv); showInvoiceDialog = false; invoiceToEdit = null }
            }
        )
    }

    if (showPrintTargetDialog) {
        PrintTargetDialog(
            employees = employeesList,
            departments = depts,
            offices = officesList,
            onDismiss = { showPrintTargetDialog = false },
            onPrintEmployee = { id ->
                viewModel.exportEmployeePdf(context, id) { file -> viewModel.printFile(context, file) }
                showPrintTargetDialog = false
            },
            onPrintDepartment = { id ->
                viewModel.exportDepartmentPdf(context, id) { file -> viewModel.printFile(context, file) }
                showPrintTargetDialog = false
            },
            onPrintOffice = { id ->
                viewModel.exportOfficePdf(context, id) { file -> viewModel.printFile(context, file) }
                showPrintTargetDialog = false
            }
        )
    }
}

@Composable
fun SummaryBentoBox(
    title: String,
    value: String,
    badge: String,
    modifier: Modifier = Modifier,
    bg: Color,
    textColor: Color
) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor.copy(alpha = 0.8f))
                Text(
                    text = badge,
                    fontSize = 8.sp,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(textColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}

@Composable
fun EliteRow(name: String, percentage: String, rating: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(DeepGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = DeepGreen, modifier = Modifier.size(14.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(percentage, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
            Text(
                rating,
                fontSize = 9.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(DeepGreen, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

// ------------------ DIALOG FORM COMPONENTS ------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    employees: List<Employee>,
    onDismiss: () -> Unit,
    onAdd: (String, String, Int, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var empId by remember { mutableStateOf(employees.firstOrNull()?.id ?: 0) }
    var urgency by remember { mutableStateOf("Urgent") }
    var importance by remember { mutableStateOf("Important") }
    var date by remember { mutableStateOf(todayIsoDate()) }
    var isSelfTask by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WarmBackground),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("تعيين مهمة جديدة", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepGreen)

                ShaheenOutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان المهمة") },
                    modifier = Modifier.fillMaxWidth().testTag("add_task_title_input")
                )

                ShaheenOutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("الوصف التفصيلي") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Checkbox(checked = isSelfTask, onCheckedChange = { isSelfTask = it })
                    Text("مهمة ذاتية للمدير", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                }

                if (!isSelfTask) {
                    Text("اختر الموظف المكلف:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    employees.forEach { emp ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { empId = emp.id }.padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = empId == emp.id, onClick = { empId = emp.id })
                            Text("${emp.name} (${emp.role})", fontSize = 12.sp)
                        }
                    }
                }

                Text("تحديد مصفوفة إيزنهاور:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { urgency = "Urgent" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (urgency == "Urgent") DeepGreen else ContainerNormal),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("عاجل", color = if (urgency == "Urgent") Color.White else TextMuted, fontSize = 11.sp)
                    }
                    Button(
                        onClick = { urgency = "Not Urgent" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (urgency == "Not Urgent") DeepGreen else ContainerNormal),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("غير عاجل", color = if (urgency == "Not Urgent") Color.White else TextMuted, fontSize = 11.sp)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { importance = "Important" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (importance == "Important") DeepGreen else ContainerNormal),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("هام", color = if (importance == "Important") Color.White else TextMuted, fontSize = 11.sp)
                    }
                    Button(
                        onClick = { importance = "Not Important" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (importance == "Not Important") DeepGreen else ContainerNormal),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("غير هام", color = if (importance == "Not Important") Color.White else TextMuted, fontSize = 11.sp)
                    }
                }

                DatePickerField(
                    label = "تاريخ / موعد الاستحقاق",
                    selectedDate = date,
                    onDateSelected = { date = it }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("إلغاء", color = TextMuted)
                    }
                    Button(
                        onClick = {
                            if (title.isNotBlank()) onAdd(title, desc, empId, urgency, importance, date)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("تعيين", color = Color.White)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDepartmentDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var loc by remember { mutableStateOf("") }
    var teamCount by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WarmBackground),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("إضافة قسم إداري جديد", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepGreen)

                ShaheenOutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم القسم") },
                    modifier = Modifier.fillMaxWidth().testTag("add_dept_name_input")
                )

                ShaheenOutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("الوصف") },
                    modifier = Modifier.fillMaxWidth()
                )

                ShaheenOutlinedTextField(
                    value = loc,
                    onValueChange = { loc = it },
                    label = { Text("Ø§Ù„Ù…ÙˆÙ‚Ø¹ / Ø§Ù„Ø¬Ù†Ø§Ø­") },
                    modifier = Modifier.fillMaxWidth()
                )

                ShaheenOutlinedTextField(
                    value = teamCount,
                    onValueChange = { teamCount = it.filter { ch -> ch.isDigit() } },
                    label = { Text("عدد الفرق داخل القسم") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("إلغاء", color = TextMuted)
                    }
                    Button(
                        onClick = {
                            if (name.isNotBlank()) onAdd(name, desc, loc, teamCount.toIntOrNull() ?: 0)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("حفظ", color = Color.White)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOfficeDialog(
    departments: List<Department>,
    employees: List<Employee>,
    onDismiss: () -> Unit,
    onAdd: (String, Int, String, Int?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var deptId by remember { mutableStateOf(departments.firstOrNull()?.id ?: 0) }
    var selectedMgrId by remember { mutableStateOf<Int?>(null) }
    var mgrDropdownExpanded by remember { mutableStateOf(false) }
    val eligibleEmployees = employees.filter { !it.isOfficeManager && !it.isDepartmentManager && it.status == "Active" }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WarmBackground),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("إضافة مكتب تنفيذي جديد", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepGreen)

                ShaheenOutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المكتب") },
                    modifier = Modifier.fillMaxWidth().testTag("add_office_name_input")
                )

                Text("اختر القسم التابع له:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                departments.forEach { d ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { deptId = d.id }
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = deptId == d.id, onClick = { deptId = d.id })
                        Text(d.name, fontSize = 12.sp)
                    }
                }

                Text("رئيس المكتب التنفيذي:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Box {
                    ShaheenOutlinedTextField(
                        value = eligibleEmployees.find { it.id == selectedMgrId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("اختر مديراً") },
                        modifier = Modifier.fillMaxWidth().clickable { mgrDropdownExpanded = true },
                        placeholder = { Text(if (eligibleEmployees.isEmpty()) "لا يوجد موظفون متاحون" else "اضغط للاختيار") }
                    )
                    DropdownMenu(expanded = mgrDropdownExpanded, onDismissRequest = { mgrDropdownExpanded = false }) {
                        if (eligibleEmployees.isEmpty()) {
                            DropdownMenuItem(text = { Text("لا يوجد موظفون متاحون", color = TextMuted) }, onClick = { mgrDropdownExpanded = false })
                        } else {
                            eligibleEmployees.forEach { emp ->
                                DropdownMenuItem(
                                    text = { Text("${emp.name} (${emp.role})", fontSize = 12.sp) },
                                    onClick = { selectedMgrId = emp.id; mgrDropdownExpanded = false }
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("إلغاء", color = TextMuted)
                    }
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val mgrName = eligibleEmployees.find { it.id == selectedMgrId }?.name ?: ""
                                onAdd(name, deptId, mgrName, selectedMgrId)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("حفظ", color = Color.White)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEmployeeRoleDialog(
    employee: Employee,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var role by remember { mutableStateOf(employee.role) }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = WarmBackground)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("تعديل منصب: ${employee.name}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                ShaheenOutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("المنصب الوظيفي") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("إلغاء", color = TextMuted) }
                    Button(
                        onClick = { if (role.isNotBlank()) onSave(role) },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
                        modifier = Modifier.weight(1f)
                    ) { Text("حفظ", color = Color.White) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEmployeeDialog(
    employee: Employee,
    onDismiss: () -> Unit,
    onSave: (Employee) -> Unit
) {
    var name by remember { mutableStateOf(employee.name) }
    var role by remember { mutableStateOf(employee.role) }
    var selfId by remember { mutableStateOf(employee.selfId) }
    var branch by remember { mutableStateOf(employee.branchLocation) }
    var achievements by remember { mutableStateOf(employee.achievements) }
    var tags by remember { mutableStateOf(employee.tags) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = WarmBackground), modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("تعديل بيانات: ${employee.name}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                ShaheenOutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("الاسم") }, modifier = Modifier.fillMaxWidth())
                ShaheenOutlinedTextField(value = selfId, onValueChange = { selfId = it }, label = { Text("الرقم الذاتي") }, modifier = Modifier.fillMaxWidth())
                ShaheenOutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("المنصب") }, modifier = Modifier.fillMaxWidth())
                ShaheenOutlinedTextField(value = branch, onValueChange = { branch = it }, label = { Text("الفرع") }, modifier = Modifier.fillMaxWidth())
                ShaheenOutlinedTextField(value = achievements, onValueChange = { achievements = it }, label = { Text("الإنجازات") }, modifier = Modifier.fillMaxWidth())
                ShaheenOutlinedTextField(value = tags, onValueChange = { tags = it }, label = { Text("المهارات") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("إلغاء", color = TextMuted) }
                    Button(onClick = { onSave(employee.copy(name = name, selfId = selfId, role = role, branchLocation = branch, achievements = achievements, tags = tags)) }, colors = ButtonDefaults.buttonColors(containerColor = DeepGreen), modifier = Modifier.weight(1f)) { Text("حفظ", color = Color.White) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEmployeeDialog(
    viewModel: MainViewModel,
    offices: List<Office>,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var officeId by remember { mutableStateOf(offices.firstOrNull()?.id ?: 0) }
    var branch by remember { mutableStateOf("المقر الرئيسي - الرياض") }
    var selfId by remember { mutableStateOf("") }
    var achievements by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WarmBackground),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("تسجيل موظف جديد", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepGreen)

                ShaheenOutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("الاسم الكامل للموظف") },
                    modifier = Modifier.fillMaxWidth().testTag("add_employee_name_input")
                )

                ShaheenOutlinedTextField(
                    value = selfId,
                    onValueChange = { selfId = it },
                    label = { Text("الرقم الذاتي (فريد)") },
                    modifier = Modifier.fillMaxWidth()
                )

                ShaheenOutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("المنصب الوظيفي") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("المكتب التنفيذي المكلف به:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                offices.forEach { o ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { officeId = o.id }
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = officeId == o.id, onClick = { officeId = o.id })
                        Text(o.name, fontSize = 12.sp)
                    }
                }

                ShaheenOutlinedTextField(
                    value = branch,
                    onValueChange = { branch = it },
                    label = { Text("الفرع والمنطقة الميدانية") },
                    modifier = Modifier.fillMaxWidth()
                )

                ShaheenOutlinedTextField(
                    value = achievements,
                    onValueChange = { achievements = it },
                    label = { Text("الإنجازات (مفصولة بفواصل)") },
                    modifier = Modifier.fillMaxWidth()
                )

                ShaheenOutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("المهارات / الخبرات (مفصولة بفواصل)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("إلغاء", color = TextMuted)
                    }
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                viewModel.addEmployee(name, role, officeId, branch, selfId, achievements, tags)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("تسجيل", color = Color.White)
                    }
                }
            }
        }
    }
}

// Dialog to let Manager log Tasks, Penalties and Attendance for an Employee
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeActionsDialog(
    employee: Employee,
    actionType: String,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Modal forms states
    var taskTitle by remember { mutableStateOf("") }
    var taskDesc by remember { mutableStateOf("") }
    var taskUrgency by remember { mutableStateOf("Urgent") }
    var taskImportance by remember { mutableStateOf("Important") }
    var taskDate by remember { mutableStateOf(todayIsoDate()) }

    var penaltyAmount by remember { mutableStateOf("") }
    var penaltyReason by remember { mutableStateOf("") }

    var selectedAttendanceStatus by remember { mutableStateOf("Present") } // Present, Absent, Late

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WarmBackground),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "${employee.name}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepGreen
                )
                Text(
                    text = "منصب: ${employee.role}",
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Divider(color = ContainerHigh)

                when (actionType) {
                    "tasks" -> {
                        Text("تعيين مهمة مخصصة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                        
                        ShaheenOutlinedTextField(
                            value = taskTitle,
                            onValueChange = { taskTitle = it },
                            label = { Text("عنوان المهمة") },
                            modifier = Modifier.fillMaxWidth().testTag("emp_action_task_title")
                        )

                        ShaheenOutlinedTextField(
                            value = taskDesc,
                            onValueChange = { taskDesc = it },
                            label = { Text("الوصف") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { taskUrgency = "Urgent" },
                                colors = ButtonDefaults.buttonColors(containerColor = if (taskUrgency == "Urgent") DeepGreen else ContainerNormal),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("عاجل", color = if (taskUrgency == "Urgent") Color.White else TextMuted, fontSize = 11.sp)
                            }
                            Button(
                                onClick = { taskUrgency = "Not Urgent" },
                                colors = ButtonDefaults.buttonColors(containerColor = if (taskUrgency == "Not Urgent") DeepGreen else ContainerNormal),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("غير عاجل", color = if (taskUrgency == "Not Urgent") Color.White else TextMuted, fontSize = 11.sp)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { taskImportance = "Important" },
                                colors = ButtonDefaults.buttonColors(containerColor = if (taskImportance == "Important") DeepGreen else ContainerNormal),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("هام", color = if (taskImportance == "Important") Color.White else TextMuted, fontSize = 11.sp)
                            }
                            Button(
                                onClick = { taskImportance = "Not Important" },
                                colors = ButtonDefaults.buttonColors(containerColor = if (taskImportance == "Not Important") DeepGreen else ContainerNormal),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("غير هام", color = if (taskImportance == "Not Important") Color.White else TextMuted, fontSize = 11.sp)
                            }
                        }

                        DatePickerField(
                            label = "موعد الاستحقاق",
                            selectedDate = taskDate,
                            onDateSelected = { taskDate = it }
                        )

                        Button(
                            onClick = {
                                if (taskTitle.isNotBlank()) {
                                    viewModel.assignTask(taskTitle, taskDesc, employee.id, taskUrgency, taskImportance, taskDate)
                                    Toast.makeText(context, "تم تعيين المهمة بنجاح", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("تعيين وإرسال", color = Color.White)
                        }
                    }

                    "penalties" -> {
                        Text("تسجيل جزاء مالي (العقوبات)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepGreen)

                        ShaheenOutlinedTextField(
                            value = penaltyAmount,
                            onValueChange = { penaltyAmount = it },
                            label = { Text("المبلغ المخصوم (ر.س)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("emp_action_penalty_amount")
                        )

                        ShaheenOutlinedTextField(
                            value = penaltyReason,
                            onValueChange = { penaltyReason = it },
                            label = { Text("سبب الجزاء المالي") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                val amt = penaltyAmount.toDoubleOrNull() ?: 0.0
                                if (amt > 0 && penaltyReason.isNotBlank()) {
                                    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                    viewModel.logPenalty(employee.id, amt, penaltyReason, dateStr)
                                    Toast.makeText(context, "تم تسجيل المخالفة وحفظها", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("تطبيق الخصم المالي", color = Color.White)
                        }
                    }

                    "attendance" -> {
                        Text("تسجيل حالة الدوام لليوم", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepGreen)

                        val states = listOf("Present" to "حاضر (Present)", "Absent" to "غائب (Absent)", "Late" to "متأخر (Late)")
                        states.forEach { (key, arabic) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedAttendanceStatus = key }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = selectedAttendanceStatus == key, onClick = { selectedAttendanceStatus = key })
                                Text(arabic, fontSize = 12.sp)
                            }
                        }

                        Button(
                            onClick = {
                                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                viewModel.logAttendance(employee.id, dateStr, selectedAttendanceStatus)
                                Toast.makeText(context, "تم حفظ حالة الدوام", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("حفظ حالة الدوام", color = Color.White)
                        }
                    }
                }

                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("إلغاء وإغلاق", color = TextMuted)
                }
            }
        }
    }
}

// Dialog that renders file sharing with System Intents to satisfy complete, premium sharing mechanics
@Composable
fun ShareFileDialog(
    file: File,
    message: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(file) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = if (file.name.endsWith(".pdf")) "application/pdf" else "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "تصدير تقرير أداء فرع AdminCenter")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة مستند التقرير عبر:"))
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WarmBackground),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = DeepGreen, modifier = Modifier.size(48.dp))
                Text(message, fontWeight = FontWeight.Bold, color = DeepGreen, textAlign = TextAlign.Center)
                Text("مسار الملف الآمن:\n${file.absolutePath}", fontSize = 9.sp, color = TextMuted, textAlign = TextAlign.Center)

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إغلاق", color = Color.White)
                }
            }
        }
    }
}

// ──────────────────── PROJECTS SCREEN ────────────────────
@Composable
fun ProjectsScreen(viewModel: MainViewModel) {
    val projectsList by viewModel.projects.collectAsState()
    val employeesList by viewModel.employees.collectAsState()
    val depts by viewModel.departments.collectAsState()
    val officesList by viewModel.offices.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text("المشاريع", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
            Text("إدارة المشاريع وتتبع التقدم والإنجازات", fontSize = 12.sp, color = TextMuted)
        }

        if (projectsList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Folder, contentDescription = null, tint = Gold.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("لا توجد مشاريع حالية", color = TextMuted, fontSize = 14.sp)
                    Text("اضغط على + لإضافة مشروع جديد", color = TextMuted, fontSize = 12.sp)
                }
            }
        } else {
            projectsList.forEach { project ->
                val deptName = depts.find { it.id == project.departmentId }?.name
                val officeName = officesList.find { it.id == project.officeId }?.name
                ProjectCard(project = project, deptName = deptName, officeName = officeName, onClick = { viewModel.selectProject(project.id) })
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 20.dp, start = 20.dp)
                .size(56.dp),
            containerColor = DeepGreen,
            contentColor = Gold,
            shape = CircleShape
        ) { Icon(Icons.Filled.Add, contentDescription = "مشروع جديد", modifier = Modifier.size(28.dp)) }
    }

    if (showAddDialog) {
        AddProjectDialog(
            departments = viewModel.departments.collectAsState().value,
            offices = viewModel.offices.collectAsState().value,
            onDismiss = { showAddDialog = false },
            onAdd = { name, desc, start, end, priority, deptId, officeId, scope ->
                viewModel.addProject(name, desc, start, end, priority, deptId, officeId, scope)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ProjectCard(project: Project, onClick: () -> Unit, deptName: String? = null, officeName: String? = null) {
    val priorityColor = when (project.priority) {
        "Critical" -> PriorityCritical
        "High" -> PriorityHigh
        "Medium" -> PriorityMedium
        else -> PriorityLow
    }
    val statusColor = when (project.status) {
        "Completed" -> StatusCompleted
        "InProgress" -> StatusInProgress
        "Delayed" -> StatusDelayed
        "Archived" -> PriorityLow
        else -> StatusNotStarted
    }
    val scopeText = when (project.scopeType) {
        "Shared" -> "مشترك (قسمين)"
        "SingleDepartment" -> "خاص بالقسم"
        else -> "خاص بالمكتب"
    }
    val statusText = when (project.status) {
        "NotStarted" -> "لم يبدأ"
        "InProgress" -> "قيد التنفيذ"
        "Completed" -> "مكتمل"
        "Delayed" -> "متأخر"
        "Archived" -> "مؤرشف"
        else -> project.status
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, priorityColor.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(priorityColor))
                    Column {
                        Text(project.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                        Text(project.description, fontSize = 11.sp, color = TextMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
                Text(statusText, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold,
                    modifier = Modifier.background(statusColor, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 2.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("تقدم المشروع", fontSize = 11.sp, color = TextMuted)
                Text("${project.progress}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = project.progress / 100f,
                color = if (project.status == "Delayed") OverdueRed else DeepGreen,
                trackColor = BentoSlate,
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("تاريخ التسليم: ${project.dueDate}", fontSize = 10.sp, color = TextMuted)
                Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "$scopeText • ${deptName ?: "—"}${if (officeName != null) " / $officeName" else ""}",
                fontSize = 10.sp,
                color = DeepGreen,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ──────────────────── PROJECT DETAIL SCREEN ────────────────────
@Composable
fun ProjectDetailScreen(viewModel: MainViewModel, projectId: Int) {
    val projectsList by viewModel.projects.collectAsState()
    val project = projectsList.find { it.id == projectId }
    val employeesList by viewModel.employees.collectAsState()
    val depts by viewModel.departments.collectAsState()
    val officesList by viewModel.offices.collectAsState()
    val tasksList by viewModel.tasks.collectAsState()
    val projectTasks = tasksList.filter { it.projectId == projectId }
    val projectMembers by viewModel.getEmployeesByProject(projectId).collectAsState(initial = emptyList())
    val context = LocalContext.current

    if (project == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("المشروع غير موجود", color = TextMuted)
        }
        return
    }

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showEditProjectDialog by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }
    var shareFilePath by remember { mutableStateOf<File?>(null) }

    val deptName = depts.find { it.id == project.departmentId }?.name ?: "غير محدد"
    val officeName = officesList.find { it.id == project.officeId }?.name
    val scopeText = when (project.scopeType) {
        "Shared" -> "مشروع مشترك (بين قسمين)"
        "SingleDepartment" -> "خاص بالقسم"
        else -> "خاص بالمكتب"
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(project.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                Text(project.description, fontSize = 12.sp, color = TextMuted)
                Spacer(modifier = Modifier.height(6.dp))
                Text(scopeText, fontSize = 11.sp, color = Gold, fontWeight = FontWeight.Bold)
                Text("القسم: $deptName${if (officeName != null) " • المكتب: $officeName" else ""}", fontSize = 11.sp, color = TextMuted)
            }
            Column(horizontalAlignment = Alignment.End) {
                if (project.status != "Archived") {
                    IconButton(onClick = { viewModel.updateProject(project.copy(status = "Archived")) }) {
                        Icon(Icons.Filled.Archive, contentDescription = "تعطيل", tint = PriorityMedium)
                    }
                } else {
                    IconButton(onClick = { viewModel.updateProject(project.copy(status = "NotStarted")) }) {
                        Icon(Icons.Filled.Unarchive, contentDescription = "إعادة تفعيل", tint = DeepGreen)
                    }
                }
                IconButton(onClick = { showEditProjectDialog = true }) {
                    Icon(Icons.Filled.Edit, contentDescription = "تعديل", tint = DeepGreen)
                }
                IconButton(onClick = { viewModel.navigateTo(AppScreen.Projects) }) {
                    Icon(Icons.Filled.Close, contentDescription = "رجوع")
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    viewModel.exportProjectPdf(context, project) { file ->
                        shareFilePath = file
                        showShareSheet = true
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = DeepGreen)
            ) { Text("تصدير PDF", color = Color.White, fontSize = 11.sp) }
            Button(
                onClick = {
                    viewModel.exportProjectPdf(context, project) { file ->
                        viewModel.printFile(context, file)
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Gold)
            ) { Text("طباعة", color = Color.White, fontSize = 11.sp) }
        }

        // Progress ring
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("تقدم المشروع", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                Spacer(modifier = Modifier.height(8.dp))
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                    CircularProgressIndicator(progress = project.progress / 100f, color = DeepGreen, trackColor = BentoSlate, strokeWidth = 8.dp)
                    Text("${project.progress}%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    DetailChip("البداية", project.startDate)
                    DetailChip("التسليم", project.dueDate)
                    DetailChip("الأولوية", project.priority)
                }
            }
        }

        // Members
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("أعضاء الفريق", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                    IconButton(onClick = { showAddMemberDialog = true }) { Icon(Icons.Filled.PersonAdd, contentDescription = "إضافة عضو", tint = DeepGreen) }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (projectMembers.isEmpty()) {
                    Text("لا يوجد أعضاء", fontSize = 12.sp, color = TextMuted)
                } else {
                    projectMembers.forEach { emp ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = DeepGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(emp.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(emp.role, fontSize = 10.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }
        }

        // Tasks
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("المهام (${projectTasks.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                    Text("${projectTasks.count { it.status == "Completed" }} مكتملة", fontSize = 11.sp, color = StatusCompleted)
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (projectTasks.isEmpty()) {
                    Text("لا توجد مهام بعد", fontSize = 12.sp, color = TextMuted)
                } else {
                    projectTasks.forEach { task ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            val emp = employeesList.find { it.id == task.employeeId }
                            Icon(
                                if (task.status == "Completed") Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (task.status == "Completed") StatusCompleted else PriorityMedium,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(task.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(emp?.name ?: "", fontSize = 10.sp, color = TextMuted)
                            }
                            Text("${task.progress}%", fontSize = 11.sp, color = DeepGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { showAddTaskDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = DeepGreen), shape = RoundedCornerShape(8.dp)) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة مهمة للمشروع", color = Color.White, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    if (showAddTaskDialog) {
        AddProjectTaskDialog(
            projectId = projectId,
            employees = employeesList,
            onDismiss = { showAddTaskDialog = false },
            onAdd = { title, desc, empId, urg, imp, date ->
                viewModel.assignTaskToProject(title, desc, empId, projectId, urg, imp, date)
                showAddTaskDialog = false
            }
        )
    }

    if (showAddMemberDialog) {
        AddProjectMemberDialog(
            projectId = projectId,
            employees = employeesList,
            onDismiss = { showAddMemberDialog = false },
            onAdd = { empId, role ->
                viewModel.addProjectMember(projectId, empId, role)
                showAddMemberDialog = false
            }
        )
    }

    if (showEditProjectDialog) {
        EditProjectDialog(
            project = project,
            departments = depts,
            offices = officesList,
            onDismiss = { showEditProjectDialog = false },
            onSave = { updated ->
                viewModel.updateProject(updated)
                showEditProjectDialog = false
            }
        )
    }

    if (showShareSheet && shareFilePath != null) {
        ShareFileDialog(
            file = shareFilePath!!,
            message = "تم تصدير تقرير المشروع",
            onDismiss = { showShareSheet = false }
        )
    }
}

@Composable
fun DetailChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = TextMuted)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
    }
}

// ──────────────────── SETTINGS SCREEN ────────────────────
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val logoUri by viewModel.logoUri.collectAsState()
    var showLogoPicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.setLogoUri(it.toString()) }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("الإعدادات", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                Text("تخصيص التطبيق", fontSize = 12.sp, color = TextMuted)
            }
            IconButton(onClick = { viewModel.navigateTo(AppScreen.Dashboard) }) {
                Icon(Icons.Filled.Close, contentDescription = "إغلاق")
            }
        }

        // Logo section
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("شعار التطبيق", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier.size(120.dp).clip(CircleShape).background(DeepGreen).border(2.dp, Gold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (logoUri != null) {
                        AsyncImage(
                            model = logoUri,
                            contentDescription = "شعار التطبيق",
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        AppLogo(modifier = Modifier.size(70.dp))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { launcher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("اختيار شعار من المعرض", color = Color.White) }
                if (logoUri != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { viewModel.clearLogo() }) {
                        Text("إعادة الشعار الافتراضي", color = CoralRed)
                    }
                }
            }
        }

        // Organization info
        val orgName by viewModel.orgName.collectAsState()
        val branchName by viewModel.branchName.collectAsState()
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("بيانات المؤسسة", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                Spacer(modifier = Modifier.height(8.dp))
                ShaheenOutlinedTextField(
                    value = orgName, onValueChange = { viewModel.setOrgName(it) },
                    label = { Text("الإدارة") }, modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                ShaheenOutlinedTextField(
                    value = branchName, onValueChange = { viewModel.setBranchName(it) },
                    label = { Text("الفرع") }, modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        // App info
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("معلومات التطبيق", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                Spacer(modifier = Modifier.height(8.dp))
                SettingsInfoRow("الإصدار", "1.0.0")
                SettingsInfoRow("اسم الحزمة", "com.example")
                SettingsInfoRow("قاعدة البيانات", "Room (SQLite)")
                SettingsInfoRow("واجهة المستخدم", "Jetpack Compose + Material3")
            }
        }

        // Export card
        var exporting by remember { mutableStateOf(false) }
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("تصدير التقارير", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                Spacer(modifier = Modifier.height(8.dp))
                Text("تصدير تقارير الأداء والفواتير بصيغة PDF أو Excel", fontSize = 11.sp, color = TextMuted)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            exporting = true
                            viewModel.exportPdf(context) { file ->
                                exporting = false
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, "com.example.fileprovider", file))
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "مشاركة تقرير PDF"))
                            }
                        },
                        enabled = !exporting,
                        colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text(if (exporting) "جار التصدير..." else "تصدير PDF", color = Color.White, fontSize = 12.sp) }
                    Button(
                        onClick = {
                            exporting = true
                            viewModel.exportExcel(context) { file ->
                                exporting = false
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, "com.example.fileprovider", file))
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "مشاركة تقرير Excel"))
                            }
                        },
                        enabled = !exporting,
                        colors = ButtonDefaults.buttonColors(containerColor = Gold),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text(if (exporting) "جار التصدير..." else "تصدير Excel", color = DeepGreen, fontSize = 12.sp) }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun SettingsInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = TextMuted)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
    }
}

// ──────────────────── EVALUATION DIALOG ────────────────────
@Composable
fun EmployeeEvaluationDialog(
    employeeId: Int,
    employeeName: String,
    adminId: Int,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var periodStart by remember { mutableStateOf("2026-07-01") }
    var periodEnd by remember { mutableStateOf("2026-07-31") }
    var taskScore by remember { mutableStateOf("20") }
    var qualityScore by remember { mutableStateOf("18") }
    var attendanceScore by remember { mutableStateOf("15") }
    var teamworkScore by remember { mutableStateOf("7") }
    var innovationScore by remember { mutableStateOf("5") }
    var penaltyDeduct by remember { mutableStateOf("0") }
    var notes by remember { mutableStateOf("") }

    val total = (taskScore.toDoubleOrNull() ?: 0.0) +
            (qualityScore.toDoubleOrNull() ?: 0.0) +
            (attendanceScore.toDoubleOrNull() ?: 0.0) +
            (teamworkScore.toDoubleOrNull() ?: 0.0) +
            (innovationScore.toDoubleOrNull() ?: 0.0) -
            (penaltyDeduct.toDoubleOrNull() ?: 0.0)

    val rating = when {
        total >= 90 -> "ممتاز"
        total >= 75 -> "جيد جداً"
        total >= 60 -> "جيد"
        total >= 45 -> "مقبول"
        else -> "ضعيف"
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = WarmBackground), modifier = Modifier.padding(8.dp)) {
            Column(
                modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("تقييم الأداء: $employeeName", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                Divider(color = Gold.copy(alpha = 0.3f))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DatePickerField(label = "من", selectedDate = periodStart, onDateSelected = { periodStart = it }, modifier = Modifier.weight(1f))
                    DatePickerField(label = "إلى", selectedDate = periodEnd, onDateSelected = { periodEnd = it }, modifier = Modifier.weight(1f))
                }

                EvaluationScoreField("إنجاز المهام في الوقت المحدد (0-30)", taskScore, { taskScore = it }, 30.0)
                EvaluationScoreField("جودة العمل (0-25)", qualityScore, { qualityScore = it }, 25.0)
                EvaluationScoreField("الالتزام بالدوام (0-20)", attendanceScore, { attendanceScore = it }, 20.0)
                EvaluationScoreField("العمل الجماعي (0-10)", teamworkScore, { teamworkScore = it }, 10.0)
                EvaluationScoreField("الإبداع والمبادرات (0-10)", innovationScore, { innovationScore = it }, 10.0)
                EvaluationScoreField("خصم الجزاءات (0-15)", penaltyDeduct, { penaltyDeduct = it }, 15.0, isDeduction = true)

                ShaheenOutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("ملاحظات التقييم") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)

                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = DeepGreen.copy(alpha = 0.1f))) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("المجموع الكلي", fontSize = 12.sp, color = TextMuted)
                        Text(String.format("%.1f", total), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                        Text(rating, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = when {
                            total >= 90 -> ScoreExcellent
                            total >= 75 -> ScoreGood
                            total >= 60 -> ScoreFair
                            else -> ScorePoor
                        })
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("إلغاء", color = TextMuted) }
                    Button(
                        onClick = {
                            viewModel.submitEvaluation(
                                employeeId = employeeId, evaluatorAdminId = adminId,
                                periodStart = periodStart, periodEnd = periodEnd,
                                taskTimelinessScore = taskScore.toDoubleOrNull() ?: 0.0,
                                qualityScore = qualityScore.toDoubleOrNull() ?: 0.0,
                                attendanceScore = attendanceScore.toDoubleOrNull() ?: 0.0,
                                teamworkScore = teamworkScore.toDoubleOrNull() ?: 0.0,
                                innovationScore = innovationScore.toDoubleOrNull() ?: 0.0,
                                penaltyDeduction = -(penaltyDeduct.toDoubleOrNull() ?: 0.0),
                                notes = notes
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
                        modifier = Modifier.weight(1f)
                    ) { Text("حفظ التقييم", color = Color.White) }
                }
            }
        }
    }
}

@Composable
fun EvaluationScoreField(label: String, value: String, onValueChange: (String) -> Unit, max: Double, isDeduction: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, fontSize = 11.sp, color = TextMuted, modifier = Modifier.weight(1f))
        ShaheenOutlinedTextField(
            value = value,
            onValueChange = {
                val num = it.toDoubleOrNull() ?: 0.0
                if (num in 0.0..max) onValueChange(it)
            },
            modifier = Modifier.width(70.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = TextStyle(textAlign = TextAlign.Center, fontSize = 14.sp)
        )
    }
}

// ──────────────────── DIALOGS ────────────────────

// Add Project Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProjectDialog(
    departments: List<Department>,
    offices: List<Office>,
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String, String, Int?, Int?, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(todayIsoDate()) }
    var dueDate by remember { mutableStateOf(todayIsoDate()) }
    var priority by remember { mutableStateOf("Medium") }
    var scopeType by remember { mutableStateOf("SingleOffice") }
    var deptId by remember { mutableStateOf(departments.firstOrNull()?.id) }
    var officeId by remember { mutableStateOf(offices.firstOrNull()?.id) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = WarmBackground)) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("مشروع جديد", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepGreen)

                ShaheenOutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("اسم المشروع") }, modifier = Modifier.fillMaxWidth().testTag("add_project_name"))
                ShaheenOutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("الوصف") }, modifier = Modifier.fillMaxWidth())
                DatePickerField(label = "تاريخ البداية", selectedDate = startDate, onDateSelected = { startDate = it })
                DatePickerField(label = "تاريخ التسليم", selectedDate = dueDate, onDateSelected = { dueDate = it })

                Text("نطاق المشروع:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("SingleOffice" to "مكتب", "SingleDepartment" to "قسم", "Shared" to "مشترك").forEach { (key, label) ->
                        FilterChip(selected = scopeType == key, onClick = { scopeType = key }, label = { Text(label, fontSize = 10.sp) })
                    }
                }

                Text("القسم المسؤول:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                departments.forEach { d ->
                    Row(modifier = Modifier.fillMaxWidth().clickable { deptId = d.id }.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = deptId == d.id, onClick = { deptId = d.id })
                        Text(d.name, fontSize = 11.sp)
                    }
                }

                if (scopeType == "SingleOffice") {
                    Text("المكتب المسؤول:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    offices.filter { it.departmentId == deptId }.forEach { o ->
                        Row(modifier = Modifier.fillMaxWidth().clickable { officeId = o.id }.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = officeId == o.id, onClick = { officeId = o.id })
                            Text(o.name, fontSize = 11.sp)
                        }
                    }
                }

                Text("الأولوية:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Low" to "منخفضة", "Medium" to "متوسطة", "High" to "عالية", "Critical" to "حرجة").forEach { (key, label) ->
                        FilterChip(selected = priority == key, onClick = { priority = key }, label = { Text(label, fontSize = 10.sp) })
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("إلغاء", color = TextMuted) }
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val finalOfficeId = if (scopeType == "SingleOffice") officeId else null
                                onAdd(name, desc, startDate, dueDate, priority, deptId, finalOfficeId, scopeType)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
                        modifier = Modifier.weight(1f)
                    ) { Text("إنشاء المشروع", color = Color.White) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProjectDialog(
    project: Project,
    departments: List<Department>,
    offices: List<Office>,
    onDismiss: () -> Unit,
    onSave: (Project) -> Unit
) {
    var scopeType by remember { mutableStateOf(project.scopeType) }
    var deptId by remember { mutableStateOf(project.departmentId) }
    var officeId by remember { mutableStateOf(project.officeId) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = WarmBackground)) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("تعديل: ${project.name}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                Text("نطاق المشروع:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("SingleOffice" to "مكتب", "SingleDepartment" to "قسم", "Shared" to "مشترك").forEach { (key, label) ->
                        FilterChip(selected = scopeType == key, onClick = { scopeType = key }, label = { Text(label, fontSize = 10.sp) })
                    }
                }
                Text("القسم:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                departments.forEach { d ->
                    Row(modifier = Modifier.fillMaxWidth().clickable { deptId = d.id }.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = deptId == d.id, onClick = { deptId = d.id })
                        Text(d.name, fontSize = 11.sp)
                    }
                }
                if (scopeType == "SingleOffice") {
                    Text("المكتب:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    offices.filter { it.departmentId == deptId }.forEach { o ->
                        Row(modifier = Modifier.fillMaxWidth().clickable { officeId = o.id }.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = officeId == o.id, onClick = { officeId = o.id })
                            Text(o.name, fontSize = 11.sp)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("إلغاء", color = TextMuted) }
                    Button(
                        onClick = {
                            onSave(project.copy(
                                departmentId = deptId,
                                officeId = if (scopeType == "SingleOffice") officeId else null,
                                scopeType = scopeType
                            ))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
                        modifier = Modifier.weight(1f)
                    ) { Text("حفظ", color = Color.White) }
                }
            }
        }
    }
}

// Add Project Task Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProjectTaskDialog(
    projectId: Int,
    employees: List<Employee>,
    onDismiss: () -> Unit,
    onAdd: (String, String, Int, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var empId by remember { mutableStateOf(employees.firstOrNull()?.id ?: 0) }
    var urgency by remember { mutableStateOf("Urgent") }
    var importance by remember { mutableStateOf("Important") }
    var date by remember { mutableStateOf(todayIsoDate()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = WarmBackground)) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("إضافة مهمة للمشروع", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                ShaheenOutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان المهمة") }, modifier = Modifier.fillMaxWidth())
                ShaheenOutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("الوصف") }, modifier = Modifier.fillMaxWidth())

                Text("الموظف المسؤول:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                employees.forEach { emp ->
                    Row(modifier = Modifier.fillMaxWidth().clickable { empId = emp.id }.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = empId == emp.id, onClick = { empId = emp.id })
                        Text("${emp.name} (${emp.role})", fontSize = 11.sp)
                    }
                }

                DatePickerField(label = "تاريخ التسليم", selectedDate = date, onDateSelected = { date = it })

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("إلغاء", color = TextMuted) }
                    Button(onClick = { if (title.isNotBlank()) onAdd(title, desc, empId, urgency, importance, date) },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepGreen), modifier = Modifier.weight(1f)) {
                        Text("إضافة", color = Color.White)
                    }
                }
            }
        }
    }
}

// Add Project Member Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProjectMemberDialog(
    projectId: Int,
    employees: List<Employee>,
    onDismiss: () -> Unit,
    onAdd: (Int, String) -> Unit
) {
    var empId by remember { mutableStateOf(employees.firstOrNull()?.id ?: 0) }
    var role by remember { mutableStateOf("Member") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = WarmBackground)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("إضافة عضو للفريق", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepGreen)

                Text("اختر الموظف:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                employees.forEach { emp ->
                    Row(modifier = Modifier.fillMaxWidth().clickable { empId = emp.id }.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = empId == emp.id, onClick = { empId = emp.id })
                        Text("${emp.name} (${emp.role})", fontSize = 11.sp)
                    }
                }

                Text("الدور في المشروع:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Lead" to "قائد", "Member" to "عضو", "Reviewer" to "مراجع").forEach { (key, label) ->
                        FilterChip(selected = role == key, onClick = { role = key }, label = { Text(label, fontSize = 10.sp) })
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("إلغاء", color = TextMuted) }
                    Button(onClick = { onAdd(empId, role) }, colors = ButtonDefaults.buttonColors(containerColor = DeepGreen), modifier = Modifier.weight(1f)) {
                        Text("إضافة", color = Color.White)
                    }
                }
            }
        }
    }
}

// ──────────────────── INVOICE & PRINT DIALOGS ────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDialog(
    offices: List<Office>,
    existing: Invoice?,
    onDismiss: () -> Unit,
    onSave: (Invoice) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var officeId by remember { mutableStateOf(existing?.officeId ?: offices.firstOrNull()?.id ?: 0) }
    var amount by remember { mutableStateOf(existing?.amount?.toString() ?: "") }
    var tracking by remember { mutableStateOf(existing?.trackingNumber ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var date by remember { mutableStateOf(existing?.date ?: todayIsoDate()) }
    var type by remember { mutableStateOf(existing?.type ?: "Operations") }
    var imageUrl by remember { mutableStateOf(existing?.imageUrl ?: "") }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { imageUrl = it.toString() }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = WarmBackground)) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(if (existing != null) "تعديل فاتورة" else "فاتورة جديدة", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                ShaheenOutlinedTextField(value = tracking, onValueChange = { tracking = it }, label = { Text("رقم التتبع") }, modifier = Modifier.fillMaxWidth())
                ShaheenOutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("المبلغ (ر.س)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                ShaheenOutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("الوصف") }, modifier = Modifier.fillMaxWidth())
                DatePickerField(label = "تاريخ الفاتورة", selectedDate = date, onDateSelected = { date = it })
                Text("المكتب:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                offices.forEach { o ->
                    Row(modifier = Modifier.fillMaxWidth().clickable { officeId = o.id }.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = officeId == o.id, onClick = { officeId = o.id })
                        Text(o.name, fontSize = 11.sp)
                    }
                }
                Text("النوع:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Operations" to "تشغيل", "Utility" to "مرافق", "Supplier" to "مورد").forEach { (key, label) ->
                        FilterChip(selected = type == key, onClick = { type = key }, label = { Text(label, fontSize = 10.sp) })
                    }
                }
                Button(onClick = { imagePicker.launch("image/*") }, colors = ButtonDefaults.buttonColors(containerColor = Gold)) {
                    Icon(Icons.Filled.Image, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (imageUrl.isBlank()) "إرفاق صورة الفاتورة" else "تم إرفاق صورة", color = Color.White, fontSize = 12.sp)
                }
                if (imageUrl.isNotBlank()) {
                    AsyncImage(model = imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(8.dp)))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (onDelete != null) {
                        TextButton(onClick = onDelete) { Text("حذف", color = CoralRed) }
                    }
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("إلغاء", color = TextMuted) }
                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull() ?: 0.0
                            if (tracking.isNotBlank()) {
                                onSave(Invoice(
                                    id = existing?.id ?: 0,
                                    officeId = officeId,
                                    amount = amt,
                                    trackingNumber = tracking,
                                    description = description,
                                    date = date,
                                    type = type,
                                    imageUrl = imageUrl
                                ))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
                        modifier = Modifier.weight(1f)
                    ) { Text("حفظ", color = Color.White) }
                }
            }
        }
    }
}

@Composable
fun PrintTargetDialog(
    employees: List<Employee>,
    departments: List<Department>,
    offices: List<Office>,
    onDismiss: () -> Unit,
    onPrintEmployee: (Int) -> Unit,
    onPrintDepartment: (Int) -> Unit,
    onPrintOffice: (Int) -> Unit
) {
    var tab by remember { mutableStateOf("employee") }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = WarmBackground)) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("طباعة تقرير محدد", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("employee" to "موظف", "department" to "قسم", "office" to "مكتب").forEach { (key, label) ->
                        FilterChip(selected = tab == key, onClick = { tab = key }, label = { Text(label, fontSize = 10.sp) })
                    }
                }
                when (tab) {
                    "employee" -> employees.forEach { emp ->
                        TextButton(onClick = { onPrintEmployee(emp.id) }, modifier = Modifier.fillMaxWidth()) {
                            Text("${emp.name} (${emp.role})", color = DeepGreen, fontSize = 12.sp)
                        }
                    }
                    "department" -> departments.forEach { dept ->
                        TextButton(onClick = { onPrintDepartment(dept.id) }, modifier = Modifier.fillMaxWidth()) {
                            Text(dept.name, color = DeepGreen, fontSize = 12.sp)
                        }
                    }
                    "office" -> offices.forEach { office ->
                        TextButton(onClick = { onPrintOffice(office.id) }, modifier = Modifier.fillMaxWidth()) {
                            Text(office.name, color = DeepGreen, fontSize = 12.sp)
                        }
                    }
                }
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = DeepGreen), modifier = Modifier.fillMaxWidth()) {
                    Text("إغلاق", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun AppLogo(
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.app_logo),
        contentDescription = "شعار التطبيق",
        modifier = modifier
    )
}

// ──────────────────── GOLDEN SHAHEEN MAJESTIC BRAND LOGO ────────────────────
@Composable
fun GoldenShaheenLogo(
    modifier: Modifier = Modifier,
    tint: Color = Gold
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // 1. Draw three golden stars at the top in a beautiful arch
        drawStar(center = Offset(w * 0.5f, h * 0.12f), radius = w * 0.05f, color = tint)
        drawStar(center = Offset(w * 0.38f, h * 0.14f), radius = w * 0.045f, color = tint)
        drawStar(center = Offset(w * 0.62f, h * 0.14f), radius = w * 0.045f, color = tint)
        
        // 2. Draw Syrian Eagle of Saladin with fine geometric detail
        val path = Path()
        val centerX = w * 0.5f
        val eagleTop = h * 0.22f
        val eagleBottom = h * 0.95f
        
        // Head & Beak facing left (viewer's left)
        path.moveTo(centerX, eagleTop)
        path.cubicTo(centerX - w * 0.05f, eagleTop, centerX - w * 0.08f, eagleTop + h * 0.05f, centerX - w * 0.08f, eagleTop + h * 0.1f)
        path.lineTo(centerX - w * 0.12f, eagleTop + h * 0.11f)
        path.lineTo(centerX - w * 0.07f, eagleTop + h * 0.13f)
        path.lineTo(centerX - w * 0.05f, eagleTop + h * 0.17f)
        
        // Left Wing with distinct Syrian heraldic rib layers
        path.cubicTo(centerX - w * 0.25f, eagleTop + h * 0.04f, centerX - w * 0.45f, eagleTop + h * 0.12f, centerX - w * 0.48f, eagleTop + h * 0.32f)
        path.lineTo(centerX - w * 0.45f, eagleTop + h * 0.42f)
        path.lineTo(centerX - w * 0.40f, eagleTop + h * 0.39f)
        path.lineTo(centerX - w * 0.38f, eagleTop + h * 0.48f)
        path.lineTo(centerX - w * 0.32f, eagleTop + h * 0.44f)
        path.lineTo(centerX - w * 0.30f, eagleTop + h * 0.54f)
        path.lineTo(centerX - w * 0.22f, eagleTop + h * 0.48f)
        path.lineTo(centerX - w * 0.15f, eagleTop + h * 0.60f)
        
        // Left Claw
        path.lineTo(centerX - w * 0.15f, eagleBottom - h * 0.12f)
        path.lineTo(centerX - w * 0.18f, eagleBottom - h * 0.08f)
        path.lineTo(centerX - w * 0.11f, eagleBottom - h * 0.08f)
        path.lineTo(centerX - w * 0.08f, eagleBottom - h * 0.14f)
        
        // Tail feathers fanning downwards
        path.lineTo(centerX - w * 0.07f, eagleBottom)
        path.lineTo(centerX + w * 0.07f, eagleBottom)
        
        // Right Claw (Symmetrical)
        path.lineTo(centerX + w * 0.08f, eagleBottom - h * 0.14f)
        path.lineTo(centerX + w * 0.11f, eagleBottom - h * 0.08f)
        path.lineTo(centerX + w * 0.18f, eagleBottom - h * 0.08f)
        path.lineTo(centerX + w * 0.15f, eagleBottom - h * 0.12f)
        
        // Right Wing (Symmetrical)
        path.lineTo(centerX + w * 0.15f, eagleTop + h * 0.60f)
        path.lineTo(centerX + w * 0.22f, eagleTop + h * 0.48f)
        path.lineTo(centerX + w * 0.30f, eagleTop + h * 0.54f)
        path.lineTo(centerX + w * 0.32f, eagleTop + h * 0.44f)
        path.lineTo(centerX + w * 0.38f, eagleTop + h * 0.48f)
        path.lineTo(centerX + w * 0.40f, eagleTop + h * 0.39f)
        path.lineTo(centerX + w * 0.45f, eagleTop + h * 0.42f)
        path.lineTo(centerX + w * 0.48f, eagleTop + h * 0.32f)
        path.cubicTo(centerX + w * 0.45f, eagleTop + h * 0.12f, centerX + w * 0.25f, eagleTop + h * 0.04f, centerX + w * 0.05f, eagleTop + h * 0.17f)
        
        // Return to Head Symmetrically
        path.lineTo(centerX + w * 0.03f, eagleTop + h * 0.17f)
        path.cubicTo(centerX + w * 0.05f, eagleTop + h * 0.11f, centerX + w * 0.04f, eagleTop + h * 0.05f, centerX, eagleTop)
        
        path.close()
        drawPath(path, color = tint)
    }
}

private fun DrawScope.drawStar(center: Offset, radius: Float, color: Color) {
    val path = Path()
    val points = 5
    val doublePi = 2 * Math.PI
    val angleStep = doublePi / points
    val innerRadius = radius * 0.382f
    for (i in 0 until points) {
        val outerAngle = i * angleStep - Math.PI / 2
        val innerAngle = outerAngle + angleStep / 2
        val xOuter = center.x + radius * Math.cos(outerAngle).toFloat()
        val yOuter = center.y + radius * Math.sin(outerAngle).toFloat()
        val xInner = center.x + innerRadius * Math.cos(innerAngle).toFloat()
        val yInner = center.y + innerRadius * Math.sin(innerAngle).toFloat()
        if (i == 0) {
            path.moveTo(xOuter, yOuter)
        } else {
            path.lineTo(xOuter, yOuter)
        }
        path.lineTo(xInner, yInner)
    }
    path.close()
    drawPath(path, color)
}

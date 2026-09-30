package com.example

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.audio.AppLanguage
import com.example.audio.SurakshaVoiceManager
import com.example.data.AppDatabase
import com.example.data.ModuleEntity
import com.example.data.ModuleRepository
import com.example.data.QuizQuestion
import com.example.data.TrainingContentRepository
import com.example.data.VocationalSop
import com.example.ui.ARSimulationType
import com.example.ui.ThreeDEnvironmentView
import com.example.ui.theme.MyApplicationTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ------------------- USER ROLES -------------------
enum class UserRole {
    WORKER,
    ADMIN
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SurakshaApp()
            }
        }
    }
}

// ------------------- VIEWMODEL WITH ROOM DATABASE & BILINGUAL VOICE -------------------
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    val repository = ModuleRepository(database.moduleDao())

    val voiceManager = SurakshaVoiceManager(application)
    val isSpeaking: StateFlow<Boolean> = voiceManager.isSpeaking
    val currentLanguage: StateFlow<AppLanguage> = voiceManager.currentLanguage
    val activeSpeechTag: StateFlow<String?> = voiceManager.activeTag

    var currentRole by mutableStateOf(UserRole.WORKER)
    var loggedInUser by mutableStateOf<String?>("Rajesh Kumar")
    var workerId by mutableStateOf<String?>("JH-MNR-00482")
    var adminId by mutableStateOf<String?>("DGMS-ADM-102")
    var userSection by mutableStateOf<String?>("Underground Drift 3 - Face 4")
    var site by mutableStateOf<String?>("Rajmahal Coal Mine")

    fun loginAsWorker(name: String, id: String, section: String) {
        currentRole = UserRole.WORKER
        loggedInUser = name.ifBlank { "Rajesh Kumar" }
        workerId = id.ifBlank { "JH-MNR-00482" }
        userSection = section.ifBlank { "Underground Drift 3 - Face 4" }
    }

    fun loginAsAdmin(name: String, id: String) {
        currentRole = UserRole.ADMIN
        loggedInUser = name.ifBlank { "Er. A. K. Sharma (Mine Safety Director)" }
        adminId = id.ifBlank { "DGMS-ADM-102" }
    }

    fun switchRole(role: UserRole) {
        currentRole = role
        if (role == UserRole.ADMIN) {
            if (loggedInUser == "Rajesh Kumar" || loggedInUser.isNullOrBlank()) {
                loggedInUser = "Er. A. K. Sharma (Mine Safety Director)"
            }
        } else {
            if (loggedInUser?.startsWith("Er.") == true) {
                loggedInUser = "Rajesh Kumar"
            }
        }
    }

    var activeSimulationType by mutableStateOf(ARSimulationType.GAS_VENTILATION)
    var activeModuleId by mutableStateOf("1")

    val modules: StateFlow<List<ModuleEntity>> = repository.allModules
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val completedCount: StateFlow<Int> = repository.completedCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    init {
        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
        }
    }

    fun speak(text: String, language: AppLanguage? = null, tag: String? = null) {
        voiceManager.speak(text, language ?: voiceManager.currentLanguage.value, tag)
    }

    fun stopSpeech() {
        voiceManager.stop()
    }

    fun toggleLanguage(): AppLanguage {
        return voiceManager.toggleLanguage()
    }

    fun setLanguage(language: AppLanguage) {
        voiceManager.setLanguage(language)
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.shutdown()
    }

    fun launchSimulation(moduleId: String) {
        activeModuleId = moduleId
        activeSimulationType = when (moduleId) {
            "1" -> ARSimulationType.GAS_VENTILATION
            "2" -> ARSimulationType.FIRE_EVACUATION
            "3" -> ARSimulationType.MACHINERY_GUARDING
            "4" -> ARSimulationType.ELECTRICAL_ISOLATION
            "5" -> ARSimulationType.PPE_INSPECTION
            else -> ARSimulationType.GAS_VENTILATION
        }
    }

    fun setSimulationType(type: ARSimulationType) {
        activeSimulationType = type
        activeModuleId = when (type) {
            ARSimulationType.GAS_VENTILATION -> "1"
            ARSimulationType.FIRE_EVACUATION -> "2"
            ARSimulationType.MACHINERY_GUARDING -> "3"
            ARSimulationType.ELECTRICAL_ISOLATION -> "4"
            ARSimulationType.PPE_INSPECTION -> "5"
        }
    }

    fun recordHazardIdentified(type: String) {
        viewModelScope.launch {
            repository.updateHazardDetected(activeModuleId)
        }
    }

    fun completeModuleDrill(score: Int) {
        viewModelScope.launch {
            repository.completeModule(activeModuleId, score)
        }
    }

    fun resetModuleDrill(id: String) {
        viewModelScope.launch {
            repository.resetModule(id)
        }
    }
}

// ------------------- APP NAVIGATION ROOT -------------------
@Composable
fun SurakshaApp() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "landing"

    Scaffold(
        topBar = { TopBar(navController, currentRoute, viewModel) },
        bottomBar = {
            // Worker sees ONLY Home & Training. Admin sees Home, Training & Admin Panel.
            val allowedBottomRoutes = if (viewModel.currentRole == UserRole.ADMIN) {
                listOf("landing", "dashboard", "admin")
            } else {
                listOf("landing", "dashboard")
            }
            if (currentRoute in allowedBottomRoutes) {
                val currentLang by viewModel.currentLanguage.collectAsStateWithLifecycle()
                BottomBar(navController, currentRoute, viewModel.currentRole, currentLang)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "landing",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("landing") { LandingScreen(navController, viewModel) }
            composable("login") { LoginScreen(navController, viewModel) }
            composable("dashboard") { DashboardScreen(navController, viewModel) }
            composable("sim") { SimulationScreen(navController, viewModel) }
            composable("quiz") { QuizScreen(navController, viewModel) }
            composable("cert") { CertificateScreen(navController, viewModel) }
            composable("admin") { AdminScreen(navController, viewModel) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavController, currentRoute: String, viewModel: MainViewModel) {
    val currentLang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { navController.navigate("landing") }
            ) {
                // User Profile Avatar with Online Beacon
                Box(
                    modifier = Modifier.size(38.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_worker_avatar),
                        contentDescription = "Profile",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(0xFF00B251), CircleShape)
                            .border(1.5.dp, Color.White, CircleShape)
                    )
                }

                Spacer(Modifier.width(8.dp))

                // App Shield Logo
                Image(
                    painter = painterResource(id = R.drawable.ic_suraksha_shield_logo),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(Modifier.width(8.dp))

                Column {
                    Text(
                        text = if (currentLang == AppLanguage.HINDI) "सुरक्षा" else "SURAKSHA",
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (currentLang == AppLanguage.HINDI) "सुरक्षा AR" else "SURAKSHA AR",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        actions = {
            // 5 Days Safe Badge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier.padding(end = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Spacer(Modifier.width(5.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (currentLang == AppLanguage.HINDI) "5 दिन" else "5 DAYS",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(3.dp))
                            Box(modifier = Modifier.size(4.dp).background(Color(0xFF00B251), CircleShape))
                            Spacer(Modifier.width(3.dp))
                            Text(
                                text = if (currentLang == AppLanguage.HINDI) "सुरक्षित" else "SAFE",
                                color = Color(0xFF006E2F),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Speaking Indicator & Stop Button
            if (isSpeaking) {
                Surface(
                    onClick = { viewModel.stopSpeech() },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFE8F5E9),
                    border = BorderStroke(1.dp, Color(0xFF00B251)),
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.VolumeUp,
                            contentDescription = "Speaking",
                            tint = Color(0xFF006E2F),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Stop",
                            tint = Color(0xFF006E2F),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // Language Toggle Pill
            Surface(
                onClick = { viewModel.toggleLanguage() },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.padding(end = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentLang == AppLanguage.HINDI) "हिन्दी" else "EN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Default.VolumeUp,
                        contentDescription = "Voice Guide",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Role Switch Pill
            Surface(
                onClick = { navController.navigate("login") },
                shape = RoundedCornerShape(16.dp),
                color = if (viewModel.currentRole == UserRole.ADMIN) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, if (viewModel.currentRole == UserRole.ADMIN) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (viewModel.currentRole == UserRole.ADMIN) Icons.Default.AdminPanelSettings else Icons.Default.Engineering,
                        contentDescription = "Role",
                        tint = if (viewModel.currentRole == UserRole.ADMIN) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (currentLang == AppLanguage.HINDI) {
                            if (viewModel.currentRole == UserRole.ADMIN) "एडमिन" else "श्रमिक"
                        } else {
                            if (viewModel.currentRole == UserRole.ADMIN) "ADMIN" else "WORKER"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (viewModel.currentRole == UserRole.ADMIN) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun BottomBar(navController: NavController, currentRoute: String, userRole: UserRole, currentLang: AppLanguage) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Drill Center
            val isDrillSelected = currentRoute == "landing"
            Surface(
                onClick = { navController.navigate("landing") { launchSingleTop = true } },
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                color = if (isDrillSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, if (isDrillSelected) Color(0xFF783200) else Color.Transparent)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Construction,
                        contentDescription = null,
                        tint = if (isDrillSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (currentLang == AppLanguage.HINDI) "अभ्यास केंद्र" else "Drill Center",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isDrillSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (currentLang == AppLanguage.HINDI) "दैनिक ड्रिल" else "Daily Drills",
                            fontSize = 9.sp,
                            color = if (isDrillSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Training Modules
            val isModulesSelected = currentRoute == "dashboard"
            Surface(
                onClick = { navController.navigate("dashboard") { launchSingleTop = true } },
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                color = if (isModulesSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, if (isModulesSelected) Color(0xFF783200) else Color.Transparent)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = if (isModulesSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (currentLang == AppLanguage.HINDI) "प्रशिक्षण" else "Training",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isModulesSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (currentLang == AppLanguage.HINDI) "सभी मॉड्यूल" else "All Modules",
                            fontSize = 9.sp,
                            color = if (isModulesSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // If Admin: Admin Panel Tab
            if (userRole == UserRole.ADMIN) {
                val isAdminSelected = currentRoute == "admin"
                Surface(
                    onClick = { navController.navigate("admin") { launchSingleTop = true } },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = if (isAdminSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, if (isAdminSelected) Color(0xFF783200) else Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = if (isAdminSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Column {
                            Text(
                                text = if (currentLang == AppLanguage.HINDI) "एडमिन" else "Admin",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isAdminSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (currentLang == AppLanguage.HINDI) "पैनल" else "Panel",
                                fontSize = 9.sp,
                                color = if (isAdminSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ------------------- LANDING & LOGIN SCREENS -------------------
@Composable
fun LandingScreen(navController: NavController, viewModel: MainViewModel) {
    val scrollState = rememberScrollState()
    val currentLang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val activeTag by viewModel.activeSpeechTag.collectAsStateWithLifecycle()
    val isIntroSpeaking = isSpeaking && activeTag == "intro_brief"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Shift Greeting & Status Strip
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.2.dp, Color(0xFFCBD5E1)),
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFCCE5FF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Engineering,
                            contentDescription = null,
                            tint = Color(0xFF006398),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (currentLang == AppLanguage.HINDI) "शिफ्ट 1 • खदान 4B" else "Shift 1 • Pit 4B",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF6BFF8F)
                            ) {
                                Text(
                                    text = if (currentLang == AppLanguage.HINDI) "सक्रिय" else "ACTIVE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF002109),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = if (currentLang == AppLanguage.HINDI) "सुरक्षा नियम सीएमआर 2017" else "DGMS CMR 2017 Safety",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Radio Guidance Beacon
                Surface(
                    onClick = {
                        val radioText = if (currentLang == AppLanguage.HINDI) {
                            "सुरक्षा रेडियो सक्रिय है। आज का मुख्य अभ्यास भूमिगत मीथेन गैस जांच है। नीचे दिए गए नारंगी बटन को दबाकर अभ्यास शुरू करें।"
                        } else {
                            "Safety Radio Active. Today's mandatory drill is Underground Methane Check. Press the orange button below to start."
                        }
                        viewModel.speak(radioText, currentLang, "radio_beacon")
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = Color(0xFF006398),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (currentLang == AppLanguage.HINDI) "रेडियो चालू" else "Radio ON",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF006398)
                        )
                    }
                }
            }
        }

        // Hero Mandatory Drill Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.2.dp, Color(0xFFCBD5E1)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Top Header Lockup
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (currentLang == AppLanguage.HINDI) "अनिवार्य अभ्यास" else "MANDATORY EXERCISE",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (currentLang == AppLanguage.HINDI) "आज का सुरक्षा अभ्यास" else "Today's Safety Drill",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFDBCA)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                tint = Color(0xFF783200),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (currentLang == AppLanguage.HINDI) "3 मिनट" else "3 Mins",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF783200)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Drill Spotlight Row with Thumbnail & Prominent 56dp Audio Button
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Image(
                                painter = painterResource(id = R.drawable.img_drill_methane),
                                contentDescription = "Drill Graphic",
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (currentLang == AppLanguage.HINDI) "भूमिगत मीथेन गैस जांच" else "Underground Methane Check",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).background(Color(0xFF00B251), CircleShape))
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = if (currentLang == AppLanguage.HINDI) "एआर सेंसर जांच" else "AR Sensor Calibration",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF006E2F)
                                    )
                                }
                            }
                        }

                        // Prominent 56dp Audio Button with "Listen" text
                        Surface(
                            onClick = {
                                if (isIntroSpeaking) {
                                    viewModel.stopSpeech()
                                } else {
                                    val brief = if (currentLang == AppLanguage.HINDI) {
                                        "भूमिगत मीथेन गैस जांच अभ्यास। सुरक्षा एआर कैमरे से छत के पास गैस सेंसर कैलिब्रेट करें। 1.25 प्रतिशत से अधिक मीथेन होने पर तुरंत वेंटिलेशन चालू करें और निकासी अलार्म दबाएं।"
                                    } else {
                                        "Underground Methane Check drill. Using the camera AR sensor, inspect drift ceiling for combustible methane. If concentration exceeds 1.25 percent, isolate power and open ventilation doors."
                                    }
                                    viewModel.speak(brief, currentLang, "intro_brief")
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFCCE5FF),
                            border = BorderStroke(1.dp, Color(0xFF93CCFF)),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (isIntroSpeaking) Icons.Default.Close else Icons.Default.VolumeUp,
                                    contentDescription = "Listen",
                                    tint = Color(0xFF006398),
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = if (currentLang == AppLanguage.HINDI) "सुनें" else "Listen",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF006398)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Massive Safety Orange 3D Call to Action Button
                Surface(
                    onClick = {
                        viewModel.launchSimulation("1")
                        navController.navigate("sim")
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    border = BorderStroke(1.dp, Color(0xFF783200)),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (currentLang == AppLanguage.HINDI) "सुरक्षा अभ्यास शुरू करें" else "START SAFETY DRILL",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = if (currentLang == AppLanguage.HINDI) "कैमरा 3D सिमुलेशन चालू होगा" else "Live Camera 3D Simulation",
                                    fontSize = 10.sp,
                                    color = Color(0xFFFFDBCA)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color.White.copy(alpha = 0.25f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section Header: Required Modules
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (currentLang == AppLanguage.HINDI) "प्रशिक्षण मॉड्यूल" else "Required Modules",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF6BFF8F)
            ) {
                Text(
                    text = if (currentLang == AppLanguage.HINDI) "1/4 पूर्ण" else "1 of 4 Completed",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF002109),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        // 2x2 Interactive Module Cards Grid
        val moduleItems = listOf(
            Triple("1", if (currentLang == AppLanguage.HINDI) "गैस व वेंटिलेशन" else "Gas & Vent", R.drawable.img_drill_ventilation),
            Triple("2", if (currentLang == AppLanguage.HINDI) "आग से बचाव मार्ग" else "Fire Escape", R.drawable.img_drill_fire),
            Triple("3", if (currentLang == AppLanguage.HINDI) "कन्वेयर लॉक-आउट" else "Conveyor Lock", R.drawable.img_drill_conveyor),
            Triple("4", if (currentLang == AppLanguage.HINDI) "उच्च वोल्टेज सुरक्षा" else "High Voltage", R.drawable.img_drill_voltage)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: Gas & Vent (100% complete)
            StitchModuleCard(
                modifier = Modifier.weight(1f),
                title = moduleItems[0].second,
                progressPercent = 100,
                statusTag = if (currentLang == AppLanguage.HINDI) "पूर्ण 100%" else "DONE 100%",
                imageRes = moduleItems[0].third,
                onCardClick = {
                    viewModel.launchSimulation("1")
                    navController.navigate("sim")
                },
                onAudioClick = {
                    val sop = TrainingContentRepository.vocationalGuides["1"]
                    val speech = if (currentLang == AppLanguage.HINDI) sop?.summaryHi ?: "" else sop?.summary ?: ""
                    viewModel.speak(speech, currentLang, "mod_audio_1")
                }
            )

            // Card 2: Fire Escape (50% in progress)
            StitchModuleCard(
                modifier = Modifier.weight(1f),
                title = moduleItems[1].second,
                progressPercent = 50,
                statusTag = if (currentLang == AppLanguage.HINDI) "जारी 50%" else "RESUME 50%",
                imageRes = moduleItems[1].third,
                onCardClick = {
                    viewModel.launchSimulation("2")
                    navController.navigate("sim")
                },
                onAudioClick = {
                    val sop = TrainingContentRepository.vocationalGuides["2"]
                    val speech = if (currentLang == AppLanguage.HINDI) sop?.summaryHi ?: "" else sop?.summary ?: ""
                    viewModel.speak(speech, currentLang, "mod_audio_2")
                }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 3: Conveyor Lock
            StitchModuleCard(
                modifier = Modifier.weight(1f),
                title = moduleItems[2].second,
                progressPercent = 0,
                statusTag = if (currentLang == AppLanguage.HINDI) "लोटो ताला" else "LOCKOUT LOTO",
                imageRes = moduleItems[2].third,
                onCardClick = {
                    viewModel.launchSimulation("3")
                    navController.navigate("sim")
                },
                onAudioClick = {
                    val sop = TrainingContentRepository.vocationalGuides["3"]
                    val speech = if (currentLang == AppLanguage.HINDI) sop?.summaryHi ?: "" else sop?.summary ?: ""
                    viewModel.speak(speech, currentLang, "mod_audio_3")
                }
            )

            // Card 4: High Voltage
            StitchModuleCard(
                modifier = Modifier.weight(1f),
                title = moduleItems[3].second,
                progressPercent = 0,
                statusTag = if (currentLang == AppLanguage.HINDI) "11 KV खतरा" else "11 KV DANGER",
                imageRes = moduleItems[3].third,
                onCardClick = {
                    viewModel.launchSimulation("4")
                    navController.navigate("sim")
                },
                onAudioClick = {
                    val sop = TrainingContentRepository.vocationalGuides["4"]
                    val speech = if (currentLang == AppLanguage.HINDI) sop?.summaryHi ?: "" else sop?.summary ?: ""
                    viewModel.speak(speech, currentLang, "mod_audio_4")
                }
            )
        }

        // Quick Voice Assistance Strip
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        onClick = {
                            val help = if (currentLang == AppLanguage.HINDI) {
                                "सुरक्षा वॉयस बॉट तैयार है। किसी भी आपातकालीन स्थिति में तुरंत निकटतम वेंटिलेशन एयरवे में जाएं और ऑक्सीजन सेल्फ रेस्क्यूर चालू करें।"
                            } else {
                                "Safety voice assistant ready. In case of emergency, immediately proceed towards the intake ventilation airway and activate self-contained breathing equipment."
                            }
                            viewModel.speak(help, currentLang, "quick_voice_help")
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF006398),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Help",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            text = if (currentLang == AppLanguage.HINDI) "मदद चाहिए? माइक दबाएं" else "Need Help? Press Mic & Speak",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (currentLang == AppLanguage.HINDI) "बोलकर त्वरित सुरक्षा मार्गदर्शन प्राप्त करें" else "Spoken safety advice without reading",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = "AI BOT",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))
    }
}

@Composable
fun StitchModuleCard(
    modifier: Modifier = Modifier,
    title: String,
    progressPercent: Int,
    statusTag: String,
    imageRes: Int,
    onCardClick: () -> Unit,
    onAudioClick: () -> Unit
) {
    Card(
        onClick = onCardClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.2.dp, Color(0xFFCBD5E1)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Top Row: Progress indicator & Mini audio chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Progress Chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        progressPercent == 100 -> Color(0xFFE8F5E9)
                        progressPercent > 0 -> Color(0xFFFFDBCA)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = "$progressPercent%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            progressPercent == 100 -> Color(0xFF006E2F)
                            progressPercent > 0 -> Color(0xFF783200)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Mini Audio Chip
                Surface(
                    onClick = onAudioClick,
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFCCE5FF),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Listen",
                            tint = Color(0xFF006398),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // 3D Graphic Image Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(14.dp))
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize()
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when {
                        progressPercent == 100 -> Color(0xFF006E2F)
                        progressPercent > 0 -> MaterialTheme.colorScheme.primary
                        else -> Color(0xFF4A5568)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                ) {
                    Text(
                        text = statusTag,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column {
        Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ModuleStaticCard(title: String, risk: String, duration: String, subtitle: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(Modifier.size(32.dp, 4.dp).background(MaterialTheme.colorScheme.error))
            Spacer(Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            Spacer(Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(color = Color(0xFF4A1F1F), shape = RoundedCornerShape(12.dp)) {
                    Text("$risk RISK", color = Color(0xFFEF8686), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
                Text(duration, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun LoginScreen(navController: NavController, viewModel: MainViewModel) {
    val currentLang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    var selectedRole by remember { mutableStateOf(viewModel.currentRole) }

    // Worker fields
    var workerName by remember { mutableStateOf(viewModel.loggedInUser ?: "Rajesh Kumar") }
    var workerId by remember { mutableStateOf(viewModel.workerId ?: "JH-MNR-00482") }
    var section by remember { mutableStateOf(viewModel.userSection ?: "Underground Drift 3 - Face 4") }

    // Admin fields
    var adminName by remember { mutableStateOf(if (viewModel.currentRole == UserRole.ADMIN) (viewModel.loggedInUser ?: "Er. A. K. Sharma") else "Er. A. K. Sharma (Mine Safety Director)") }
    var adminId by remember { mutableStateOf(viewModel.adminId ?: "DGMS-ADM-102") }
    var pin by remember { mutableStateOf("admin123") }
    var pinVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(if (selectedRole == UserRole.ADMIN) MaterialTheme.colorScheme.primary else Color(0xFF00FF88))
                )

                Column(Modifier.padding(24.dp)) {
                    Text(
                        text = if (currentLang == AppLanguage.HINDI) "सुरक्षा पोर्टल लॉगिन" else "Suraksha Portal Login",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (currentLang == AppLanguage.HINDI) "वर्कस्पेस अनुमतियों के लिए अपनी भूमिका चुनें" else "Select your role to configure workspace permissions",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(20.dp))

                    // Role Selector Tabs (Mining Worker vs Safety Admin)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        Surface(
                            onClick = {
                                selectedRole = UserRole.WORKER
                                errorMessage = null
                            },
                            shape = RoundedCornerShape(6.dp),
                            color = if (selectedRole == UserRole.WORKER) MaterialTheme.colorScheme.surface else Color.Transparent,
                            border = if (selectedRole == UserRole.WORKER) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Engineering,
                                    contentDescription = null,
                                    tint = if (selectedRole == UserRole.WORKER) Color(0xFF00FF88) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (currentLang == AppLanguage.HINDI) "खनन कर्मचारी" else "Mining Worker",
                                    fontWeight = if (selectedRole == UserRole.WORKER) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = if (selectedRole == UserRole.WORKER) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(Modifier.width(4.dp))

                        Surface(
                            onClick = {
                                selectedRole = UserRole.ADMIN
                                errorMessage = null
                            },
                            shape = RoundedCornerShape(6.dp),
                            color = if (selectedRole == UserRole.ADMIN) MaterialTheme.colorScheme.surface else Color.Transparent,
                            border = if (selectedRole == UserRole.ADMIN) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = if (selectedRole == UserRole.ADMIN) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (currentLang == AppLanguage.HINDI) "सुरक्षा अधिकारी" else "Safety Admin",
                                    fontWeight = if (selectedRole == UserRole.ADMIN) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = if (selectedRole == UserRole.ADMIN) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    if (selectedRole == UserRole.WORKER) {
                        // WORKER FORM - ACCESS RESTRICTED TO HOME & TRAINING MODULES ONLY
                        Surface(
                            color = Color(0xFF0D2518),
                            border = BorderStroke(1.dp, Color(0xFF1E4D32)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00FF88), modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = if (currentLang == AppLanguage.HINDI) "कर्मचारी मोड: रूम डेटाबेस प्रगति और एआर सिमुलेशन के साथ होम और सुरक्षा प्रशिक्षण मॉड्यूल खोलें।" else "Worker Mode: Unlocks Home & Safety Training Modules with Room DB progress and AR simulation drills.",
                                    fontSize = 12.sp,
                                    color = Color(0xFFC7F3DB),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = workerId,
                            onValueChange = { workerId = it },
                            label = { Text(if (currentLang == AppLanguage.HINDI) "कर्मचारी आईडी / कोड" else "Worker ID / Staff Code") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = workerName,
                            onValueChange = { workerName = it },
                            label = { Text(if (currentLang == AppLanguage.HINDI) "कर्मचारी का पूरा नाम" else "Worker Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = section,
                            onValueChange = { section = it },
                            label = { Text(if (currentLang == AppLanguage.HINDI) "भूमिगत सेक्शन / खदान का स्थान" else "Underground Section / Mine Face") },
                            leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(24.dp))

                        Button(
                            onClick = {
                                viewModel.loginAsWorker(workerName, workerId, section)
                                navController.navigate("dashboard") {
                                    popUpTo("landing")
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black)
                        ) {
                            Text(
                                text = if (currentLang == AppLanguage.HINDI) "कर्मचारी के रूप में लॉगिन करें →" else "Sign In as Worker → Open Training Modules",
                                fontWeight = FontWeight.Bold
                            )
                        }

                    } else {
                        // ADMIN FORM - ACCESS TO ADMIN PANEL & WORKFORCE ANALYTICS
                        Surface(
                            color = Color(0xFF2C2210),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = if (currentLang == AppLanguage.HINDI) "एडमिन मोड: साइट सुरक्षा पैनल, पंजीकृत कर्मी रजिस्टर, लाइव एआर ऑडिट लॉग और अभ्यास खोलें।" else "Admin Mode: Unlocks Site Safety Panel, Registered Worker Compliance Roster, Live AR Audit Logs & Training Drills.",
                                    fontSize = 12.sp,
                                    color = Color(0xFFFFE0A3),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = adminId,
                            onValueChange = { adminId = it },
                            label = { Text(if (currentLang == AppLanguage.HINDI) "अधिकारी कोड" else "Admin Inspector Code") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = adminName,
                            onValueChange = { adminName = it },
                            label = { Text(if (currentLang == AppLanguage.HINDI) "अधिकारी का नाम और पद" else "Officer Name & Designation") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = pin,
                            onValueChange = {
                                pin = it
                                errorMessage = null
                            },
                            label = { Text(if (currentLang == AppLanguage.HINDI) "सुरक्षा पासवर्ड / पिन" else "Security Passcode / PIN") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { pinVisible = !pinVisible }) {
                                    Icon(
                                        imageVector = if (pinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle visibility"
                                    )
                                }
                            },
                            visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (currentLang == AppLanguage.HINDI) "डेमो पासवर्ड: admin123" else "Demo Passcode: admin123",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (errorMessage != null) {
                            Spacer(Modifier.height(6.dp))
                            Text(errorMessage!!, fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                        }

                        Spacer(Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (pin.trim().isEmpty()) {
                                    errorMessage = if (currentLang == AppLanguage.HINDI) "कृपया सुरक्षा पिन दर्ज करें (जैसे admin123)" else "Please enter the security PIN (e.g. admin123)"
                                } else {
                                    viewModel.loginAsAdmin(adminName, adminId)
                                    navController.navigate("admin") {
                                        popUpTo("landing")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black)
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (currentLang == AppLanguage.HINDI) "व्यवस्थापक के रूप में लॉगिन करें →" else "Sign In as Administrator → Open Admin Panel",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    TextButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (currentLang == AppLanguage.HINDI) "रद्द करें और होम पर लौटें" else "Cancel & Return to Home",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ------------------- ROOM PROGRESS DASHBOARD -------------------
@Composable
fun DashboardScreen(navController: NavController, viewModel: MainViewModel) {
    val modules by viewModel.modules.collectAsStateWithLifecycle()
    val currentLang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val activeTag by viewModel.activeSpeechTag.collectAsStateWithLifecycle()

    val completedCount = modules.count { it.status == "COMPLETED" }
    val inProgressCount = modules.count { it.status == "IN_PROGRESS" }
    val totalCount = modules.size
    val averageScore = if (completedCount > 0) {
        modules.filter { it.status == "COMPLETED" }.map { it.score }.average().toInt()
    } else {
        0
    }
    val overallProgress = if (totalCount > 0) {
        modules.sumOf { it.progress.toDouble() }.toFloat() / totalCount
    } else {
        0f
    }

    var viewingSop by remember { mutableStateOf<VocationalSop?>(null) }

    if (viewingSop != null) {
        VocationalSopDialog(
            sop = viewingSop!!,
            viewModel = viewModel,
            onDismiss = { viewingSop = null }
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        if (currentLang == AppLanguage.HINDI) "सुरक्षा प्रशिक्षण प्रगति" else "Safety Training Progress",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (currentLang == AppLanguage.HINDI) "रूम डेटाबेस सक्रिय ट्रैकिंग • डीजीएमएस सत्यापित" else "Room Database Active Tracking • DGMS Verified",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = if (overallProgress >= 0.8f) {
                            if (currentLang == AppLanguage.HINDI) "डीजीएमएस ग्रेड A" else "DGMS GRADE A"
                        } else {
                            if (currentLang == AppLanguage.HINDI) "प्रशिक्षण जारी" else "IN DRILL CYCLE"
                        },
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            // Progress Summary Dashboard Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.2.dp, Color(0xFFCBD5E1)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (currentLang == AppLanguage.HINDI) "समग्र व्यावसायिक क्षमता" else "Overall Competency Progress",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("${(overallProgress * 100).toInt()}%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { overallProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.background
                    )
                    Spacer(Modifier.height(16.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatItem(totalCount.toString(), if (currentLang == AppLanguage.HINDI) "कुल मॉड्यूल" else "Assigned")
                        StatItem(completedCount.toString(), if (currentLang == AppLanguage.HINDI) "पूर्ण" else "Completed")
                        StatItem(inProgressCount.toString(), if (currentLang == AppLanguage.HINDI) "प्रगति पर" else "In Progress")
                        StatItem("$averageScore%", if (currentLang == AppLanguage.HINDI) "औसत स्कोर" else "Avg Score")
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (currentLang == AppLanguage.HINDI) "प्रशिक्षण मॉड्यूल (${modules.size})" else "Training Modules (${modules.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    onClick = { viewModel.toggleLanguage() },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = if (currentLang == AppLanguage.HINDI) "वॉयस: हिन्दी" else "Voice: English",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        items(modules, key = { it.id }) { module ->
            val isSpeakingThisModule = isSpeaking && activeTag == "mod_${module.id}"
            ModuleProgressCard(
                module = module,
                currentLang = currentLang,
                isSpeakingThisModule = isSpeakingThisModule,
                onPlayVoiceGuide = {
                    if (isSpeakingThisModule) {
                        viewModel.stopSpeech()
                    } else {
                        val sop = TrainingContentRepository.vocationalGuides[module.id]
                        val textToSpeak = if (currentLang == AppLanguage.HINDI) {
                            "${sop?.titleHi ?: module.title}। ${sop?.summaryHi ?: module.description}। अवधि ${module.duration}।"
                        } else {
                            "${sop?.title ?: module.title}. ${sop?.summary ?: module.description}. Duration ${module.duration}."
                        }
                        viewModel.speak(textToSpeak, currentLang, "mod_${module.id}")
                    }
                },
                onStartDrill = {
                    viewModel.launchSimulation(module.id)
                    navController.navigate("sim")
                },
                onViewCertificate = {
                    viewModel.activeModuleId = module.id
                    navController.navigate("cert")
                },
                onReset = {
                    viewModel.resetModuleDrill(module.id)
                },
                onViewSop = {
                    viewingSop = TrainingContentRepository.vocationalGuides[module.id]
                }
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun ModuleProgressCard(
    module: ModuleEntity,
    currentLang: AppLanguage,
    isSpeakingThisModule: Boolean,
    onPlayVoiceGuide: () -> Unit,
    onStartDrill: () -> Unit,
    onViewCertificate: () -> Unit,
    onReset: () -> Unit,
    onViewSop: () -> Unit
) {
    val sop = TrainingContentRepository.vocationalGuides[module.id]
    val (vectorIcon, accentColor, shortBadge) = when (module.id) {
        "1" -> Triple(Icons.Default.Air, Color(0xFFFFB300), if (currentLang == AppLanguage.HINDI) "गैस रिसाव" else "Gas Leak")
        "2" -> Triple(Icons.Default.LocalFireDepartment, Color(0xFFFF5252), if (currentLang == AppLanguage.HINDI) "आग व निकासी" else "Fire Escape")
        "3" -> Triple(Icons.Default.Lock, Color(0xFFFF9800), if (currentLang == AppLanguage.HINDI) "मशीन लोटो" else "LOTO Lock")
        "4" -> Triple(Icons.Default.Bolt, Color(0xFF00E5FF), if (currentLang == AppLanguage.HINDI) "बिजली सुरक्षा" else "High Voltage")
        "5" -> Triple(Icons.Default.HealthAndSafety, Color(0xFF00E676), if (currentLang == AppLanguage.HINDI) "सुरक्षा किट" else "PPE Kit")
        else -> Triple(Icons.Default.HealthAndSafety, MaterialTheme.colorScheme.primary, if (currentLang == AppLanguage.HINDI) "सुरक्षा" else "Safety")
    }

    val moduleTitle = if (currentLang == AppLanguage.HINDI && sop != null) sop.titleHi else module.title
    val moduleDesc = if (currentLang == AppLanguage.HINDI && sop != null) sop.summaryHi else module.description

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, if (isSpeakingThisModule) Color(0xFF006E2F) else Color(0xFFCBD5E1)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            // Top Row: Big Visual Vector Badge + Title + Big Audio Pill Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(accentColor.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                            .border(1.dp, accentColor, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = vectorIcon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Surface(
                            color = accentColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = shortBadge,
                                color = accentColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = moduleTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF131B2E)
                        )
                    }
                }

                // DEDICATED AUDIO VOICE PILL
                Surface(
                    onClick = onPlayVoiceGuide,
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSpeakingThisModule) Color(0xFF00E676) else Color(0xFFEFF6FF),
                    border = BorderStroke(1.5.dp, if (isSpeakingThisModule) Color(0xFF00E676) else Color(0xFF93CCFF)),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSpeakingThisModule) Icons.Default.Close else Icons.Default.VolumeUp,
                            contentDescription = "Speak",
                            tint = if (isSpeakingThisModule) Color.Black else Color(0xFF006398),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (isSpeakingThisModule) {
                                if (currentLang == AppLanguage.HINDI) "रोकें" else "Stop"
                            } else {
                                if (currentLang == AppLanguage.HINDI) "सुनें" else "Listen"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSpeakingThisModule) Color.Black else Color(0xFF006398)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Concise summary line
            Text(
                text = moduleDesc,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                maxLines = 2
            )

            Spacer(Modifier.height(12.dp))

            // Progress Bar & Status Pill
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { module.progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (module.status == "COMPLETED") Color(0xFF00E676) else accentColor,
                    trackColor = MaterialTheme.colorScheme.background
                )
                Spacer(Modifier.width(10.dp))
                val statusText = when (module.status) {
                    "COMPLETED" -> if (currentLang == AppLanguage.HINDI) "पूर्ण" else "Completed"
                    "IN_PROGRESS" -> "${(module.progress * 100).toInt()}%"
                    else -> if (currentLang == AppLanguage.HINDI) "शुरू करें" else "Start"
                }
                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    color = if (module.status == "COMPLETED") Color(0xFF006E2F) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(14.dp))

            // BIG ACTION BUTTONS - Large touch targets (50dp height)
            if (module.status == "COMPLETED") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onViewCertificate,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B251), contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (currentLang == AppLanguage.HINDI) "सर्टिफिकेट" else "Certificate", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onViewSop,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(4.dp))
                        Text(if (currentLang == AppLanguage.HINDI) "नियमावली" else "Manual", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Big Primary 3D Drill Button
                    Button(
                        onClick = onStartDrill,
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (module.status == "IN_PROGRESS") (if (currentLang == AppLanguage.HINDI) "3D जारी रखें" else "Resume 3D")
                            else (if (currentLang == AppLanguage.HINDI) "3D सिमुलेशन" else "Start 3D AR"),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }

                    // SOP Rules Quick Audio Dialog Button
                    OutlinedButton(
                        onClick = onViewSop,
                        modifier = Modifier.height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.2.dp, Color(0xFFCBD5E1))
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(4.dp))
                        Text(if (currentLang == AppLanguage.HINDI) "नियमावली" else "Manual", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ------------------- ENHANCED AR SIMULATION SCREEN -------------------
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SimulationScreen(navController: NavController, viewModel: MainViewModel) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val currentLang by viewModel.currentLanguage.collectAsStateWithLifecycle()

    if (cameraPermissionState.status.isGranted) {
        ARSimulationView(navController, viewModel)
    } else {
        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (currentLang == AppLanguage.HINDI) "कैमरा अनुमति आवश्यक" else "Camera Permission Required",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (currentLang == AppLanguage.HINDI) "सुरक्षा एआर आपके कैमरे पर 3D गैस रिसाव, आग, कन्वेयर बेल्ट और सुरक्षा उपकरणों का सिमुलेशन दिखाता है।" else "Suraksha AR projects 3D gas fumes, fire hazards, conveyors, electrical substations, and PPE directly over your physical camera feed.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { cameraPermissionState.launchPermissionRequest() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black)
            ) {
                Text(
                    text = if (currentLang == AppLanguage.HINDI) "कैमरा अनुमति दें" else "Grant Camera Permission",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ARSimulationView(navController: NavController, viewModel: MainViewModel) {
    val activeType = viewModel.activeSimulationType
    val currentLang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val activeTag by viewModel.activeSpeechTag.collectAsStateWithLifecycle()
    val isDrillAudioPlaying = isSpeaking && activeTag == "drill_${viewModel.activeModuleId}"
    val isStepsAudioPlaying = isSpeaking && activeTag == "steps_${viewModel.activeModuleId}"
    val isDgmsAudioPlaying = isSpeaking && activeTag == "dgms_${viewModel.activeModuleId}"

    var hazardIdentifiedLocally by remember { mutableStateOf(false) }
    var viewingSopInDrill by remember { mutableStateOf<VocationalSop?>(null) }

    if (viewingSopInDrill != null) {
        VocationalSopDialog(
            sop = viewingSopInDrill!!,
            viewModel = viewModel,
            onDismiss = { viewingSopInDrill = null },
            onStartQuiz = {
                viewingSopInDrill = null
                navController.navigate("quiz")
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth().background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            // Layer 1: Native Physical Camera Feed (CameraX)
            CameraPreview()

            // Layer 2: 3D AR Holographic Simulation with dynamic WebGL models
            ThreeDEnvironmentView(
                simulationType = viewModel.activeSimulationType,
                onHazardIdentified = { type ->
                    hazardIdentifiedLocally = true
                    viewModel.recordHazardIdentified(type)
                    val confirmMsg = if (currentLang == AppLanguage.HINDI) {
                        "सावधानी! आपने सफलतापूर्वक खतरे को चिन्हित कर लिया है और सुरक्षा क्रिया पूरी कर ली है।"
                    } else {
                        "Safety protocol verified! Hazard successfully tagged and neutralized."
                    }
                    viewModel.speak(confirmMsg, currentLang, "hazard_cleared")
                }
            )

            // Top HUD Bar: Multi-Module Switcher & Telemetry
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // AR Recording Status Badge
                    Surface(
                        color = Color.Black.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.error, CircleShape))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (currentLang == AppLanguage.HINDI) "एआर सिमुलेशन सक्रिय" else "AR PASSTHROUGH ACTIVE",
                                color = Color(0xFFEF8686),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Simulation Type Switcher Pills (Horizontal Scrollable)
                    Surface(
                        color = Color.Black.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(
                                Triple(ARSimulationType.GAS_VENTILATION, if (currentLang == AppLanguage.HINDI) "1. गैस रिसाव" else "1. Gas Leak", Color(0xFFFFD166)),
                                Triple(ARSimulationType.FIRE_EVACUATION, if (currentLang == AppLanguage.HINDI) "2. आग व निकासी" else "2. Fire & Egress", Color(0xFF06D6A0)),
                                Triple(ARSimulationType.MACHINERY_GUARDING, if (currentLang == AppLanguage.HINDI) "3. मशीन लोटो" else "3. Conveyor LOTO", Color(0xFFF39C12)),
                                Triple(ARSimulationType.ELECTRICAL_ISOLATION, if (currentLang == AppLanguage.HINDI) "4. बिजली स्पार्क" else "4. Arc Flash", Color(0xFF5DADE2)),
                                Triple(ARSimulationType.PPE_INSPECTION, if (currentLang == AppLanguage.HINDI) "5. किट व मास्क" else "5. PPE Check", Color(0xFF2ECC71))
                            ).forEach { (type, label, activeColor) ->
                                val isSelected = activeType == type
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) activeColor else Color.Transparent,
                                    modifier = Modifier.clickable {
                                        viewModel.setSimulationType(type)
                                        hazardIdentifiedLocally = false
                                    }
                                ) {
                                    Text(
                                        label,
                                        color = if (isSelected) Color.Black else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Real-time Environmental Telemetry Card for active simulation with Voice Audio Button
                Surface(
                    color = Color.Black.copy(alpha = 0.88f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        1.dp,
                        when (activeType) {
                            ARSimulationType.GAS_VENTILATION -> Color(0xFFFFB020)
                            ARSimulationType.FIRE_EVACUATION -> Color(0xFFFF4500)
                            ARSimulationType.MACHINERY_GUARDING -> Color(0xFFF39C12)
                            ARSimulationType.ELECTRICAL_ISOLATION -> Color(0xFF5DADE2)
                            ARSimulationType.PPE_INSPECTION -> Color(0xFF2ECC71)
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            when (activeType) {
                                ARSimulationType.GAS_VENTILATION -> {
                                    Text(if (currentLang == AppLanguage.HINDI) "मीथेन CH4: 1.48% (खतरनाक)" else "CH4 Methane: 1.48% (HIGH LEL)", color = Color(0xFFFFD166), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(if (currentLang == AppLanguage.HINDI) "जहरीला धुआं • पीले वॉल्व पर टैप करें" else "Fumes Plume • Tap Valve Hotspot", color = Color.White, fontSize = 10.sp)
                                }
                                ARSimulationType.FIRE_EVACUATION -> {
                                    Text(if (currentLang == AppLanguage.HINDI) "आपातकाल: सक्रिय कन्वेयर आग" else "CODE RED: Active Conveyor Fire", color = Color(0xFFFF6B6B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(if (currentLang == AppLanguage.HINDI) "फर्श के हरे तीरों का अनुसरण करें →" else "Follow Dynamic Green Chevrons →", color = Color(0xFF00FF88), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                ARSimulationType.MACHINERY_GUARDING -> {
                                    Text(if (currentLang == AppLanguage.HINDI) "खतरा: खुला हुआ घूमने वाला ड्रम" else "DANGER: Exposed Rotating Pinch Point", color = Color(0xFFF39C12), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(if (currentLang == AppLanguage.HINDI) "ताला लगाने के लिए लाल लोटो बॉक्स पर टैप करें" else "Tap Red LOTO Box to Lock Out", color = Color.White, fontSize = 10.sp)
                                }
                                ARSimulationType.ELECTRICAL_ISOLATION -> {
                                    Text(if (currentLang == AppLanguage.HINDI) "सावधानी: 3.3kV उच्च वोल्टेज स्पार्क" else "WARNING: 3.3kV Arc Flash Plasma Active", color = Color(0xFF5DADE2), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(if (currentLang == AppLanguage.HINDI) "आइसोलेटर लीवर नीचे गिराएं" else "Tap Isolator Lever to Ground", color = Color.White, fontSize = 10.sp)
                                }
                                ARSimulationType.PPE_INSPECTION -> {
                                    Text(if (currentLang == AppLanguage.HINDI) "डीजीएमएस जांच: शिफ्ट पूर्व निरीक्षण" else "DGMS MUSTER: Pre-Shift Inspection", color = Color(0xFF2ECC71), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(if (currentLang == AppLanguage.HINDI) "सील जांचने के लिए वर्कबेंच पर टैप करें" else "Tap Workbench to Verify Seal", color = Color.White, fontSize = 10.sp)
                                }
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        // Voice Instruction Bar inside HUD
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                onClick = {
                                    if (isDrillAudioPlaying) {
                                        viewModel.stopSpeech()
                                    } else {
                                        val sop = TrainingContentRepository.vocationalGuides[viewModel.activeModuleId]
                                        val drillAudioText = if (currentLang == AppLanguage.HINDI) {
                                            sop?.drillAudioHi ?: "खदान में सुरक्षा नियमों का पालन करें।"
                                        } else {
                                            sop?.drillAudioEn ?: "Follow mine safety regulations."
                                        }
                                        viewModel.speak(drillAudioText, currentLang, "drill_${viewModel.activeModuleId}")
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDrillAudioPlaying) Color(0xFF1B3824) else Color(0xFF222222),
                                border = BorderStroke(1.dp, if (isDrillAudioPlaying) Color(0xFF00FF88) else MaterialTheme.colorScheme.primary)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isDrillAudioPlaying) Icons.Default.Close else Icons.Default.VolumeUp,
                                        contentDescription = "Voice Audio",
                                        tint = if (isDrillAudioPlaying) Color(0xFF00FF88) else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = if (isDrillAudioPlaying) {
                                            if (currentLang == AppLanguage.HINDI) "आवाज बंद करें" else "Stop Voice Drill"
                                        } else {
                                            if (currentLang == AppLanguage.HINDI) "निर्देश आवाज में सुनें" else "Listen Drill Briefing"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDrillAudioPlaying) Color(0xFF00FF88) else Color.White
                                    )
                                }
                            }

                            // Language Switcher inside AR HUD
                            Surface(
                                onClick = { viewModel.toggleLanguage() },
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF222222),
                                border = BorderStroke(1.dp, Color.Gray)
                            ) {
                                Text(
                                    text = if (currentLang == AppLanguage.HINDI) "HI" else "EN",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Actions inside HUD
            Row(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        hazardIdentifiedLocally = false
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Black.copy(alpha = 0.6f), contentColor = Color.White)
                ) {
                    Text(if (currentLang == AppLanguage.HINDI) "पुनः सेट करें" else "Recalibrate", color = Color.White)
                }
                Button(
                    onClick = { navController.navigate("quiz") },
                    modifier = Modifier.weight(1.6f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black)
                ) {
                    Text(if (currentLang == AppLanguage.HINDI) "प्रश्नोत्तरी शुरू करें →" else "Take Vocational Quiz →", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Bottom Inspection, Emergency Steps, DGMS Laws & Module Completion Panel
        val sop = TrainingContentRepository.vocationalGuides[viewModel.activeModuleId]
        val checkpointTitle = when (activeType) {
            ARSimulationType.GAS_VENTILATION -> if (currentLang == AppLanguage.HINDI) "जांच बिंदु 1 — भूमिगत गैस एवं वेंटिलेशन" else "CHECKPOINT 1 — UNDERGROUND GAS & VENTILATION"
            ARSimulationType.FIRE_EVACUATION -> if (currentLang == AppLanguage.HINDI) "जांच बिंदु 2 — आग एवं आपातकालीन निकासी" else "CHECKPOINT 2 — FIRE & RUNWAY EVACUATION"
            ARSimulationType.MACHINERY_GUARDING -> if (currentLang == AppLanguage.HINDI) "जांच बिंदु 3 — कन्वेयर एवं मशीनरी लोटो" else "CHECKPOINT 3 — CONVEYOR & MACHINERY LOTO"
            ARSimulationType.ELECTRICAL_ISOLATION -> if (currentLang == AppLanguage.HINDI) "जांच बिंदु 4 — 3.3kV विद्युत सुरक्षा" else "CHECKPOINT 4 — 3.3kV ELECTRICAL ISOLATION"
            ARSimulationType.PPE_INSPECTION -> if (currentLang == AppLanguage.HINDI) "जांच बिंदु 5 — अनिवार्य पीपीई सुरक्षा उपकरण" else "CHECKPOINT 5 — MANDATORY PPE MUSTER INSPECTION"
        }

        Column(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Critical Condition Alert Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.2.dp, Color(0xFFCBD5E1)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFFFFDBCA),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (currentLang == AppLanguage.HINDI) "आपातकालीन स्थिति" else "EMERGENCY HAZARD CONDITION",
                                color = Color(0xFF783200),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            color = if (hazardIdentifiedLocally) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (hazardIdentifiedLocally) {
                                    if (currentLang == AppLanguage.HINDI) "खतरा नियंत्रित" else "HAZARD NEUTRALIZED"
                                } else {
                                    if (currentLang == AppLanguage.HINDI) "सक्रिय खतरा" else "ACTIVE THREAT"
                                },
                                color = if (hazardIdentifiedLocally) Color(0xFF006E2F) else Color(0xFFBA1A1A),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    val condTitle = if (currentLang == AppLanguage.HINDI) (sop?.emergencyConditionHi ?: checkpointTitle) else (sop?.emergencyCondition ?: checkpointTitle)
                    Text(condTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF131B2E))

                    Spacer(Modifier.height(4.dp))
                    val condSummary = if (currentLang == AppLanguage.HINDI) (sop?.summaryHi ?: "") else (sop?.summary ?: "")
                    Text(condSummary, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 17.sp)
                }
            }

            // 2. What Steps Should Worker Take in this Condition Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.2.dp, Color(0xFFCBD5E1)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier.size(32.dp).background(Color(0xFFFFDBCA), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.DirectionsRun, contentDescription = null, tint = Color(0xFF783200), modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (currentLang == AppLanguage.HINDI) "इस स्थिति में क्या कदम उठाएं" else "Steps Worker Must Take",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF131B2E)
                                )
                                Text(
                                    text = if (currentLang == AppLanguage.HINDI) "मानक संचालन प्रक्रिया (SOP)" else "Immediate Vocational SOP",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Vocal Audio Button for Emergency Steps
                        Surface(
                            onClick = {
                                if (isStepsAudioPlaying) {
                                    viewModel.stopSpeech()
                                } else {
                                    val stepsList = if (currentLang == AppLanguage.HINDI) (sop?.emergencyStepsHi ?: emptyList()) else (sop?.emergencySteps ?: emptyList())
                                    val narration = stepsList.joinToString(". ")
                                    viewModel.speak(narration, currentLang, "steps_${viewModel.activeModuleId}")
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isStepsAudioPlaying) Color(0xFF1B3824) else Color(0xFFEFF6FF),
                            border = BorderStroke(1.dp, if (isStepsAudioPlaying) Color(0xFF00FF88) else Color(0xFF93CCFF))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isStepsAudioPlaying) Icons.Default.Close else Icons.Default.VolumeUp,
                                    contentDescription = "Listen Steps",
                                    tint = if (isStepsAudioPlaying) Color(0xFF00FF88) else Color(0xFF006398),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (isStepsAudioPlaying) {
                                        if (currentLang == AppLanguage.HINDI) "रोकें" else "Stop"
                                    } else {
                                        if (currentLang == AppLanguage.HINDI) "कदम सुनें" else "Listen Steps"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isStepsAudioPlaying) Color(0xFF00FF88) else Color(0xFF006398)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    val stepsList = if (currentLang == AppLanguage.HINDI) (sop?.emergencyStepsHi ?: emptyList()) else (sop?.emergencySteps ?: emptyList())
                    stepsList.forEachIndexed { idx, step ->
                        Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text((idx + 1).toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(step, fontSize = 12.sp, color = Color(0xFF131B2E), lineHeight = 17.sp)
                        }
                    }
                }
            }

            // 3. DGMS Guidelines & Statutory Laws in Vocal Format Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.2.dp, Color(0xFFCBD5E1)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier.size(32.dp).background(Color(0xFFE0F2FE), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Gavel, contentDescription = null, tint = Color(0xFF006398), modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (currentLang == AppLanguage.HINDI) "डीजीएमएस नियम व वैधानिक कानून" else "DGMS Statutory Regulations",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF131B2E)
                                )
                                Text(
                                    text = sop?.statutoryRef ?: "DGMS Mining Standards",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Vocal Audio Button for DGMS Laws
                        Surface(
                            onClick = {
                                if (isDgmsAudioPlaying) {
                                    viewModel.stopSpeech()
                                } else {
                                    val lawAudio = if (currentLang == AppLanguage.HINDI) (sop?.dgmsLawsAudioHi ?: "") else (sop?.dgmsLawsAudioEn ?: "")
                                    viewModel.speak(lawAudio, currentLang, "dgms_${viewModel.activeModuleId}")
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDgmsAudioPlaying) Color(0xFF1B3824) else Color(0xFFEFF6FF),
                            border = BorderStroke(1.dp, if (isDgmsAudioPlaying) Color(0xFF00FF88) else Color(0xFF93CCFF))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isDgmsAudioPlaying) Icons.Default.Close else Icons.Default.RecordVoiceOver,
                                    contentDescription = "Listen Law",
                                    tint = if (isDgmsAudioPlaying) Color(0xFF00FF88) else Color(0xFF006398),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (isDgmsAudioPlaying) {
                                        if (currentLang == AppLanguage.HINDI) "रोकें" else "Stop"
                                    } else {
                                        if (currentLang == AppLanguage.HINDI) "कानून सुनें" else "Listen Law"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDgmsAudioPlaying) Color(0xFF00FF88) else Color(0xFF006398)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    val dgmsLawText = if (currentLang == AppLanguage.HINDI) (sop?.dgmsLawsHi ?: "") else (sop?.dgmsLaws ?: "")
                    Text(dgmsLawText, fontSize = 12.sp, color = Color(0xFF131B2E), lineHeight = 17.sp)

                    Spacer(Modifier.height(8.dp))
                    // Key statutory thresholds chips
                    val thresholds = if (currentLang == AppLanguage.HINDI) (sop?.keyThresholdsHi ?: emptyList()) else (sop?.keyThresholds ?: emptyList())
                    thresholds.take(2).forEach { (metric, desc) ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        ) {
                            Row(Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(metric, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, modifier = Modifier.width(130.dp))
                                Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // 4. Practical Drill Checklist & Interactive Action
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.2.dp, Color(0xFFCBD5E1)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text(
                        if (currentLang == AppLanguage.HINDI) "प्रैक्टिकल सिमुलेशन चेकलिस्ट" else "PRACTICAL SIMULATION CHECKLIST",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(6.dp))

                    when (activeType) {
                        ARSimulationType.GAS_VENTILATION -> {
                            ChecklistItem(if (currentLang == AppLanguage.HINDI) "सहायक वेंटिलेशन रुकावट सत्यापित" else "Auxiliary ventilation stopping verified", true)
                            ChecklistItem(if (currentLang == AppLanguage.HINDI) "ज्वलनशील गैस डिटेक्टर कैलिब्रेट किया गया (<0.8%)" else "Flammable gas detector calibrated (<0.8% baseline)", true)
                            ChecklistItem(if (currentLang == AppLanguage.HINDI) "मीथेन गैस रिसाव चिन्हित और वाल्व बंद किया गया" else "Billowy gas fumes located & valve isolated", hazardIdentifiedLocally)
                        }
                        ARSimulationType.FIRE_EVACUATION -> {
                            ChecklistItem(if (currentLang == AppLanguage.HINDI) "अग्नि शमन अलार्म बीकन सक्रिय" else "Fire suppression alarm beacon triggered", true)
                            ChecklistItem(if (currentLang == AppLanguage.HINDI) "सुरक्षित रास्ता चुना गया (दायां मार्ग साफ)" else "Safe walkway route chosen (Right side clear)", true)
                            ChecklistItem(if (currentLang == AppLanguage.HINDI) "रिफ्यूज बे तक हरी चमकदार तीरों का अनुसरण किया गया" else "Dynamic green floor arrows followed to refuge bay", hazardIdentifiedLocally)
                        }
                        ARSimulationType.MACHINERY_GUARDING -> {
                            ChecklistItem(if (currentLang == AppLanguage.HINDI) "इमरजेंसी पुल-कॉर्ड ट्रिप स्विच का परीक्षण किया गया" else "Emergency pull-cord trip switch verified along walkway", true)
                            ChecklistItem(if (currentLang == AppLanguage.HINDI) "घूमने वाले ड्रम का पिंच-पॉइंट खतरा पहचाना गया" else "Rotating nip-point pinch hazard identified", true)
                            ChecklistItem(if (currentLang == AppLanguage.HINDI) "मास्टर लोटो पैडलॉक लगाया गया और शून्य ऊर्जा सत्यापित" else "Master LOTO padlock applied & zero-energy verified", hazardIdentifiedLocally)
                        }
                        ARSimulationType.ELECTRICAL_ISOLATION -> {
                            ChecklistItem(if (currentLang == AppLanguage.HINDI) "श्रेणी 4 आर्क-रेटेड फेस शील्ड और दस्ताने सत्यापित" else "Category 4 arc-rated face shield & dielectric gloves verified", true)
                            ChecklistItem(if (currentLang == AppLanguage.HINDI) "अपस्ट्रीम 3.3kV वैक्यूम सर्किट ब्रेकर खोला गया" else "Upstream 3.3kV vacuum circuit breaker opened", true)
                            ChecklistItem(if (currentLang == AppLanguage.HINDI) "अर्थ ग्राउंडिंग चाकू स्विच लगाया गया और डिस्चार्ज पूर्ण" else "Earth grounding knife switch engaged & busbars discharged", hazardIdentifiedLocally)
                        }
                        ARSimulationType.PPE_INSPECTION -> {
                            ChecklistItem(if (currentLang == AppLanguage.HINDI) "सेल्फ-रेस्क्यूअर सील और नमी संकेतक सत्यापित" else "Self-Rescuer hermetic seal & moisture indicator verified", true)
                            ChecklistItem(if (currentLang == AppLanguage.HINDI) "माइनर एलईडी कैप लैंप का 12 घंटे का परीक्षण" else "Miner LED cap lamp tested for 12hr continuous beam", true)
                            ChecklistItem(if (currentLang == AppLanguage.HINDI) "निषिद्ध सामग्री जमा की गई और हेलमेट स्ट्रैप लॉक" else "Contraband surrender clearance & helmet chin strap locked", hazardIdentifiedLocally)
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    if (!hazardIdentifiedLocally) {
                        Button(
                            onClick = {
                                hazardIdentifiedLocally = true
                                viewModel.recordHazardIdentified(activeType.name)
                                val msg = if (currentLang == AppLanguage.HINDI) "खतरा सफलतापूर्ण नियंत्रित कर लिया गया है।" else "Hazard isolated and neutralized."
                                viewModel.speak(msg, currentLang, "hazard_cleared")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (currentLang == AppLanguage.HINDI) "सुरक्षा क्रिया पूरी करें (अभ्यास पूर्ण)" else "Neutralize Hazard & Complete Drill", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 5. Post-Drill Next Actions Card (Study Manual & Quiz)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.2.dp, if (hazardIdentifiedLocally) Color(0xFF00B251) else Color(0xFFCBD5E1)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (hazardIdentifiedLocally) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (hazardIdentifiedLocally) Color(0xFF006E2F) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (hazardIdentifiedLocally) {
                                    if (currentLang == AppLanguage.HINDI) "प्रैक्टिकल अभ्यास पूर्ण! अब मूल्यांकन" else "Drill Completed! Next: Assessment"
                                } else {
                                    if (currentLang == AppLanguage.HINDI) "अध्ययन नियमावली व प्रश्नोत्तरी" else "Study Manual & Module Quiz"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF131B2E)
                            )
                            Text(
                                text = if (currentLang == AppLanguage.HINDI) "क्विज़ में बेहतर अंक पाने के लिए पहले नियमावली पढ़ें" else "Read manual before quiz for better scores",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Button 1: Read Manual for Better Marks
                        OutlinedButton(
                            onClick = { viewingSopInDrill = sop },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (currentLang == AppLanguage.HINDI) "नियमावली पढ़ें" else "Study Manual",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Button 2: Take Module Quiz Now
                        Button(
                            onClick = { navController.navigate("quiz") },
                            modifier = Modifier.weight(1.2f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (currentLang == AppLanguage.HINDI) "प्रश्नोत्तरी दें →" else "Take Quiz →",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
fun ChecklistItem(text: String, checked: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .border(1.dp, if (checked) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline, RoundedCornerShape(3.dp))
                .background(if (checked) MaterialTheme.colorScheme.tertiary else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            if (checked) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(13.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text,
            color = if (checked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

// ------------------- MULTI-QUESTION QUIZ SCREEN (ROOM SYNCED & BILINGUAL VOICE) -------------------
@Composable
fun QuizScreen(navController: NavController, viewModel: MainViewModel) {
    val questions = TrainingContentRepository.moduleQuestions[viewModel.activeModuleId]
        ?: TrainingContentRepository.moduleQuestions["1"]!!

    val currentLang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val activeTag by viewModel.activeSpeechTag.collectAsStateWithLifecycle()

    var currentIndex by remember { mutableStateOf(0) }
    var selectedOpt by remember { mutableStateOf<Int?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }
    var correctCount by remember { mutableStateOf(0) }
    var isQuizCompleted by remember { mutableStateOf(false) }

    val currentQuestion = questions.getOrElse(currentIndex) { questions.first() }

    val qText = if (currentLang == AppLanguage.HINDI && currentQuestion.questionHi.isNotBlank()) {
        currentQuestion.questionHi
    } else {
        currentQuestion.question
    }

    val optionsList = if (currentLang == AppLanguage.HINDI && currentQuestion.optionsHi.isNotEmpty()) {
        currentQuestion.optionsHi
    } else {
        currentQuestion.options
    }

    val expText = if (currentLang == AppLanguage.HINDI && currentQuestion.explanationHi.isNotBlank()) {
        currentQuestion.explanationHi
    } else {
        currentQuestion.explanation
    }

    val isQuestionAudioPlaying = isSpeaking && activeTag == "q_${currentIndex}"
    val isExplanationAudioPlaying = isSpeaking && activeTag == "exp_${currentIndex}"

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        if (!isQuizCompleted) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp)) {
                    // Header with Progress, Question Counter & Language Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                if (currentLang == AppLanguage.HINDI) "प्रश्न ${currentIndex + 1} / ${questions.size}" else "QUESTION ${currentIndex + 1} OF ${questions.size}",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                onClick = { viewModel.toggleLanguage() },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                            ) {
                                Text(
                                    text = if (currentLang == AppLanguage.HINDI) "हिन्दी" else "English",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Score: $correctCount / ${questions.size}",
                                color = MaterialTheme.colorScheme.tertiary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { (currentIndex + 1).toFloat() / questions.size.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.background
                    )

                    Spacer(Modifier.height(16.dp))

                    // Question Text & Audio Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = qText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 22.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (isQuestionAudioPlaying) {
                                    viewModel.stopSpeech()
                                } else {
                                    val optionsSpeech = optionsList.mapIndexed { i, opt ->
                                        "${('A' + i)}: $opt"
                                    }.joinToString(". ")
                                    val speechText = "$qText. $optionsSpeech"
                                    viewModel.speak(speechText, currentLang, "q_${currentIndex}")
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (isQuestionAudioPlaying) Color(0xFF1B3824) else MaterialTheme.colorScheme.surfaceVariant,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (isQuestionAudioPlaying) Icons.Default.Close else Icons.Default.VolumeUp,
                                contentDescription = "Listen Question Aloud",
                                tint = if (isQuestionAudioPlaying) Color(0xFF00FF88) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    optionsList.forEachIndexed { i, text ->
                        val isSelected = selectedOpt == i
                        val isCorrectOption = i == currentQuestion.correctIndex

                        val (borderColor, bgColor) = when {
                            isSubmitted && isCorrectOption -> Color(0xFF00FF88) to Color(0xFF00FF88).copy(alpha = 0.15f)
                            isSubmitted && isSelected && !isCorrectOption -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            isSelected -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            else -> MaterialTheme.colorScheme.outline to Color.Transparent
                        }

                        Surface(
                            onClick = {
                                if (!isSubmitted) selectedOpt = i
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, borderColor),
                            color = bgColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier
                                        .size(26.dp)
                                        .border(1.dp, borderColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        ('A' + i).toString(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected || (isSubmitted && isCorrectOption)) borderColor else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = text,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    // Educational Explanation Banner after submitting with Voice Audio
                    if (isSubmitted) {
                        Spacer(Modifier.height(16.dp))
                        val isCorrect = selectedOpt == currentQuestion.correctIndex
                        Surface(
                            color = if (isCorrect) Color(0xFF143020) else Color(0xFF3B1818),
                            border = BorderStroke(1.dp, if (isCorrect) Color(0xFF00FF88) else Color(0xFFFF6B6B)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = if (isCorrect) Color(0xFF00FF88) else Color(0xFFFF6B6B),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = if (isCorrect) {
                                                if (currentLang == AppLanguage.HINDI) "सही उत्तर — डीजीएमएस अनुपालन" else "CORRECT ANSWER — DGMS COMPLIANT"
                                            } else {
                                                if (currentLang == AppLanguage.HINDI) "गलत उत्तर — सुरक्षा खतरा" else "HAZARD DETECTED — NON-COMPLIANT"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isCorrect) Color(0xFF00FF88) else Color(0xFFFF6B6B)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            if (isExplanationAudioPlaying) {
                                                viewModel.stopSpeech()
                                            } else {
                                                val statusPrefix = if (isCorrect) {
                                                    if (currentLang == AppLanguage.HINDI) "सही उत्तर।" else "Correct answer."
                                                } else {
                                                    if (currentLang == AppLanguage.HINDI) "गलत उत्तर।" else "Incorrect answer."
                                                }
                                                viewModel.speak("$statusPrefix $expText", currentLang, "exp_${currentIndex}")
                                            }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isExplanationAudioPlaying) Icons.Default.Close else Icons.Default.VolumeUp,
                                            contentDescription = "Listen Explanation",
                                            tint = if (isExplanationAudioPlaying) Color(0xFF00FF88) else Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = expText,
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    if (!isSubmitted) {
                        Button(
                            onClick = {
                                if (selectedOpt != null) {
                                    isSubmitted = true
                                    val isCorrect = selectedOpt == currentQuestion.correctIndex
                                    if (isCorrect) {
                                        correctCount++
                                    }
                                    val feedback = if (isCorrect) {
                                        if (currentLang == AppLanguage.HINDI) "सही उत्तर! व्याख्या सुनें।" else "Correct answer! Well done."
                                    } else {
                                        if (currentLang == AppLanguage.HINDI) "गलत उत्तर। कृपया डीजीएमएस नियम देखें।" else "Incorrect answer. Review statutory protocol."
                                    }
                                    viewModel.speak(feedback, currentLang, "feedback_${currentIndex}")
                                }
                            },
                            enabled = selectedOpt != null,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black)
                        ) {
                            Text(if (currentLang == AppLanguage.HINDI) "उत्तर सत्यापित करें →" else "Confirm & Verify Answer →", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                if (currentIndex + 1 < questions.size) {
                                    currentIndex++
                                    selectedOpt = null
                                    isSubmitted = false
                                } else {
                                    isQuizCompleted = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentIndex + 1 < questions.size) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                                contentColor = Color.Black
                            )
                        ) {
                            Text(
                                text = if (currentIndex + 1 < questions.size) {
                                    if (currentLang == AppLanguage.HINDI) "अगला प्रश्न (${currentIndex + 2}/${questions.size}) →" else "Next Question (${currentIndex + 2}/${questions.size}) →"
                                } else {
                                    if (currentLang == AppLanguage.HINDI) "परिणाम देखें →" else "View Assessment Results →"
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            // Final Assessment Summary Card
            val finalScore = (correctCount * 100) / questions.size
            val passed = finalScore >= 70
            val isResultSpeaking = isSpeaking && activeTag == "quiz_result"

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, if (passed) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (passed) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (passed) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = if (passed) {
                            if (currentLang == AppLanguage.HINDI) "व्यावसायिक मूल्यांकन में उत्तीर्ण!" else "Vocational Assessment Passed"
                        } else {
                            if (currentLang == AppLanguage.HINDI) "पुनः मूल्यांकन की सिफारिश" else "Re-evaluation Recommended"
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (passed) {
                            if (currentLang == AppLanguage.HINDI) "आपने डीजीएमएस व्यावसायिक सुरक्षा मानकों को सफलतापूर्वक पूरा किया है।" else "You have satisfied DGMS vocational competency benchmarks."
                        } else {
                            if (currentLang == AppLanguage.HINDI) "स्कोर अनिवार्य सीमा से कम है। कृपया व्यावसायिक एसओपी की समीक्षा करें।" else "Score is below statutory threshold. Please review the Vocational SOP."
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(Modifier.height(20.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("FINAL SCORE", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                            Text("$finalScore%", fontSize = 38.sp, fontWeight = FontWeight.Bold, color = if (passed) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                if (currentLang == AppLanguage.HINDI) "${questions.size} में से $correctCount प्रश्न सही" else "$correctCount of ${questions.size} Questions Correct",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(8.dp))

                            val gradeText = when {
                                finalScore >= 90 -> if (currentLang == AppLanguage.HINDI) "ग्रेड A — उत्कृष्ट (डीजीएमएस अनुपालन)" else "GRADE A — HIGH DISTINCTION (DGMS COMPLIANT)"
                                finalScore >= 70 -> if (currentLang == AppLanguage.HINDI) "उत्तीर्ण — बुनियादी योग्यता प्राप्त" else "QUALIFIED — BASIC COMPETENCY ACHIEVED"
                                else -> if (currentLang == AppLanguage.HINDI) "पुनः प्रयास आवश्यक — एसओपी देखें" else "NEEDS RETEST — REVIEW VOCATIONAL SOP"
                            }
                            Surface(
                                color = if (passed) Color(0xFF143020) else Color(0xFF3B1818),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, if (passed) Color(0xFF00FF88) else Color(0xFFFF6B6B))
                            ) {
                                Text(
                                    gradeText,
                                    color = if (passed) Color(0xFF00FF88) else Color(0xFFFF6B6B),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Voice Output for Quiz Results Aloud
                    OutlinedButton(
                        onClick = {
                            if (isResultSpeaking) {
                                viewModel.stopSpeech()
                            } else {
                                val resultAnnouncement = if (currentLang == AppLanguage.HINDI) {
                                    "व्यावसायिक मूल्यांकन समाप्त। आपका स्कोर $finalScore प्रतिशत है। ${if (passed) "बधाई! आप उत्तीर्ण हो गए हैं और प्रमाणपत्र सुरक्षित हो गया है।" else "कृपया एसओपी गाइड की समीक्षा करें और पुनः प्रयास करें।"}"
                                } else {
                                    "Vocational assessment complete. Your score is $finalScore percent. ${if (passed) "Congratulations! You have passed and your certificate is ready." else "Please review the SOP and retake the assessment."}"
                                }
                                viewModel.speak(resultAnnouncement, currentLang, "quiz_result")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isResultSpeaking) Color(0xFF1B3824) else Color.Transparent
                        )
                    ) {
                        Icon(
                            imageVector = if (isResultSpeaking) Icons.Default.Close else Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = if (isResultSpeaking) Color(0xFF00FF88) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isResultSpeaking) {
                                if (currentLang == AppLanguage.HINDI) "ऑडियो बंद करें" else "Stop Audio"
                            } else {
                                if (currentLang == AppLanguage.HINDI) "परिणाम आवाज में सुनें" else "Read Results Aloud"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isResultSpeaking) Color(0xFF00FF88) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.completeModuleDrill(finalScore)
                            navController.navigate("cert")
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black)
                    ) {
                        Text(if (currentLang == AppLanguage.HINDI) "डेटाबेस में सहेजें और प्रमाणपत्र देखें →" else "Save to Room DB & View Certificate →", fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            currentIndex = 0
                            selectedOpt = null
                            isSubmitted = false
                            correctCount = 0
                            isQuizCompleted = false
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text(if (currentLang == AppLanguage.HINDI) "पुनः परीक्षा दें" else "Retake Assessment", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

// ------------------- VOCATIONAL TRAINING MANUAL & DGMS STUDY GUIDE DIALOG -------------------
@Composable
fun VocationalSopDialog(
    sop: VocationalSop,
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onStartQuiz: (() -> Unit)? = null
) {
    val currentLang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val activeTag by viewModel.activeSpeechTag.collectAsStateWithLifecycle()
    val isSopAudioPlaying = isSpeaking && activeTag == "sop_${sop.moduleId}"
    val isDgmsAudioPlaying = isSpeaking && activeTag == "sop_dgms_${sop.moduleId}"

    val title = if (currentLang == AppLanguage.HINDI) sop.titleHi else sop.title
    val summary = if (currentLang == AppLanguage.HINDI) sop.summaryHi else sop.summary
    val steps = if (currentLang == AppLanguage.HINDI) sop.sopStepsHi else sop.sopSteps
    val thresholds = if (currentLang == AppLanguage.HINDI) sop.keyThresholdsHi else sop.keyThresholds
    val dosAndDonts = if (currentLang == AppLanguage.HINDI) sop.dosAndDontsHi else sop.dosAndDonts
    val quizTips = if (currentLang == AppLanguage.HINDI) sop.manualQuizTipsHi else sop.manualQuizTips
    val emergencyCond = if (currentLang == AppLanguage.HINDI) sop.emergencyConditionHi else sop.emergencyCondition
    val emergencyStepsList = if (currentLang == AppLanguage.HINDI) sop.emergencyStepsHi else sop.emergencySteps
    val dgmsLaw = if (currentLang == AppLanguage.HINDI) sop.dgmsLawsHi else sop.dgmsLaws

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header with statutory tag, Language Switcher & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFFFDBCA),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFF9D4300))
                    ) {
                        Text(
                            if (currentLang == AppLanguage.HINDI) "व्यावसायिक प्रशिक्षण नियमावली व परीक्षा गाइड" else "TRAINING MANUAL & DGMS EXAM GUIDE",
                            color = Color(0xFF783200),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Language Switcher Pill inside SOP Dialog
                        Surface(
                            onClick = { viewModel.toggleLanguage() },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                text = if (currentLang == AppLanguage.HINDI) "हिन्दी" else "English",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF131B2E))
                Spacer(Modifier.height(3.dp))
                Text(sop.statutoryRef, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))

                // Bilingual Voice Output Player Bar
                Surface(
                    onClick = {
                        if (isSopAudioPlaying) {
                            viewModel.stopSpeech()
                        } else {
                            val sopStepsNarrated = steps.joinToString(". ")
                            val fullNarration = "$title. $summary. $sopStepsNarrated"
                            viewModel.speak(fullNarration, currentLang, "sop_${sop.moduleId}")
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSopAudioPlaying) Color(0xFF143020) else Color(0xFFEFF6FF),
                    border = BorderStroke(1.dp, if (isSopAudioPlaying) Color(0xFF00FF88) else Color(0xFF93CCFF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isSopAudioPlaying) Icons.Default.Close else Icons.Default.VolumeUp,
                                contentDescription = "Voice Guide",
                                tint = if (isSopAudioPlaying) Color(0xFF00FF88) else Color(0xFF006398),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isSopAudioPlaying) {
                                        if (currentLang == AppLanguage.HINDI) "आवाज में नियमावली चल रही है... (रोकने के लिए दबाएं)" else "Speaking Manual Aloud... (Tap to Stop)"
                                    } else {
                                        if (currentLang == AppLanguage.HINDI) "पूरी नियमावली आवाज में सुनें" else "Listen Complete Manual Aloud"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSopAudioPlaying) Color(0xFF00FF88) else Color(0xFF131B2E)
                                )
                                Text(
                                    text = if (currentLang == AppLanguage.HINDI) "डीजीएमएस अनुपालन वॉयस गाइड" else "DGMS Statutory Audio Guide",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = if (isSopAudioPlaying) "STOP" else "PLAY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSopAudioPlaying) Color(0xFF00FF88) else Color(0xFF006398)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                Divider(color = Color(0xFFCBD5E1))
                Spacer(Modifier.height(10.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // SECTION 1: High-Yield DGMS Study Points for Quiz
                    if (quizTips.isNotEmpty()) {
                        Surface(
                            color = Color(0xFFFFFBEB),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.2.dp, Color(0xFFF59E0B)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = if (currentLang == AppLanguage.HINDI) "क्विज़ में बेहतर अंकों के लिए महत्वपूर्ण परीक्षा नोट्स" else "High-Yield DGMS Study Points for Quiz",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF78350F)
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                quizTips.forEach { tip ->
                                    Row(Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                                        Text("• ", color = Color(0xFFB45309), fontWeight = FontWeight.Bold)
                                        Text(tip, fontSize = 12.sp, color = Color(0xFF451A03), lineHeight = 17.sp)
                                    }
                                }
                            }
                        }
                    }

                    // SECTION 2: Emergency Condition & Steps Worker Must Take
                    if (emergencyStepsList.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFFED7AA)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(
                                    text = if (currentLang == AppLanguage.HINDI) "आपातकालीन स्थिति में क्या कदम उठाएं" else "Steps Worker Must Take in this Condition",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF9A3412)
                                )
                                Spacer(Modifier.height(3.dp))
                                Text(emergencyCond, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2410C))
                                Spacer(Modifier.height(6.dp))
                                emergencyStepsList.forEachIndexed { idx, stp ->
                                    Row(Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                                        Text("${idx + 1}. ", fontWeight = FontWeight.Bold, color = Color(0xFF9A3412), fontSize = 12.sp)
                                        Text(stp, fontSize = 12.sp, color = Color(0xFF431407), lineHeight = 16.sp)
                                    }
                                }
                            }
                        }
                    }

                    // SECTION 3: DGMS Statutory Laws
                    if (dgmsLaw.isNotBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Gavel, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = if (currentLang == AppLanguage.HINDI) "डीजीएमएस वैधानिक कानून व नियम" else "DGMS Statutory Regulations",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF166534)
                                        )
                                    }

                                    Surface(
                                        onClick = {
                                            if (isDgmsAudioPlaying) {
                                                viewModel.stopSpeech()
                                            } else {
                                                val lawAudio = if (currentLang == AppLanguage.HINDI) (sop.dgmsLawsAudioHi ?: "") else (sop.dgmsLawsAudioEn ?: "")
                                                viewModel.speak(lawAudio, currentLang, "sop_dgms_${sop.moduleId}")
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isDgmsAudioPlaying) Color(0xFF1B3824) else Color(0xFFDCFCE7),
                                        border = BorderStroke(1.dp, if (isDgmsAudioPlaying) Color(0xFF00FF88) else Color(0xFF86EFAC))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (isDgmsAudioPlaying) Icons.Default.Close else Icons.Default.RecordVoiceOver,
                                                contentDescription = null,
                                                tint = if (isDgmsAudioPlaying) Color(0xFF00FF88) else Color(0xFF166534),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(Modifier.width(3.dp))
                                            Text(
                                                text = if (isDgmsAudioPlaying) {
                                                    if (currentLang == AppLanguage.HINDI) "रोकें" else "Stop"
                                                } else {
                                                    if (currentLang == AppLanguage.HINDI) "कानून सुनें" else "Listen Law"
                                                },
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDgmsAudioPlaying) Color(0xFF00FF88) else Color(0xFF166534)
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(dgmsLaw, fontSize = 12.sp, color = Color(0xFF14532D), lineHeight = 17.sp)
                            }
                        }
                    }

                    // SECTION 4: Executive Summary
                    Text(
                        if (currentLang == AppLanguage.HINDI) "कार्यकारी सारांश" else "Executive Summary",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF131B2E)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(summary, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                    Spacer(Modifier.height(16.dp))

                    // SECTION 5: Statutory Thresholds
                    Text(
                        if (currentLang == AppLanguage.HINDI) "वैधानिक सीमाएं और मानक" else "Statutory Thresholds & Parameters",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF131B2E)
                    )
                    Spacer(Modifier.height(8.dp))
                    thresholds.forEach { (metric, desc) ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(metric, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, modifier = Modifier.width(130.dp))
                                Text(desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    // SECTION 6: Step-by-Step Procedure
                    Text(
                        if (currentLang == AppLanguage.HINDI) "चरण-दर-चरण मानक संचालन प्रक्रिया" else "Step-by-Step Vocational Procedure (SOP)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF131B2E)
                    )
                    Spacer(Modifier.height(8.dp))
                    steps.forEach { step ->
                        Text(step, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(vertical = 3.dp), lineHeight = 18.sp)
                    }
                    Spacer(Modifier.height(16.dp))

                    // SECTION 7: DOs and DON'Ts
                    Text(
                        if (currentLang == AppLanguage.HINDI) "महत्वपूर्ण क्या करें और क्या न करें" else "Critical DOs and DON'Ts",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF131B2E)
                    )
                    Spacer(Modifier.height(8.dp))
                    dosAndDonts.forEach { (doText, dontText) ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Text("DO: ", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(doText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Top) {
                                Text("DONT: ", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(dontText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                if (onStartQuiz != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.2.dp, Color(0xFFCBD5E1))
                        ) {
                            Text(if (currentLang == AppLanguage.HINDI) "नियमावली बंद करें" else "Close Manual", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = onStartQuiz,
                            modifier = Modifier.weight(1.3f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (currentLang == AppLanguage.HINDI) "अब क्विज़ दें →" else "Take Quiz Now →", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (currentLang == AppLanguage.HINDI) "नियमावली समझी और स्वीकार की" else "Understood & Acknowledge Manual", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ------------------- CERTIFICATE SCREEN WITH BILINGUAL VOICE OUTPUT -------------------
@Composable
fun CertificateScreen(navController: NavController, viewModel: MainViewModel) {
    val modules by viewModel.modules.collectAsStateWithLifecycle()
    val activeMod = modules.find { it.id == viewModel.activeModuleId } ?: modules.firstOrNull()

    val currentLang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val activeTag by viewModel.activeSpeechTag.collectAsStateWithLifecycle()
    val isCertSpeaking = isSpeaking && activeTag == "cert_audio"

    val score = activeMod?.score ?: 94
    val sop = TrainingContentRepository.vocationalGuides[activeMod?.id ?: "1"]
    val title = if (currentLang == AppLanguage.HINDI && sop != null) sop.titleHi else (activeMod?.title ?: "Underground Gas & Ventilation")
    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Box(Modifier.background(MaterialTheme.colorScheme.background)) {
                Column(Modifier.padding(28.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("SURAKSHA AR", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Surface(
                            color = Color.Transparent,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("✓ ROOM PERSISTED", color = MaterialTheme.colorScheme.tertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        if (currentLang == AppLanguage.HINDI) "व्यावसायिक योग्यता प्रमाणपत्र" else "Certificate of Competency",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 30.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (currentLang == AppLanguage.HINDI) "खनन कर्मी ने डीजीएमएस (खान सुरक्षा महानिदेशालय) अनुपालन दिशानिर्देशों के तहत सिम्युलेटेड व्यावसायिक ड्रिल मानकों को सफलतापूर्वक पूरा किया है।"
                        else "Worker has satisfied simulated vocational drill parameters under DGMS (Directorate General of Mines Safety) compliance guidelines.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(16.dp))

                    // Voice Output for Certificate
                    OutlinedButton(
                        onClick = {
                            if (isCertSpeaking) {
                                viewModel.stopSpeech()
                            } else {
                                val certSpeech = if (currentLang == AppLanguage.HINDI) {
                                    "व्यावसायिक योग्यता प्रमाणपत्र। कर्मी ${viewModel.loggedInUser}। कर्मी आईडी ${viewModel.workerId}। ड्रिल: $title। योग्यता स्कोर $score प्रतिशत। खदान स्थल: ${viewModel.site}। खान सुरक्षा महानिदेशालय मानकों के अनुसार सत्यापित।"
                                } else {
                                    "Certificate of Competency. Worker ${viewModel.loggedInUser}, ID ${viewModel.workerId}. Drill: $title. Competency score $score percent. Mine Site: ${viewModel.site}. DGMS compliance verified."
                                }
                                viewModel.speak(certSpeech, currentLang, "cert_audio")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isCertSpeaking) Color(0xFF143020) else Color.Transparent
                        )
                    ) {
                        Icon(
                            imageVector = if (isCertSpeaking) Icons.Default.Close else Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = if (isCertSpeaking) Color(0xFF00FF88) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isCertSpeaking) {
                                if (currentLang == AppLanguage.HINDI) "आवाज बंद करें" else "Stop Audio"
                            } else if (currentLang == AppLanguage.HINDI) "प्रमाणपत्र विवरण सुनें"
                            else "Listen Certificate Aloud",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCertSpeaking) Color(0xFF00FF88) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    Divider(color = MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.height(16.dp))

                    val items = listOf(
                        (if (currentLang == AppLanguage.HINDI) "कर्मी" else "Worker") to (viewModel.loggedInUser ?: "Rajesh Kumar"),
                        (if (currentLang == AppLanguage.HINDI) "कर्मी आईडी" else "Worker ID") to (viewModel.workerId ?: "JH-MNR-00482"),
                        (if (currentLang == AppLanguage.HINDI) "पूर्ण ड्रिल" else "Completed Drill") to title,
                        (if (currentLang == AppLanguage.HINDI) "योग्यता स्कोर" else "Competency Score") to "$score%",
                        (if (currentLang == AppLanguage.HINDI) "खदान स्थल" else "Mine Site") to (viewModel.site ?: "Rajmahal Coal Mine"),
                        (if (currentLang == AppLanguage.HINDI) "दर्ज दिनांक" else "Date Recorded") to dateStr
                    )

                    items.chunked(2).forEach { row ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            row.forEach { (k, v) ->
                                Column(Modifier.weight(1f)) {
                                    Text(k.uppercase(), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                                    Text(v, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            "db_hash: room_sha256_88b2a0c4_${activeMod?.id ?: "1"}_dgms_verified",
                            color = Color(0xFF8FD6A8),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(10.dp),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                navController.navigate("dashboard") { popUpTo("landing") }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black)
        ) {
            Text(if (currentLang == AppLanguage.HINDI) "प्रगति डैशबोर्ड पर वापस जाएं" else "Back to Progress Dashboard", fontWeight = FontWeight.Bold)
        }
    }
}

// ------------------- ADMIN SCREEN -------------------
@Composable
fun AdminScreen(navController: NavController, viewModel: MainViewModel) {
    val currentLang by viewModel.currentLanguage.collectAsStateWithLifecycle()

    if (viewModel.currentRole != UserRole.ADMIN) {
        // Access Restricted Guard for Worker Accounts
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Restricted",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        if (currentLang == AppLanguage.HINDI) "एडमिन पैनल प्रतिबंधित" else "Admin Panel Restricted",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (currentLang == AppLanguage.HINDI)
                            "आप वर्तमान में खनन श्रमिक (${viewModel.loggedInUser ?: "श्रमिक"}) के रूप में लॉग इन हैं। सुरक्षा एडमिन पैनल केवल प्रमाणित डीजीएमएस सुरक्षा अधिकारियों और खान प्रबंधकों के लिए आरक्षित है।"
                        else
                            "You are currently signed in as Mining Worker (${viewModel.loggedInUser ?: "Worker"}). The Safety Admin Panel is strictly reserved for certified DGMS safety officers and mine managers.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { navController.navigate("login") },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black)
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.Black)
                        Spacer(Modifier.width(8.dp))
                        Text(if (currentLang == AppLanguage.HINDI) "प्रशासक के रूप में लॉग इन करें" else "Log In as Administrator", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { navController.navigate("dashboard") },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(if (currentLang == AppLanguage.HINDI) "प्रशिक्षण मॉड्यूल पर वापस जाएं" else "Return to Training Modules", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    } else {
        // Authenticated Admin Dashboard
        val workerRoster = remember(currentLang) {
            if (currentLang == AppLanguage.HINDI) {
                listOf(
                    Triple("राजेश कुमार (JH-00482)", "कार्य क्षेत्र 4 - ड्रिफ्ट 3", "5/5 ड्रिल • 94% स्कोर • अनुपालित"),
                    Triple("सुनीता सोरेन (JH-00519)", "कन्वेयर बेल्ट अनुभाग", "4/5 ड्रिल • 88% स्कोर • प्रगति पर"),
                    Triple("अमित हांसदा (JH-00311)", "3.3kV सबस्टेशन", "2/5 ड्रिल • चेतावनी: लोटो अलर्ट"),
                    Triple("मनोज महतो (JH-00644)", "हॉलेज शाफ्ट बी", "5/5 ड्रिल • 98% स्कोर • अनुपालित"),
                    Triple("विकाश रॉय (JH-00287)", "इंटेक वायु मार्ग", "3/5 ड्रिल • समीक्षा आवश्यक")
                )
            } else {
                listOf(
                    Triple("Rajesh Kumar (JH-00482)", "Face 4 - Drift 3", "5/5 Drills • 94% Score • COMPLIANT"),
                    Triple("Sunita Soren (JH-00519)", "Conveyor Belt Section", "4/5 Drills • 88% Score • IN PROGRESS"),
                    Triple("Amit Hansda (JH-00311)", "3.3kV Substation", "2/5 Drills • FLAGGED: LOTO Alert"),
                    Triple("Manoj Mahato (JH-00644)", "Haulage Shaft B", "5/5 Drills • 98% Score • COMPLIANT"),
                    Triple("Vikash Roy (JH-00287)", "Intake Air Drift", "3/5 Drills • REVIEW REQUIRED")
                )
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(if (currentLang == AppLanguage.HINDI) "खदान सुरक्षा एडमिन पैनल" else "Mine Safety Admin Panel", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "${viewModel.loggedInUser} • ${viewModel.adminId}",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Surface(
                        onClick = {
                            viewModel.switchRole(UserRole.WORKER)
                            navController.navigate("dashboard")
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Engineering, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.tertiary)
                            Spacer(Modifier.width(4.dp))
                            Text(if (currentLang == AppLanguage.HINDI) "श्रमिक मोड में बदलें" else "Switch to Worker", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.height(180.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { AdminStatBox("1,284", if (currentLang == AppLanguage.HINDI) "पंजीकृत कर्मी" else "Registered Workers") }
                    item { AdminStatBox("94%", if (currentLang == AppLanguage.HINDI) "ड्रिल पास दर" else "Drill Pass Rate") }
                    item { AdminStatBox("100%", if (currentLang == AppLanguage.HINDI) "डेटाबेस सिंक" else "Room DB Sync") }
                    item { AdminStatBox("0", if (currentLang == AppLanguage.HINDI) "कार्यस्थल घटनाएं" else "Physical Incidents") }
                }
                Spacer(Modifier.height(16.dp))
            }

            item {
                // Workforce Compliance Roster
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (currentLang == AppLanguage.HINDI) "श्रमिक सुरक्षा अनुपालन रजिस्टर" else "Worker Compliance Roster", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(if (currentLang == AppLanguage.HINDI) "5 सक्रिय रिकॉर्ड" else "5 Active Records", fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                        }
                        Spacer(Modifier.height(12.dp))

                        workerRoster.forEach { (name, location, status) ->
                            val isFlag = status.contains("FLAGGED") || status.contains("चेतावनी")
                            val isCompliant = status.contains("COMPLIANT") || status.contains("अनुपालित")
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(location, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Surface(
                                        color = if (isFlag) Color(0xFF4A1F1F) else if (isCompliant) Color(0xFF143020) else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, if (isFlag) MaterialTheme.colorScheme.error else if (isCompliant) Color(0xFF00FF88) else MaterialTheme.colorScheme.outline)
                                    ) {
                                        Text(
                                            text = if (isFlag) {
                                                if (currentLang == AppLanguage.HINDI) "चेतावनी" else "FLAGGED"
                                            } else if (isCompliant) {
                                                if (currentLang == AppLanguage.HINDI) "अनुपालित" else "COMPLIANT"
                                            } else {
                                                if (currentLang == AppLanguage.HINDI) "प्रगति पर" else "IN PROGRESS"
                                            },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isFlag) Color(0xFFFFA0A0) else if (isCompliant) Color(0xFF8FD6A8) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(if (currentLang == AppLanguage.HINDI) "लाइव एआर सिमुलेशन इवेंट लॉग" else "Live AR Simulation Event Log", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))

                        if (currentLang == AppLanguage.HINDI) {
                            AdminLogRow("JH-00482", "गैस एवं वेंटिलेशन", "गैस वाल्व 24 सेकंड में सुरक्षित रूप से बंद किया", isFlag = false)
                            AdminLogRow("JH-00482", "अग्नि एवं निकासी", "फर्श निकासी मार्ग सत्यापित", isFlag = false)
                            AdminLogRow("JH-00311", "कन्वेयर गार्ड", "लोटो सुरक्षा टैग छोड़ दिया गया", isFlag = true)
                            AdminLogRow("JH-00287", "गैस एवं वायु", "अलार्म सूचना में देरी", isFlag = true)
                        } else {
                            AdminLogRow("JH-00482", "Gas & Ventilation", "Fumes valve isolated in 24s", isFlag = false)
                            AdminLogRow("JH-00482", "Fire & Evacuation", "Floor path escape verified", isFlag = false)
                            AdminLogRow("JH-00311", "Conveyor Guard", "Lock-out tag skipped", isFlag = true)
                            AdminLogRow("JH-00287", "Gas & Vent", "Delayed alarm call", isFlag = true)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStatBox(value: String, label: String) {
    Surface(color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shape = RoundedCornerShape(8.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AdminLogRow(id: String, module: String, issue: String, isFlag: Boolean) {
    Column {
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(id, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(module, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                issue,
                fontSize = 12.sp,
                color = if (isFlag) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isFlag) FontWeight.Bold else FontWeight.Normal
            )
        }
        Divider(color = MaterialTheme.colorScheme.outline)
    }
}

// ------------------- CAMERAX PREVIEW -------------------
@Composable
fun CameraPreview() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            val executor = ContextCompat.getMainExecutor(ctx)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview
                    )
                } catch (exc: Exception) {
                    // Handled gracefully in emulator / headless environment
                }
            }, executor)
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

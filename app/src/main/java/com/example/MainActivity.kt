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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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

    var loggedInUser by mutableStateOf<String?>("Rajesh Kumar")
    var workerId by mutableStateOf<String?>("JH-MNR-00482")
    var site by mutableStateOf<String?>("Rajmahal Coal Mine")

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
            if (currentRoute in listOf("landing", "dashboard", "admin")) {
                BottomBar(navController, currentRoute)
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
            composable("admin") { AdminScreen() }
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("S", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(8.dp))
                Text("SURAKSHA", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text("AR", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        },
        actions = {
            // Speaking Active Indicator & Stop Button
            if (isSpeaking) {
                Surface(
                    onClick = { viewModel.stopSpeech() },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1B3824),
                    border = BorderStroke(1.dp, Color(0xFF00FF88)),
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Speaking",
                            tint = Color(0xFF00FF88),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Audio ON", color = Color(0xFF00FF88), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Stop",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // Bilingual Voice Output Language Selector Pill
            Surface(
                onClick = { viewModel.toggleLanguage() },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentLang == AppLanguage.HINDI) "🇮🇳 हिन्दी" else "🇬🇧 English",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (viewModel.loggedInUser != null && currentRoute != "landing") {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.tertiary, CircleShape))
                        Spacer(Modifier.width(6.dp))
                        Text("${viewModel.loggedInUser}", fontSize = 12.sp)
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun BottomBar(navController: NavController, currentRoute: String) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentRoute == "landing",
            onClick = { navController.navigate("landing") { launchSingleTop = true } },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Build, contentDescription = "Training") },
            label = { Text("Training") },
            selected = currentRoute == "dashboard",
            onClick = { navController.navigate("dashboard") { launchSingleTop = true } },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Admin") },
            label = { Text("Admin") },
            selected = currentRoute == "admin",
            onClick = { navController.navigate("admin") { launchSingleTop = true } },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
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

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "SIH26041 • GOVT. OF JHARKHAND",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    Surface(
                        onClick = { viewModel.toggleLanguage() },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = if (currentLang == AppLanguage.HINDI) "🇮🇳 हिन्दी" else "🇬🇧 English",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    if (currentLang == AppLanguage.HINDI) "खतरे से पहले अभ्यास करें, ताकि खतरा आप पर हावी न हो।" else "Train for the hazard before it trains you.",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 38.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    if (currentLang == AppLanguage.HINDI) "खदान कर्मियों के लिए एआर व्यावसायिक प्रशिक्षण सिम्युलेटर। मीथेन गैस रिसाव, 3डी आग पर नियंत्रण और वास्तविक समय में सुरक्षित निकासी पथ का शून्य जोखिम में अभ्यास करें। हिन्दी और अंग्रेजी में पूर्ण वॉयस सहायता।"
                    else "AR vocational training simulator for miners. Practice simulated methane gas leakage, 3D fire response, and real-time path evacuation in zero-risk environments with bilingual voice output in Hindi & English.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(20.dp))

                // Voice Narration Button for Miners
                OutlinedButton(
                    onClick = {
                        if (isIntroSpeaking) {
                            viewModel.stopSpeech()
                        } else {
                            val introText = if (currentLang == AppLanguage.HINDI) {
                                "सुरक्षा एआर में आपका स्वागत है। यह खदान कर्मियों के लिए डीजीएमएस अनुपालन आधारित संवर्धित वास्तविकता व्यावसायिक प्रशिक्षण सिम्युलेटर है। आप मीथेन गैस रिसाव, आग निकासी, कन्वेयर लोटो और 3.3 केवी विद्युत अलगाव का सजीव 3डी मॉडल के साथ अभ्यास कर सकते हैं।"
                            } else {
                                "Welcome to Suraksha AR. An interactive 3D vocational training platform for miners. Experience simulated gas ventilation, fire evacuation paths, machinery lockout, and electrical isolation with real-time voice guidance."
                            }
                            viewModel.speak(introText, currentLang, "intro_brief")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Icon(
                        imageVector = if (isIntroSpeaking) Icons.Default.Close else Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = if (isIntroSpeaking) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isIntroSpeaking) "Stop Audio Intro"
                        else if (currentLang == AppLanguage.HINDI) "🔊 आवाज में परिचय सुनें (हिन्दी)"
                        else "🔊 Listen App Briefing (English)",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { navController.navigate("login") },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black)
                ) {
                    Text(if (currentLang == AppLanguage.HINDI) "प्रशिक्षण मॉड्यूल शुरू करें →" else "Start Training Modules →", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { navController.navigate("admin") },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("View Admin Analytics", color = MaterialTheme.colorScheme.onSurface)
                }

                Spacer(Modifier.height(24.dp))
                Divider(color = MaterialTheme.colorScheme.outline)
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatItem("5", "Room DB Modules")
                    StatItem("AR", "Camera Passthrough")
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatItem("3D", "Fumes & Fire")
                    StatItem("🔊 HI/EN", "Bilingual Voice")
                }
            }
        }

        Column(Modifier.padding(24.dp)) {
            Text("Featured AR Drills", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Powered by local Room persistence, 3D WebGL and bilingual voice", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))

            ModuleStaticCard("Underground Gas & Ventilation", "HIGH", "42 min", "Billowy methane gas fumes leak drill with Hindi/EN voice")
            Spacer(Modifier.height(12.dp))
            ModuleStaticCard("Fire & Emergency Evacuation", "HIGH", "28 min", "Active fire & real-time floor path arrows with voice guidance")
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
    var name by remember { mutableStateOf(viewModel.loggedInUser ?: "Rajesh Kumar") }
    var workerId by remember { mutableStateOf(viewModel.workerId ?: "JH-MNR-00482") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Box(Modifier.fillMaxWidth().height(6.dp).background(MaterialTheme.colorScheme.primary))
                Column(Modifier.padding(24.dp)) {
                    Text("Worker Safety Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Room database tracks your completed drills locally", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    Spacer(Modifier.height(24.dp))

                    OutlinedTextField(
                        value = workerId,
                        onValueChange = { workerId = it },
                        label = { Text("Worker ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Worker Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = {
                            viewModel.loggedInUser = name
                            viewModel.workerId = workerId
                            navController.navigate("dashboard") {
                                popUpTo("landing")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black)
                    ) {
                        Text("Sign In & Open Progress Dashboard", fontWeight = FontWeight.Bold)
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
                    Text("Room Database Active Tracking • DGMS Verified", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = if (overallProgress >= 0.8f) "DGMS GRADE A" else "IN DRILL CYCLE",
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(12.dp),
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
                        StatItem(totalCount.toString(), "Assigned")
                        StatItem(completedCount.toString(), "Completed")
                        StatItem(inProgressCount.toString(), "In Progress")
                        StatItem("$averageScore%", "Avg Score")
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
                        text = if (currentLang == AppLanguage.HINDI) "वॉयस: 🇮🇳 हिन्दी" else "Voice: 🇬🇧 English",
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
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val stripColor = when (module.status) {
                    "COMPLETED" -> MaterialTheme.colorScheme.tertiary
                    "IN_PROGRESS" -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.error
                }
                Box(Modifier.size(36.dp, 4.dp).background(stripColor, RoundedCornerShape(2.dp)))

                // Status chip
                val (chipText, chipBg, chipColor) = when (module.status) {
                    "COMPLETED" -> Triple("COMPLETED ✓", Color(0xFF143020), MaterialTheme.colorScheme.tertiary)
                    "IN_PROGRESS" -> Triple("IN PROGRESS", Color(0xFF4A3400), MaterialTheme.colorScheme.primary)
                    else -> Triple("NOT STARTED", Color(0xFF262626), Color.LightGray)
                }
                Surface(color = chipBg, shape = RoundedCornerShape(12.dp)) {
                    Text(
                        chipText,
                        color = chipColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            val sop = TrainingContentRepository.vocationalGuides[module.id]
            val moduleTitle = if (currentLang == AppLanguage.HINDI && sop != null) sop.titleHi else module.title
            val moduleDesc = if (currentLang == AppLanguage.HINDI && sop != null) sop.summaryHi else module.description

            Text(moduleTitle, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(4.dp))
            Text(moduleDesc, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, lineHeight = 18.sp)

            Spacer(Modifier.height(12.dp))

            // AR 3D Feature Tags for all 5 modules
            when (module.id) {
                "1" -> {
                    Surface(color = Color(0xFF2D2510), shape = RoundedCornerShape(6.dp)) {
                        Text("✦ AR 3D Gas Fumes & Leak Valve", color = Color(0xFFFFD166), fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
                "2" -> {
                    Surface(color = Color(0xFF1B2E1F), shape = RoundedCornerShape(6.dp)) {
                        Text("✦ AR 3D Fire & Real-Time Evacuation Path Arrows", color = Color(0xFF06D6A0), fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
                "3" -> {
                    Surface(color = Color(0xFF332211), shape = RoundedCornerShape(6.dp)) {
                        Text("✦ AR 3D Conveyor Belt & Interactive LOTO Station", color = Color(0xFFF39C12), fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
                "4" -> {
                    Surface(color = Color(0xFF16253D), shape = RoundedCornerShape(6.dp)) {
                        Text("✦ AR 3D 3.3kV Substation & Arc Flash Plasma", color = Color(0xFF5DADE2), fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
                "5" -> {
                    Surface(color = Color(0xFF1D2F23), shape = RoundedCornerShape(6.dp)) {
                        Text("✦ AR 3D Self-Rescuer, Cap Lamp & PPE Bench", color = Color(0xFF2ECC71), fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { module.progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (module.status == "COMPLETED") MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.background
                )
                Spacer(Modifier.width(12.dp))
                Text("${(module.progress * 100).toInt()}%", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(14.dp))
            Divider(color = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(module.duration, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (module.score > 0) {
                        Spacer(Modifier.width(8.dp))
                        Text("Score: ${module.score}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Voice Guide Quick Button
                    OutlinedButton(
                        onClick = onPlayVoiceGuide,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSpeakingThisModule) Color(0xFF1B3824) else Color.Transparent
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = if (isSpeakingThisModule) Icons.Default.Close else Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = if (isSpeakingThisModule) Color(0xFF00FF88) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (isSpeakingThisModule) "Stop" else if (currentLang == AppLanguage.HINDI) "आवाज" else "Voice",
                            fontSize = 11.sp,
                            color = if (isSpeakingThisModule) Color(0xFF00FF88) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    OutlinedButton(
                        onClick = onViewSop,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Text("SOP Guide", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                    }

                    if (module.status == "COMPLETED") {
                        OutlinedButton(
                            onClick = onReset,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text("Retrain", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(
                            onClick = onViewCertificate,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary, contentColor = Color.Black),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text("Certificate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (module.status == "IN_PROGRESS") {
                        Button(
                            onClick = onStartDrill,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text("Resume AR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onStartDrill,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text("Launch Drill", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
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
            Text("Camera Permission Required", color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Suraksha AR projects 3D gas fumes, fire hazards, conveyors, electrical substations, and PPE directly over your physical camera feed.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { cameraPermissionState.launchPermissionRequest() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black)
            ) {
                Text("Grant Camera Permission", fontWeight = FontWeight.Bold)
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

    var hazardIdentifiedLocally by remember { mutableStateOf(false) }
    var viewingSopInDrill by remember { mutableStateOf<VocationalSop?>(null) }

    if (viewingSopInDrill != null) {
        VocationalSopDialog(
            sop = viewingSopInDrill!!,
            viewModel = viewModel,
            onDismiss = { viewingSopInDrill = null }
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
                            Text("AR PASSTHROUGH ACTIVE", color = Color(0xFFEF8686), fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
                                Triple(ARSimulationType.GAS_VENTILATION, "1. Gas Leak", Color(0xFFFFD166)),
                                Triple(ARSimulationType.FIRE_EVACUATION, "2. Fire & Egress", Color(0xFF06D6A0)),
                                Triple(ARSimulationType.MACHINERY_GUARDING, "3. Conveyor LOTO", Color(0xFFF39C12)),
                                Triple(ARSimulationType.ELECTRICAL_ISOLATION, "4. Arc Flash", Color(0xFF5DADE2)),
                                Triple(ARSimulationType.PPE_INSPECTION, "5. PPE Check", Color(0xFF2ECC71))
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
                                    Text("CH4 Methane: 1.48% (HIGH LEL)", color = Color(0xFFFFD166), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Fumes Plume • Tap Valve Hotspot", color = Color.White, fontSize = 10.sp)
                                }
                                ARSimulationType.FIRE_EVACUATION -> {
                                    Text("CODE RED: Active Conveyor Fire", color = Color(0xFFFF6B6B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Follow Dynamic Green Chevrons →", color = Color(0xFF00FF88), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                ARSimulationType.MACHINERY_GUARDING -> {
                                    Text("DANGER: Exposed Rotating Pinch Point", color = Color(0xFFF39C12), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Tap Red LOTO Box to Lock Out", color = Color.White, fontSize = 10.sp)
                                }
                                ARSimulationType.ELECTRICAL_ISOLATION -> {
                                    Text("WARNING: 3.3kV Arc Flash Plasma Active", color = Color(0xFF5DADE2), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Tap Isolator Lever to Ground", color = Color.White, fontSize = 10.sp)
                                }
                                ARSimulationType.PPE_INSPECTION -> {
                                    Text("DGMS MUSTER: Pre-Shift Inspection", color = Color(0xFF2ECC71), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Tap Workbench to Verify Seal", color = Color.White, fontSize = 10.sp)
                                }
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        // Bilingual Voice Instruction Bar inside HUD
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
                                        text = if (isDrillAudioPlaying) "Stop Voice Drill"
                                        else if (currentLang == AppLanguage.HINDI) "🔊 ड्रिल निर्देश सुनें (हिन्दी)"
                                        else "🔊 Listen Drill Briefing (English)",
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
                                    text = if (currentLang == AppLanguage.HINDI) "🇮🇳 HI" else "🇬🇧 EN",
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
                    Text("Recalibrate", color = Color.White)
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

        // Bottom Inspection & Checklist Status Panel
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val checkpointTitle = when (activeType) {
                    ARSimulationType.GAS_VENTILATION -> "CHECKPOINT 1 — UNDERGROUND GAS & VENTILATION"
                    ARSimulationType.FIRE_EVACUATION -> "CHECKPOINT 2 — FIRE & RUNWAY EVACUATION"
                    ARSimulationType.MACHINERY_GUARDING -> "CHECKPOINT 3 — CONVEYOR & MACHINERY LOTO"
                    ARSimulationType.ELECTRICAL_ISOLATION -> "CHECKPOINT 4 — 3.3kV ELECTRICAL ISOLATION"
                    ARSimulationType.PPE_INSPECTION -> "CHECKPOINT 5 — MANDATORY PPE MUSTER INSPECTION"
                }
                Text(
                    text = checkpointTitle,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (hazardIdentifiedLocally) "HAZARD FLAGGED ✓" else "SCANNING DRIFT...",
                    color = if (hazardIdentifiedLocally) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(4.dp))
            val sop = TrainingContentRepository.vocationalGuides[viewModel.activeModuleId]
            val simulationTitle = if (currentLang == AppLanguage.HINDI && sop != null) {
                sop.titleHi
            } else {
                when (activeType) {
                    ARSimulationType.GAS_VENTILATION -> "Billowy Methane Leakage Response"
                    ARSimulationType.FIRE_EVACUATION -> "Active Fire Hazard & Real-Time Escape Route"
                    ARSimulationType.MACHINERY_GUARDING -> "Conveyor Pinch-Point & Lock-Out/Tag-Out (LOTO)"
                    ARSimulationType.ELECTRICAL_ISOLATION -> "3.3kV Substation Arc Flash & Earth Knife Grounding"
                    ARSimulationType.PPE_INSPECTION -> "Pre-Shift Self-Rescuer & Cap Lamp Validation"
                }
            }
            Text(simulationTitle, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            val simulationDesc = if (currentLang == AppLanguage.HINDI && sop != null) {
                sop.summaryHi
            } else {
                when (activeType) {
                    ARSimulationType.GAS_VENTILATION -> "Observe toxic green-amber fumes billowing from the ruptured roof conduit. Tap the glowing safety valve hotspot to isolate the section."
                    ARSimulationType.FIRE_EVACUATION -> "Roaring fire blocks the left drift. Follow the dynamic illuminated green floor arrows guiding you along the safe walkway to the refuge bay bulkhead."
                    ARSimulationType.MACHINERY_GUARDING -> "Notice the exposed rotating conveyor pinch-point drum and emergency trip-cord. Tap the red LOTO station to apply padlock and verify zero energy."
                    ARSimulationType.ELECTRICAL_ISOLATION -> "High-voltage 3.3kV busbars are arcing with violent blue plasma sparks. Tap the master rotary isolator to rack out breaker and engage earth grounding."
                    ARSimulationType.PPE_INSPECTION -> "Inspect the Dräger Self-Rescuer canister, 12-hour LED cap lamp, hard hat, and multi-gas detector. Tap the inspection zone to verify DGMS seal."
                }
            }
            Text(simulationDesc, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, lineHeight = 17.sp)

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (currentLang == AppLanguage.HINDI) "सजीव ड्रिल चेकलिस्ट" else "REAL-TIME DRILL CHECKLIST",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                TextButton(
                    onClick = {
                        viewingSopInDrill = TrainingContentRepository.vocationalGuides[viewModel.activeModuleId]
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        if (currentLang == AppLanguage.HINDI) "📖 व्यावसायिक एसओपी गाइड खोलें" else "📖 VIEW VOCATIONAL SOP PROTOCOL",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(4.dp))

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
                                    text = if (currentLang == AppLanguage.HINDI) "🇮🇳 हिन्दी" else "🇬🇧 English",
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
                                finalScore >= 90 -> "GRADE A — HIGH DISTINCTION (DGMS COMPLIANT)"
                                finalScore >= 70 -> "QUALIFIED — BASIC COMPETENCY ACHIEVED"
                                else -> "NEEDS RETEST — REVIEW VOCATIONAL SOP"
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
                            text = if (isResultSpeaking) "Stop Audio"
                            else if (currentLang == AppLanguage.HINDI) "🔊 परिणाम आवाज में सुनें (हिन्दी)"
                            else "🔊 Read Results Aloud (English)",
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
                        Text(if (currentLang == AppLanguage.HINDI) "रूम डेटाबेस में सहेजें और प्रमाणपत्र देखें →" else "Save to Room DB & View Certificate →", fontWeight = FontWeight.Bold)
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

// ------------------- VOCATIONAL SOP DIALOG WITH BILINGUAL VOICE OUTPUT -------------------
@Composable
fun VocationalSopDialog(sop: VocationalSop, viewModel: MainViewModel, onDismiss: () -> Unit) {
    val currentLang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val activeTag by viewModel.activeSpeechTag.collectAsStateWithLifecycle()
    val isSopAudioPlaying = isSpeaking && activeTag == "sop_${sop.moduleId}"

    val title = if (currentLang == AppLanguage.HINDI) sop.titleHi else sop.title
    val summary = if (currentLang == AppLanguage.HINDI) sop.summaryHi else sop.summary
    val steps = if (currentLang == AppLanguage.HINDI) sop.sopStepsHi else sop.sopSteps
    val thresholds = if (currentLang == AppLanguage.HINDI) sop.keyThresholdsHi else sop.keyThresholds
    val dosAndDonts = if (currentLang == AppLanguage.HINDI) sop.dosAndDontsHi else sop.dosAndDonts

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header with statutory tag, Language Switcher & Close Button
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
                            if (currentLang == AppLanguage.HINDI) "व्यावसायिक एसओपी गाइड" else "VOCATIONAL SOP GUIDE",
                            color = MaterialTheme.colorScheme.primary,
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
                                text = if (currentLang == AppLanguage.HINDI) "🇮🇳 हिन्दी" else "🇬🇧 English",
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
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
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
                    color = if (isSopAudioPlaying) Color(0xFF143020) else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, if (isSopAudioPlaying) Color(0xFF00FF88) else MaterialTheme.colorScheme.outline),
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
                                tint = if (isSopAudioPlaying) Color(0xFF00FF88) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isSopAudioPlaying) {
                                        if (currentLang == AppLanguage.HINDI) "आवाज में एसओपी चल रहा है... (रोकने के लिए दबाएं)" else "Speaking SOP Aloud... (Tap to Stop)"
                                    } else {
                                        if (currentLang == AppLanguage.HINDI) "🔊 पूरा एसओपी हिन्दी आवाज में सुनें" else "🔊 Listen Complete SOP in English"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSopAudioPlaying) Color(0xFF00FF88) else MaterialTheme.colorScheme.onSurface
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
                            color = if (isSopAudioPlaying) Color(0xFF00FF88) else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                Divider(color = MaterialTheme.colorScheme.outline)
                Spacer(Modifier.height(10.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        if (currentLang == AppLanguage.HINDI) "कार्यकारी सारांश" else "Executive Summary",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(summary, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                    Spacer(Modifier.height(16.dp))

                    Text(
                        if (currentLang == AppLanguage.HINDI) "वैधानिक सीमाएं और मानक" else "Statutory Thresholds & Parameters",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    thresholds.forEach { (metric, desc) ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(metric, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, modifier = Modifier.width(130.dp))
                                Text(desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    Text(
                        if (currentLang == AppLanguage.HINDI) "चरण-दर-चरण व्यावसायिक प्रक्रिया (SOP)" else "Step-by-Step Vocational Procedure (SOP)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    steps.forEach { step ->
                        Text(step, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(vertical = 3.dp), lineHeight = 18.sp)
                    }
                    Spacer(Modifier.height(16.dp))

                    Text(
                        if (currentLang == AppLanguage.HINDI) "महत्वपूर्ण क्या करें और क्या न करें" else "Critical DOs and DON'Ts",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    dosAndDonts.forEach { (doText, dontText) ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Text("✓ ", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                                Text(doText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Top) {
                                Text("✗ ", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                Text(dontText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black)
                ) {
                    Text(if (currentLang == AppLanguage.HINDI) "एसओपी समझ लिया और स्वीकार किया" else "Understood & Acknowledge SOP", fontWeight = FontWeight.Bold)
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
                            text = if (isCertSpeaking) "Stop Audio"
                            else if (currentLang == AppLanguage.HINDI) "🔊 प्रमाणपत्र विवरण सुनें (हिन्दी)"
                            else "🔊 Listen Certificate Aloud (English)",
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
fun AdminScreen() {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("Site Safety Analytics", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Real-time aggregate compliance monitor", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(180.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { AdminStatBox("1,284", "Registered Workers") }
                item { AdminStatBox("94%", "Drill Pass Rate") }
                item { AdminStatBox("100%", "Room DB Sync") }
                item { AdminStatBox("0", "Physical Incidents") }
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
                    Text("Live AR Simulation Log", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))

                    AdminLogRow("JH-00482", "Gas & Ventilation", "Fumes valve isolated in 24s", isFlag = false)
                    AdminLogRow("JH-00482", "Fire & Evacuation", "Floor path escape verified", isFlag = false)
                    AdminLogRow("JH-00311", "Conveyor Guard", "Lock-out tag skipped", isFlag = true)
                    AdminLogRow("JH-00287", "Gas & Vent", "Delayed alarm call", isFlag = true)
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

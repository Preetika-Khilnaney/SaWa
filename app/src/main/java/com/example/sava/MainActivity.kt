package com.example.sava

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sava.auth.EmailLinkSessionStore
import com.example.sava.ui.screens.*
import com.example.sava.ui.theme.adaptive
import com.example.sava.ui.theme.rememberAdaptiveUi
import com.example.sava.ui.theme.SavaTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private var incomingEmailLink = mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        incomingEmailLink.value = intent?.data
        enableEdgeToEdge()
        setContent {
            SavaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SavaApp(
                        incomingEmailLink = incomingEmailLink.value,
                        onEmailLinkHandled = { incomingEmailLink.value = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        incomingEmailLink.value = intent.data
    }
}

private data class BottomDestination(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun SavaApp(
    incomingEmailLink: Uri? = null,
    onEmailLinkHandled: () -> Unit = {}
) {
    val navController = rememberNavController()
    val auth = remember { FirebaseAuth.getInstance() }
    val context = androidx.compose.ui.platform.LocalContext.current
    var pendingEmailLink by remember { mutableStateOf<String?>(null) }
    var postRegistrationEmail by remember { mutableStateOf<String?>(null) }
    val verifiedEmail = EmailLinkSessionStore.getVerifiedEmail(context)
    val startDestination = when {
        incomingEmailLink != null -> "email_link_callback"
        !verifiedEmail.isNullOrBlank() -> "register"
        auth.currentUser != null -> "home"
        else -> "splash"
    }
    val bottomDestinations = listOf(
        BottomDestination("home", "Home", Icons.Default.Home),
        BottomDestination("money_mastery", "Money Mastery", Icons.Default.Star),
        BottomDestination("calendar", "Calendar", Icons.Default.DateRange),
        BottomDestination("your_plans", "Your Plans", Icons.Default.List),
        BottomDestination("profile", "Profile", Icons.Default.Person)
    )
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val authlessRoutes = setOf(
        "splash",
        "user_gate",
        "register",
        "email_login",
        "email_link_callback"
    )
    val showBottomBar = currentRoute !in authlessRoutes

    fun completeEmailLinkSignIn(email: String, emailLink: String) {
        auth.signInWithEmailLink(email, emailLink)
            .addOnSuccessListener { result ->
                val verifiedEmail = result.user?.email ?: email
                EmailLinkSessionStore.markVerifiedEmail(context, verifiedEmail)
                EmailLinkSessionStore.clearPendingEmail(context)
                pendingEmailLink = null
                navController.navigate("register") {
                    popUpTo("email_link_callback") { inclusive = true }
                    launchSingleTop = true
                }
                Toast.makeText(
                    context,
                    "Email verified successfully. You can finish creating your profile now.",
                    Toast.LENGTH_LONG
                ).show()
                onEmailLinkHandled()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    context,
                    exception.message ?: "We couldn't verify this email link.",
                    Toast.LENGTH_LONG
                ).show()
                onEmailLinkHandled()
            }
    }

    LaunchedEffect(incomingEmailLink?.toString()) {
        val emailLink = incomingEmailLink?.toString() ?: return@LaunchedEffect
        if (!auth.isSignInWithEmailLink(emailLink)) {
            return@LaunchedEffect
        }

        val pendingEmail = EmailLinkSessionStore.getPendingEmail(context)
        if (pendingEmail.isNullOrBlank()) {
            pendingEmailLink = emailLink
            navController.navigate("email_link_callback") {
                launchSingleTop = true
            }
            onEmailLinkHandled()
            return@LaunchedEffect
        }

        completeEmailLinkSignIn(pendingEmail, emailLink)
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                CustomBottomBar(
                    destinations = bottomDestinations,
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (route == "home") {
                            navController.navigate("home") {
                                popUpTo(navController.graph.id) {
                                    saveState = false
                                }
                                launchSingleTop = true
                                restoreState = false
                            }
                        } else {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        val screenTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 14.dp
        val screenBottomPadding = if (showBottomBar) {
            innerPadding.calculateBottomPadding() + 24.dp
        } else {
            0.dp
        }

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize()
        ) {
            composable("splash") {
                Surface(modifier = Modifier.padding(top = screenTopPadding)) {
                    SplashScreen(onNext = {
                        navController.navigate("user_gate") {
                            popUpTo("splash") { inclusive = true }
                        }
                    })
                }
            }
            composable("user_gate") {
                Surface(modifier = Modifier.padding(top = screenTopPadding)) {
                    UserGateScreen(
                        onRegister = { navController.navigate("register") },
                        onLogin = { navController.navigate("email_login") },
                        onGoogleLoginSuccess = {
                            navController.navigate("home") {
                                popUpTo("user_gate") { inclusive = true }
                            }
                        }
                    )
                }
            }
            composable("register") {
                Surface(modifier = Modifier.padding(top = screenTopPadding)) {
                    RegistrationScreen(
                        onSuccess = { registeredEmail ->
                            postRegistrationEmail = registeredEmail
                            navController.navigate("home") {
                                popUpTo("register") { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onBack = {
                            auth.signOut()
                            EmailLinkSessionStore.clearAll(context)
                            navController.navigate("user_gate") {
                                popUpTo(navController.graph.id) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
            composable("email_login") {
                Surface(modifier = Modifier.padding(top = screenTopPadding)) {
                    EmailLoginScreen(
                        onLoginSuccess = {
                            navController.navigate("home") {
                                popUpTo("user_gate") { inclusive = true }
                            }
                        },
                        initialEmail = postRegistrationEmail,
                        onBack = { navController.popBackStack() },
                        onSignUp = { navController.navigate("register") }
                    )
                }
            }
            composable("email_link_callback") {
                Surface(modifier = Modifier.padding(top = screenTopPadding)) {
                    EmailLinkCallbackScreen(
                        onBack = { navController.popBackStack() },
                        onComplete = { email ->
                            val emailLink = pendingEmailLink
                            if (emailLink.isNullOrBlank()) {
                                Toast.makeText(
                                    context,
                                    "This verification link has expired. Please request a new one.",
                                    Toast.LENGTH_LONG
                                ).show()
                                navController.navigate("register") {
                                    popUpTo("email_link_callback") { inclusive = true }
                                    launchSingleTop = true
                                }
                                return@EmailLinkCallbackScreen
                            }

                            EmailLinkSessionStore.savePendingEmail(context, email)
                            completeEmailLinkSignIn(email, emailLink)
                        }
                    )
                }
            }
            composable("home") {
                Surface(modifier = Modifier.padding(top = screenTopPadding, bottom = screenBottomPadding)) {
                    HomeScreen(
                        onStartRiskQuiz = {
                            navController.navigate("risk_assessment_gate")
                        },
                        onSignOut = {
                            navController.navigate("user_gate") {
                                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onGoalCardClick = { goalSlug ->
                            navController.navigate("risk_assessment_gate?goal=$goalSlug")
                        }
                    )
                }
            }
            composable("money_mastery") {
                Surface(modifier = Modifier.padding(top = screenTopPadding, bottom = screenBottomPadding)) {
                    MoneyMasteryScreen()
                }
            }
            composable("your_plans") {
                Surface(modifier = Modifier.padding(top = screenTopPadding, bottom = screenBottomPadding)) {
                    YourPlansScreen()
                }
            }
            composable("calendar") {
                Surface(modifier = Modifier.padding(top = screenTopPadding, bottom = screenBottomPadding)) {
                    CalendarScreen()
                }
            }
            composable("calendar_schedule") {
                Surface(modifier = Modifier.padding(top = screenTopPadding, bottom = screenBottomPadding)) {
                    CalendarScreen(
                        openScheduleOnStart = true,
                        onCloseRequest = { navController.popBackStack() }
                    )
                }
            }
            composable("profile") {
                Surface(modifier = Modifier.padding(top = screenTopPadding, bottom = screenBottomPadding)) {
                    ProfileScreen(
                        onSignedOut = {
                            navController.navigate("user_gate") {
                                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
            composable("risk_assessment_gate") {
                Surface(modifier = Modifier.padding(top = screenTopPadding, bottom = screenBottomPadding)) {
                    RiskAssessmentGateScreen(
                        onBack = { navController.popBackStack() },
                        onStartQuiz = {
                            navController.navigate("risk_quiz") {
                                popUpTo("risk_assessment_gate") { inclusive = true }
                            }
                        },
                        onOpenSavedResult = { record ->
                            navController.navigate("risk_result_saved/${record.behaviouralRiskProfile}/${record.age}/${record.investableCorpusPerMonth}") {
                                popUpTo("risk_assessment_gate") { inclusive = true }
                            }
                        },
                        onOpenGoalPlanner = { _, _ -> }
                    )
                }
            }
            composable("risk_assessment_gate?goal={goal}") { gateBackStackEntry ->
                val goalSlug = gateBackStackEntry.arguments?.getString("goal")
                Surface(modifier = Modifier.padding(top = screenTopPadding, bottom = screenBottomPadding)) {
                    RiskAssessmentGateScreen(
                        selectedGoalSlug = goalSlug,
                        onBack = { navController.popBackStack() },
                        onStartQuiz = {
                            navController.navigate("risk_quiz") {
                                popUpTo("risk_assessment_gate") { inclusive = true }
                            }
                        },
                        onOpenSavedResult = { record ->
                            navController.navigate("risk_result_saved/${record.behaviouralRiskProfile}/${record.age}/${record.investableCorpusPerMonth}") {
                                popUpTo("risk_assessment_gate") { inclusive = true }
                            }
                        },
                        onOpenGoalPlanner = { record, selectedGoal ->
                            navController.navigate("goal_planner/${record.optimumRiskCapability}/${record.age}/${record.investableCorpusPerMonth}?goal=$selectedGoal") {
                                popUpTo("risk_assessment_gate") { inclusive = true }
                            }
                        }
                    )
                }
            }
            composable("risk_quiz") {
                Surface(modifier = Modifier.padding(top = screenTopPadding, bottom = screenBottomPadding)) {
                    RiskQuizScreen(onComplete = { profile, age, corpus ->
                        navController.navigate("risk_result/$profile/$age/$corpus")
                    }, onBack = { navController.popBackStack() }, onScheduleMeet = {
                        navController.navigate("calendar_schedule")
                    })
                }
            }
            composable("risk_result/{profile}/{age}/{corpus}") { resultBackStackEntry ->
                val profile = resultBackStackEntry.arguments?.getString("profile") ?: "Moderate"
                val age = resultBackStackEntry.arguments?.getString("age")?.toIntOrNull() ?: 30
                val corpus = resultBackStackEntry.arguments?.getString("corpus")?.toLongOrNull() ?: 0L
                Surface(modifier = Modifier.padding(top = screenTopPadding, bottom = screenBottomPadding)) {
                    RiskResultScreen(
                        profile = profile,
                        age = age,
                        investableCorpusPerMonth = corpus,
                        onBack = { navController.popBackStack() },
                        onCalculatePlan = { optimumProfile ->
                            navController.navigate("goal_planner/$optimumProfile/$age/$corpus?goal=")
                        },
                        onRetakeQuiz = {
                            navController.navigate("risk_quiz") {
                                popUpTo("risk_result/$profile/$age/$corpus") { inclusive = true }
                            }
                        },
                        persistAssessment = true
                    )
                }
            }
            composable("risk_result_saved/{profile}/{age}/{corpus}") { resultBackStackEntry ->
                val profile = resultBackStackEntry.arguments?.getString("profile") ?: "Moderate"
                val age = resultBackStackEntry.arguments?.getString("age")?.toIntOrNull() ?: 30
                val corpus = resultBackStackEntry.arguments?.getString("corpus")?.toLongOrNull() ?: 0L
                Surface(modifier = Modifier.padding(top = screenTopPadding, bottom = screenBottomPadding)) {
                    RiskResultScreen(
                        profile = profile,
                        age = age,
                        investableCorpusPerMonth = corpus,
                        onBack = { navController.popBackStack() },
                        onCalculatePlan = { optimumProfile ->
                            navController.navigate("goal_planner/$optimumProfile/$age/$corpus?goal=")
                        },
                        onRetakeQuiz = {
                            navController.navigate("risk_quiz") {
                                popUpTo("risk_result_saved/$profile/$age/$corpus") { inclusive = true }
                            }
                        },
                        persistAssessment = false
                    )
                }
            }
            composable("goal_planner/{profile}/{age}/{corpus}?goal={goal}") { plannerBackStackEntry ->
                val profile = plannerBackStackEntry.arguments?.getString("profile") ?: "Moderate"
                val age = plannerBackStackEntry.arguments?.getString("age")?.toIntOrNull() ?: 30
                val corpus = plannerBackStackEntry.arguments?.getString("corpus")?.toLongOrNull() ?: 0L
                val goalSlug = plannerBackStackEntry.arguments?.getString("goal")
                Surface(modifier = Modifier.padding(top = screenTopPadding, bottom = screenBottomPadding)) {
                    TailoredPlansScreen(
                        profile = profile,
                        age = age,
                        investableCorpusPerMonth = corpus.toDouble(),
                        initialGoalSlug = goalSlug,
                        onBack = { navController.popBackStack() },
                        onScheduleMeet = { navController.navigate("calendar_schedule") }
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomBottomBar(
    destinations: List<BottomDestination>,
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val adaptiveUi = rememberAdaptiveUi()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                horizontal = 18.dp.adaptive(adaptiveUi),
                vertical = 10.dp.adaptive(adaptiveUi)
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 18.dp.adaptive(adaptiveUi),
                    shape = RoundedCornerShape(30.dp.adaptive(adaptiveUi)),
                    ambientColor = Color(0x1F9C8A73),
                    spotColor = Color(0x339C8A73)
                )
                .background(
                    Color.White.copy(alpha = 0.97f),
                    RoundedCornerShape(30.dp.adaptive(adaptiveUi))
                )
                .padding(
                    horizontal = 6.dp.adaptive(adaptiveUi),
                    vertical = 10.dp.adaptive(adaptiveUi)
                ),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            destinations.forEach { destination ->
                val selected = currentRoute == destination.route
                BottomBarItem(
                    destination = destination,
                    selected = selected,
                    onClick = { onNavigate(destination.route) },
                    adaptiveUi = adaptiveUi
                )
            }
        }
    }
}

@Composable
private fun RowScope.BottomBarItem(
    destination: BottomDestination,
    selected: Boolean,
    onClick: () -> Unit,
    adaptiveUi: com.example.sava.ui.theme.AdaptiveUi
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp.adaptive(adaptiveUi)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (selected) Color(0xFFEFEAFF) else Color.Transparent,
                    shape = RoundedCornerShape(18.dp.adaptive(adaptiveUi))
                )
                .padding(
                    horizontal = 12.dp.adaptive(adaptiveUi),
                    vertical = 10.dp.adaptive(adaptiveUi)
                ),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(
                imageVector = destination.icon,
                contentDescription = destination.label,
                tint = if (selected) Color(0xFF5C5FEF) else Color.Black,
                modifier = Modifier.size(22.dp.adaptive(adaptiveUi))
            )
        }
        Spacer(modifier = Modifier.height(4.dp.adaptive(adaptiveUi)))
        androidx.compose.material3.Text(
            text = destination.label,
            modifier = Modifier.fillMaxWidth(),
            color = Color.Black,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = MaterialTheme.typography.labelMedium.fontSize.adaptive(adaptiveUi)
            ),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

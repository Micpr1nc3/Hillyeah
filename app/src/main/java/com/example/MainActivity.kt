package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.HillYeahViewModel
import com.example.ui.components.AuthRequiredDialog
import com.example.ui.components.HillYeahBottomBar
import com.example.ui.components.HillYeahTopAppBar
import com.example.ui.navigation.Screen
import com.example.ui.screens.CommunityScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HikeDetailScreen
import com.example.ui.screens.HikesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HostHikeScreen
import com.example.ui.screens.InviteResponseScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MemberProfileScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ProfileSetupScreen
import com.example.ui.screens.RegisterScreen
import com.example.ui.screens.TrailDetailScreen
import com.example.ui.screens.TrailsScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.HillYeahTheme

class MainActivity : ComponentActivity() {
    private val viewModel: HillYeahViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleDeepLink(intent)
        enableEdgeToEdge(
            statusBarStyle = androidx.activity.SystemBarStyle.dark(
                android.graphics.Color.parseColor("#1B3324")
            ),
            navigationBarStyle = androidx.activity.SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            HillYeahTheme {
                HillYeahApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val (hikeId, hostId) = extractHikeParams(intent)
        if (!hikeId.isNullOrBlank()) {
            viewModel.handleIncomingInvite(hikeId, hostId)
        }
    }

    companion object {
        fun extractHikeParams(intent: Intent?): Pair<String?, String?> {
            val uri = intent?.data
            var hikeId: String? = null
            var hostId: String? = null

            // 1. Check URI Fragment (Hash routing: /#/?hikId=... or /#/invite?hikId=...)
            val fragment = uri?.fragment
            if (!fragment.isNullOrBlank()) {
                val fragHike = Regex("(?:[?&#/]|\\b)(?:hikId|id|hikeld|hikid|hikeId|hike_id|hike)=([a-zA-Z0-9_-]+)", RegexOption.IGNORE_CASE).find(fragment)
                if (fragHike != null) {
                    hikeId = fragHike.groupValues[1]
                }
                val fragHost = Regex("(?:[?&#/]|\\b)(?:hostId|hostid|hostld|host_id)=([a-zA-Z0-9_-]+)", RegexOption.IGNORE_CASE).find(fragment)
                if (fragHost != null) {
                    hostId = fragHost.groupValues[1]
                }
            }

            // 2. Check Standard Query Parameters
            if (uri != null && hikeId.isNullOrBlank()) {
                hikeId = uri.getQueryParameter("hikId")
                    ?: uri.getQueryParameter("id")
                    ?: uri.getQueryParameter("hikeld")
                    ?: uri.getQueryParameter("hikid")
                    ?: uri.getQueryParameter("hikeId")
                    ?: uri.getQueryParameter("hike_id")
                    ?: uri.getQueryParameter("hike")
                    ?: uri.pathSegments.lastOrNull()?.takeIf { it != "invite" && it != "hike" && it.isNotBlank() }

                if (hostId.isNullOrBlank()) {
                    hostId = uri.getQueryParameter("hostId")
                        ?: uri.getQueryParameter("hostld")
                        ?: uri.getQueryParameter("hostid")
                        ?: uri.getQueryParameter("host_id")
                }
            }

            // 3. Check Full dataString (handles unparsed or raw browser URLs)
            val dataString = intent?.dataString
            if (!dataString.isNullOrBlank() && hikeId.isNullOrBlank()) {
                val match = Regex("(?:[?&#/]|\\b)(?:hikId|id|hikeld|hikid|hikeId|hike_id|hike)=([a-zA-Z0-9_-]+)", RegexOption.IGNORE_CASE).find(dataString)
                if (match != null) {
                    hikeId = match.groupValues[1]
                }
                if (hostId.isNullOrBlank()) {
                    val hostMatch = Regex("(?:[?&#/]|\\b)(?:hostId|hostid|hostld|host_id)=([a-zA-Z0-9_-]+)", RegexOption.IGNORE_CASE).find(dataString)
                    if (hostMatch != null) {
                        hostId = hostMatch.groupValues[1]
                    }
                }
            }

            // 4. Check Intent string extras
            if (hikeId.isNullOrBlank()) {
                hikeId = intent?.getStringExtra("hikId")
                    ?: intent?.getStringExtra("id")
                    ?: intent?.getStringExtra("hikeld")
                    ?: intent?.getStringExtra("hikid")
                    ?: intent?.getStringExtra("hikeId")
            }
            if (hostId.isNullOrBlank()) {
                hostId = intent?.getStringExtra("hostId")
                    ?: intent?.getStringExtra("hostld")
                    ?: intent?.getStringExtra("hostid")
            }

            // 5. Check extra text if shared via message / intent
            val extraText = intent?.getStringExtra(Intent.EXTRA_TEXT)
            if (!extraText.isNullOrBlank() && hikeId.isNullOrBlank()) {
                val match = Regex("(?:[?&#/]|\\b)(?:hikId|id|hikeld|hikid|hikeId|hike_id|hike)=([a-zA-Z0-9_-]+)", RegexOption.IGNORE_CASE).find(extraText)
                if (match != null) {
                    hikeId = match.groupValues[1]
                }
                if (hostId.isNullOrBlank()) {
                    val hostMatch = Regex("(?:[?&#/]|\\b)(?:hostId|hostld|hostid|host_id)=([a-zA-Z0-9_-]+)", RegexOption.IGNORE_CASE).find(extraText)
                    if (hostMatch != null) {
                        hostId = hostMatch.groupValues[1]
                    }
                }
            }

            return Pair(hikeId, hostId)
        }
    }
}

@Composable
fun HillYeahApp(
    viewModel: HillYeahViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val showAuthPrompt by viewModel.showAuthPrompt.collectAsState()
    val authPromptMessage by viewModel.authPromptMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val isAuthScreen = currentScreen is Screen.Welcome || currentScreen is Screen.Login || currentScreen is Screen.Register
    val isInviteScreen = currentScreen is Screen.InviteResponse

    // Handle Android system back button
    BackHandler(enabled = currentScreen !is Screen.Home && currentScreen !is Screen.Welcome) {
        if (!viewModel.navigateBack()) {
            if (currentUser != null) {
                viewModel.navigateRoot(Screen.Home)
            } else {
                viewModel.navigateRoot(Screen.Welcome)
            }
        }
    }

    val activity = context as? ComponentActivity
    // Client-side parameter interception: capture incoming search/query parameters before rendering home page
    LaunchedEffect(activity?.intent) {
        val (paramHikeId, hostId) = MainActivity.extractHikeParams(activity?.intent)
        if (!paramHikeId.isNullOrBlank()) {
            viewModel.handleIncomingInvite(paramHikeId, hostId)
        }
    }

    // Observe Toast & notification events
    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val title = when (currentScreen) {
        is Screen.Home -> "HillYeah"
        is Screen.Trails -> "Discover Trails"
        is Screen.TrailDetail -> "Trail Details"
        is Screen.Hikes -> "Explore Hikes"
        is Screen.HikeDetail -> "Hike Details"
        is Screen.HostHike -> "Host a Hike"
        is Screen.Dashboard -> "My Hikes"
        is Screen.Community -> "Wellness"
        is Screen.Profile -> "My Profile"
        is Screen.Welcome -> "HillYeah"
        is Screen.Login -> "Log In"
        is Screen.Register -> "Register"
        is Screen.ProfileSetup -> "Profile Setup"
        is Screen.MemberProfile -> "Member Profile"
        is Screen.InviteResponse -> "Hike Invitation"
    }

    val canNavigateBack = currentScreen !is Screen.Home && currentScreen !is Screen.Welcome

    Scaffold(
        topBar = {
            if (currentScreen !is Screen.Welcome) {
                HillYeahTopAppBar(
                    title = title,
                    canNavigateBack = canNavigateBack,
                    currentUser = currentUser,
                    onNavigateBack = {
                        if (!viewModel.navigateBack()) {
                            if (currentUser != null) {
                                viewModel.navigateRoot(Screen.Home)
                            } else {
                                viewModel.navigateRoot(Screen.Welcome)
                            }
                        }
                    },
                    onOpenProfile = { viewModel.navigateTo(Screen.Profile) },
                    onOpenEditProfile = { viewModel.navigateTo(Screen.ProfileSetup) },
                    onOpenMyHikes = { viewModel.navigateTo(Screen.Dashboard) },
                    onOpenLogin = { viewModel.navigateTo(Screen.Login()) },
                    onLogout = { viewModel.logout() }
                )
            }
        },
        bottomBar = {
            if (currentUser != null && !isAuthScreen && !isInviteScreen) {
                HillYeahBottomBar(
                    currentScreen = currentScreen,
                    onNavigate = { screen ->
                        if (screen is Screen.Dashboard) {
                            viewModel.requireAuth("Please log in to view your joined and hosted hikes.") {
                                viewModel.navigateRoot(screen)
                            }
                        } else {
                            viewModel.navigateRoot(screen)
                        }
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "ScreenTransition",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { screen ->
            when (screen) {
                is Screen.Home -> HomeScreen(viewModel = viewModel)
                is Screen.Trails -> TrailsScreen(viewModel = viewModel)
                is Screen.TrailDetail -> TrailDetailScreen(viewModel = viewModel)
                is Screen.Hikes -> HikesScreen(viewModel = viewModel)
                is Screen.HikeDetail -> HikeDetailScreen(viewModel = viewModel)
                is Screen.HostHike -> HostHikeScreen(
                    viewModel = viewModel,
                    prefillTrailId = screen.prefillTrailId
                )
                is Screen.Dashboard -> DashboardScreen(viewModel = viewModel)
                is Screen.Community -> CommunityScreen(viewModel = viewModel)
                is Screen.Profile -> ProfileScreen(viewModel = viewModel)
                is Screen.Welcome -> WelcomeScreen(viewModel = viewModel)
                is Screen.Login -> LoginScreen(
                    viewModel = viewModel,
                    returnToScreen = screen.returnToScreen
                )
                is Screen.Register -> RegisterScreen(
                    viewModel = viewModel,
                    returnToScreen = screen.returnToScreen
                )
                is Screen.ProfileSetup -> ProfileSetupScreen(viewModel = viewModel)
                is Screen.MemberProfile -> MemberProfileScreen(viewModel = viewModel)
                is Screen.InviteResponse -> InviteResponseScreen(
                    hikeId = screen.hikeId,
                    senderId = screen.senderId,
                    viewModel = viewModel
                )
            }
        }
    }

    // Modal prompt if unauthenticated user attempts a restricted action
    if (showAuthPrompt) {
        AuthRequiredDialog(
            message = authPromptMessage,
            onLoginClick = {
                viewModel.dismissAuthPrompt()
                viewModel.navigateTo(Screen.Login())
            },
            onRegisterClick = {
                viewModel.dismissAuthPrompt()
                viewModel.navigateTo(Screen.Register())
            },
            onDismiss = {
                viewModel.dismissAuthPrompt()
            }
        )
    }
}

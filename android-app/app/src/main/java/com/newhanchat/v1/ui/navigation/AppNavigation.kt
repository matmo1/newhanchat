package com.newhanchat.v1.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.newhanchat.v1.data.api.ChatManager
import com.newhanchat.v1.data.api.TokenManager
import com.newhanchat.v1.data.repository.AuthPreferences
import com.newhanchat.v1.ui.components.BottomNavBar

import com.newhanchat.v1.ui.screens.*
import com.newhanchat.v1.ui.viewmodel.*

@Composable
fun AppNavigation(
    authPreferences: AuthPreferences,
    chatManager: ChatManager // Passed in from MainActivity so we can observe connection state globally
) {
    val navController = rememberNavController()

    // Simple state to hold current user ID across screens
    var currentUserId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        authPreferences.authToken.collect { token ->
            TokenManager.token = token
            if (!token.isNullOrEmpty()) {
                chatManager.connect(token)
            } else {
                chatManager.disconnect()
            }
        }
    }

    LaunchedEffect(Unit) {
        authPreferences.userId.collect { id ->
            currentUserId = id
        }
    }

    // --- NAVIGATION BAR STATE TRACKING ---
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route?.substringBefore("/")

    val mainTabs = listOf("post_list", "user_list", "profile")
    val showBottomBar = currentRoute in mainTabs

    // --- GLOBAL SCAFFOLD (Fixes the "Trapped" Bug) ---
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute ?: "",
                    onNavigate = { route ->
                        navController.navigate(route) {
                            // This pops the stack so tabs don't pile up on top of each other!
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding) // Prevents content from hiding behind the bar
        ) {

            composable("login") {
                // Notice how clean this is with Hilt!
                val viewModel: LoginViewModel = hiltViewModel()
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = { _, _, _ ->
                        navController.navigate("post_list") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate("register") }
                )
            }

            composable("register") {
                val viewModel: RegisterViewModel = hiltViewModel()
                RegisterScreen(
                    viewModel = viewModel,
                    onRegisterSuccess = { navController.popBackStack() },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            composable("post_list") {
                val viewModel: PostViewModel = hiltViewModel()
                PostListScreen(
                    viewModel = viewModel,
                    onCreatePostClick = { navController.navigate("create_post") }
                )
            }

            composable("create_post") {
                val viewModel: PostViewModel = hiltViewModel()
                CreatePostScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onPostCreated = { navController.popBackStack() }
                )
            }

            composable("user_list") {
                // We don't need to pass token here since we removed it from UserListScreen's parameters
                UserListScreen(
                    currentUserId = currentUserId ?: "",
                    onUserSelected = { selectedUser -> // FIXED: Renamed to match your file
                        navController.navigate("chat/${selectedUser.id}")
                    },
                    onLogout = { // FIXED: Added missing onLogout parameter
                        chatManager.disconnect()
                        navController.navigate("login") { popUpTo(0) }
                    }
                )
            }

            composable(
                route = "chat/{recipientId}",
                arguments = listOf(navArgument("recipientId") { type = NavType.StringType })
            ) { backStackEntry ->
                val recipientId = backStackEntry.arguments?.getString("recipientId") ?: return@composable
                // We let ChatScreen handle injecting its own ChatViewModel now!
                ChatScreen(
                    chatManager = chatManager, // FIXED: Pass the global chatManager!
                    myUserId = currentUserId ?: "",
                    recipientId = recipientId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("profile") {
                val profileViewModel: ProfileViewModel = hiltViewModel()
                ProfileScreen(
                    viewModel = profileViewModel,
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }

            composable("settings") {
                val profileViewModel: ProfileViewModel = hiltViewModel()
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onEditProfile = { navController.navigate("edit_profile") },
                    onLogout = {
                        profileViewModel.logout(onLogoutComplete = {
                            chatManager.disconnect()
                            navController.navigate("login") {
                                popUpTo(0)
                            }
                        })
                    }
                )
            }

            composable("edit_profile") {
                val profileViewModel: ProfileViewModel = hiltViewModel()
                EditProfileScreen(
                    viewModel = profileViewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToEditBio = { navController.navigate("edit_bio") },
                    onNavigateToEditName = { navController.navigate("edit_name") }
                )
            }

            composable("edit_bio") {
                val profileViewModel: ProfileViewModel = hiltViewModel()
                EditBioScreen(
                    viewModel = profileViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("edit_name") {
                val profileViewModel: ProfileViewModel = hiltViewModel()
                EditNameScreen(
                    viewModel = profileViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
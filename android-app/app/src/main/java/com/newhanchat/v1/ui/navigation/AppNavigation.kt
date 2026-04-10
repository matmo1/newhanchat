package com.newhanchat.v1.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    authPreferences: AuthPreferences,
    chatManager: ChatManager
) {
    val navController = rememberNavController()
    var currentUserId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // ✨ THE SUPERPOWER: Reactive Auto-Login & Auto-Logout
    LaunchedEffect(Unit) {
        authPreferences.authToken.collect { token ->
            TokenManager.token = token
            if (!token.isNullOrEmpty()) {
                chatManager.connect(token)
                // We have a token! Auto-route to Feed and erase the back button history
                navController.navigate("post_list") {
                    popUpTo(0)
                }
            } else {
                chatManager.disconnect()
                // No token! Auto-route to Login and erase the back button history
                navController.navigate("login") {
                    popUpTo(0)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        authPreferences.userId.collect { id ->
            currentUserId = id
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route?.substringBefore("/")

    val mainTabs = listOf("post_list", "user_list", "profile")
    val showBottomBar = currentRoute in mainTabs

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute ?: "",
                    onNavigate = { route ->
                        navController.navigate(route) {
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
            startDestination = "splash", // ✨ START AT SPLASH INSTEAD OF LOGIN
            modifier = Modifier.padding(innerPadding)
        ) {

            // ✨ NEW: Temporary loading screen while DataStore fetches the token from disk
            composable("splash") {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            composable("login") {
                val viewModel: LoginViewModel = hiltViewModel()
                LoginScreen(
                    viewModel = viewModel,
                    // We don't even need manual navigation here anymore!
                    // When the ViewModel saves the token, the LaunchedEffect above will auto-teleport us!
                    onLoginSuccess = { _, _, _ -> },
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
                UserListScreen(
                    currentUserId = currentUserId ?: "",
                    onUserSelected = { selectedUser ->
                        navController.navigate("chat/${selectedUser.id}")
                    },
                    onLogout = {
                        // Just clear the cache! The LaunchedEffect will handle kicking us out.
                        scope.launch { authPreferences.clearCredentials() }
                    }
                )
            }

            composable(
                route = "chat/{recipientId}",
                arguments = listOf(navArgument("recipientId") { type = NavType.StringType })
            ) { backStackEntry ->
                val recipientId = backStackEntry.arguments?.getString("recipientId") ?: return@composable
                ChatScreen(
                    chatManager = chatManager,
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
                        // Just clear the cache! The LaunchedEffect will handle kicking us out.
                        scope.launch { authPreferences.clearCredentials() }
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
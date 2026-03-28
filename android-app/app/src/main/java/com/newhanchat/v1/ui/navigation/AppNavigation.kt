package com.newhanchat.v1.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.newhanchat.v1.data.api.ChatManager
import com.newhanchat.v1.data.api.TokenManager
import com.newhanchat.v1.data.api.apiService
import com.newhanchat.v1.data.repository.AuthPreferences
import com.newhanchat.v1.data.repository.PostRepository
import com.newhanchat.v1.ui.screens.LoginScreen
import com.newhanchat.v1.ui.screens.PostListScreen
import com.newhanchat.v1.ui.screens.ProfileScreen
import com.newhanchat.v1.ui.screens.RegisterScreen
import com.newhanchat.v1.ui.screens.UserListScreen
import com.newhanchat.v1.ui.viewmodel.PostViewModel
import com.newhanchat.v1.ui.viewmodel.ProfileViewModel
import com.newhanchat.v1.ui.components.BottomNavBar
import com.newhanchat.v1.ui.screens.EditBioScreen
import com.newhanchat.v1.ui.screens.EditNameScreen
import com.newhanchat.v1.ui.screens.EditProfileScreen
import com.newhanchat.v1.ui.screens.SettingsScreen

@Composable
fun AppNavigation(authPreferences: AuthPreferences) {
    val navController = rememberNavController()
    // Chat Manager singleton
    val chatManager = remember { ChatManager() }

    val postRepository = remember { PostRepository(apiService) }

    // Simple state to hold current user ID across screens
    var currentUserId by remember { androidx.compose.runtime.mutableStateOf("") }

    // 1. Read device cache
    val cachedToken by authPreferences.authToken.collectAsState(initial = null)
    val cachedUserId by authPreferences.userId.collectAsState(initial = null)

    // 2. Decide Start Destination dynamically
    val startDestination = if (cachedToken != null) {
        TokenManager.token = cachedToken // Ensure interceptor gets it
        "post_list"
    } else {
        "login"
    }

    NavHost(navController = navController, startDestination = startDestination) {

        // 1. LOGIN
        // 1. LOGIN
        composable("login") {
            LoginScreen(
                // 🚀 Add the three expected String parameters here:
                onLoginSuccess = { token, userId, username ->

                    // Update your global state with the new user's info!
                    currentUserId = userId
                    TokenManager.token = token

                    // Now navigate to the post feed
                    navController.navigate("post_list") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }
        // 2. REGISTER
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { navController.popBackStack() },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        // 3. POST FEED (Uses ViewModel)
        composable("post_list") {
            // Create Repository & ViewModel manually (Dependency Injection lite)
            val postRepository = remember { PostRepository(apiService) }
            val postViewModel: PostViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { PostViewModel(postRepository) }
                }
            )

            Scaffold(
                bottomBar = {
                    // Add Bottom Bar UI here or in a wrapper if you want it on multiple screens
                    com.newhanchat.v1.ui.components.BottomNavBar(
                        currentRoute = "post_list",
                        onNavigate = { route -> navController.navigate(route) }
                    )
                }
            ) { padding ->
                androidx.compose.foundation.layout.Box(modifier = Modifier.padding(padding)) {
                    PostListScreen(
                        viewModel = postViewModel,
                        currentUserId = currentUserId
                    )
                }
            }
        }

        // 4. USER LIST (Chats)
        composable("user_list") {
            Scaffold(
                bottomBar = {
                    com.newhanchat.v1.ui.components.BottomNavBar(
                        currentRoute = "user_list",
                        onNavigate = { route -> navController.navigate(route) }
                    )
                }
            ) { padding ->
                androidx.compose.foundation.layout.Box(modifier = Modifier.padding(padding)) {
                    UserListScreen(
                        currentUserId = currentUserId,
                        onUserSelected = { user ->
                            // Navigate to chat (Not implemented fully in this snippet)
                            // navController.navigate("chat/${user.id}")
                        },
                        onLogout = {
                            chatManager.disconnect()
                            navController.navigate("login") {
                                popUpTo(0) // Clear everything
                            }
                        }
                    )
                }
            }
        }
        composable("profile") {
            val profileViewModel: ProfileViewModel = hiltViewModel()

            Scaffold(
                bottomBar = {
                    BottomNavBar(
                        currentRoute = "profile",
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            ) { padding ->
                // 🛠️ FIX: Only applying bottom padding so the top bar sits flush
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.padding(bottom = padding.calculateBottomPadding())
                ) {
                    ProfileScreen(
                        viewModel = profileViewModel,
                        onNavigateToSettings = { navController.navigate("settings") }
                    )
                }
            }
        }

        // --- ✨ ADD THESE NEW SCREENS RIGHT BELOW THE PROFILE COMPOSABLE ---

        composable("settings") {
            val profileViewModel: ProfileViewModel = hiltViewModel()

            SettingsScreen(
                onBack = { navController.popBackStack() },
                onEditProfile = { navController.navigate("edit_profile") },
                onLogout = {
                    profileViewModel.logout(onLogoutComplete = {
                        chatManager.disconnect()
                        navController.navigate("login") {
                            popUpTo(0) // Clear the backstack completely
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
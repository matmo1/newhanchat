package com.newhanchat.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.newhanchat.demo.chatservices.ChatManager
import com.newhanchat.demo.chatservices.TokenManager
import com.newhanchat.demo.chatservices.apiService
import com.newhanchat.demo.loginandregister.UserResponse
import com.newhanchat.demo.ui.screens.*
import com.newhanchat.demo.ui.theme.NewHanChatDemoTheme

class MainActivity : ComponentActivity() {
    private val chatManager = ChatManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewHanChatDemoTheme {
                // Global State
                var currentScreen by remember { mutableStateOf("LOGIN") }
                var myUserId by remember { mutableStateOf("") }
                var selectedChatUser by remember { mutableStateOf<UserResponse?>(null) }

                // Logic: Show Bottom Bar ONLY when logged in and NOT in a chat
                val showBottomBar = currentScreen == "USER_LIST" || currentScreen == "POST_LIST"

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.List, contentDescription = "Feed") },
                                    label = { Text("Feed") },
                                    selected = currentScreen == "POST_LIST",
                                    onClick = { currentScreen = "POST_LIST" }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Chat, contentDescription = "Chats") },
                                    label = { Text("Chats") },
                                    selected = currentScreen == "USER_LIST",
                                    onClick = { currentScreen = "USER_LIST" }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        when (currentScreen) {
                            "LOGIN" -> LoginScreen(
                                onLoginSuccess = { token, userId, _ ->
                                    // 1. Save State
                                    myUserId = userId
                                    TokenManager.token = token

                                    // 2. Connect
                                    chatManager.connect(token)

                                    // 3. Navigate
                                    currentScreen = "POST_LIST"
                                },
                                onNavigateToRegister = {
                                    currentScreen = "REGISTER"
                                }
                            )

                            "REGISTER" -> RegisterScreen(
                                onRegisterSuccess = { currentScreen = "LOGIN" },
                                onNavigateToLogin = { currentScreen = "LOGIN" }
                            )

                            "POST_LIST" -> PostListScreen(
                                apiService = apiService
                            )

                            "USER_LIST" -> UserListScreen(
                                onUserSelected = { user ->
                                    selectedChatUser = user
                                    currentScreen = "CHAT"
                                },
                                onLogout = {
                                    chatManager.disconnect()
                                    currentScreen = "LOGIN"
                                },
                                currentUserId = myUserId
                            )

                            "CHAT" -> {
                                if (selectedChatUser != null) {
                                    ChatScreen(
                                        chatManager = chatManager,
                                        myUserId = myUserId,
                                        recipient = selectedChatUser!!,
                                        onBack = { currentScreen = "USER_LIST" }
                                    )
                                } else {
                                    currentScreen = "USER_LIST"
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        chatManager.disconnect()
    }
}
package com.newhanchat.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

// Imports
import com.newhanchat.demo.chatservices.ChatManager
import com.newhanchat.demo.loginandregister.UserResponse
import com.newhanchat.demo.ui.screens.* // Imports LoginScreen, UserListScreen, etc.
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
                var jwtToken by remember { mutableStateOf("") }
                var myUserId by remember { mutableStateOf("") }
                var selectedChatUser by remember { mutableStateOf<UserResponse?>(null) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        when (currentScreen) {
                            "LOGIN" -> LoginScreen(
                                onLoginSuccess = { token, userId, _ ->
                                    // 1. Save to Compose State (for UI logic)
                                    jwtToken = token
                                    myUserId = userId

                                    // 2. IMPORTANT: Save to TokenManager (for Retrofit/ApiService)
                                    com.newhanchat.demo.chatservices.TokenManager.token = token

                                    // 3. Connect WebSocket
                                    chatManager.connect(token)
                                    currentScreen = "USER_LIST"
                                },
                                onNavigateToRegister = { currentScreen = "REGISTER" }
                            )
                            "REGISTER" -> RegisterScreen(
                                onRegisterSuccess = { currentScreen = "LOGIN" },
                                onNavigateToLogin = { currentScreen = "LOGIN" }
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
                            "CHAT" -> ChatScreen(
                                chatManager = chatManager,
                                myUserId = myUserId,
                                recipient = selectedChatUser!!,
                                onBack = { currentScreen = "USER_LIST" }
                            )
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
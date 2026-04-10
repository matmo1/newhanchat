package com.newhanchat.v1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.newhanchat.v1.data.api.ChatManager
import com.newhanchat.v1.data.repository.AuthPreferences
import com.newhanchat.v1.ui.navigation.AppNavigation
import com.newhanchat.v1.ui.theme.NewHanChatDemoTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var authPreferences: AuthPreferences
    @Inject lateinit var chatManager: ChatManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NewHanChatDemoTheme {
                AppNavigation(
                    authPreferences = authPreferences,
                    chatManager = chatManager
                )
            }
        }
    }
}
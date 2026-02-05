package com.newhanchat.v1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.newhanchat.v1.ui.navigation.AppNavigation
import com.newhanchat.v1.ui.theme.NewHanChatDemoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewHanChatDemoTheme {
                // The entire app logic is now handled by Navigation
                AppNavigation()
            }
        }
    }
}
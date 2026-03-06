package com.newhanchat.v1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.newhanchat.v1.data.repository.AuthPreferences
import com.newhanchat.v1.ui.navigation.AppNavigation
import com.newhanchat.v1.ui.theme.NewHanChatDemoTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // 🚀 Let Hilt inject your AuthPreferences automatically!
    @Inject
    lateinit var authPreferences: AuthPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewHanChatDemoTheme {
                AppNavigation(authPreferences = authPreferences)
            }
        }
    }
}
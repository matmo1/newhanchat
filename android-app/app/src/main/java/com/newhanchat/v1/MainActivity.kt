package com.newhanchat.v1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
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

        enableEdgeToEdge()

        setContent {
            NewHanChatDemoTheme {
                // ✨ 1. Read the saved background image from memory
                val backgroundUri by authPreferences.backgroundUri.collectAsState(initial = null)

                Box(modifier = Modifier.fillMaxSize()) {

                    // ✨ 2. Draw and Blur the Custom Image!
                    if (backgroundUri != null) {
                        AsyncImage(
                            model = backgroundUri,
                            contentDescription = "Custom Blurred Background",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .blur(0.dp) // Flawless, non-flickering blur
                        )
                    } else {
                        // Fallback background if they haven't picked a photo yet
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                        )
                    }

                    // ✨ 3. Draw the App (with Translucent UI) on top
                    AppNavigation(
                        authPreferences = authPreferences,
                        chatManager = chatManager
                    )
                }
            }
        }
    }
}
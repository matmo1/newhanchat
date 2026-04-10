package com.newhanchat.v1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.stringPreferencesKey
import coil3.compose.AsyncImage
import com.newhanchat.v1.data.api.ChatManager
import com.newhanchat.v1.data.repository.AuthPreferences
import com.newhanchat.v1.data.repository.dataStore
import com.newhanchat.v1.ui.navigation.AppNavigation
import com.newhanchat.v1.ui.theme.NewHanChatDemoTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var authPreferences: AuthPreferences
    @Inject lateinit var chatManager: ChatManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Force the top (clock/battery) and bottom (nav line) bars to be 100% transparent!
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        setContent {
            NewHanChatDemoTheme {
                WallpaperBackgroundWrapper {
                    AppNavigation(
                        authPreferences = authPreferences,
                        chatManager = chatManager
                    )
                }
            }
        }
    }
}

@Composable
fun WallpaperBackgroundWrapper(content: @Composable () -> Unit) {
    val context = LocalContext.current

    // ✨ FIXED: Calling dataStore on 'context' and using the proper imported map function
    val backgroundUri by remember(context) {
        context.dataStore.data.map { preferences ->
            preferences[stringPreferencesKey("background_uri")]
        }
    }.collectAsState(initial = null)

    Box(modifier = Modifier.fillMaxSize()) {
        if (backgroundUri != null) {
            AsyncImage(
                model = backgroundUri,
                contentDescription = "Custom Blurred Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(10.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            )
        }
        content()
    }
}
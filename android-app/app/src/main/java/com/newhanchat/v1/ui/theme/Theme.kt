package com.newhanchat.v1.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
fun NewHanChatDemoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    // ✨ MAKE BACKGROUNDS TRANSLUCENT SO THE BLUR SHOWS THROUGH
    val transparentScheme = colorScheme.copy(
        background = colorScheme.background.copy(alpha = 0.6f),
        surface = colorScheme.surface.copy(alpha = 0.6f),
        surfaceVariant = colorScheme.surfaceVariant.copy(alpha = 0.8f)
    )

    MaterialTheme(
        colorScheme = transparentScheme,
        typography = Typography,
        content = content
    )
}
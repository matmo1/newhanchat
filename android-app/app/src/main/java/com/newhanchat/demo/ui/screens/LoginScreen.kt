package com.newhanchat.demo.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// --- FIX: Import the property (lowercase 'a'), not just the interface ---
import com.newhanchat.demo.chatservices.apiService
import com.newhanchat.demo.loginandregister.AuthRequest

@Composable
fun LoginScreen(
    onLoginSuccess: (String, String, String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("NewHan Chat", style = MaterialTheme.typography.displaySmall)
        Spacer(modifier = Modifier.height(32.dp))

        TextField(value = username, onValueChange = { username = it }, label = { Text("Username") })
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = password, onValueChange = { password = it }, label = { Text("Password") })
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    try {
                        val response = apiService.login(AuthRequest(username, password))

                        if (response.isSuccessful && response.body() != null) {
                            val body = response.body()!!

                            // --- ADD THIS LINE ---
                            com.newhanchat.demo.chatservices.TokenManager.token = body.token
                            println("✅ DEBUG: Token saved to manager")
                            // ---------------------

                            onLoginSuccess(body.token, body.userId, body.username)
                        } else {
                            Toast.makeText(context, "Login Failed", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        TextButton(onClick = onNavigateToRegister) {
            Text("Don't have an account? Register")
        }
    }
}
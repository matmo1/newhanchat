package com.newhanchat.demo.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// --- FIX: Import the property (lowercase 'a') ---
import com.newhanchat.demo.chatservices.apiService
import com.newhanchat.demo.loginandregister.RegisterRequest

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var fname by remember { mutableStateOf("") }
    var lname by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("2000-01-01") }
    var password by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create Account", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        TextField(value = username, onValueChange = { username = it }, label = { Text("Username") })
        TextField(value = fname, onValueChange = { fname = it }, label = { Text("First Name") })
        TextField(value = lname, onValueChange = { lname = it }, label = { Text("Last Name") })
        TextField(
            value = dob,
            onValueChange = { dob = it },
            label = { Text("Date of Birth (YYYY-MM-DD)") },
            placeholder = { Text("2000-01-01") }
        )
        TextField(value = password, onValueChange = { password = it }, label = { Text("Password") })

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    try {
                        val formattedDob = "${dob}T12:00:00"
                        val request = RegisterRequest(username, fname, lname, formattedDob, password)
                        val response = apiService.register(request)
                        if (response.isSuccessful) {
                            Toast.makeText(context, "Registration Success!", Toast.LENGTH_LONG).show()
                            onRegisterSuccess()
                        } else {
                            val error = response.errorBody()?.string() ?: "Unknown error"
                            Toast.makeText(context, "Failed: $error", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }

        TextButton(onClick = onNavigateToLogin) {
            Text("Back to Login")
        }
    }
}
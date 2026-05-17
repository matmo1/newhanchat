package com.newhanchat.v1.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.newhanchat.v1.R
import com.newhanchat.v1.ui.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: (String, String, String) -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    // ✨ NEW: State to track if the password should be shown or hidden
    var passwordVisible by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(error) {
        if (error != null) {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }

    // Crisp, modern rounded fields
    val crispShape = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.displaySmall)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username, onValueChange = { username = it }, label = { Text(stringResource(R.string.username_title)) },
            shape = crispShape, modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password_title)) },
            shape = crispShape,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            // ✨ FIXED: Applies dots/asterisks when passwordVisible is false
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            // ✨ FIXED: Adds the toggleable eye icon
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                val description = if (passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = description)
                }
            }
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // ✨ FIXED: Strip out the invisible spaces added by the Bulgarian keyboard!
                val cleanUsername = username.trim()
                val cleanPassword = password.trim()

                viewModel.login(cleanUsername, cleanPassword) { token, userId ->
                    onLoginSuccess(token, userId, cleanUsername)
                }
            },
            enabled = !isLoading, modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp)) else Text(
                stringResource(R.string.login_title)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onNavigateToRegister) { Text(stringResource(R.string.don_t_have_an_account_register_title)) }
    }
}
package com.newhanchat.v1.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
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
import com.newhanchat.v1.data.model.RegisterRequest
import com.newhanchat.v1.ui.viewmodel.RegisterViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    var username by remember { mutableStateOf("") }
    var fname by remember { mutableStateOf("") }
    var lname by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    // ✨ NEW: State to track if the password should be shown or hidden
    var passwordVisible by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current
    val crispShape = RoundedCornerShape(16.dp)

    LaunchedEffect(error) {
        if (error != null) {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }


    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        dob = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.create_account_title), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text(stringResource(R.string.username_title)) }, shape = crispShape, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = fname, onValueChange = { fname = it }, label = { Text(
            stringResource(R.string.first_name_title)
        ) }, shape = crispShape, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = lname, onValueChange = { lname = it }, label = { Text(
            stringResource(R.string.last_name_title)
        ) }, shape = crispShape, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = dob, onValueChange = {}, label = { Text(stringResource(R.string.date_of_birth_title)) },
            readOnly = true, shape = crispShape, modifier = Modifier.fillMaxWidth(),
            trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.DateRange,
                stringResource(R.string.pick_date_title)
            ) } }
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password_title)) },
            shape = crispShape,
            modifier = Modifier.fillMaxWidth(),
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
                val cleanFname = fname.trim()
                val cleanLname = lname.trim()
                val cleanPassword = password.trim()

                val req = RegisterRequest(cleanUsername, cleanFname, cleanLname, "${dob}T12:00:00", cleanPassword)
                viewModel.register(req) {
                    Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show()
                    onRegisterSuccess()
                }
            },
            enabled = !isLoading && dob.isNotBlank(), modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp)) else Text(
                stringResource(R.string.register_title)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onNavigateToLogin) { Text(stringResource(R.string.back_to_login_title)) }
    }
}
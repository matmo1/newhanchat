package com.newhanchat.v1.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.newhanchat.v1.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNameScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit
) {
    val currentProfile by viewModel.profile.collectAsState()

    // FIXED: Added remember keys so the text fields populate when the network loads!
    var fname by remember(currentProfile?.fname) { mutableStateOf(currentProfile?.fname ?: "") }
    var lname by remember(currentProfile?.lname) { mutableStateOf(currentProfile?.lname ?: "") }
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Name") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.updateName(fname, lname, onSuccess = onBack) },
                        enabled = !isLoading && fname.isNotBlank() && lname.isNotBlank()
                    ) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        else Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)
        ) {
            OutlinedTextField(
                value = fname,
                onValueChange = { fname = it },
                label = { Text("First Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = lname,
                onValueChange = { lname = it },
                label = { Text("Last Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}
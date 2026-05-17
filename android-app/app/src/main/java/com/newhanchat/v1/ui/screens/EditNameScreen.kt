package com.newhanchat.v1.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.newhanchat.v1.ui.viewmodel.ProfileViewModel
import com.newhanchat.v1.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNameScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit
) {
    val currentProfile by viewModel.profile.collectAsState()
    var fname by remember(currentProfile?.fname) { mutableStateOf(currentProfile?.fname ?: "") }
    var lname by remember(currentProfile?.lname) { mutableStateOf(currentProfile?.lname ?: "") }
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_name_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cancel_title))
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.updateName(fname, lname, onSuccess = onBack) },
                        enabled = !isLoading && fname.isNotBlank() && lname.isNotBlank()
                    ) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        else Icon(Icons.Default.Check, contentDescription = stringResource(R.string.save_title))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        // ✨ FIXED: Made the Scaffold transparent so wallpaper shows through system bars
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            OutlinedTextField(
                value = fname,
                onValueChange = { fname = it },
                label = { Text(stringResource(R.string.first_name_title)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = lname,
                onValueChange = { lname = it },
                label = { Text(stringResource(R.string.last_name_title)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}
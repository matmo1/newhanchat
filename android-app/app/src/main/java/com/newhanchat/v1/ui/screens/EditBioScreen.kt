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
fun EditBioScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit
) {
    val currentProfile by viewModel.profile.collectAsState()
    var bioText by remember(currentProfile?.bio) { mutableStateOf(currentProfile?.bio ?: "") }
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_bio_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cancel_title))
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.updateBio(newBio = bioText, onSuccess = onBack) },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Check, contentDescription = stringResource(R.string.save_title))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        // ✨ FIXED: Made the Scaffold transparent so wallpaper shows through system bars
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = bioText,
                onValueChange = { bioText = it },
                label = { Text(stringResource(R.string.about_you_title)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.write_a_little_bit_about_yourself_so_others_can_get_to_know_you_title),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
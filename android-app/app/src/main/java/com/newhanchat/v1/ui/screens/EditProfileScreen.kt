package com.newhanchat.v1.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.newhanchat.v1.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel, // Pass the ViewModel here!
    onBack: () -> Unit,
    onNavigateToEditBio: () -> Unit,
    onNavigateToEditName: () -> Unit // ✨ New navigation callback
) {
    val context = LocalContext.current

    // ✨ The Android Photo Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                // If the user selected an image, send it to the ViewModel to upload!
                viewModel.uploadProfilePicture(context, uri)
                // Automatically go back to profile while it uploads
                onBack()
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

            ListItem(
                headlineContent = { Text("Change Profile Picture") },
                leadingContent = { Icon(Icons.Default.Image, contentDescription = null) },
                modifier = Modifier.clickable {
                    // ✨ Launch the photo picker showing only images
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )
            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Edit Bio") },
                leadingContent = { Icon(Icons.Default.Notes, contentDescription = null) },
                modifier = Modifier.clickable { onNavigateToEditBio() }
            )
            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Edit Name") },
                leadingContent = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.clickable { onNavigateToEditName() } // ✨ Navigate to name screen
            )
        }
    }
}
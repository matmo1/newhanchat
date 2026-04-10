package com.newhanchat.v1.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
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
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.newhanchat.v1.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    onNavigateToEditBio: () -> Unit,
    onNavigateToEditName: () -> Unit
) {
    val context = LocalContext.current

    // ✨ NEW: The CanHub Image Cropper Launcher
    val cropImageLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val croppedUri = result.uriContent
            if (croppedUri != null) {
                // Upload the perfectly cropped, lightweight image!
                viewModel.uploadProfilePicture(context, croppedUri)
                onBack()
            }
        } else {
            // Optional: Handle error (e.g., user closed the cropper without saving)
            result.error?.printStackTrace()
        }
    }

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
                    // ✨ Launch the Cropper!
                    // uri = null means it will open the gallery for the user to pick an image first.
                    cropImageLauncher.launch(
                        CropImageContractOptions(
                            uri = null,
                            cropImageOptions = CropImageOptions(
                                imageSourceIncludeCamera = true, // Lets them take a live photo too!
                                imageSourceIncludeGallery = true,
                                cropShape = CropImageView.CropShape.OVAL, // Circular UI overlay
                                fixAspectRatio = true, // Forces 1:1 square
                                aspectRatioX = 1,
                                aspectRatioY = 1
                            )
                        )
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
                modifier = Modifier.clickable { onNavigateToEditName() }
            )
        }
    }
}
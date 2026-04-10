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
import androidx.compose.ui.graphics.Color
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

    val cropImageLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful && result.uriContent != null) {
            viewModel.uploadProfilePicture(context, result.uriContent!!)
            onBack()
        } else {
            result.error?.printStackTrace()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        // ✨ FIXED: Made the Scaffold transparent so wallpaper shows through system bars
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

            ListItem(
                headlineContent = { Text("Change Profile Picture") },
                leadingContent = { Icon(Icons.Default.Image, contentDescription = null) },
                modifier = Modifier.clickable {
                    cropImageLauncher.launch(
                        CropImageContractOptions(
                            uri = null,
                            cropImageOptions = CropImageOptions(
                                imageSourceIncludeCamera = true,
                                imageSourceIncludeGallery = true,
                                cropShape = CropImageView.CropShape.OVAL,
                                fixAspectRatio = true,
                                aspectRatioX = 1,
                                aspectRatioY = 1
                            )
                        )
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Edit Bio") },
                leadingContent = { Icon(Icons.Default.Notes, contentDescription = null) },
                modifier = Modifier.clickable { onNavigateToEditBio() },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Edit Name") },
                leadingContent = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.clickable { onNavigateToEditName() },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
        }
    }
}
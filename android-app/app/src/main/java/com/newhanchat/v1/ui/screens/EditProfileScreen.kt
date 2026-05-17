package com.newhanchat.v1.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.newhanchat.v1.ui.viewmodel.ProfileViewModel
import com.newhanchat.v1.R

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
                title = { Text(stringResource(R.string.edit_profile_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_title)) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()) {

            // ✨ NEW: Language Toggle Dropdown
            var languageDropdownExpanded by remember { mutableStateOf(false) }
            val currentLangTag = AppCompatDelegate.getApplicationLocales().toLanguageTags()

            ListItem(
                headlineContent = { Text(stringResource(R.string.language)) },
                leadingContent = { Icon(Icons.Default.Language, contentDescription = null) },
                trailingContent = {
                    Box {
                        TextButton(onClick = { languageDropdownExpanded = true }) {
                            Text(if (currentLangTag == "bg") "Български" else "English")
                        }
                        DropdownMenu(
                            expanded = languageDropdownExpanded,
                            onDismissRequest = { languageDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.english)) },
                                onClick = {
                                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                                    languageDropdownExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.bulgarian)) },
                                onClick = {
                                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("bg"))
                                    languageDropdownExpanded = false
                                }
                            )
                        }
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
            HorizontalDivider()

            ListItem(
                headlineContent = { Text(stringResource(R.string.change_profile_picture_title)) },
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
                headlineContent = { Text(stringResource(R.string.edit_bio_title)) },
                leadingContent = { Icon(Icons.Default.Notes, contentDescription = null) },
                modifier = Modifier.clickable { onNavigateToEditBio() },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
            HorizontalDivider()

            ListItem(
                headlineContent = { Text(stringResource(R.string.edit_name_title)) },
                leadingContent = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.clickable { onNavigateToEditName() },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
        }
    }
}
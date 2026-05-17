package com.newhanchat.v1.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.newhanchat.v1.ui.viewmodel.PostViewModel
import com.newhanchat.v1.ui.viewmodel.ProfileViewModel
import com.newhanchat.v1.utils.ImageUtils.createMultipartBodyPartFromUri
import com.newhanchat.v1.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    viewModel: PostViewModel,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onPostCreated: () -> Unit
) {
    var content by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val isUploading by viewModel.isUploading.collectAsState()
    val profile by profileViewModel.profile.collectAsState()

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_post_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_title))
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (content.isNotBlank() || selectedImageUri != null) {
                                val imagePart = selectedImageUri?.let { uri ->
                                    createMultipartBodyPartFromUri(context, uri, "file")
                                }
                                val authorName = if (profile != null) "${profile!!.fname} ${profile!!.lname}" else "Unknown User"
                                val authorPic = profile?.profilePictureUrl ?: ""

                                viewModel.createPost(content, imagePart, authorName, authorPic, onPostCreated)
                            }
                        },
                        enabled = !isUploading && (content.isNotBlank() || selectedImageUri != null)
                    ) {
                        Text(stringResource(R.string.share_title))
                    }
                },
                // ✨ FIXED: Made the top bar transparent
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        // ✨ FIXED: Made the Scaffold transparent so wallpaper shows through system bars
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    placeholder = { Text(stringResource(R.string.what_s_on_your_mind_title)) },
                    maxLines = 10,
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { imagePicker.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Image, contentDescription = stringResource(R.string.pick_image_title))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (selectedImageUri == null) stringResource(R.string.add_phototitle) else stringResource(
                        R.string.change_photo_title
                    ))
                }

                if (selectedImageUri != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        elevation = CardDefaults.cardElevation(0.dp), // Keeps it glassy
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            if (isUploading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
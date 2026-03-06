package com.newhanchat.v1.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed // 🚀 Changed to itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.newhanchat.v1.ui.components.PostCard
import com.newhanchat.v1.ui.viewmodel.PostViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

@Composable
fun PostListScreen(
    viewModel: PostViewModel,
    currentUserId: String
) {
    // 1. Observe ViewModel State
    val posts by viewModel.posts.collectAsState()
    val userMap by viewModel.userMap.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var zoomedImageUrl by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // 2. Error Handling
    if (error != null) {
        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        viewModel.clearError()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Post")
            }
        }
    ) { padding ->
        // 🚀 UPDATED CONDITION: Only show the massive spinner if we are loading the very FIRST page
        if (isLoading && posts.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 🚀 CHANGED: items to itemsIndexed
                itemsIndexed(posts) { index, post ->
                    val name = userMap[post.authorId] ?: "Unknown"
                    PostCard(
                        post = post,
                        username = if (post.authorId == currentUserId) "Me ($name)" else name,
                        isMine = post.authorId == currentUserId,
                        onDelete = { viewModel.deletePost(post.id) },
                        onImageClick = { url -> zoomedImageUrl = url }
                    )

                    // 🚀 PAGINATION TRIGGER 🚀
                    // Triggers the next load when scrolling near the bottom
                    LaunchedEffect(index) {
                        if (index >= posts.size - 2) {
                            viewModel.loadMorePosts()
                        }
                    }
                }
            }
        }
    }

    // 3. Zoom Dialog
    if (zoomedImageUrl != null) {
        FullScreenImageDialog(
            imageUrl = zoomedImageUrl!!,
            onDismiss = { zoomedImageUrl = null }
        )
    }

    // 4. Create Post Dialog
    if (showDialog) {
        CreatePostDialog(
            onDismiss = { showDialog = false },
            onPost = { content, uri ->
                // Helper to convert URI to FilePart
                val filePart = prepareFilePart(context, uri)
                // Call ViewModel
                viewModel.createPost(content, filePart) {
                    showDialog = false
                }
            }
        )
    }
}

@Composable
fun CreatePostDialog(
    onDismiss: () -> Unit,
    onPost: (String, Uri?) -> Unit // Changed: No ApiService, just a callback
) {
    var content by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isSubmitting by remember { mutableStateOf(false) } // Local UI state

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> selectedImageUri = uri }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Post") },
        text = {
            Column {
                TextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("What's on your mind?") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Text(if (selectedImageUri == null) "Pick Image" else "Image Selected")
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isSubmitting,
                onClick = {
                    if (content.isBlank()) return@Button
                    isSubmitting = true
                    onPost(content, selectedImageUri)
                }
            ) {
                if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                else Text("Post")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun FullScreenImageDialog(imageUrl: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black)
        ) {
            var scale by remember { mutableFloatStateOf(1f) }
            var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

            AsyncImage(
                model = imageUrl,
                contentDescription = "Zoomed Image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 4f)
                            offset += pan
                        }
                    }
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }
    }
}

// Utility to convert URI to MultipartBody.Part
fun prepareFilePart(context: Context, uri: Uri?): MultipartBody.Part? {
    if (uri == null) return null
    return try {
        val contentResolver = context.contentResolver
        val tempFile = File(context.cacheDir, "upload_temp_${System.currentTimeMillis()}.jpg")
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val outputStream = FileOutputStream(tempFile)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        val requestBody = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        MultipartBody.Part.createFormData("file", tempFile.name, requestBody)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
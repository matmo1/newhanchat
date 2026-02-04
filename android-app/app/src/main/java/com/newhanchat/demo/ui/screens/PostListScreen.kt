package com.newhanchat.demo.ui.screens

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.newhanchat.demo.chatservices.ApiService
import com.newhanchat.demo.loginandregister.PostResponse
import com.newhanchat.demo.loginandregister.PostRequest
import com.newhanchat.demo.ui.components.PostCard
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

@Composable
fun PostListScreen(
    apiService: ApiService,
    currentUserId: String
) {
    var posts by remember { mutableStateOf<List<PostResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Refresh function
    fun refreshPosts() {
        isLoading = true
        scope.launch(kotlinx.coroutines.Dispatchers.Main) {
            try {
                val response = apiService.getAllPosts()
                if (response.isSuccessful) {
                    posts = response.body() ?: emptyList()
                } else {
                    Log.e("PostList", "Error fetching posts: ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // Delete function
    fun deletePost(postId: Long) {
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val response = apiService.deletePost(postId)
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show()
                        refreshPosts()
                    } else {
                        Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(Unit) { refreshPosts() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Post")
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(posts) { post ->
                    // Fallback username if needed
                    val username = if (post.authorId == currentUserId) "Me" else "User ${post.authorId.take(4)}"

                    PostCard(
                        post = post,
                        username = username,
                        isMine = post.authorId == currentUserId,
                        onDelete = { deletePost(post.id) },
                        onImageClick = { /* Handle zoom here if needed */ }
                    )
                }
            }
        }
    }

    if (showDialog) {
        CreatePostDialog(
            onDismiss = { showDialog = false },
            apiService = apiService,
            onPostCreated = {
                showDialog = false
                refreshPosts()
            }
        )
    }
}

@Composable
fun CreatePostDialog(
    onDismiss: () -> Unit,
    apiService: ApiService,
    onPostCreated: () -> Unit
) {
    var content by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isPosting by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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
                enabled = !isPosting,
                onClick = {
                    if (content.isBlank()) return@Button
                    isPosting = true

                    scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            var uploadedUrl: String? = null

                            // 1. Upload Image (if selected)
                            if (selectedImageUri != null) {
                                // THIS IS THE FUNCTION THAT WAS MISSING
                                val filePart = prepareFilePart(context, selectedImageUri!!)

                                if (filePart != null) {
                                    val res = apiService.uploadImage(filePart)
                                    if (res.isSuccessful) {
                                        uploadedUrl = res.body()
                                        Log.d("PostDebug", "Image uploaded: $uploadedUrl")
                                    } else {
                                        val errorMsg = res.errorBody()?.string()
                                        throw Exception("Upload failed: ${res.code()} - $errorMsg")
                                    }
                                } else {
                                    throw Exception("Could not prepare file")
                                }
                            }

                            // 2. Create Post
                            val res = apiService.createPost(PostRequest(content, uploadedUrl))

                            withContext(kotlinx.coroutines.Dispatchers.Main) {
                                if (res.isSuccessful) {
                                    onPostCreated()
                                } else {
                                    val errorMsg = res.errorBody()?.string()
                                    Toast.makeText(context, "Post failed: $errorMsg", Toast.LENGTH_LONG).show()
                                    isPosting = false
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace() // PRINT ERROR TO LOGCAT
                            withContext(kotlinx.coroutines.Dispatchers.Main) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                isPosting = false
                            }
                        }
                    }
                }
            ) {
                if (isPosting) CircularProgressIndicator(modifier = Modifier.size(24.dp)) else Text("Post")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// --- THIS IS THE MISSING FUNCTION ---
fun prepareFilePart(context: Context, uri: Uri): MultipartBody.Part? {
    return try {
        val contentResolver = context.contentResolver
        // Create a temp file in the app's cache directory
        val tempFile = File(context.cacheDir, "upload_temp_${System.currentTimeMillis()}.jpg")

        // Copy the image from the Gallery URI to the temp file
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val outputStream = FileOutputStream(tempFile)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()

        // Create the RequestBody (standard Retrofit logic)
        val requestBody = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())

        // Create the MultipartBody.Part
        MultipartBody.Part.createFormData("file", tempFile.name, requestBody)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
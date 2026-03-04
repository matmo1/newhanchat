package com.newhanchat.demo.ui.screens

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import coil.compose.AsyncImage
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

    // 1. NEW: Store User Names (ID -> Name)
    var userMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var zoomedImageUrl by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // 2. UPDATED: Fetch Posts AND Users
    fun loadData() {
        isLoading = true
        scope.launch(kotlinx.coroutines.Dispatchers.Main) {
            try {
                // A. Get Posts
                val postsRes = apiService.getAllPosts()
                if (postsRes.isSuccessful) {
                    posts = postsRes.body() ?: emptyList()
                }

                // B. Get Users (To fix the names)
                val usersRes = apiService.getUsers()
                if (usersRes.isSuccessful) {
                    val users = usersRes.body() ?: emptyList()
                    // Create map: "12345" -> "John Doe"
                    userMap = users.associate { it.id to "${it.fname} ${it.lname}" }
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
                        loadData() // Refresh
                    } else {
                        Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(Unit) { loadData() }

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

                    // 3. LOGIC: Lookup Name in Map
                    val realName = userMap[post.authorId] ?: "User ${post.authorId.take(4)}"

                    // Display "Me" if it's you, otherwise the real name
                    val displayName = if (post.authorId == currentUserId) "Me ($realName)" else realName

                    PostCard(
                        post = post,
                        username = displayName, // Pass the fixed name
                        isMine = post.authorId == currentUserId,
                        onDelete = { deletePost(post.id) },
                        onImageClick = { url -> zoomedImageUrl = url }
                    )
                }
            }
        }
    }

    if (zoomedImageUrl != null) {
        FullScreenImageDialog(
            imageUrl = zoomedImageUrl!!,
            onDismiss = { zoomedImageUrl = null }
        )
    }

    if (showDialog) {
        CreatePostDialog(
            onDismiss = { showDialog = false },
            apiService = apiService,
            onPostCreated = {
                showDialog = false
                loadData()
            }
        )
    }
}

@Composable
fun FullScreenImageDialog(imageUrl: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
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

                            if (selectedImageUri != null) {
                                val filePart = prepareFilePart(context, selectedImageUri!!)
                                if (filePart != null) {
                                    val res = apiService.uploadImage(filePart)
                                    if (res.isSuccessful) {
                                        uploadedUrl = res.body()
                                    } else {
                                        val errorMsg = res.errorBody()?.string()
                                        throw Exception("Upload failed: ${res.code()} - $errorMsg")
                                    }
                                }
                            }

                            val res = apiService.createPost(PostRequest(content, uploadedUrl))

                            withContext(kotlinx.coroutines.Dispatchers.Main) {
                                if (res.isSuccessful) {
                                    onPostCreated()
                                } else {
                                    Toast.makeText(context, "Post failed", Toast.LENGTH_LONG).show()
                                    isPosting = false
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
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

fun prepareFilePart(context: Context, uri: Uri): MultipartBody.Part? {
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
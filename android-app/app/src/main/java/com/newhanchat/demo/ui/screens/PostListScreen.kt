package com.newhanchat.demo.ui.screens

import android.content.Context
import android.net.Uri
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import com.newhanchat.demo.ui.components.PostCard

// NOTE: Must match your Server IP
private const val SERVER_BASE_URL = "http://192.168.1.89:8082"

@Composable
fun PostListScreen(
    apiService: ApiService,
    currentUserId: String // Received from MainActivity
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
                        refreshPosts() // Reload list
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
                    PostCard(
                        post = post,
                        isMine = post.authorId == currentUserId,
                        onDelete = { deletePost(post.id) }
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
                            if (selectedImageUri != null) {
                                val filePart = prepareFilePart(context, selectedImageUri!!)
                                if (filePart != null) {
                                    val res = apiService.uploadImage(filePart)
                                    if (res.isSuccessful) uploadedUrl = res.body()
                                }
                            }
                            val res = apiService.createPost(PostRequest(content, uploadedUrl))
                            withContext(kotlinx.coroutines.Dispatchers.Main) {
                                if (res.isSuccessful) onPostCreated()
                                else {
                                    Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                                    isPosting = false
                                }
                            }
                        } catch (e: Exception) {
                            withContext(kotlinx.coroutines.Dispatchers.Main) { isPosting = false }
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
        val tempFile = File(context.cacheDir, "upload_temp.jpg")
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val outputStream = FileOutputStream(tempFile)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        val requestBody = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        MultipartBody.Part.createFormData("file", tempFile.name, requestBody)
    } catch (e: Exception) { null }
}
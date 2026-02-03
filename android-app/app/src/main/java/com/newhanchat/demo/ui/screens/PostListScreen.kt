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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
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

@Composable
fun PostListScreen(apiService: ApiService) {
    var posts by remember { mutableStateOf<List<PostResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }

    // Refresh function
    fun refreshPosts() {
        isLoading = true
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            try {
                val response = apiService.getAllPosts()
                if (response.isSuccessful) {
                    posts = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("PostList", "Error fetching posts", e)
            } finally {
                isLoading = false
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
                items(posts) { post -> PostCard(post) }
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

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        selectedImageUri = uri
    }

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

                            // 1. Upload Image (if exists)
                            if (selectedImageUri != null) {
                                val filePart = prepareFilePart(context, selectedImageUri!!)
                                if (filePart != null) {
                                    val res = apiService.uploadImage(filePart)
                                    if (res.isSuccessful) {
                                        uploadedUrl = res.body()
                                        Log.d("Upload", "Success: $uploadedUrl")
                                    } else {
                                        // LOG ERROR
                                        val err = res.errorBody()?.string()
                                        Log.e("Upload", "Failed: ${res.code()} - $err")
                                        withContext(kotlinx.coroutines.Dispatchers.Main) {
                                            Toast.makeText(context, "Upload Failed: ${res.code()}", Toast.LENGTH_LONG).show()
                                            isPosting = false
                                        }
                                        return@launch
                                    }
                                }
                            }

                            // 2. Create Post
                            val postReq = PostRequest(content, uploadedUrl)
                            val postRes = apiService.createPost(postReq)

                            withContext(kotlinx.coroutines.Dispatchers.Main) {
                                if (postRes.isSuccessful) {
                                    onPostCreated()
                                } else {
                                    val err = postRes.errorBody()?.string()
                                    Toast.makeText(context, "Post Failed: $err", Toast.LENGTH_LONG).show()
                                    isPosting = false
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("Upload", "Exception", e)
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
        val tempFile = File(context.cacheDir, "upload_image.jpg")

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

@Composable
fun PostCard(post: PostResponse) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = post.authorId.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(post.authorId, style = MaterialTheme.typography.titleMedium)
                    Text(post.createdAt, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(text = post.content, style = MaterialTheme.typography.bodyLarge)

            if (!post.imageUrl.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                // Ensure URL is complete
                val fullUrl = if (post.imageUrl.startsWith("http")) post.imageUrl
                else "http://192.168.1.89:8082${post.imageUrl}" // Fix relative paths

                AsyncImage(
                    model = fullUrl,
                    contentDescription = "Post Image",
                    modifier = Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
package com.newhanchat.v1.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.newhanchat.v1.BuildConfig
import com.newhanchat.v1.data.model.PostResponse

@Composable
fun PostCard(post: PostResponse, currentUserId: String, onDelete: () -> Unit) {
    // ✨ NEW: State to control the full-screen image viewer
    var showFullImage by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!post.authorProfilePic.isNullOrBlank()) {
                    val pfpUrl = if (post.authorProfilePic.startsWith("http")) post.authorProfilePic
                    else "${BuildConfig.API_BASE_URL}/api/users/media/${post.authorProfilePic}"

                    AsyncImage(
                        model = pfpUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(40.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Default Profile", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = post.authorName ?: ("User " + post.userId.take(5) + "..."),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                if (post.userId == currentUserId) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Post", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (post.content.isNotBlank()) {
                Text(text = post.content, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (!post.imageUrl.isNullOrBlank()) {
                val fullImageUrl = if (post.imageUrl.startsWith("http")) post.imageUrl
                else "${BuildConfig.API_BASE_URL}/api/posts/${post.imageUrl}"

                AsyncImage(
                    model = fullImageUrl,
                    contentDescription = "Post Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showFullImage = true }, // ✨ NEW: Tap to expand!
                    contentScale = ContentScale.Crop
                )

                // ✨ NEW: Full Screen Pinch-to-Zoom Viewer
                if (showFullImage) {
                    Dialog(
                        onDismissRequest = { showFullImage = false },
                        properties = DialogProperties(usePlatformDefaultWidth = false) // Makes it truly full screen
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.95f)),
                            contentAlignment = Alignment.Center
                        ) {
                            var scale by remember { mutableFloatStateOf(1f) }
                            var offset by remember { mutableStateOf(Offset.Zero) }

                            AsyncImage(
                                model = fullImageUrl,
                                contentDescription = "Zoomed Image",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        detectTransformGestures { _, pan, zoom, _ ->
                                            scale = (scale * zoom).coerceIn(1f, 5f) // Limit zoom between 1x and 5x
                                            offset += pan
                                        }
                                    }
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        translationX = offset.x,
                                        translationY = offset.y
                                    ),
                                contentScale = ContentScale.Fit
                            )

                            // Close Button
                            IconButton(
                                onClick = { showFullImage = false },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(32.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
package com.newhanchat.v1.ui.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
// UPDATED FOR COIL 3
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.newhanchat.v1.data.api.TokenManager
import com.newhanchat.v1.data.model.PostResponse

private const val SERVER_BASE_URL = "http://192.168.1.89:8082"

@Composable
fun PostCard(
    post: PostResponse,
    username: String,
    isMine: Boolean,
    onDelete: () -> Unit,
    onImageClick: (String) -> Unit
) {
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
                            text = username.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = username, style = MaterialTheme.typography.titleMedium)
                    Text(text = post.createdAt, style = MaterialTheme.typography.bodySmall)
                }

                if (isMine) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(text = post.content, style = MaterialTheme.typography.bodyLarge)

            if (!post.imageUrl.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))

                val baseUrl = if (post.imageUrl.startsWith("http")) {
                    post.imageUrl
                } else {
                    "$SERVER_BASE_URL${post.imageUrl}"
                }

                val fullUrlWithToken = "$baseUrl?token=${TokenManager.token}"
                val context = LocalContext.current

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(fullUrlWithToken)
                        .crossfade(true)
                        .listener(
                            onError = { _, result ->
                                Log.e("ImageDebug", "❌ FAILED: ${result.throwable.message}")
                            }
                        )
                        .build(),
                    contentDescription = "Post Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onImageClick(fullUrlWithToken) },
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
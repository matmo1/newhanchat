package com.newhanchat.v1.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.newhanchat.v1.BuildConfig
import com.newhanchat.v1.data.model.PostResponse

@Composable
fun PostCard(post: PostResponse, currentUserId: String, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        // ✨ FIXED: Elevation set to 0.dp to stop Material 3 from painting a solid white overlay!
        elevation = CardDefaults.cardElevation(0.dp),
        // Makes the card a beautifully translucent glass surface
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
                    modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
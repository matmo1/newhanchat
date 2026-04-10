package com.newhanchat.v1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.newhanchat.v1.BuildConfig
import com.newhanchat.v1.data.api.apiService
import com.newhanchat.v1.data.model.UserResponse
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(
    onUserSelected: (UserResponse) -> Unit,
    onLogout: () -> Unit,
    currentUserId: String
) {
    var users by remember { mutableStateOf<List<UserResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showStatusMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val response = apiService.getUsers()
            if (response.isSuccessful) {
                users = response.body()?.filter { it.id != currentUserId } ?: emptyList()
            } else {
                errorMessage = "Failed to load users"
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "An error occurred"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("People") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    Box {
                        // ✨ FIXED: Changed from three dots to a text button
                        TextButton(onClick = { showStatusMenu = true }) {
                            Text("Set Status")
                        }
                        DropdownMenu(
                            expanded = showStatusMenu,
                            onDismissRequest = { showStatusMenu = false },
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                        ) {
                            DropdownMenuItem(text = { Text("🟢 Online") }, onClick = { showStatusMenu = false })
                            DropdownMenuItem(text = { Text("🔴 Busy") }, onClick = { showStatusMenu = false })
                            DropdownMenuItem(text = { Text("🟡 Away") }, onClick = { showStatusMenu = false })
                            DropdownMenuItem(text = { Text("⚫ Offline") }, onClick = { showStatusMenu = false })
                        }
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null) {
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
            } else if (users.isEmpty()) {
                Text("No other users found.", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(users) { user ->
                        UserListItem(user = user, onClick = { onUserSelected(user) })
                    }
                }
            }
        }
    }
}

@Composable
fun UserListItem(user: UserResponse, onClick: () -> Unit) {
    val statusColor = when (user.userStatus?.type) {
        "ONLINE" -> Color(0xFF4CAF50)
        "BUSY" -> Color(0xFFF44336)
        "AWAY" -> Color(0xFFFFEB3B)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {

            if (!user.profilePictureUrl.isNullOrBlank()) {
                val fullUrl = if (user.profilePictureUrl.startsWith("http")) user.profilePictureUrl
                else "${BuildConfig.API_BASE_URL}/api/users/media/${user.profilePictureUrl}"

                AsyncImage(
                    model = fullUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(48.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(statusColor))

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(text = "${user.fname} ${user.lname}", style = MaterialTheme.typography.titleMedium)
                Text(text = "@${user.username}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                if (user.userStatus?.type == "OFFLINE" && user.userStatus.lastActive != null) {
                    Text(text = "Last seen: ${user.userStatus.lastActive}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
    }
}
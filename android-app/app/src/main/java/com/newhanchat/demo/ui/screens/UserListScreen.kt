package com.newhanchat.demo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.newhanchat.demo.chatservices.apiService
import com.newhanchat.demo.loginandregister.UserResponse
import kotlinx.coroutines.launch

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
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val response = apiService.getUsers()
            if (response.isSuccessful) {
                users = response.body()?.filter { it.id != currentUserId } ?: emptyList()
            } else {
                errorMessage = "Failed to load users: ${response.code()}"
            }
        } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Contacts", style = MaterialTheme.typography.headlineMedium)
                TextButton(onClick = { showStatusMenu = true }) {
                    Text("Set My Status ▾")
                }
                DropdownMenu(
                    expanded = showStatusMenu,
                    onDismissRequest = { showStatusMenu = false }
                ) {
                    listOf("ONLINE", "BUSY", "AWAY", "OFFLINE").forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status) },
                            onClick = {
                                scope.launch {
                                    try {
                                        apiService.updateUserStatus(currentUserId, status)
                                        showStatusMenu = false
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        )
                    }
                }
            }
            IconButton(onClick = onLogout) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
        } else if (users.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No other users found.")
            }
        } else {
            LazyColumn {
                items(users) { user ->
                    UserCard(user = user, onClick = { onUserSelected(user) })
                }
            }
        }
    }
}

@Composable
fun UserCard(user: UserResponse, onClick: () -> Unit) {
    val statusColor = when (user.userStatus?.type) {
        "ONLINE" -> Color.Green
        "BUSY" -> Color.Red
        "AWAY" -> Color.Yellow
        else -> Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(statusColor, shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "${user.fname} ${user.lname}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "@${user.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                if (user.userStatus?.type == "OFFLINE" && user.userStatus.lastActive != null) {
                    Text(
                        text = "Last seen: ${user.userStatus.lastActive}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
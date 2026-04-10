package com.newhanchat.v1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.newhanchat.v1.data.api.ChatManager
import com.newhanchat.v1.data.api.apiService
import com.newhanchat.v1.data.model.ChatMessage
import com.newhanchat.v1.data.model.ChatMessageDTO
import com.newhanchat.v1.data.model.UserResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatManager: ChatManager,
    myUserId: String,
    recipientId: String,
    onBack: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMessageDTO>() }
    var editingMessageId by remember { mutableStateOf<String?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editContent by remember { mutableStateOf("") }

    var recipientProfile by remember { mutableStateOf<UserResponse?>(null) }

    LaunchedEffect(recipientId) {
        try {
            val response = apiService.getHistory(senderId = myUserId, recipientId = recipientId)
            if (response.isSuccessful && response.body() != null) {
                messages.clear()
                messages.addAll(response.body()!!.reversed())
            }
            recipientProfile = apiService.getUserProfile(recipientId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    LaunchedEffect(Unit) {
        chatManager.incomingMessages.collect { newMessage ->
            val isFromRecipient = newMessage.senderId == recipientId
            val isFromMeToRecipient = newMessage.senderId == myUserId && newMessage.recipientId == recipientId

            if (isFromRecipient) {
                messages.add(0, newMessage)
            } else if (isFromMeToRecipient) {
                val pendingIndex = messages.indexOfLast { it.senderId == myUserId && it.id == null }
                if (pendingIndex != -1) messages[pendingIndex] = newMessage
                else messages.add(0, newMessage)
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Message") },
            text = { TextField(value = editContent, onValueChange = { editContent = it }) },
            confirmButton = {
                Button(onClick = {
                    if (editingMessageId != null) {
                        chatManager.editMessage(editingMessageId!!, editContent)
                        val index = messages.indexOfFirst { it.id == editingMessageId }
                        if (index != -1) messages[index] = messages[index].copy(content = editContent)
                    }
                    showEditDialog = false
                }) { Text("Save") }
            },
            dismissButton = { Button(onClick = { showEditDialog = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!recipientProfile?.profilePictureUrl.isNullOrBlank()) {
                            val fullUrl = if (recipientProfile!!.profilePictureUrl!!.startsWith("http")) recipientProfile!!.profilePictureUrl
                            else "${BuildConfig.API_BASE_URL}/api/users/media/${recipientProfile!!.profilePictureUrl}"

                            AsyncImage(
                                model = fullUrl, contentDescription = "PFP",
                                modifier = Modifier.size(36.dp).clip(CircleShape), contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }

                        val isOnline = recipientProfile?.userStatus?.type == "ONLINE"
                        Box(
                            modifier = Modifier.padding(start = 6.dp).size(10.dp).clip(CircleShape)
                                .background(if (isOnline) Color(0xFF4CAF50) else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(recipientProfile?.fname ?: "Chat")
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp)) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                reverseLayout = true,
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(messages) { msg ->
                    val isMe = msg.senderId == myUserId
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(4.dp),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier
                                .widthIn(max = 300.dp)
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = {
                                        if (isMe && msg.id != null) {
                                            editingMessageId = msg.id
                                            editContent = msg.content
                                            showEditDialog = true
                                        }
                                    }
                                )
                        ) {
                            Text(text = msg.content, modifier = Modifier.padding(12.dp))
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") },
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    if (messageText.isNotBlank()) {
                        chatManager.sendMessage(ChatMessage(content = messageText, recipientId = recipientId))
                        messages.add(0, ChatMessageDTO(id = null, content = messageText, senderId = myUserId, recipientId = recipientId, timestamp = null))
                        messageText = ""
                    }
                }) {
                    Text("Send")
                }
            }
        }
    }
}
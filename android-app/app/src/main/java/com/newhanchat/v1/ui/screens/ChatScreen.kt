package com.newhanchat.v1.ui.screens

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
    recipient: UserResponse,
    onBack: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }

    // Index 0 = Bottom of screen (Newest message)
    val messages = remember { mutableStateListOf<ChatMessageDTO>() }

    // Edit State
    var editingMessageId by remember { mutableStateOf<String?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editContent by remember { mutableStateOf("") }

    // 1. Fetch History
    LaunchedEffect(recipient.id) {
        try {
            val response = apiService.getHistory(senderId = myUserId, recipientId = recipient.id)
            if (response.isSuccessful && response.body() != null) {
                messages.clear()
                // REVERSE history so newest is at index 0 (Bottom)
                messages.addAll(response.body()!!.reversed())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 2. Listen for Incoming
    LaunchedEffect(Unit) {
        chatManager.incomingMessages.collect { newMessage ->
            val isFromRecipient = newMessage.senderId == recipient.id
            val isFromMeToRecipient = newMessage.senderId == myUserId && newMessage.recipientId == recipient.id

            if (isFromRecipient) {
                // Add to bottom (Index 0)
                messages.add(0, newMessage)
            } else if (isFromMeToRecipient) {
                // Find pending message (id is null) and replace it
                val pendingIndex = messages.indexOfLast { it.senderId == myUserId && it.id == null }
                if (pendingIndex != -1) {
                    messages[pendingIndex] = newMessage
                } else {
                    messages.add(0, newMessage)
                }
            }
        }
    }

    // 3. Edit Dialog logic
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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Chat with ${recipient.fname}", style = MaterialTheme.typography.titleMedium)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Messages List
        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = true, // KEY FIX: Anchors content to bottom
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
                            containerColor = if (isMe) MaterialTheme.colorScheme.primaryContainer else Color.LightGray
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

        // Input
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (messageText.isNotBlank()) {
                    // Send to Websocket
                    chatManager.sendMessage(ChatMessage(content = messageText, recipientId = recipient.id))

                    // Add optimistic local message (id = null)
                    messages.add(0, ChatMessageDTO(
                        id = null,
                        content = messageText,
                        senderId = myUserId,
                        recipientId = recipient.id,
                        timestamp = null
                    ))
                    messageText = ""
                }
            }) {
                Text("Send")
            }
        }
    }
}
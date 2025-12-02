package com.newhanchat.demo.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.newhanchat.demo.chatservices.ChatManager
import com.newhanchat.demo.chatservices.apiService
import com.newhanchat.demo.loginandregister.ChatMessage
import com.newhanchat.demo.loginandregister.ChatMessageDTO
import com.newhanchat.demo.loginandregister.UserResponse

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(
    chatManager: ChatManager,
    myUserId: String,
    recipient: UserResponse,
    onBack: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
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
                messages.addAll(response.body()!!)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 2. Listen for Incoming Messages
    LaunchedEffect(Unit) {
        chatManager.incomingMessages.collect { newMessage ->
            val isFromRecipient = newMessage.senderId == recipient.id
            val isFromMeToRecipient = newMessage.senderId == myUserId && newMessage.recipientId == recipient.id

            if (isFromRecipient) {
                // Message from them -> Add to bottom
                messages.add(newMessage)
            } else if (isFromMeToRecipient) {
                // Message from ME (Echo from server with the real ID)

                // Find the temporary message we added earlier (it has null ID)
                val pendingIndex = messages.indexOfLast { it.senderId == myUserId && it.id == null }

                if (pendingIndex != -1) {
                    // Replace the temporary message with the real one (now we have the ID!)
                    messages[pendingIndex] = newMessage
                } else {
                    // If we didn't find a pending one (rare), just add it
                    messages.add(newMessage)
                }
            }
        }
    }

    // 3. Listen for Edits
    LaunchedEffect(Unit) {
        chatManager.messageUpdates.collect { update ->
            if (update.type == "MESSAGE_EDIT") {
                val index = messages.indexOfFirst { it.id == update.messageId }
                if (index != -1) {
                    val old = messages[index]
                    messages[index] = old.copy(content = update.newContent)
                }
            }
        }
    }

    // Edit Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Message") },
            text = {
                TextField(value = editContent, onValueChange = { editContent = it })
            },
            confirmButton = {
                Button(onClick = {
                    if (editingMessageId != null) {
                        chatManager.editMessage(editingMessageId!!, editContent)
                        // Optimistic update
                        val index = messages.indexOfFirst { it.id == editingMessageId }
                        if (index != -1) {
                            messages[index] = messages[index].copy(content = editContent)
                        }
                    }
                    showEditDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                Button(onClick = { showEditDialog = false }) { Text("Cancel") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onBack) { Text("<") }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Chat with ${recipient.fname}", style = MaterialTheme.typography.titleMedium)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(messages) { msg ->
                val isMe = msg.senderId == myUserId
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isMe) MaterialTheme.colorScheme.primaryContainer else Color.LightGray
                        ),
                        modifier = Modifier
                            .padding(4.dp)
                            .combinedClickable(
                                onClick = { },
                                onLongClick = {
                                    if (isMe) {
                                        editingMessageId = msg.id
                                        editContent = msg.content
                                        showEditDialog = true
                                    }
                                }
                            )
                    ) {
                        Text(text = msg.content, modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") }
            )
            Button(onClick = {
                if (messageText.isNotBlank()) {
                    chatManager.sendMessage(ChatMessage(content = messageText, recipientId = recipient.id))
                    messages.add(
                        ChatMessageDTO(
                            id = null,
                            content = messageText,
                            senderId = myUserId,
                            recipientId = recipient.id,
                            timestamp = null
                        )
                    )
                    messageText = ""
                }
            }) {
                Text("Send")
            }
        }
    }
}
package com.newhanchat.v1.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newhanchat.v1.data.api.ApiService
import com.newhanchat.v1.data.api.ChatManager
import com.newhanchat.v1.data.model.ChatMessage
import com.newhanchat.v1.data.model.ChatMessageDTO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val apiService: ApiService,
    private val chatManager: ChatManager
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessageDTO>>(emptyList())
    val messages: StateFlow<List<ChatMessageDTO>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private var currentUserId: String = ""
    private var currentRecipientId: String = ""

    init {
        // Listen for new incoming messages from WebSockets
        viewModelScope.launch {
            chatManager.incomingMessages.collect { newMessage ->
                handleIncomingMessage(newMessage)
            }
        }

        // Listen for message edits
        viewModelScope.launch {
            chatManager.messageUpdates.collect { update ->
                val currentList = _messages.value.toMutableList()
                val index = currentList.indexOfFirst { it.id == update.messageId }
                if (index != -1) {
                    currentList[index] = currentList[index].copy(content = update.newContent)
                    _messages.value = currentList
                }
            }
        }
    }

    fun loadChat(myUserId: String, recipientId: String) {
        currentUserId = myUserId
        currentRecipientId = recipientId
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Fetch history using the corrected API endpoint
                val response = apiService.getHistory(senderId = myUserId, recipientId = recipientId)
                if (response.isSuccessful) {
                    // Reverse so the newest message is at index 0 (bottom of the screen)
                    _messages.value = response.body()?.reversed() ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return

        val newMessage = ChatMessage(content = content, recipientId = currentRecipientId)

        // 1. Send via WebSocket
        chatManager.sendMessage(newMessage)

        // 2. Optimistic UI Update (Show immediately)
        val optimisticMsg = ChatMessageDTO(
            id = null, // Null means it's still sending
            senderId = currentUserId,
            recipientId = currentRecipientId,
            content = content,
            timestamp = null
        )
        _messages.value = listOf(optimisticMsg) + _messages.value
    }

    fun editMessage(messageId: String, newContent: String) {
        chatManager.editMessage(messageId, newContent)
        // Optimistic UI update for the edit
        val currentList = _messages.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == messageId }
        if (index != -1) {
            currentList[index] = currentList[index].copy(content = newContent)
            _messages.value = currentList
        }
    }

    private fun handleIncomingMessage(newMessage: ChatMessageDTO) {
        val isFromRecipient = newMessage.senderId == currentRecipientId
        val isFromMeToRecipient = newMessage.senderId == currentUserId && newMessage.recipientId == currentRecipientId

        val currentList = _messages.value.toMutableList()

        if (isFromRecipient) {
            currentList.add(0, newMessage)
        } else if (isFromMeToRecipient) {
            // Find the pending message (with null ID) and replace it with the confirmed server message
            val pendingIndex = currentList.indexOfFirst { it.senderId == currentUserId && it.id == null }
            if (pendingIndex != -1) {
                currentList[pendingIndex] = newMessage
            } else {
                currentList.add(0, newMessage)
            }
        }
        _messages.value = currentList
    }
}
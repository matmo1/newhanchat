package com.newhanchat.v1.data.model

data class ChatMessageDTO(
    val id: String?,
    val content: String,
    val senderId: String,
    val recipientId: String,
    val timestamp: String? // We'll handle date as String for simplicity
)
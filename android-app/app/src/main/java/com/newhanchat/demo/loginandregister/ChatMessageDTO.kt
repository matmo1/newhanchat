package com.newhanchat.demo.loginandregister

data class ChatMessageDTO(
    val id: String?,
    val content: String,
    val senderId: String,
    val recipientId: String,
    val timestamp: String? // We'll handle date as String for simplicity
)
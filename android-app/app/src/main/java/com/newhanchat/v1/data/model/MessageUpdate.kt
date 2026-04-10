package com.newhanchat.v1.data.model

data class MessageUpdate(
    val type: String,
    val messageId: String,
    val newContent: String,
    val editedAt: String?
)

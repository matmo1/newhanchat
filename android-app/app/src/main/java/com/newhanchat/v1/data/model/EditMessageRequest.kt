package com.newhanchat.v1.data.model

data class EditMessageRequest(
    val messageId: String,
    val newContent: String
)
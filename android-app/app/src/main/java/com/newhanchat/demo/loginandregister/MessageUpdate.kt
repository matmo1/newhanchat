package com.newhanchat.demo.loginandregister

data class MessageUpdate(
    val type: String,
    val messageId: String,
    val newContent: String,
    val editedAt: String?
)

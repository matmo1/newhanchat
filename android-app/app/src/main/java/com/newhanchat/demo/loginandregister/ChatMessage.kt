package com.newhanchat.demo.loginandregister

import com.google.gson.annotations.SerializedName

data class ChatMessage(
    @SerializedName("content")
    val content: String,

    val recipientId: String
)
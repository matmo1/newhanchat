package com.newhanchat.v1.data.model

import com.google.gson.annotations.SerializedName

data class ChatMessage(
    @SerializedName("content")
    val content: String,

    val recipientId: String
)
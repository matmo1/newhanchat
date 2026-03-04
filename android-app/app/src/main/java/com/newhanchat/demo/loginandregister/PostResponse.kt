package com.newhanchat.demo.loginandregister

import com.google.gson.annotations.SerializedName

data class PostResponse(
    val id: Long,
    val content: String,
    val imageUrl: String?,

    // FIXED: Backend sends "userId", mapping it to authorId
    @SerializedName("userId")
    val authorId: String,

    val createdAt: String
)
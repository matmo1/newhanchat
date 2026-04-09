package com.newhanchat.v1.data.model

data class PostRequest(
    val content: String,
    val imageUrl: String?,
    val authorName: String,
    val authorProfilePic: String
)
package com.newhanchat.v1.data.model

import com.google.gson.annotations.SerializedName

data class PostResponse(
    val id: Long,
    val content: String,
    val imageUrl: String?,

    // FIXED: Backend sends "userId", mapping it to authorId
    @SerializedName("userId")
    val userId: String,
    val authorName: String?,       // ✨ NEW (Nullable just in case old posts don't have it)
    val authorProfilePic: String?,
    val createdAt: String
)
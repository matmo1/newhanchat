package com.newhanchat.v1.data.model

data class UserResponse(
    val id: String,
    val username: String,
    val fname: String,
    val lname: String,
    val userStatus: UserStatus?, // Make sure you have a UserStatus data class!
    val bio: String?,            // ✨ Added the bio (nullable in case they haven't set one)
    val profilePictureUrl: String?
)
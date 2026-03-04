package com.newhanchat.v1.data.model

data class UserResponse(
    val id: String,
    val username: String,
    val fname: String,
    val lname: String,
    val userStatus: UserStatus?,
    val profilePictureUrl: String?
)
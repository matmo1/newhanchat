package com.newhanchat.demo.loginandregister

data class UserResponse(
    val id: String,
    val username: String,
    val fname: String,
    val lname: String,
    val userStatus: UserStatus?
)
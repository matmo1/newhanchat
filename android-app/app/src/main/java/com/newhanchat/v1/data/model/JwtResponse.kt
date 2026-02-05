package com.newhanchat.v1.data.model

data class JwtResponse(
    val userId: String,
    val token: String,
    val username: String
)

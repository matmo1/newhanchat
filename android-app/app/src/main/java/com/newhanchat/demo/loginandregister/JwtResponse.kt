package com.newhanchat.demo.loginandregister

data class JwtResponse(
    val userId: String,
    val token: String,
    val username: String
)

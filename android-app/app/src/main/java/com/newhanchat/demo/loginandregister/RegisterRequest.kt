package com.newhanchat.demo.loginandregister

data class RegisterRequest(
    val userName: String,  // Note the capital 'N' to match backend
    val fname: String,
    val lname: String,
    val dOfBirth: String,  // Send as String "YYYY-MM-DDTHH:MM:SS"
    val password: String
)
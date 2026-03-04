package com.newhan.chatservice.dto;

public record JwtResponseDTO(
    String token,   // <--- Ensure 'token' is FIRST
    String userId,  // <--- 'userId' is SECOND
    String username // <--- 'username' is THIRD
) {}

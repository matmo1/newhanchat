package com.newhan.chatservice.dto.userdtos; // (Adjust package name per service)

public record UserUpdateEvent(
    String userId,       // ✨ Added for the post-service database
    String username,     // ✨ Kept for the chat-service WebSockets
    String fullName,
    String profilePictureUrl
) {}
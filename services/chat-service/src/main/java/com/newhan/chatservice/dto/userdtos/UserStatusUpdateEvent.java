package com.newhan.chatservice.dto.userdtos; // Use com.newhan.chatservice.dto in the chat service!

import java.time.LocalDateTime;

public record UserStatusUpdateEvent(
    String username,
    String status, // "ONLINE" or "OFFLINE"
    LocalDateTime lastSeen
) {}
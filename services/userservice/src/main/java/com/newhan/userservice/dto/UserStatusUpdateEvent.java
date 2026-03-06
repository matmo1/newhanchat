package com.newhan.userservice.dto; // Use com.newhan.chatservice.dto in the chat service!

import java.time.LocalDateTime;

public record UserStatusUpdateEvent(
    String username,
    String status, // "ONLINE" or "OFFLINE"
    LocalDateTime lastSeen
) {}
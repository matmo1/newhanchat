package com.newhan.chatservice.dto.userdtos;

public record UserUpdateEvent(
    String username,
    String fullName,
    String profilePictureUrl
) {}
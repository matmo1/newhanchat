package com.newhan.userservice.dto;

public record UserUpdateEvent(
    String username,
    String fullName,
    String profilePictureUrl
) {} 

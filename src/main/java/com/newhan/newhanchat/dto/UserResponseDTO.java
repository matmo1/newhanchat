package com.newhan.newhanchat.dto;

import org.bson.types.ObjectId;

import com.newhan.newhanchat.model.user.UserStatus;

public record UserResponseDTO(
    ObjectId id,
    String username,
    String fname,
    String lname,
    UserStatus userStatus 
) {}

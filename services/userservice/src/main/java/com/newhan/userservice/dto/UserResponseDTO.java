package com.newhan.userservice.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.newhan.userservice.model.UserStatus;

public record UserResponseDTO(
    @JsonSerialize(using = ToStringSerializer.class)
    String id,
    String username,
    String fname,
    String lname,
    UserStatus userStatus,
    String bio,
    String profilePictureUrl
) {}

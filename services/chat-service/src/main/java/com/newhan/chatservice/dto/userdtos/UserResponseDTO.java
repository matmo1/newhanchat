package com.newhan.chatservice.dto.userdtos;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.newhan.chatservice.model.user.UserStatus;

public record UserResponseDTO(
    @JsonSerialize(using = ToStringSerializer.class)
    ObjectId id,
    String username,
    String fname,
    String lname,
    UserStatus userStatus 
) {}

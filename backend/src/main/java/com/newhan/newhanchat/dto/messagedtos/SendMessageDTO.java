package com.newhan.newhanchat.dto.messagedtos;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import jakarta.validation.constraints.NotBlank;

public record SendMessageDTO(
    @NotBlank
    String content,
    @NotBlank
    @JsonSerialize(using = ToStringSerializer.class)
    ObjectId recipientId
) {}
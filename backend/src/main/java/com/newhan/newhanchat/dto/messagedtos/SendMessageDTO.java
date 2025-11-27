package com.newhan.newhanchat.dto.messagedtos;

import org.bson.types.ObjectId;

import jakarta.validation.constraints.NotBlank;

public record SendMessageDTO(
    @NotBlank
    String content,
    @NotBlank
    ObjectId recipientId
) {}
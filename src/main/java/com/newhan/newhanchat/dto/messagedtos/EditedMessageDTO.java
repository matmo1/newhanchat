package com.newhan.newhanchat.dto.messagedtos;

import org.bson.types.ObjectId;

import jakarta.validation.constraints.NotBlank;

public record EditedMessageDTO(
    @NotBlank ObjectId mesId,
    @NotBlank String newContent
) {}

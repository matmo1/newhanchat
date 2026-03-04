package com.newhan.chatservice.dto.messagedtos;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import jakarta.validation.constraints.NotBlank;

public record EditedMessageDTO(
    @JsonSerialize(using = ToStringSerializer.class)
    @NotBlank ObjectId mesId,
    @NotBlank String newContent
) {}

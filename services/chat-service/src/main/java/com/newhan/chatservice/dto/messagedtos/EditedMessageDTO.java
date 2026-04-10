package com.newhan.chatservice.dto.messagedtos;

import jakarta.validation.constraints.NotBlank;

public record EditedMessageDTO(
    @NotBlank String messageId,
    @NotBlank String newContent
) {}
package com.newhan.chatservice.dto.messagedtos;

import jakarta.validation.constraints.NotBlank;

public record SendMessageDTO(
    @NotBlank String content,
    @NotBlank String recipientId
) {}
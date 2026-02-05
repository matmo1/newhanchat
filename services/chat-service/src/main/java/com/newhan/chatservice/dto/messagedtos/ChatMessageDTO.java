package com.newhan.chatservice.dto.messagedtos;

import com.newhan.chatservice.model.chatmessage.MessageSatus;
import java.time.LocalDateTime;

public record ChatMessageDTO(
    String id,
    String senderId,
    String recipientId,
    String content,
    LocalDateTime timestamp,
    MessageSatus status,
    boolean edited,
    LocalDateTime lastEdited
) {}
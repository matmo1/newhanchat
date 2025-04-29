package com.newhan.newhanchat.dto.messagedtos;

import java.time.LocalDateTime;

import org.bson.types.ObjectId;

import com.newhan.newhanchat.model.chatmessage.MessageSatus;

public record ChatMessageDTO(
    ObjectId id,
    String content,
    ObjectId senderId,
    ObjectId recipientId,
    LocalDateTime timestamp,
    MessageSatus status
) {}

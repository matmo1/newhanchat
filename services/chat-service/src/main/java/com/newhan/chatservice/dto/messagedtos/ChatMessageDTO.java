package com.newhan.chatservice.dto.messagedtos;

import java.time.LocalDateTime;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.newhan.chatservice.model.chatmessage.MessageSatus;

public record ChatMessageDTO(
    @JsonSerialize(using = ToStringSerializer.class)
    ObjectId id,

    String content,

    @JsonSerialize(using = ToStringSerializer.class)
    ObjectId senderId,

    @JsonSerialize(using = ToStringSerializer.class)
    ObjectId recipientId,
    
    LocalDateTime timestamp,
    MessageSatus status,
    boolean edited,
    LocalDateTime lastEdited
) {}

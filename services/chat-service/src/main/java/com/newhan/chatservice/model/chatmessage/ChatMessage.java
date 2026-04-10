package com.newhan.chatservice.model.chatmessage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "chat_messages")
public class ChatMessage {
    
    @Id
    private String id; // Mongo generates this as an ObjectId automatically

    private String chatId; // Auto-generated: "minUUID_maxUUID"
    private String senderId;
    private String recipientId;
    private String content;
    private Date timestamp;

    private boolean isEdited;
    private Date lastEdited;
    
    private MessageSatus status; // SENT, DELIVERED, READ
}
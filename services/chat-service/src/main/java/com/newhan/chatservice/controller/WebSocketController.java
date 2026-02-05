package com.newhan.chatservice.controller;

import com.newhan.chatservice.dto.messagedtos.ChatMessageDTO;
import com.newhan.chatservice.dto.messagedtos.EditedMessageDTO;
import com.newhan.chatservice.dto.messagedtos.SendMessageDTO;
import com.newhan.chatservice.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;

    // --- 1. SEND MESSAGE ---
    @MessageMapping("/chat")
    public void processMessage(@Payload SendMessageDTO messageDTO,
                               Authentication authentication) {

        // 1. Get Sender ID (String UUID from the JWT Token)
        String senderId = authentication.getName();

        // 2. Delegate to Service
        // The service should create the Entity, set timestamps, and return the DTO
        ChatMessageDTO savedMessage = chatMessageService.saveMessage(messageDTO, senderId);

        // 3. Send to Recipient's Queue
        messagingTemplate.convertAndSendToUser(
                savedMessage.recipientId(), 
                "/queue/messages", 
                savedMessage
        );

        // 4. Send Confirmation to Sender's Queue (so UI updates ID/Status)
        messagingTemplate.convertAndSendToUser(
                savedMessage.senderId(), 
                "/queue/messages", 
                savedMessage
        );
    }

    // --- 2. EDIT MESSAGE ---
    @MessageMapping("/edit")
    public void handleEdit(@Payload EditedMessageDTO dto,
                           Authentication authentication) {
        
        String requesterId = authentication.getName();

        ChatMessageDTO editedMessage = chatMessageService.editMessage(dto, requesterId);

        if (editedMessage != null) {
            Map<String, Object> updatePayload = Map.of(
                "type", "MESSAGE_EDIT",
                "messageId", editedMessage.id(),
                "newContent", editedMessage.content(),
                "editedAt", editedMessage.lastEdited()
            );

            // Notify Recipient
            messagingTemplate.convertAndSendToUser(
                editedMessage.recipientId(), 
                "/queue/message-updates", 
                updatePayload
            );
            
            // Notify Sender
            messagingTemplate.convertAndSendToUser(
                editedMessage.senderId(), 
                "/queue/message-updates", 
                updatePayload
            );
        }
    }
}
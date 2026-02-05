package com.newhan.chatservice.controller;

import com.newhan.chatservice.model.chatmessage.ChatMessage;
import com.newhan.chatservice.model.chatmessage.MessageSatus;
import com.newhan.chatservice.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class MessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage chatMessage) {
        chatMessage.setTimestamp(new Date());
        chatMessage.setStatus(MessageSatus.DELIVERED);
        
        // Save to Mongo
        ChatMessage saved = chatMessageService.saveMessage(chatMessage);
        
        // Send to Recipient's Queue
        messagingTemplate.convertAndSendToUser(
                chatMessage.getRecipientId(), "/queue/messages", saved);
    }

    // Fix: REST endpoint to get history
    @GetMapping("/api/messages/{senderId}/{recipientId}")
    public ResponseEntity<List<ChatMessage>> getChatMessages(
            @PathVariable String senderId,
            @PathVariable String recipientId) {
        
        return ResponseEntity.ok(chatMessageService.findChatMessages(senderId, recipientId));
    }
}
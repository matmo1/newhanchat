package com.newhan.chatservice.controller;

import com.newhan.chatservice.dto.messagedtos.ChatMessageDTO;
import com.newhan.chatservice.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/messages") // Standardize the base path
@RequiredArgsConstructor
public class MessageController {

    private final ChatMessageService chatMessageService;

    // REMOVED: @MessageMapping("/chat") - This is now handled by WebSocketController
    // REMOVED: SimpMessagingTemplate - Not needed for fetching history

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessageDTO>> getChatMessages(
            @RequestParam("senderId") String senderId,
            @RequestParam("recipientId") String recipientId) {
        
        // The service now returns DTOs, so we pass them through
        return ResponseEntity.ok(chatMessageService.findChatMessages(senderId, recipientId));
    }
}
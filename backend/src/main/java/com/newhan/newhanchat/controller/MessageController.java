package com.newhan.newhanchat.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.newhan.newhanchat.dto.messagedtos.ChatMessageDTO;
import com.newhan.newhanchat.service.ChatMessageService;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/messagages")
public class MessageController {
    private final ChatMessageService chatMessageService;

    public MessageController(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    @GetMapping("/history")
    public List<ChatMessageDTO> getHistory(
        @RequestParam ObjectId senderId,
        @RequestParam ObjectId recipientId
        ) {
        return chatMessageService.getChatHistory(senderId, recipientId);
    }
    
}

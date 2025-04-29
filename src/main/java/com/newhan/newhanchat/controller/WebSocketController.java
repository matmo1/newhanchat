package com.newhan.newhanchat.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import com.newhan.newhanchat.dto.messagedtos.ChatMessageDTO;
import com.newhan.newhanchat.dto.messagedtos.EditedMessageDTO;
import com.newhan.newhanchat.dto.messagedtos.SendMessageDTO;
import com.newhan.newhanchat.service.ChatMessageService;

@Controller
public class WebSocketController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;

    public WebSocketController(SimpMessagingTemplate messagingTemplate, ChatMessageService chatMessageService) {
        this.messagingTemplate = messagingTemplate;
        this.chatMessageService = chatMessageService;
    }

    @MessageMapping("/chat")
    public void processMessage(@Payload SendMessageDTO messageDTO, @Header("simpSeesioId") ObjectId sessionId, @AuthenticationPrincipal ObjectId  senderId) {
        ChatMessageDTO savedMessage = chatMessageService.saveMessage(messageDTO, senderId);

        messagingTemplate.convertAndSendToUser(messageDTO.recipientId().toString(), "/queue/messages", savedMessage);

        messagingTemplate.convertAndSendToUser(senderId.toString(), "/queue/notifications", Map.of("type", "DELIVERY_CONFIRMATION", "messageId", savedMessage.id(), "timestamp", LocalDateTime.now()));
    }

    @MessageMapping("/edit")
    public void handleEdit(
        @Payload EditedMessageDTO dto,
        @AuthenticationPrincipal ObjectId requesterId
    ) {
        ChatMessageDTO editedMessage = chatMessageService.editMessage(dto, requesterId);

        messagingTemplate.convertAndSendToUser(editedMessage.recipientId().toString(), "/queue/message-updates", 
            Map.of(
                "type", "MESSAGE_EDIT",
                "messageId", editedMessage.id(),
                "newContent", editedMessage.content(),
                "editedAt", editedMessage.lastEdited()));
    }
}

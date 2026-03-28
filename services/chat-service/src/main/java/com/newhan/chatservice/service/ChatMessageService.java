package com.newhan.chatservice.service;

import com.newhan.chatservice.dto.messagedtos.ChatMessageDTO;
import com.newhan.chatservice.dto.messagedtos.EditedMessageDTO;
import com.newhan.chatservice.dto.messagedtos.SendMessageDTO;
import com.newhan.chatservice.model.chatmessage.ChatMessage;
import com.newhan.chatservice.model.chatmessage.MessageSatus;
import com.newhan.chatservice.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository repository;

    /**
     * Saves a new message to MongoDB.
     * Generates a stateless ChatID so it belongs to the conversation between A and B.
     */
    public ChatMessageDTO saveMessage(SendMessageDTO dto, String senderId) {
        // 1. Generate the Room ID (Stateless)
        String chatId = generateChatId(senderId, dto.recipientId());

        // 2. Create Entity
        ChatMessage message = ChatMessage.builder()
                .chatId(chatId)
                .senderId(senderId)
                .recipientId(dto.recipientId())
                .content(dto.content())
                .timestamp(new Date()) // Mongo uses legacy Date
                .status(MessageSatus.DELIVERED)
                .build();

        // 3. Save (Mongo generates the unique 'id')
        ChatMessage saved = repository.save(message);

        // 4. Return DTO
        return mapToDTO(saved);
    }

    /**
     * Updates an existing message content.
     * Verifies that the requester is the original sender.
     */
    public ChatMessageDTO editMessage(EditedMessageDTO dto, String requesterId) {
        // We use orElseThrow to handle the "Not Found" case cleanly
        ChatMessage msg = repository.findById(dto.messageId())
                .orElseThrow(() -> new RuntimeException("Message not found"));

        // SECURITY: Only allow the original sender to edit
        if (!msg.getSenderId().equals(requesterId)) {
            // Throwing an exception is better than returning null!
            throw new RuntimeException("Unauthorized: You can only edit your own messages.");
        }

        // Apply the updates
        msg.setContent(dto.newContent());
        msg.setEdited(true);             // Now using our new entity field!
        msg.setLastEdited(new Date());   // Now using our new entity field!

        ChatMessage updated = repository.save(msg);
        return mapToDTO(updated);
    }

    /**
     * Fetches chat history between two users.
     */
    public List<ChatMessageDTO> findChatMessages(String senderId, String recipientId) {
        String chatId = generateChatId(senderId, recipientId);
        
        List<ChatMessage> messages = repository.findByChatId(chatId);
        
        return messages.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // --- Helpers ---

    private ChatMessageDTO mapToDTO(ChatMessage msg) {
        return new ChatMessageDTO(
                msg.getId(),
                msg.getSenderId(),
                msg.getRecipientId(),
                msg.getContent(),
                convertToLocalDateTime(msg.getTimestamp()),
                msg.getStatus(),
                msg.isEdited(),                              // No longer hardcoded to false!
                convertToLocalDateTime(msg.getLastEdited())
        );
    }

    private LocalDateTime convertToLocalDateTime(Date dateToConvert) {
        if (dateToConvert == null) return LocalDateTime.now();
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    private String generateChatId(String senderId, String recipientId) {
        if (senderId.compareTo(recipientId) < 0) {
            return senderId + "_" + recipientId;
        } else {
            return recipientId + "_" + senderId;
        }
    }
}
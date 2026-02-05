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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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
        Optional<ChatMessage> optionalMsg = repository.findById(dto.messageId());

        if (optionalMsg.isPresent()) {
            ChatMessage msg = optionalMsg.get();

            // SECURITY: Only allow the original sender to edit
            if (msg.getSenderId().equals(requesterId)) {
                msg.setContent(dto.newContent());
                
                // If you add 'edited' fields to your Entity later, update them here:
                // msg.setEdited(true);
                // msg.setLastEdited(new Date());

                ChatMessage updated = repository.save(msg);
                return mapToDTO(updated);
            }
        }
        return null; // Or throw custom exception "Message not found or Unauthorized"
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
                false, // Todo: Add 'boolean edited' to your Mongo Entity to support this
                null   // Todo: Add 'Date lastEdited' to your Mongo Entity to support this
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
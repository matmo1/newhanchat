package com.newhan.chatservice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Comparator; // Import Comparator

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.newhan.chatservice.dto.messagedtos.ChatMessageDTO;
import com.newhan.chatservice.dto.messagedtos.EditedMessageDTO;
import com.newhan.chatservice.dto.messagedtos.SendMessageDTO;
import com.newhan.chatservice.model.chatmessage.ChatMessage;
import com.newhan.chatservice.model.chatmessage.MessageSatus;
import com.newhan.chatservice.repository.ChatMessageRepository;
import com.newhan.chatservice.repository.UserRepository;

@Service
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    public ChatMessageService(ChatMessageRepository chatMessageRepository, UserRepository userRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ChatMessageDTO saveMessage(SendMessageDTO sendMessageDTO, ObjectId senderId) {
        userRepository.findById(senderId)
            .orElseThrow(() -> new IllegalArgumentException("Sender not found"));
        userRepository.findById(sendMessageDTO.recipientId())
            .orElseThrow(() -> new IllegalArgumentException("Recipient not found"));

        ChatMessage message = new ChatMessage(null);
        message.setMessageContent(sendMessageDTO.content());
        message.setSenderId(senderId);
        message.setReciepentId(sendMessageDTO.recipientId());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageSatus.DELIVERED);

        ChatMessage savedMessage = chatMessageRepository.save(message);
        return toDtoMessage(savedMessage);
    }

    // --- FIX: Use the new bidirectional query and sort results ---
    public List<ChatMessageDTO> getChatHistory(ObjectId senderId, ObjectId recipientId) {
        List<ChatMessage> messages = chatMessageRepository.findChatHistory(senderId, recipientId);
        
        // Sort oldest to newest
        messages.sort(Comparator.comparing(ChatMessage::getTimestamp));

        return messages.stream()
            .map(this::toDtoMessage)
            .toList();
    }

    public ChatMessageDTO editMessage(EditedMessageDTO dto, ObjectId requesterId) {
        ChatMessage message = chatMessageRepository.findById(dto.mesId())
            .orElseThrow(() -> new IllegalArgumentException("Message not found"));
        
        if (!message.getSenderId().equals(requesterId)) {
            throw new SecurityException("Only the sender can edit the message");
        }

        message.setMessageContent(dto.newContent());
        message.setEdited(true);
        message.setLastEdited();

        ChatMessage updated = chatMessageRepository.save(message);
        return toDtoMessage(updated);
    }

    private ChatMessageDTO toDtoMessage(ChatMessage chatMessage) {
        return new ChatMessageDTO(
            chatMessage.getId(), 
            chatMessage.getContent(), 
            chatMessage.getSenderId(), 
            chatMessage.getReciepentId(), 
            chatMessage.getTimestamp(), 
            chatMessage.getStatus(), 
            chatMessage.getEdited(),
            chatMessage.getLastEdited()
            );
    }
}
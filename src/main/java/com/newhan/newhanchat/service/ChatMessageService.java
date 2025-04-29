package com.newhan.newhanchat.service;

import java.time.LocalDateTime;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.newhan.newhanchat.dto.messagedtos.ChatMessageDTO;
import com.newhan.newhanchat.dto.messagedtos.EditedMessageDTO;
import com.newhan.newhanchat.dto.messagedtos.SendMessageDTO;
import com.newhan.newhanchat.model.chatmessage.ChatMessage;
import com.newhan.newhanchat.model.chatmessage.MessageSatus;
import com.newhan.newhanchat.repository.ChatMessageRepository;
import com.newhan.newhanchat.repository.UserRepository;

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
        message.setMessageContent(sendMessageDTO.contenet());
        message.setSenderId(senderId);
        message.setReciepentId(sendMessageDTO.recipientId());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageSatus.DELIVERED);

        ChatMessage savedMessage = chatMessageRepository.save(message);
        return toDtoMessage(savedMessage);
    }

    public List<ChatMessageDTO> getChatHistory(ObjectId senderId, ObjectId recipientId) {
        return chatMessageRepository.findBySenderIdAndRecipientId(senderId, recipientId)
            .stream()
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
            chatMessage.getStatus()
            );
    }
}

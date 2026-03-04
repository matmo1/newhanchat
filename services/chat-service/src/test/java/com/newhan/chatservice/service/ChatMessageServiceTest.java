package com.newhan.chatservice.service;

import com.newhan.chatservice.dto.messagedtos.ChatMessageDTO;
import com.newhan.chatservice.dto.messagedtos.EditedMessageDTO;
import com.newhan.chatservice.dto.messagedtos.SendMessageDTO;
import com.newhan.chatservice.model.chatmessage.ChatMessage;
import com.newhan.chatservice.model.chatmessage.MessageSatus;
import com.newhan.chatservice.repository.ChatMessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @InjectMocks
    private ChatMessageService chatMessageService;

    @Test
    void saveMessage_Success() {
        // Arrange
        String senderId = "user-uuid-1";
        String recipientId = "user-uuid-2";
        SendMessageDTO dto = new SendMessageDTO("Hello World", recipientId);

        ChatMessage savedMessage = new ChatMessage();
        savedMessage.setId("mongo-object-id-123");
        savedMessage.setSenderId(senderId);
        savedMessage.setRecipientId(recipientId);
        savedMessage.setContent("Hello World");
        savedMessage.setTimestamp(new Date());
        savedMessage.setStatus(MessageSatus.DELIVERED);
        
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(savedMessage);

        // Act
        ChatMessageDTO result = chatMessageService.saveMessage(dto, senderId);

        // Assert
        assertNotNull(result);
        assertEquals("Hello World", result.content());
        assertEquals(senderId, result.senderId());
        assertEquals(recipientId, result.recipientId());
        assertNotNull(result.timestamp());
        
        verify(chatMessageRepository).save(any(ChatMessage.class));
    }

    @Test
    void findChatMessages_Success() {
        String userA = "alice";
        String userB = "bob";
        String expectedChatId = "alice_bob";
        
        ChatMessage msg1 = ChatMessage.builder()
                .chatId(expectedChatId).senderId(userA).recipientId(userB)
                .content("Hi").timestamp(new Date())
                .build();
        
        ChatMessage msg2 = ChatMessage.builder()
                .chatId(expectedChatId).senderId(userB).recipientId(userA)
                .content("Hello").timestamp(new Date())
                .build();

        when(chatMessageRepository.findByChatId(expectedChatId)).thenReturn(List.of(msg1, msg2));

        List<ChatMessageDTO> history = chatMessageService.findChatMessages(userA, userB);

        assertEquals(2, history.size());
        assertEquals("Hi", history.get(0).content());
        assertEquals("Hello", history.get(1).content());
    }

    @Test
    void editMessage_Success_WhenUserIsSender() {
        String messageId = "msg-123";
        String senderId = "alice";
        EditedMessageDTO dto = new EditedMessageDTO(messageId, "Updated text");

        ChatMessage existingMessage = new ChatMessage();
        existingMessage.setId(messageId);
        existingMessage.setSenderId(senderId);
        existingMessage.setContent("Old text");
        existingMessage.setTimestamp(new Date());

        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.of(existingMessage));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(existingMessage);

        ChatMessageDTO result = chatMessageService.editMessage(dto, senderId);

        assertNotNull(result);
        assertEquals("Updated text", result.content());
    }

    @Test
    void editMessage_Fails_WhenUserIsNotSender() {
        String messageId = "msg-123";
        String hackerId = "bob";
        EditedMessageDTO dto = new EditedMessageDTO(messageId, "Hacked text");

        ChatMessage existingMessage = new ChatMessage();
        existingMessage.setId(messageId);
        existingMessage.setSenderId("alice"); // Original sender is Alice
        existingMessage.setContent("Old text");

        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.of(existingMessage));

        ChatMessageDTO result = chatMessageService.editMessage(dto, hackerId);

        assertNull(result); // Service should reject edit
        verify(chatMessageRepository, never()).save(any());
    }
}
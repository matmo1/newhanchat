package com.newhan.chatservice.service; // Adjust to match your folder structure if needed

import com.newhan.chatservice.dto.messagedtos.ChatMessageDTO;
import com.newhan.chatservice.dto.messagedtos.SendMessageDTO;
import com.newhan.chatservice.model.chatmessage.ChatMessage;
import com.newhan.chatservice.model.user.User;
import com.newhan.chatservice.repository.ChatMessageRepository;
import com.newhan.chatservice.repository.UserRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList; // Import ArrayList
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatMessageService chatMessageService;

    @Test
    void saveMessage_Success() {
        ObjectId senderId = new ObjectId();
        ObjectId recipientId = new ObjectId();
        SendMessageDTO dto = new SendMessageDTO("Hello World", recipientId);

        ChatMessage savedMessage = new ChatMessage("Hello World");
        savedMessage.setSenderId(senderId);
        savedMessage.setReciepentId(recipientId);
        savedMessage.setTimestamp(LocalDateTime.now());

        when(userRepository.findById(senderId)).thenReturn(Optional.of(new User()));
        when(userRepository.findById(recipientId)).thenReturn(Optional.of(new User()));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(savedMessage);

        ChatMessageDTO result = chatMessageService.saveMessage(dto, senderId);

        assertNotNull(result);
        assertEquals("Hello World", result.content());
        verify(chatMessageRepository).save(any(ChatMessage.class));
    }

    @Test
    void getChatHistory_Success() {
        ObjectId senderId = new ObjectId();
        ObjectId recipientId = new ObjectId();
        
        ChatMessage msg1 = new ChatMessage("Hi");
        msg1.setSenderId(senderId);
        msg1.setReciepentId(recipientId);
        msg1.setTimestamp(LocalDateTime.now().minusMinutes(5));
        
        ChatMessage msg2 = new ChatMessage("Hello");
        msg2.setSenderId(recipientId);
        msg2.setReciepentId(senderId);
        msg2.setTimestamp(LocalDateTime.now());

        // FIX: Wrap List.of in new ArrayList<>() to make it MUTABLE (so it can be sorted)
        List<ChatMessage> mockList = new ArrayList<>(List.of(msg1, msg2));

        // Use findChatHistory (the bidirectional method)
        when(chatMessageRepository.findChatHistory(senderId, recipientId))
                .thenReturn(mockList);

        List<ChatMessageDTO> history = chatMessageService.getChatHistory(senderId, recipientId);

        assertEquals(2, history.size());
    }
}
package com.newhan.chatservice.service;

import com.newhan.chatservice.dto.userdtos.UserUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class UserProfileConsumer {

    // Standard Logger initialization (No Lombok)
    private static final Logger log = LoggerFactory.getLogger(UserProfileConsumer.class);

    private final UserService userService;

    // Explicit Constructor Injection (No Lombok)
    public UserProfileConsumer(UserService userService) {
        this.userService = userService;
    }

    @KafkaListener(topics = "user-profile-updates", groupId = "chat-service-group")
    public void consumeProfileUpdate(UserUpdateEvent event) {
        log.info("Received profile update for user: {}", event.username());
        
        // Pass the event to the UserService to update MongoDB
        userService.updateProfileFromEvent(event);
    }
}
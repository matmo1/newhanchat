package com.newhan.userservice.service;

import com.newhan.userservice.dto.UserUpdateEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, UserUpdateEvent> kafkaTemplate;

    // Explicit Constructor Injection (No Lombok)
    public KafkaProducerService(KafkaTemplate<String, UserUpdateEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendProfileUpdate(UserUpdateEvent event) {
        // Topic: "user-profile-updates"
        // Key: event.username() -> Using a key ensures updates for the SAME user are processed in order!
        kafkaTemplate.send("user-profile-updates", event.username(), event);
    }
}
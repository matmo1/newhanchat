package com.newhan.userservice.service;

import com.newhan.userservice.dto.UserUpdateEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, UserUpdateEvent> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, UserUpdateEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendProfileUpdate(UserUpdateEvent event) {
        // ✨ FIXED: Using userId() as the strict chronological routing key
        kafkaTemplate.send("user-profile-updates", event.userId(), event);
    }
}
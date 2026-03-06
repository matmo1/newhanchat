package com.newhan.chatservice.service;

import com.newhan.chatservice.dto.userdtos.UserStatusUpdateEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class StatusProducerService {

    private final KafkaTemplate<String, UserStatusUpdateEvent> kafkaTemplate;

    public StatusProducerService(KafkaTemplate<String, UserStatusUpdateEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendStatusUpdate(UserStatusUpdateEvent event) {
        // Topic: "user-status-updates"
        // Key: username (Ensures updates for the same user are processed in order)
        kafkaTemplate.send("user-status-updates", event.username(), event);
    }
}
package com.newhan.userservice.service;

import com.newhan.userservice.dto.UserStatusUpdateEvent;
import com.newhan.userservice.model.StatusType;
import com.newhan.userservice.model.User;
import com.newhan.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StatusConsumerService {

    private static final Logger log = LoggerFactory.getLogger(StatusConsumerService.class);
    private final UserRepository userRepository;

    public StatusConsumerService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @KafkaListener(topics = "user-status-updates", groupId = "user-service-group")
    public void consumeStatusUpdate(UserStatusUpdateEvent event) {
        log.info("Received status update: {} is now {}", event.username(), event.status());

        Optional<User> optionalUser = userRepository.findByUsername(event.username());
        
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            
            // Update the embedded object safely
            if (user.getStatus() != null) {
                user.getStatus().setType(StatusType.valueOf(event.status()));
                user.getStatus().setLastSeen(event.lastSeen());
            }
            
            userRepository.save(user); // Commits the embedded object change to Postgres
        }
    }
}